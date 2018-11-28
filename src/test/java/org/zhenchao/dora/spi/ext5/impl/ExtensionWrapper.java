package org.zhenchao.dora.spi.ext5.impl;

import org.zhenchao.dora.spi.ext1.SimpleExt;
import org.zhenchao.dora.spi.ext5.Extension;

/**
 * @author zhenchao.wang 2018-01-02 10:39
 * @version 1.0.0
 */
public class ExtensionWrapper implements Extension {

    private SimpleExt simpleExt;

    public ExtensionWrapper() {
    }

    public ExtensionWrapper(SimpleExt simpleExt) {
        this.simpleExt = simpleExt;
    }

    @Override
    public String hello(String name) {
        return "ExtensionWrapper-" + name;
    }
}
