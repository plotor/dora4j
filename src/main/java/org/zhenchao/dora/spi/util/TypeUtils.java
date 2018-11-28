package org.zhenchao.dora.spi.util;

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
            this.add(byte.class);
            this.add(Byte.class);
            this.add(short.class);
            this.add(Short.class);
            this.add(int.class);
            this.add(Integer.class);
            this.add(long.class);
            this.add(Long.class);
            this.add(float.class);
            this.add(Float.class);
            this.add(double.class);
            this.add(Double.class);
            this.add(char.class);
            this.add(Character.class);
            this.add(boolean.class);
            this.add(Boolean.class);
            this.add(String.class);
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
