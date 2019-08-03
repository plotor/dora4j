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
 * The SessionWindowCollection transforms a List into an iterable collection of session windows. This wrapper
 * class can be used to manage the retention policy of the source collection as well as to insert new objects in
 * chronological order.
 */
public class SessionWindowCollection<T> implements Iterable<TimeWindow<T>> {

    private List<T> source;
    private TimestampSelector<T> selector;
    private long startTime;
    private long timeout;

    /**
     * Instantiates a new SessionWindowCollection
     *
     * @param source the underlying source.
     * @param selector the selector used to pull a timestamp from an item in the source collection and subsequent insertions
     * @param startTime the first time an object can be in a time window -- items before the start time will be evicted from the source collection.
     * @param timeout the minimum amount of time between session window ranges
     */
    public SessionWindowCollection(List<T> source, TimestampSelector<T> selector, long startTime, long timeout) {
        this.init(source, selector, startTime, timeout);
    }

    private void init(List<T> source, TimestampSelector<T> selector, long startTime, long timeout) {
        this.source = source;
        this.selector = selector;
        this.startTime = startTime;
        this.timeout = timeout;

        this.performEviction();
    }

    /**
     * Adds an item to the source collection in time ordered fashion.
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
            return Windowing.toSessionWindows(source, selector, startTime, end, timeout).iterator();
        }
    }

    @Override
    public void forEach(Consumer<? super TimeWindow<T>> action) {
        if (source != null && source.size() > 0) {
            long end = selector.select(source.get(source.size() - 1)) + 1;
            Windowing.toSessionWindows(source, selector, startTime, end, timeout).forEach(action);
        }
    }

    @Override
    public Spliterator<TimeWindow<T>> spliterator() {
        if (source == null || source.size() == 0) {
            return Spliterators.emptySpliterator();
        } else {
            long end = selector.select(source.get(source.size() - 1)) + 1;
            return Windowing.toSessionWindows(source, selector, startTime, end, timeout).spliterator();
        }
    }
}
