package org.zhenchao.dubbo.spi.adaptive;

import org.zhenchao.dubbo.spi.Adaptive;
import org.zhenchao.dubbo.spi.SPI;

@SPI
public interface AdaptiveExt {

    @Adaptive()
    String echo(int pt, String s);
}
