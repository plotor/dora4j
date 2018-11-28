package org.zhenchao.dora.spi.ext3.impl;

import org.zhenchao.dora.spi.ext3.WrappedExt;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhenchao.wang 2017-12-30 13:05
 * @version 1.0.0
 */
public class ExtWrapperImpl2 implements WrappedExt {

    public static AtomicInteger echoCount = new AtomicInteger();
    private WrappedExt instance;

    public ExtWrapperImpl2(WrappedExt instance) {
        this.instance = instance;
    }

    @Override
    public String echo(int pt, String s) {
        echoCount.incrementAndGet();
        System.out.println("wrapper " + instance.getClass().getSimpleName() + " by ExtWrapperImpl2, count=" + echoCount.get());
        return instance.echo(pt, s);
    }
}
