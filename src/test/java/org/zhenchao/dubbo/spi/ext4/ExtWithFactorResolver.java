package org.zhenchao.dubbo.spi.ext4;

import org.zhenchao.dubbo.spi.Adaptive;
import org.zhenchao.dubbo.spi.SPI;
import org.zhenchao.dubbo.spi.support.FactorResolver;

@SPI("impl1")
public interface ExtWithFactorResolver {

    // 未指定决策因子
    @Adaptive
    String one(Element element, String s);

    @Adaptive(mapping = {"1=impl1", "2=impl2", "3=impl3"})
    String two(Element element, String s, FactorResolver resolver);

    @Adaptive(mapping = {"a=impl1", "b=impl2", "c=impl3"})
    String three(Element element, String s);

    // 无@Adaptive
    String four(Element element, String s);

}