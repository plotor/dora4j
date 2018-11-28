package org.zhenchao.dora.spi.ext4.impl;

import org.zhenchao.dora.spi.ext4.Element;
import org.zhenchao.dora.spi.ext4.ExtWithFactorResolver;
import org.zhenchao.dora.spi.support.FactorResolver;

public class FactorExtImpl3 implements ExtWithFactorResolver {

    @Override
    public String one(Element element, String s) {
        return "FactorExtImpl3-one";
    }

    @Override
    public String two(Element element, String s, FactorResolver resolver) {
        return "FactorExtImpl3-two";
    }

    @Override
    public String three(Element element, String s) {
        return "FactorExtImpl3-three";
    }

    @Override
    public String four(Element element, String s) {
        return "FactorExtImpl3-four";
    }

}