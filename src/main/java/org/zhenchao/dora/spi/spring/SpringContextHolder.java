package org.zhenchao.dora.spi.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author zhenchao.wang 2018-01-02 15:02
 * @version 1.0.0
 */
@Component
@SuppressWarnings("unchecked")
public class SpringContextHolder implements ApplicationContextAware {

    protected static ApplicationContext applicationContext;

    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    public static Object getBean(String beanName, Object... args) {
        return applicationContext.getBean(beanName, args);
    }

    public static <T> T getBean(String beanName, Class<T> requiredType) {
        return applicationContext.getBean(beanName, requiredType);
    }

    public static <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    public static <T> T getBean(Class<T> requiredType, Object... args) {
        return applicationContext.getBean(requiredType, args);
    }

    public static <T> T getProxyBean(Class<T> requiredType) {
        return getBean(AdaptiveBeanFactoryPostProcessor.createProxyBeanName(requiredType), requiredType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
    }
}
