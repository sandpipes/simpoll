package dev.dacbiet.simpoll;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Generate DeferredResults.
 */
public class ResultGenerator {

    /**
     * Get a DeferredResult with ResponseEntity that only updates based on the given hash.
     * The timeout will result in a 204 No Content HTTP Response.
     *
     * @param watcher watcher for the content string
     * @param fetcher function to get content string
     * @param hash hash supplied by the client
     * @param timeout timeout in ms
     *
     * @return DeferredResult that updates when required
     */
    public static DeferredResult getStringResult(ContentWatcher watcher, Fetcher fetcher, @Nullable String hash, long timeout) {

        // no further content
        if(watcher.isCompleted()) {
            DeferredResult result = new DeferredResult();
            result.setResult(ResponseEntity.status(HttpStatus.GONE).body(""));
            return result;
        }

        // update immediately
        if(hash == null || !hash.equals(getHash(fetcher.getString()))) {
            DeferredResult result = new DeferredResult();
            result.setResult(ResponseEntity.status(HttpStatus.OK).body(fetcher.getString()));
            return result;
        }

        DeferredResult result = new DeferredResult(timeout);
        result.onTimeout(() -> result.setErrorResult(ResponseEntity.status(HttpStatus.NO_CONTENT).body("No update.")));
        new Thread(new ContentUpdateWait(watcher, fetcher, result, timeout)).start();

        return result;
    }

    /**
     * Get the MD5 hash of the given string.
     *
     * @param content string of content
     *
     * @return MD5 hash of the content
     */
    private static String getHash(String content) {
        return DigestUtils.md5Hex(content);
    }

}
