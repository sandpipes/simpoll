package dev.dacbiet.simpoll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Handles updating threads only when content changes.
 */
public class ContentWatcher {

    private CountDownLatch barrier;
    private boolean completed;

    /**
     * Construct a new ContentWatcher.
     */
    public ContentWatcher() {
        this.completed = false;
        this.barrier = new CountDownLatch(1);
    }

    /**
     * Mark the content as dirty and cause any threads to send an update.
     */
    public void markDirty() {
        this.barrier.countDown();
        this.barrier = new CountDownLatch(1);
    }

    /**
     * Block until the content has been marked dirty or if timed out.
     *
     * @param timeout timeout in ms
     * @return flag for if there has been an update
     */
    protected boolean awaitUpdate(long timeout) {
        if(this.isCompleted()) {
            return false;
        }

        try {
            return this.barrier.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Mark that there's no need to watch the content anymore.
     */
    public void setCompleted() {
        this.completed = true;
        this.barrier.countDown();
    }

    /**
     * Get if we need to continue watching the content.
     *
     * @return true if we should continue watching
     */
    public boolean isCompleted() {
        return this.completed;
    }

}