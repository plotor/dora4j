package org.zhenchao.dora.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhenchao.wang 2018-01-01 13:52
 * @version 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DIoC {

    String value() default "";

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
