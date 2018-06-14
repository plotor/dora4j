package org.zhenchao.dubbo.spi.spring.ext2;

import org.springframework.stereotype.Service;

/**
 * @author zhenchao.wang 2018-01-02 11:11
 * @version 1.0.0
 */
@Service
public class Ext2ServiceImpl1 implements Ext2Service {

    @Override
    public String hello() {
        System.out.println("ext2 service impl1");
        return "Ext2ServiceImpl1";
    }
}
