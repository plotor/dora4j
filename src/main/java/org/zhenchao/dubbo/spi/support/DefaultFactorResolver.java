package org.zhenchao.dubbo.spi.support;

import org.zhenchao.dubbo.spi.util.TypeUtils;

/**
 * @author zhenchao.wang 2017-12-30 14:34
 * @version 1.0.0
 */
public class DefaultFactorResolver implements FactorResolver {

    @Override
    public String resolve(Object arg) {
        if (TypeUtils.isNotPrimitiveInstance(arg)) {
            throw new IllegalStateException("default adaptive factor must be primitive type (or you can specify your factor resolver) : " + (null == arg ? null : arg.getClass()));
        }
        return String.valueOf(arg);
    }

}
