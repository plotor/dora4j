package org.zhenchao.dubbo.spi.ext1.impl;

import org.zhenchao.dubbo.spi.ext1.SimpleExt;

public class SimpleExtImpl2 implements SimpleExt {

    @Override
    public String one(int pt, String s) {
        return "Ext1Impl2-one";
    }

    @Override
    public String two(int pt, String s) {
        return "Ext1Impl2-two";
    }

    @Override
    public String three(int pt, String s) {
        return "Ext1Impl2-three";
    }

    @Override
    public String four(int pt, String s) {
        return "Ext1Impl2-four";
    }

}