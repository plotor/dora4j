package org.zhenchao.dora.spi.adaptive;

import org.zhenchao.dora.spi.Adaptive;
import org.zhenchao.dora.spi.SPI;

@SPI
public interface AdaptiveExt {

    @Adaptive()
    String echo(int pt, String s);
}
