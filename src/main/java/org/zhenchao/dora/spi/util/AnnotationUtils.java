package org.zhenchao.dora.spi.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * @author zhenchao.wang 2017-12-30 10:41
 * @version 1.0.0
 */
public class AnnotationUtils {

    private AnnotationUtils() {
    }

    public static <A extends Annotation> A getInheritedAnnotation(Class<A> annotationClass, AnnotatedElement element) {
        A annotation = element.getAnnotation(annotationClass);
        if (annotation == null && element instanceof Method) {
            annotation = getOverriddenAnnotation(annotationClass, (Method) element);
        }
        return annotation;
    }

    private static <A extends Annotation> A getOverriddenAnnotation(Class<A> annotationClass, Method method) {
        final Class<?> methodClass = method.getDeclaringClass();
        final String name = method.getName();
        final Class<?>[] params = method.getParameterTypes();

        // prioritize all superclasses over all interfaces
        final Class<?> superclass = methodClass.getSuperclass();
        if (superclass != null) {
            final A annotation = getOverriddenAnnotationFrom(annotationClass, superclass, name, params);
            if (annotation != null) {
                return annotation;
            }
        }

        // depth-first search over interface hierarchy
        for (final Class<?> itf : methodClass.getInterfaces()) {
            final A annotation = getOverriddenAnnotationFrom(annotationClass, itf, name, params);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private static <A extends Annotation> A getOverriddenAnnotationFrom(Class<A> annotationClass, Class<?> searchClass, String name, Class<?>[] params) {
        try {
            final Method method = searchClass.getMethod(name, params);
            final A annotation = method.getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
            return getOverriddenAnnotation(annotationClass, method);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

}
