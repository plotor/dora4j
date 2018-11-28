package org.zhenchao.dora.callback;

/**
 * @author zhenchao.wang 2018-06-14 18:04
 * @version 1.0.0
 */
public interface Callback {

    <T> Object execute(T... args);

}
