package org.zhenchao.dora.window.selector;

/**
 * The TimestampSelector is used to pull a timestamp from an object.
 */
public interface TimestampSelector<T> {
    long select(T t);
}
