package org.zhenchao.dubbo.spi.spring;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zhenchao.dubbo.spi.DIoC;
import org.zhenchao.dubbo.spi.spring.ext1.Ext1Service;
import org.zhenchao.dubbo.spi.spring.ext2.Ext2Service;
import org.zhenchao.dubbo.spi.spring.ext3.Ext3Service;
import org.zhenchao.dubbo.spi.support.FactorResolver;

/**
 * @author zhenchao.wang 2018-01-01 14:38
 * @version 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-core.xml")
public class SpringAdaptiveTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    @Qualifier("Ext1Service$proxy")
    private Ext1Service ext1Service;

    @Autowired
    @Qualifier("ext2_service_proxy")
    private Ext2Service ext2Service;

    @Autowired
    @Qualifier("Ext3Service$proxy")
    private Ext3Service ext3Service;

    @Test
    public void adaptiveInstance() throws Exception {
        // Ext1Service ext1Service = applicationContext.getBean(this.createProxyBeanName(Ext1Service.class), Ext1Service.class);
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("DemoServiceImpl1-zhenchao", ext1Service.hello(1, "zhenchao"));
            Assert.assertEquals("DemoServiceImpl2-zhenchao", ext1Service.hello(2, "zhenchao"));
        }
    }

    @Test
    public void manualAdaptiveInstance() throws Exception {
        Assert.assertEquals("ManualExt2Service", ext2Service.hello());
    }

    @Test
    public void typeLevelAdaptive() throws Exception {
        Assert.assertEquals("Ext3ServiceImpl1-one", ext3Service.one("a"));
        Assert.assertEquals("Ext3ServiceImpl2-one", ext3Service.one("b"));

        FactorResolver resolver = new FactorResolver() {
            @Override
            public String resolve(Object arg) {
                Integer i = (Integer) arg;
                if (1 == i) return "a";
                if (2 == i) return "b";
                return null;
            }
        };
        Assert.assertEquals("Ext3ServiceImpl1-two", ext3Service.two(RandomStringUtils.randomAlphabetic(8), 1, resolver));
        Assert.assertEquals("Ext3ServiceImpl2-two", ext3Service.two(RandomStringUtils.randomAlphabetic(8), 2, resolver));

        Assert.assertEquals("Ext3ServiceImpl1-three", ext3Service.three(1));
        Assert.assertEquals("Ext3ServiceImpl2-three", ext3Service.three(2));

        Assert.assertEquals("Ext3ServiceImpl1-four", ext3Service.four(RandomStringUtils.randomAlphabetic(8), 1));
        Assert.assertEquals("Ext3ServiceImpl2-four", ext3Service.four(RandomStringUtils.randomAlphabetic(8), 2));

        try {
            ext3Service.five();
            Assert.fail();
        } catch (Throwable e) {
            Assert.assertTrue(e instanceof UnsupportedOperationException);
        }

    }

    private String createProxyBeanName(Class<?> clazz) {
        if (!clazz.isInterface() || !clazz.isAnnotationPresent(DIoC.class)) {
            throw new IllegalArgumentException(clazz.getName() + " must be interface and annotated with @" + DIoC.class.getName());
        }
        DIoC di = clazz.getAnnotation(DIoC.class);
        return StringUtils.isNotBlank(di.value()) ? di.value().trim() : clazz.getSimpleName() + "$proxy";
    }

}
