package org.zhenchao.dora.window;

/**
 * Supports iteration over a collection of elements that fall within the given start and end timestamps.
 */
public interface TimeWindow<T> extends Iterable<T> {

    /**
     * Returns the start time of this window.
     *
     * @return the start time of the window.
     */
    long getStartTime();

    /**
     * Returns the end time of this window.
     *
     * @return the end time of this window.
     */
    long getEndTime();

    /**
     * Returns the size of the iterable collection.
     *
     * @return the size of the iterable collection.
     */
    int size();
}
