package org.zhenchao.dubbo.spi.ext3.impl;

import org.zhenchao.dubbo.spi.ext3.WrappedExt;

/**
 * @author zhenchao.wang 2017-12-30 13:04
 * @version 1.0.0
 */
public class ExtImpl2 implements WrappedExt {

    @Override
    public String echo(int pt, String s) {
        return "ExtImpl2-echo";
    }

}
