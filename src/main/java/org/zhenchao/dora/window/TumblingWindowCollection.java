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
 * The TumblingWindowCollection transforms a collection into an iterable collection of sequential time windows. This
 * wrapper class can be used to manage the retention policy and add objects in chronological order to the underlying
 * source collection.
 */
public class TumblingWindowCollection<T> implements Iterable<TimeWindow<T>> {

    private List<T> source;
    private TimestampSelector<T> selector;
    private long startTime;
    private long windowDuration;

    public TumblingWindowCollection(List<T> source, TimestampSelector<T> selector, long windowDuration, long startTime) {
        this.init(source, selector, windowDuration, startTime);
    }

    private void init(List<T> source, TimestampSelector<T> selector, long windowDuration, long startTime) {
        this.source = source;
        this.selector = selector;
        this.windowDuration = windowDuration;
        this.startTime = startTime;

        this.performEviction();
    }

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
            return Windowing.toTumblingWindows(source, selector, startTime, end, windowDuration).iterator();
        }
    }

    @Override
    public void forEach(Consumer<? super TimeWindow<T>> action) {
        if (source != null && source.size() != 0) {
            long end = selector.select(source.get(source.size() - 1)) + 1;
            Windowing.toTumblingWindows(source, selector, startTime, end, windowDuration).forEach(action);
        }
    }

    @Override
    public Spliterator<TimeWindow<T>> spliterator() {
        if (source == null || source.size() == 0) {
            return Spliterators.emptySpliterator();
        } else {
            long end = selector.select(source.get(source.size() - 1)) + 1;
            return Windowing.toTumblingWindows(source, selector, startTime, end, windowDuration).spliterator();
        }
    }
}

