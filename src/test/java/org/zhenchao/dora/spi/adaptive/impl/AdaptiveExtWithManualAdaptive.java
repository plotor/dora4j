package org.zhenchao.dora.spi.adaptive.impl;

import org.zhenchao.dora.spi.Adaptive;
import org.zhenchao.dora.spi.adaptive.AdaptiveExt;
import org.zhenchao.dora.spi.support.ExtensionLoader;

@Adaptive
public class AdaptiveExtWithManualAdaptive implements AdaptiveExt {

    @Override
    public String echo(int pt, String s) {
        AdaptiveExt addExt1 = ExtensionLoader.getExtensionLoader(AdaptiveExt.class).getExtension("impl1");
        return addExt1.echo(pt, s);
    }
}