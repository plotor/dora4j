package org.zhenchao.dubbo.spi.util;

import java.lang.reflect.GenericArrayType;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhenchao.wang 2017-12-29 15:27
 * @version 1.0.0
 */
public class TypeUtils {

    private static final Set<Class<?>> PRIMITIVE_TYPES = new HashSet<Class<?>>() {
        private static final long serialVersionUID = -8211463099842117491L;

        {
            add(byte.class);
            add(Byte.class);
            add(short.class);
            add(Short.class);
            add(int.class);
            add(Integer.class);
            add(long.class);
            add(Long.class);
            add(float.class);
            add(Float.class);
            add(double.class);
            add(Double.class);
            add(char.class);
            add(Character.class);
            add(boolean.class);
            add(Boolean.class);
            add(String.class);
        }
    };

    public static boolean isPrimitive(Class<?> clazz) {
        return PRIMITIVE_TYPES.contains(clazz);
    }

    public static boolean isNotPrimitive(Class<?> clazz) {
        return !isPrimitive(clazz);
    }

    public static boolean isPrimitiveInstance(Object obj) {
        return null != obj && isPrimitive(obj.getClass());
    }

    public static boolean isNotPrimitiveInstance(Object obj) {
        return !isPrimitiveInstance(obj);
    }

    public static boolean isArray(Class<?> clazz) {
        return null != clazz && (GenericArrayType.class.equals(clazz) || clazz.isArray());
    }

    public static boolean isNotArray(Class<?> clazz) {
        return !isArray(clazz);
    }

    public static boolean isArrayInstance(Object obj) {
        return null != obj && isArray(obj.getClass());
    }

    public static boolean isNotArrayInstance(Object obj) {
        return !isArrayInstance(obj);
    }

}
