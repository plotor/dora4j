package org.zhenchao.dora.window;

import org.zhenchao.dora.window.selector.TimestampSelector;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * A sequence of items in a collection before the specified end time has been reached.
 */
class SlidingTimeWindow<T> implements TimeWindow<T> {

    private long startTime;
    private long endTime;
    private List<T> items;

    SlidingTimeWindow(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Set the items in this window from the parameter source collection
     *
     * @param source the source collection to pull items from
     * @param startIndex the index to start scanning from
     * @param selector the selector used to pull timestamps from items
     * @return the last index touched
     */
    int setItems(List<T> source, int startIndex, TimestampSelector<T> selector) {
        boolean foundItem = false;
        LinkedList<T> items = null;
        int i;
        // loop from the last index used, until we run out of items or the timestamp is greater than the end time
        // for this window
        for (i = startIndex; i < source.size(); i++) {
            T item = source.get(i);
            long timestamp = selector.select(item);
            if (timestamp < startTime) {
                continue;
            }

            if (timestamp >= endTime) {
                break;
            }

            if (!foundItem) {
                foundItem = true;
                startIndex = i;
                items = new LinkedList<T>();
            }

            items.add(item);
        }

        this.items = items;
        return startIndex;
    }

    @Override
    public int size() {
        if (items == null) {
            return 0;
        } else {
            return items.size();
        }
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public Iterator<T> iterator() {
        if (items == null) {
            return Collections.emptyIterator();
        } else {
            return items.iterator();
        }
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        if (items != null && items.size() > 0) {
            items.forEach(action);
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        if (items == null) {
            return Spliterators.emptySpliterator();
        } else {
            return items.spliterator();
        }
    }

}
