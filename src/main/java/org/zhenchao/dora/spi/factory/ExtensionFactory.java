package org.zhenchao.dora.spi.factory;

import org.zhenchao.dora.spi.SPI;

/**
 * @author zhenchao.wang 2017-12-29 13:34
 * @version 1.0.0
 */
@SPI
public interface ExtensionFactory {

    <T> T getExtension(Class<T> type, String name);

}
