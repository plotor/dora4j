package org.zhenchao.dora.spi.factory;

import org.zhenchao.dora.spi.SPI;
import org.zhenchao.dora.spi.support.ExtensionLoader;

/**
 * @author zhenchao.wang 2017-12-29 13:36
 * @version 1.0.0
 */
public class SpiExtensionFactory implements ExtensionFactory {

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
            ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(type);
            if (loader.getSupportedExtensions().size() > 0) {
                return loader.getAdaptiveExtension();
            }
        }
        return null;
    }

}
