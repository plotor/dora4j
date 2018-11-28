package org.zhenchao.dora.spi.spring.ext3;

import org.junit.Assert;
import org.springframework.stereotype.Service;
import org.zhenchao.dora.spi.support.FactorResolver;

/**
 * @author zhenchao.wang 2018-01-02 17:50
 * @version 1.0.0
 */
@Service("ext3_impl1")
public class Ext3ServiceImpl1 implements Ext3Service {

    @Override
    public String one(String s) {
        return "Ext3ServiceImpl1-one";
    }

    @Override
    public String two(String s, int i, FactorResolver factorResolver) {
        String result = this.four(s, i);
        System.out.println("output : " + result);
        Assert.assertEquals("Ext3ServiceImpl1-four", result);
        return "Ext3ServiceImpl1-two";
    }

    @Override
    public String three(int i) {
        return "Ext3ServiceImpl1-three";
    }

    @Override
    public String four(String s, int i) {
        return "Ext3ServiceImpl1-four";
    }

    @Override
    public String five() {
        return null;
    }
}
