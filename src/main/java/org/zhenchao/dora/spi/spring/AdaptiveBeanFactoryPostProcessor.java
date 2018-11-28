package org.zhenchao.dora.spi.spring;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.zhenchao.dora.spi.Adaptive;
import org.zhenchao.dora.spi.DIoC;
import org.zhenchao.dora.spi.support.DefaultFactorResolver;
import org.zhenchao.dora.spi.support.FactorResolver;
import org.zhenchao.dora.spi.util.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhenchao.wang 2018-01-01 13:05
 * @version 1.0.0
 */
public class AdaptiveBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(AdaptiveBeanFactoryPostProcessor.class);

    private FactorResolver factorResolver = new DefaultFactorResolver();

    private String basePackage;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (StringUtils.isBlank(basePackage)) {
            throw new IllegalArgumentException("base package is null");
        }

        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> withManualAdapters = new HashSet<Class<?>>();

        // 注册自定义适配器类
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Adaptive.class);
        for (final Class<?> clazz : annotated) {
            String[] beanNames = beanFactory.getBeanNamesForType(clazz);
            if (ArrayUtils.isEmpty(beanNames)) {
                // 如果对应的 bean 之前没有注册过，则注册
                try {
                    withManualAdapters.addAll(this.registerProxyBean(beanFactory, clazz));
                } catch (IllegalAccessException e) {
                    throw new BeanCreationException("register adapter bran exception : " + clazz.getName(), e);
                } catch (InstantiationException e) {
                    throw new BeanCreationException("register adapter bran exception : " + clazz.getName(), e);
                }
            } else {
                // 如果对应的 bean 之前已经注册，则建立别名
                Class<?>[] interfaces = clazz.getInterfaces();
                for (final Class<?> itf : interfaces) {
                    if (itf.isAnnotationPresent(DIoC.class)) {
                        String beanName = createProxyBeanName(clazz);
                        Set<String> bns = new HashSet<String>(Arrays.asList(beanNames));
                        if (bns.contains(beanName)) {
                            throw new IllegalStateException("more than one adaptive class found, type : " + itf.getName());
                        }
                        beanFactory.registerAlias(beanNames[0], beanName);
                        withManualAdapters.add(itf);
                    }
                }
            }
        }

        // 动态创建适配器类
        Set<Class<?>> ditfs = reflections.getTypesAnnotatedWith(DIoC.class, true);
        for (final Class<?> itf : ditfs) {
            if (!itf.isInterface()) {
                throw new IllegalStateException("dynamic ioc must be annotated on interface : " + itf.getName());
            }
            if (withManualAdapters.contains(itf)) {
                continue;
            }
            String beanName = createProxyBeanName(itf);
            if (beanFactory.containsBean(beanName)) {
                continue;
            }
            // 动态创建当前 bean 对应的实例
            beanFactory.registerSingleton(beanName, this.createAdaptiveExtensionInstance(itf, beanFactory));
        }

    }

    @SuppressWarnings("unchecked")
    private <T> T createAdaptiveExtensionInstance(final Class<T> type, final ConfigurableListableBeanFactory beanFactory) {
        final DIoC di = type.getAnnotation(DIoC.class);
        Method[] methods = type.getMethods();
        boolean withoutAdaptiveAnnotation = ArrayUtils.isEmpty(di.mapping());
        if (withoutAdaptiveAnnotation) {
            for (Method m : methods) {
                if (m.isAnnotationPresent(Adaptive.class)) {
                    withoutAdaptiveAnnotation = false;
                    break;
                }
            }
            if (withoutAdaptiveAnnotation) {
                throw new IllegalStateException("no adaptive method on extension " + type.getName() + ", refuse to create the adaptive class");
            }
        }

        // 基于动态代理生成对应的 adaptive 类
        log.info("Create adaptive extension by cglib, type : " + type.getName());
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                if (ArrayUtils.isEmpty(method.getParameterTypes())) {
                    throw new UnsupportedOperationException();
                }
                int index = di.index();
                String[] mapping = di.mapping();
                Adaptive adaptive = AnnotationUtils.getInheritedAnnotation(Adaptive.class, method);
                if (null != adaptive) {
                    index = adaptive.index() > 0 ? adaptive.index() : index;
                    mapping = ArrayUtils.isNotEmpty(adaptive.mapping()) ? adaptive.mapping() : mapping;
                }
                if (index < 0 || index >= args.length) {
                    throw new IllegalArgumentException("illegal adaptive index " + index + ", args length " + args.length + ", pointcut : " + type.getName() + "#" + method.getName());
                }
                if (ArrayUtils.isEmpty(mapping)) {
                    throw new IllegalStateException("illegal adaptive mapping, index " + index + ", args length " + args.length + ", pointcut : " + type.getName() + "#" + method.getName());
                }

                // 获取参数解析
                FactorResolver resolver = factorResolver;
                for (final Object arg : args) {
                    if (arg instanceof FactorResolver) {
                        resolver = (FactorResolver) arg;
                    }
                }

                String pv = resolver.resolve(args[index]), factor = null;
                for (final String mpg : mapping) {
                    String text = StringUtils.trimToEmpty(mpg);
                    if (StringUtils.isEmpty(mpg)) continue;
                    int i = text.indexOf("=");
                    String name = mpg.substring(0, i), value = mpg.substring(i + 1);
                    if (StringUtils.isBlank(name) || StringUtils.isBlank(value)) {
                        throw new IllegalArgumentException("adaptive mapping illegal, mapping : " + Arrays.toString(mapping) + ", type : " + type.getName());
                    }
                    if (name.equalsIgnoreCase(pv)) {
                        factor = value;
                        break;
                    }
                }
                if (StringUtils.isBlank(factor)) {
                    throw new IllegalArgumentException("input param " + pv + " has no adaptive mapping, config : " + Arrays.toString(mapping));
                }
                Object instance = beanFactory.getBean(factor);
                if (null == instance) {
                    throw new IllegalStateException("no bean found by name : " + factor + ", type : " + type.getName());
                }
                return method.invoke(instance, args);
            }
        });
        return (T) enhancer.create();
    }

    public static String createProxyBeanName(Class<?> clazz) {
        if (!clazz.isInterface() || !clazz.isAnnotationPresent(DIoC.class)) {
            throw new IllegalArgumentException(clazz.getName() + " must be interface and annotated with @" + DIoC.class.getName());
        }
        DIoC di = clazz.getAnnotation(DIoC.class);
        return StringUtils.isNotBlank(di.value()) ? di.value().trim() : clazz.getSimpleName() + "$proxy";
    }

    private Set<Class<?>> registerProxyBean(ConfigurableListableBeanFactory beanFactory, Class<?> clazz)
            throws IllegalAccessException, InstantiationException {
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("adapter must not be interface : " + clazz.getName());
        }
        Set<Class<?>> ditfs = new HashSet<Class<?>>();
        Class<?>[] interfaces = clazz.getInterfaces();
        for (final Class<?> itf : interfaces) {
            if (itf.isAnnotationPresent(DIoC.class)) {
                beanFactory.registerSingleton(createProxyBeanName(itf), clazz.newInstance());
                ditfs.add(itf);
            }
        }
        return ditfs;
    }

    public AdaptiveBeanFactoryPostProcessor setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }

    public AdaptiveBeanFactoryPostProcessor setFactorResolver(FactorResolver factorResolver) {
        this.factorResolver = factorResolver;
        return this;
    }
}
