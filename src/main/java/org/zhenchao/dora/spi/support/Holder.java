package org.zhenchao.dora.spi.support;

/**
 * @author zhenchao.wang 2017-12-29 13:38
 * @version 1.0.0
 */
public class Holder<T> {

    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

}