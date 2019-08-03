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
 * The sequence of items in a collection before a predetermined timeout between items has been reached.
 */
class SessionTimeWindow<T> implements TimeWindow<T> {

    private long startTime;
    private long endTime;
    private long timeout;
    private List<T> items;
    private int size;

    public SessionTimeWindow(long timeout) {
        this.timeout = timeout;
        this.startTime = 0;
        this.endTime = 0;
    }

    /**
     * Set the items used in this SessionWindow.
     *
     * @param source the source list to iterate over.
     * @param startIndex the index in the source list to start at
     * @param selector the selector used to pull a timestamp out of an object
     * @return the last touched index
     */
    int setItems(List<T> source, int startIndex, TimestampSelector<T> selector) {
        LinkedList<T> list = new LinkedList<T>();
        long prev = 0, cur = 0;
        boolean first = true;
        int index;
        // start at the last used index, loop until the timeout between items is reached
        for (index = startIndex; index < source.size(); index++) {
            T itemCur = source.get(index);
            cur = selector.select(itemCur);
            if (first) {
                prev = cur;
                first = false;
                // set the start time of this window
                startTime = prev;
                list.add(itemCur);
                continue;
            }

            if (cur - prev > timeout) {
                // set the end time of this window
                endTime = prev;
                break;
            } else {
                prev = cur;
                list.add(itemCur);
                size++;
            }
            endTime = cur;
        }

        items = list;
        return index;
    }

    @Override
    public int size() {
        if (items != null) {
            return items.size();
        } else {
            return 0;
        }
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

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }
}
