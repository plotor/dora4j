package org.zhenchao.dora.util;

import org.zhenchao.dora.window.selector.TimestampSelector;

import java.util.List;

/**
 * Helper functions used within the library.
 */
public class WindowUtils {

    private WindowUtils() {
    }

    /**
     * Adds an item to the parameter source collection in chronological order.
     *
     * @param source the source collection
     * @param selector the timestamp selector used to pull a timestamp from an item
     * @param toAdd the item to add
     * @param <T> the type of items in the source collection
     */
    public static <T> void addTimeOrdered(List<T> source, TimestampSelector<T> selector, T toAdd) {
        int index = source.size() - 1;
        long timeToAdd = selector.select(toAdd);
        long last = selector.select(source.get(index));

        if (timeToAdd < last) {
            while (index > 0) {
                if (timeToAdd < last) {
                    index--;
                    last = selector.select(source.get(index));
                } else {
                    index++;
                    break;
                }
            }
            source.add(index, toAdd);
        } else {
            source.add(++index, toAdd);
        }
    }

    /**
     * Removes items from the parameter source collection that have timestamps before the start time
     *
     * @param source the source collection
     * @param selector the timestamp selector used to pull a timestamp from an item
     * @param startTime the start of the collection
     * @param <T> the type of the items in the source collection
     */
    public static <T> void performEviction(List<T> source, TimestampSelector<T> selector, long startTime) {
        if (source != null) {
            int from = 0, to = 0;
            boolean clear = false;
            while (to < source.size()) {
                if (selector.select(source.get(to)) < startTime) {
                    to++;
                    clear = true;
                } else {
                    break;
                }
            }

            if (clear) {
                source.subList(from, to).clear();
            }
        }
    }

}
