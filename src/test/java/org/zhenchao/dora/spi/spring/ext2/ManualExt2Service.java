package org.zhenchao.dora.spi.spring.ext2;

import org.zhenchao.dora.spi.Adaptive;

/**
 * @author zhenchao.wang 2018-01-02 11:08
 * @version 1.0.0
 */
@Adaptive
public class ManualExt2Service implements Ext2Service {

    @Override
    public String hello() {
        System.out.println("manual ext2 service");
        return "ManualExt2Service";
    }
}
