package org.zhenchao.dora.spi.ext4;

import org.zhenchao.dora.spi.support.FactorResolver;

/**
 * @author zhenchao.wang 2017-12-30 14:53
 * @version 1.0.0
 */
public class ElementFactorResolver implements FactorResolver {

    @Override
    public String resolve(Object arg) {
        if (arg instanceof Element) {
            return String.valueOf(((Element) arg).getName());
        }
        throw new IllegalArgumentException("not element type");
    }

}
