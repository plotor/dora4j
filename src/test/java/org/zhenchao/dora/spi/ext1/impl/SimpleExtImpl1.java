package org.zhenchao.dora.spi.ext1.impl;

import org.zhenchao.dora.spi.ext1.SimpleExt;

public class SimpleExtImpl1 implements SimpleExt {

    @Override
    public String one(int pt, String s) {
        return "Ext1Impl1-one";
    }

    @Override
    public String two(int pt, String s) {
        return "Ext1Impl1-two";
    }

    @Override
    public String three(int pt, String s) {
        return "Ext1Impl1-three";
    }

    @Override
    public String four(int pt, String s) {
        return "Ext1Impl1-four";
    }
}