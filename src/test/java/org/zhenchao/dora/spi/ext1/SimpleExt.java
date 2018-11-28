package org.zhenchao.dora.spi.ext1;

import org.zhenchao.dora.spi.Adaptive;
import org.zhenchao.dora.spi.SPI;

@SPI("impl1")
public interface SimpleExt {

    // 未指定决策因子
    @Adaptive
    String one(int pt, String s);

    // 默认以第一个参数作为决策因子
    @Adaptive(mapping = {"1=impl1", "2=impl2", "3=impl3"})
    String two(int pt, String s);

    // 指定以第二个参数作为决策因子
    @Adaptive(index = 1, mapping = {"a=impl1", "b=impl2", "c=impl3"})
    String three(int pt, String s);

    // 无@Adaptive
    String four(int pt, String s);

}