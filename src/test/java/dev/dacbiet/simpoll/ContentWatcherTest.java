package dev.dacbiet.simpoll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the functions of ContentWatcher.
 */
public class ContentWatcherTest {

    ContentWatcher watcher;

    @BeforeEach
    void setup() {
        watcher = new ContentWatcher();
    }

    @Test
    @DisplayName("Test timeout happens when no update occurred")
    void testTimeoutNoUpdate() {
        assertFalse(watcher.awaitUpdate(1000));
    }

    @Test
    @DisplayName("Test marking watcher as completed")
    void testWatchCompleted() {
        watcher.setCompleted();
        assertFalse(watcher.awaitUpdate(10));
    }

    @Test
    @DisplayName("Test watcher gets notified when marking content as dirty")
    void testNotifyUpdate() {
        new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            watcher.markDirty();
        }).start();
        assertTrue(watcher.awaitUpdate(10000));
    }

}
