package org.zhenchao.dora.spi.adaptive.impl;

import org.zhenchao.dora.spi.adaptive.AdaptiveExt;

public class AdaptiveExtImpl1 implements AdaptiveExt {

    @Override
    public String echo(int pt, String s) {
        return this.getClass().getSimpleName();
    }

}