package org.zhenchao.dubbo.spi.ext4.impl;

import org.zhenchao.dubbo.spi.ext4.Element;
import org.zhenchao.dubbo.spi.ext4.ExtWithFactorResolver;
import org.zhenchao.dubbo.spi.support.FactorResolver;

public class FactorExtImpl1 implements ExtWithFactorResolver {

    @Override
    public String one(Element element, String s) {
        return "FactorExtImpl1-one";
    }

    @Override
    public String two(Element element, String s, FactorResolver resolver) {
        return "FactorExtImpl1-two";
    }

    @Override
    public String three(Element element, String s) {
        return "FactorExtImpl1-three";
    }

    @Override
    public String four(Element element, String s) {
        return "FactorExtImpl1-four";
    }
}