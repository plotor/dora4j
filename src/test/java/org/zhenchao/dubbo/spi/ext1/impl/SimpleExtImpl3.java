package org.zhenchao.dubbo.spi.ext1.impl;

import org.zhenchao.dubbo.spi.ext1.SimpleExt;

public class SimpleExtImpl3 implements SimpleExt {

    @Override
    public String one(int pt, String s) {
        return "Ext1Impl3-one";
    }

    @Override
    public String two(int pt, String s) {
        return "Ext1Impl3-two";
    }

    @Override
    public String three(int pt, String s) {
        return "Ext1Impl3-three";
    }

    @Override
    public String four(int pt, String s) {
        return "Ext1Impl3-four";
    }

}