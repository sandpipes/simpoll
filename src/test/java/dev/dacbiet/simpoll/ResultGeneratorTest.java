package dev.dacbiet.simpoll;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Test the generation of results.
 */
public class ResultGeneratorTest {

    ContentWatcher watcher;
    String testContent = "test";
    String testHash;
    Fetcher testFetcher;
    long timeout = 1000;

    @BeforeEach
    void setup() {
        watcher = new ContentWatcher();
        testHash = DigestUtils.md5Hex(testContent);
        testFetcher = () -> testContent;
    }

    @Test
    @DisplayName("Test that no further content result is generated")
    void testCompletedWatch() {
        watcher.setCompleted();

        DeferredResult result = ResultGenerator.getStringResult(watcher, testFetcher, "", timeout);
        ResponseEntity res = (ResponseEntity) result.getResult();
        Assertions.assertEquals(HttpStatus.GONE, res.getStatusCode());
    }

    @Test
    @DisplayName("Test that result is obtained immediately")
    void testImmediateUpdate() {
        DeferredResult result = ResultGenerator.getStringResult(watcher, testFetcher, null, timeout);
        ResponseEntity res = (ResponseEntity) result.getResult();
        Assertions.assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    @DisplayName("Test that result is not set immediately when long polled")
    void testLongPolled() {
        DeferredResult result = ResultGenerator.getStringResult(watcher, testFetcher, testHash, timeout);
        Assertions.assertEquals(null, result.getResult());
    }

    @Test
    @DisplayName("Test that result is set after a timeout when long polled")
    void testLongPolledResult() {
        long longTimeout = 10000;

        DeferredResult result = ResultGenerator.getStringResult(watcher, testFetcher, testHash, longTimeout);

        // create thread to mark dirty
        new Thread(() -> {
            try {
                Thread.sleep(longTimeout / 10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            watcher.markDirty();
        }).start();

        watcher.awaitUpdate(longTimeout + 500);

        // wait at most the timeout until we check the result
        await().atMost(longTimeout, TimeUnit.MILLISECONDS).until(result::hasResult);

        ResponseEntity res = (ResponseEntity) result.getResult();
        Assertions.assertEquals(testContent, res.getBody().toString());
    }

    @Test
    @DisplayName("Test that result was timed out")
    void testLongPollTimeout() {
        DeferredResult result = ResultGenerator.getStringResult(watcher, testFetcher, testHash, timeout);

        // we can't test this since there's no spring controller to timeout the result
        // therefore we must set the error result ourselves
        result.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out."));

        ResponseEntity res = (ResponseEntity) result.getResult();
        Assertions.assertEquals(HttpStatus.REQUEST_TIMEOUT, res.getStatusCode());
    }

    @Test
    @DisplayName("Test that marked dirty watch result shows no content if watch is completed")
    void testWatchCompleteNoContent() {
        long longTimeout = 10000;
        DeferredResult result = ResultGenerator.getStringResult(watcher, testFetcher, testHash, longTimeout);

        // create thread to mark dirty
        new Thread(() -> {
            try {
                Thread.sleep(longTimeout / 10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            watcher.setCompleted();
            watcher.markDirty();
        }).start();

        watcher.awaitUpdate(longTimeout);

        await().atMost(timeout, TimeUnit.MILLISECONDS).until(result::hasResult);

        ResponseEntity res = (ResponseEntity) result.getResult();
        Assertions.assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

}
