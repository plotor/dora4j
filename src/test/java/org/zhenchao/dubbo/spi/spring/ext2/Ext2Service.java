package org.zhenchao.dubbo.spi.spring.ext2;

import org.zhenchao.dubbo.spi.DIoC;

/**
 * @author zhenchao.wang 2018-01-02 11:06
 * @version 1.0.0
 */
@DIoC("ext2_service_proxy")
public interface Ext2Service {

    String hello();

}
