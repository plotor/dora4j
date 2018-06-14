package org.zhenchao.dubbo.spi.spring.ext3;

import org.zhenchao.dubbo.spi.Adaptive;
import org.zhenchao.dubbo.spi.DIoC;
import org.zhenchao.dubbo.spi.support.FactorResolver;

/**
 * @author zhenchao.wang 2018-01-02 17:39
 * @version 1.0.0
 */
@DIoC(mapping = {"a=ext3_impl1", "b=ext3_impl2"})
public interface Ext3Service {

    String one(String s);

    @Adaptive(index = 1)
    String two(String s, int i, FactorResolver factorResolver);

    @Adaptive(mapping = {"1=ext3_impl1", "2=ext3_impl2"})
    String three(int i);

    @Adaptive(index = 1, mapping = {"1=ext3_impl1", "2=ext3_impl2"})
    String four(String s, int i);

    String five();

}
