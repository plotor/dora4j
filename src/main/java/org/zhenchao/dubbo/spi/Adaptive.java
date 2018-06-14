package org.zhenchao.dubbo.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解 {@link Adaptive} 可以注解于类或方法，用于实现适配器机制
 * 如果某个扩展接口的实现类被注解，则表示以该类作为适配器类
 * 否则会自动创建适配器类，并适配扩展接口中所有被 {@link Adaptive} 注解的方法
 * 程序会依据指定的入参（决策因子）值选择正确的扩展实现类执行实际操作，如果没有指定决策因子，则尝试执行默认的扩展类型
 *
 * @author zhenchao.wang 2017-12-29 13:32
 * @version 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {

    /**
     * 指定参数列表中作为决策因子的参数索引，以 0 开始计数
     * 默认以第 1 个参数作为决策因子
     *
     * @return
     */
    int index() default 0;

    /**
     * 指定参数与扩展名称之间的映射关系
     *
     * @return
     */
    String[] mapping() default {};

}
