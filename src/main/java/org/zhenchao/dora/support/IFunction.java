package org.zhenchao.dora.support;

/**
 * @author zhenchao.wang 2018-06-20 16:30
 * @version 1.0.0
 */
public interface IFunction<T, R> {

    R apply(T t);

}
