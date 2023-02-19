package dev.dacbiet.simpoll;

/**
 * Define a simple function to retrieve a string.
 */
public interface Fetcher {

    /**
     * Get the content.
     * The code executed should be thread safe as it will be called within a thread without synchronizing with
     * the main thread.
     *
     * @return content represented as a string
     */
    String getString();

}
