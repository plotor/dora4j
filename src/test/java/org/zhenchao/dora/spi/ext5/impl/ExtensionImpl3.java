package org.zhenchao.dora.spi.ext5.impl;

import org.zhenchao.dora.spi.ext5.Extension;

/**
 * @author zhenchao.wang 2018-01-02 10:39
 * @version 1.0.0
 */
public class ExtensionImpl3 implements Extension {

    @Override
    public String hello(String name) {
        return "ExtensionImpl3-" + name;
    }
}
