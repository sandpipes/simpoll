package dev.dacbiet.simpoll;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * This runnable task waits for an update on the content.
 */
public class ContentUpdateWait implements Runnable {

    private final ContentWatcher contentWatcher;
    private final Fetcher fetcher;
    private final DeferredResult deferredResult;
    private final long timeout;

    /**
     * Runnable task for updating the deferred result when required.
     *
     * @param timeout timeout in ms (hopefully >= 1s)
     * @param fetcher fetcher to get content string
     * @param contentWatcher watcher for content string
     * @param deferredResult deferred result to set
     */
    public ContentUpdateWait(ContentWatcher contentWatcher, Fetcher fetcher, DeferredResult deferredResult, long timeout) {
        this.contentWatcher = contentWatcher;
        this.fetcher = fetcher;
        this.deferredResult = deferredResult;
        // add small timeout to avoid timing out before a possible update occurred
        this.timeout = timeout + timeout / 10;
    }

    @Override
    public void run() {

        // wait on if an update occurs or timeout
        if(!this.contentWatcher.awaitUpdate(this.timeout)) {
            // timeout
            return;
        }

        if(!this.contentWatcher.isCompleted()) {
            deferredResult.setResult(ResponseEntity.status(HttpStatus.OK).body(this.fetcher.getString()));
        } else {
            deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.GONE).body(""));
        }

    }

}
