package org.zhenchao.dubbo.spi.adaptive.impl;

import org.zhenchao.dubbo.spi.adaptive.AdaptiveExt;

public class AdaptiveExtImpl1 implements AdaptiveExt {

    @Override
    public String echo(int pt, String s) {
        return this.getClass().getSimpleName();
    }

}