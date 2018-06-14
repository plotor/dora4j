package org.zhenchao.dubbo.spi;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.zhenchao.dubbo.spi.adaptive.AdaptiveExt;
import org.zhenchao.dubbo.spi.adaptive.impl.AdaptiveExtWithManualAdaptive;
import org.zhenchao.dubbo.spi.ext1.SimpleExt;
import org.zhenchao.dubbo.spi.ext2.ExtWithInject;
import org.zhenchao.dubbo.spi.ext3.WrappedExt;
import org.zhenchao.dubbo.spi.ext4.Element;
import org.zhenchao.dubbo.spi.ext4.ElementFactorResolver;
import org.zhenchao.dubbo.spi.ext4.ExtWithFactorResolver;
import org.zhenchao.dubbo.spi.ext5.Extension;
import org.zhenchao.dubbo.spi.support.ExtensionLoader;
import org.zhenchao.dubbo.spi.support.FactorResolver;

import java.util.List;

/**
 * @author zhenchao.wang 2017-12-29 16:55
 * @version 1.0.0
 */
public class SpiTest {

    @Test
    public void manualAdaptiveClass() throws Exception {
        ExtensionLoader<AdaptiveExt> loader = ExtensionLoader.getExtensionLoader(AdaptiveExt.class);
        AdaptiveExt ext = loader.getAdaptiveExtension();
        assertTrue(ext instanceof AdaptiveExtWithManualAdaptive);
    }

    @Test
    public void adaptiveExtension() throws Exception {
        // 没有指定决策因子，使用默认的
        ExtensionLoader<SimpleExt> loader = ExtensionLoader.getExtensionLoader(SimpleExt.class);
        SimpleExt ext = loader.getAdaptiveExtension();
        Assert.assertEquals("Ext1Impl1-one", ext.one(2, "hello"));

        List<String> extensionNames = loader.getExtensionNames();
        for (int i = 1; i <= extensionNames.size(); i++) {
            Assert.assertEquals("impl" + i, extensionNames.get(i - 1));
        }

        // 默认以第一个参数作为决策因子
        Assert.assertEquals("Ext1Impl1-two", ext.two(1, "hello"));
        Assert.assertEquals("Ext1Impl2-two", ext.two(2, "hello"));
        Assert.assertEquals("Ext1Impl3-two", ext.two(3, "hello"));
        try {
            ext.two(0, "hello");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // 指定以第二个参数作为决策因子
        Assert.assertEquals("Ext1Impl1-three", ext.three(1, "a"));
        Assert.assertEquals("Ext1Impl2-three", ext.three(2, "b"));
        Assert.assertEquals("Ext1Impl3-three", ext.three(3, "c"));
        try {
            ext.three(1, "hello");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // 无@Adaptive注解
        try {
            ext.four(1, "a");
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void adaptiveExtensionWithInject() throws Exception {
        ExtWithInject ext = ExtensionLoader.getExtensionLoader(ExtWithInject.class).getExtension("impl1");
        Assert.assertEquals("ExtWithInjectImpl1-echo", ext.echo(1, "hello"));
    }

    @Test
    public void wrapperExtension() throws Exception {
        ExtensionLoader<WrappedExt> loader = ExtensionLoader.getExtensionLoader(WrappedExt.class);
        WrappedExt ext1 = loader.getExtension("impl1");
        ext1.echo(1, "hello");

        WrappedExt ext2 = loader.getExtension("impl2");
        ext2.echo(1, "hello");

        List<String> extensionNames = loader.getExtensionNames();
        for (final String extensionName : extensionNames) {
            System.out.println(extensionName);
        }
    }

    @Test
    public void factorResolver() throws Exception {
        ExtWithFactorResolver ext = ExtensionLoader.getExtensionLoader(ExtWithFactorResolver.class)
                .setFactorResolver(new ElementFactorResolver()).getAdaptiveExtension();

        Assert.assertEquals("FactorExtImpl1-one", ext.one(null, "hello"));
        Assert.assertEquals("FactorExtImpl2-two", ext.two(new Element("zhenchao", 2), "hello", new FactorResolver() {
            @Override
            public String resolve(Object arg) {
                if (arg instanceof Element) {
                    return String.valueOf(((Element) arg).getAge());
                }
                throw new IllegalArgumentException("not element type");
            }
        }));
        Assert.assertEquals("FactorExtImpl3-three", ext.three(new Element("c", 1), "hello"));
    }

    @Test
    public void extensionNames() throws Exception {
        ExtensionLoader<Extension> loader = ExtensionLoader.getExtensionLoader(Extension.class);
        List<String> names = loader.getExtensionNames();
        System.out.println(StringUtils.join(names, "\n"));
        Assert.assertEquals(loader.getSupportedExtensions().size(), names.size());
        Assert.assertEquals("zzz", names.get(0));
        Assert.assertEquals("extensionimpl2", names.get(1));
        Assert.assertEquals("wrapper0", names.get(2));
        Assert.assertEquals("111", names.get(3));
        Assert.assertEquals("wrapper", names.get(4));
        Assert.assertEquals("adaptive", names.get(5));
    }

}
