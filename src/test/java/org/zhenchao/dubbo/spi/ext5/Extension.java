package org.zhenchao.dubbo.spi.ext5;

import org.zhenchao.dubbo.spi.SPI;

/**
 * @author zhenchao.wang 2018-01-02 10:38
 * @version 1.0.0
 */
@SPI
public interface Extension {

    String hello(String name);

}
