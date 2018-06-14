package org.zhenchao.dubbo.spi.ext2.impl;

import org.junit.Assert;
import org.zhenchao.dubbo.spi.ext1.SimpleExt;
import org.zhenchao.dubbo.spi.ext2.ExtWithInject;

/**
 * @author zhenchao.wang 2017-12-30 12:44
 * @version 1.0.0
 */
public class ExtWithInjectImpl1 implements ExtWithInject {

    private SimpleExt ext;

    @Override
    public String echo(int pt, String s) {
        String text = ext.one(1, "hello");
        Assert.assertEquals("Ext1Impl1-one", text);
        System.out.println(text);
        text = ext.two(2, "hello");
        Assert.assertEquals("Ext1Impl2-two", text);
        System.out.println(text);
        text = ext.three(1, "c");
        Assert.assertEquals("Ext1Impl3-three", text);
        System.out.println(text);
        return "ExtWithInjectImpl1-echo";
    }

    public ExtWithInjectImpl1 setExt(SimpleExt ext) {
        this.ext = ext;
        return this;
    }
}
