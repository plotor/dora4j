package org.zhenchao.dora.support;

/**
 * @author zhenchao.wang 2018-11-14 12:28
 * @version 1.0.0
 */
public interface ITuple {

    Object get(int index);

    Object[] toArray();

    int size();

}
