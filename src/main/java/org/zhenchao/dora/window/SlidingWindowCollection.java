package org.zhenchao.dora.window;

import org.zhenchao.dora.util.WindowUtils;
import org.zhenchao.dora.window.selector.TimestampSelector;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * The SlidingWindowCollection transforms a collection into an iterable collection of overlapping time windows. This
 * wrapper class can be used to manage the retention policy and add objects in chronological order to the underlying
 * source collection.
 */
public class SlidingWindowCollection<T> implements Iterable<TimeWindow<T>> {

    private List<T> source;
    private TimestampSelector<T> selector;
    private long startTime;
    private long windowDuration;
    private long every;

    /**
     * Instantiates a new SlidingWindowCollection
     *
     * @param source the underlying source collection
     * @param selector the interface used to select a timestamp from an item
     * @param windowDuration the duration of a time window
     * @param every the time between the starting point of each time window
     * @param startTime the first time an object can be in a time window -- items before the start time will be evicted from the source collection.
     */
    public SlidingWindowCollection(List<T> source, TimestampSelector<T> selector, long windowDuration, long every, long startTime) {
        this.init(source, selector, windowDuration, every, startTime);
    }

    private void init(List<T> source, TimestampSelector<T> selector, long windowDuration, long every, long startTime) {
        this.source = source;
        this.selector = selector;
        this.windowDuration = windowDuration;
        this.every = every;
        this.startTime = startTime;

        this.performEviction();
    }

    /**
     * Adds an item to the underlying source collection in chronological order.
     *
     * @param item the item to add
     */
    public void add(T item) {
        if (source.size() == 0) {
            source.add(0, item);
        } else {
            WindowUtils.addTimeOrdered(source, selector, item);
        }

        this.performEviction();
    }

    private void performEviction() {
        WindowUtils.performEviction(source, selector, startTime);
    }

    @Override
    public Iterator<TimeWindow<T>> iterator() {
        if (source == null || source.size() == 0) {
            return Collections.emptyIterator();
        } else {
            long end = selector.select(source.get(source.size() - 1)) + 1;
            return Windowing.toSlidingWindows(source, selector, startTime, end, windowDuration, every).iterator();
        }
    }

    @Override
    public void forEach(Consumer<? super TimeWindow<T>> action) {
        if (source != null && source.size() != 0) {
            long end = selector.select(source.get(source.size() - 1)) + 1;
            Windowing.toSlidingWindows(source, selector, startTime, end, windowDuration, every).forEach(action);
        }
    }

    @Override
    public Spliterator<TimeWindow<T>> spliterator() {
        if (source == null || source.size() == 0) {
            return Spliterators.emptySpliterator();
        } else {
            long end = selector.select(source.get(source.size() - 1)) + 1;
            return Windowing.toSlidingWindows(source, selector, startTime, end, windowDuration, every).spliterator();
        }
    }

}
