package org.zhenchao.dubbo.spi.adaptive.impl;

import org.zhenchao.dubbo.spi.Adaptive;
import org.zhenchao.dubbo.spi.adaptive.AdaptiveExt;
import org.zhenchao.dubbo.spi.support.ExtensionLoader;

@Adaptive
public class AdaptiveExtWithManualAdaptive implements AdaptiveExt {

    @Override
    public String echo(int pt, String s) {
        AdaptiveExt addExt1 = ExtensionLoader.getExtensionLoader(AdaptiveExt.class).getExtension("impl1");
        return addExt1.echo(pt, s);
    }
}