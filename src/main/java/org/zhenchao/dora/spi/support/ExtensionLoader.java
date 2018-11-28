package org.zhenchao.dora.spi.support;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zhenchao.dora.spi.Adaptive;
import org.zhenchao.dora.spi.SPI;
import org.zhenchao.dora.spi.factory.ExtensionFactory;
import org.zhenchao.dora.spi.util.AnnotationUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

/**
 * @author zhenchao.wang 2017-12-29 13:40
 * @version 1.0.0
 */
public class ExtensionLoader<T> {

    private static final Logger log = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String INTERNAL_SERVICES_DIRECTORY = "META-INF/spi/internal/";
    private static final String SERVICES_DIRECTORY = "META-INF/spi/";

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();

    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();

    private final Class<?> type;

    private final ExtensionFactory objectFactory;

    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();

    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String, Class<?>>>();

    private final List<String> extensionNames = new ArrayList<String>();

    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();

    private final Holder<Object> cachedAdaptiveInstance = new Holder<Object>();

    private volatile Class<?> cachedAdaptiveClass = null;

    private String cachedDefaultName;

    private volatile Throwable createAdaptiveInstanceError;

    private Set<Class<?>> cachedWrapperClasses;

    private FactorResolver factorResolver = new DefaultFactorResolver();

    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
        this.objectFactory = (type == ExtensionFactory.class ? null :
                ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("input extension type is null");
        }
        // 必须是接口类型
        if (!type.isInterface()) {
            throw new IllegalArgumentException("extension type(" + type + ") is not interface");
        }
        // 必须被 @SPI 注解
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException(
                    "extension type(" + type + ") must be annotated by @" + SPI.class.getSimpleName());
        }

        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    private static ClassLoader findClassLoader() {
        return ExtensionLoader.class.getClassLoader();
    }

    public String getExtensionName(T extensionInstance) {
        return this.getExtensionName(extensionInstance.getClass());
    }

    public String getExtensionName(Class<?> extensionClass) {
        return cachedNames.get(extensionClass);
    }

    /**
     * 返回扩展点实例，如果没有指定的扩展点或是还没加载（即实例化）则返回<code>null</code>。注意：此方法不会触发扩展点的加载。
     * <p/>
     * 一般应该调用{@link #getExtension(String)}方法获得扩展，这个方法会触发扩展点加载。
     *
     * @see #getExtension(String)
     */
    @SuppressWarnings("unchecked")
    public T getLoadedExtension(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("input extension name is null");
        }
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            holder = cachedInstances.get(name);
        }
        return (T) holder.get();
    }

    /**
     * 返回已经加载的扩展点的名字。
     * <p/>
     * 一般应该调用{@link #getSupportedExtensions()}方法获得扩展，这个方法会返回所有的扩展点。
     *
     * @see #getSupportedExtensions()
     */
    public Set<String> getLoadedExtensions() {
        return Collections.unmodifiableSet(new TreeSet<String>(cachedInstances.keySet()));
    }

    /**
     * 返回指定名字的扩展实例
     * 如果指定名字的扩展不存在，则抛异常 {@link IllegalStateException}.
     *
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("input extension name is null");
        }
        if ("true".equalsIgnoreCase(name)) {
            return this.getDefaultExtension();
        }

        // 获取指定名称的扩展类型实例
        Holder<Object> holder = cachedInstances.get(name); // 先尝试从缓存中获取
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    // 创建扩展类型实现
                    instance = this.createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 返回缺省的扩展，如果没有设置则返回<code>null</code>。
     */
    public T getDefaultExtension() {
        this.getExtensionClasses();
        if (StringUtils.isBlank(cachedDefaultName) || "true".equalsIgnoreCase(cachedDefaultName)) {
            return null;
        }
        return this.getExtension(cachedDefaultName);
    }

    public boolean hasExtension(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("input extension name is null");
        }
        try {
            return this.getExtensionClass(name) != null;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 获取支持的扩展名称列表
     *
     * @return
     */
    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> clazzes = this.getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<String>(clazzes.keySet()));
    }

    /**
     * 获取支持的扩展名称（按照配置顺序组织）
     *
     * @return
     */
    public List<String> getExtensionNames() {
        if (extensionNames.isEmpty()) {
            this.getExtensionClasses();
        }
        return extensionNames;
    }

    /**
     * 返回缺省的扩展点名，如果没有设置缺省则返回<code>null</code>。
     */
    public String getDefaultExtensionName() {
        this.getExtensionClasses();
        return cachedDefaultName;
    }

    /**
     * 编程方式添加新扩展点，记录正向与反向映射关系
     *
     * @param name 扩展点名
     * @param clazz 扩展点类
     * @throws IllegalStateException 要添加扩展点名已经存在。
     */
    public void addExtension(String name, Class<?> clazz) {
        this.getExtensionClasses(); // load classes

        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("input type " + clazz + " not implement extension " + type);
        }
        if (clazz.isInterface()) {
            throw new IllegalStateException("input type " + clazz + " must be interface");
        }

        if (!clazz.isAnnotationPresent(Adaptive.class)) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalStateException("input extension name is blank");
            }
            if (cachedClasses.get().containsKey(name)) {
                throw new IllegalStateException("extension name " + name + " already existed, extension : " + type);
            }
            cachedNames.put(clazz, name);
            cachedClasses.get().put(name, clazz);
            extensionNames.add(name);
        } else {
            if (cachedAdaptiveClass != null) {
                throw new IllegalStateException("adaptive extension already existed, extension : " + type);
            }
            cachedAdaptiveClass = clazz;
        }
    }

    /**
     * 编程方式添加替换已有扩展点
     *
     * @param name 扩展点名
     * @param clazz 扩展点类
     * @throws IllegalStateException 要添加扩展点名已经存在。
     * @deprecated 不推荐应用使用，一般只在测试时可以使用
     */
    @Deprecated
    public void replaceExtension(String name, Class<?> clazz) {
        this.getExtensionClasses(); // load classes

        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("input type " + clazz + " not implement extension " + type);
        }
        if (clazz.isInterface()) {
            throw new IllegalStateException("input type " + clazz + " must be interface");
        }

        if (!clazz.isAnnotationPresent(Adaptive.class)) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalStateException("input extension name is blank");
            }
            if (!cachedClasses.get().containsKey(name)) {
                throw new IllegalStateException("extension name " + name + " not existed, extension : " + type);
            }
            cachedNames.put(clazz, name);
            cachedClasses.get().put(name, clazz);
            cachedInstances.remove(name);
            extensionNames.remove(name);
        } else {
            if (cachedAdaptiveClass == null) {
                throw new IllegalStateException("adaptive extension not existed, extension : " + type);
            }
            cachedAdaptiveClass = clazz;
            cachedAdaptiveInstance.set(null);
            extensionNames.remove(name);
        }
    }

    @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            if (createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = this.createAdaptiveExtension();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("fail to create adaptive instance : " + t.toString(), t);
                        }
                    }
                }
            } else {
                throw new IllegalStateException("fail to create adaptive instance : " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
            }
        }
        return (T) instance;
    }

    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("no such extension " + type.getName() + " by name " + name);

        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(entry.getValue().getMessage());
        }
        return new IllegalStateException(buf.toString());
    }

    /**
     * 1. 创建指定扩展名称对应的扩展类型实例
     * 2. 激活扩展类型参数的 setter 方法
     * 3. 应用包装类对实例进行包装
     *
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        Class<?> clazz = this.getExtensionClasses().get(name);
        if (clazz == null) {
            throw this.findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            // 执行 setter 注入
            this.injectExtension(instance);
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            if (wrapperClasses != null && wrapperClasses.size() > 0) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    // 采用包装类逐层包装
                    instance = this.injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException(
                    "extension instance(name: " + name + ", class: " + type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    /**
     * 遍历 setter 注入对应的扩展类型实例
     *
     * @param instance
     * @return
     */
    private T injectExtension(T instance) {
        try {
            if (objectFactory != null) {
                for (Method method : instance.getClass().getMethods()) {
                    if (method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        // 获取属性类型
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            // 获取属性名称
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                                // 反射调用
                                method.invoke(instance, object);
                            }
                        } catch (Exception e) {
                            log.error("fail to inject via method " + method.getName() + " of interface " + type.getName() + " : " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return instance;
    }

    /**
     * 获取指定扩展名称的扩展实现类
     *
     * @param name
     * @return
     */
    private Class<?> getExtensionClass(String name) {
        if (type == null) {
            throw new IllegalArgumentException("extension type is null");
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("input extension name is null");
        }
        Class<?> clazz = this.getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalStateException("no extension found by name : " + name + ", type : " + type.getName());
        }
        return clazz;
    }

    /**
     * 加载当前 SPI 类型的所有扩展配置，记录的正向绑定集合中
     *
     * @return
     */
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    // 加载当前类型的 SPI 配置
                    classes = this.loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            // 指定默认的扩展名称
            if ((value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    // 只能指定一个默认扩展名称
                    throw new IllegalStateException(
                            "more than one default extension name found, type " + type.getName() + " : " + Arrays.toString(names));
                }
                if (names.length == 1) {
                    cachedDefaultName = names[0];
                }
            }
        }

        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        this.loadSpiFile(extensionClasses, INTERNAL_SERVICES_DIRECTORY);
        this.loadSpiFile(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    private void loadSpiFile(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + type.getName();
        try {
            ClassLoader classLoader = findClassLoader();
            Enumeration<URL> urls = classLoader != null ? classLoader.getResources(fileName) : ClassLoader.getSystemResources(fileName);
            if (null == urls) return;

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                    try {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            final int ci = line.indexOf('#');
                            if (ci >= 0) {
                                line = line.substring(0, ci);
                            }
                            line = line.trim();
                            if (line.length() > 0) { // 忽略纯注释行
                                try {
                                    String name = null; // 扩展名
                                    int i = line.indexOf('=');
                                    if (i > 0) {
                                        name = line.substring(0, i).trim();
                                        line = line.substring(i + 1).trim();
                                    }
                                    if (line.length() > 0) {
                                        Class<?> clazz = Class.forName(line, true, classLoader);
                                        if (!type.isAssignableFrom(clazz)) {
                                            // 扩展类型必须是接口的实现类型
                                            throw new IllegalStateException("load extension error, class(interface : " + type + ", class line: " + clazz.getName() + "), " +
                                                    "class " + clazz.getName() + " is not subtype of interface.");
                                        }
                                        if (clazz.isAnnotationPresent(Adaptive.class)) {
                                            if (cachedAdaptiveClass == null) {
                                                cachedAdaptiveClass = clazz;
                                            } else if (!cachedAdaptiveClass.equals(clazz)) {
                                                // 不允许存在多个 Adaptive 类
                                                throw new IllegalStateException(
                                                        "more than one adaptive class found : " + cachedAdaptiveClass.getClass().getName() + ", " + clazz.getClass().getName());
                                            }
                                        } else {
                                            // 非 Adaptive 类
                                            try {
                                                // 检测是否具备只有 spi 接口类型的构造函数，如果有的话就是一个包装类
                                                clazz.getConstructor(type);
                                                Set<Class<?>> wrappers = cachedWrapperClasses;
                                                if (wrappers == null) {
                                                    cachedWrapperClasses = new CopyOnWriteArraySet<Class<?>>();
                                                    wrappers = cachedWrapperClasses;
                                                }
                                                wrappers.add(clazz);
                                            } catch (NoSuchMethodException e) {
                                                clazz.getConstructor();
                                                if (StringUtils.isBlank(name)) {
                                                    // 按照策略获取实现类的扩展名
                                                    name = this.createExtensionName(clazz);
                                                    if (StringUtils.isBlank(name)) {
                                                        if (clazz.getSimpleName().length() > type.getSimpleName().length()
                                                                && clazz.getSimpleName().endsWith(type.getSimpleName())) {
                                                            name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - type.getSimpleName().length()).toLowerCase();
                                                        } else {
                                                            throw new IllegalStateException("no such extension name for the class " + clazz.getName() + " in the config " + url);
                                                        }
                                                    }
                                                }
                                                // 以逗号分割
                                                String[] names = NAME_SEPARATOR.split(name);
                                                if (ArrayUtils.isNotEmpty(names)) {
                                                    for (String n : names) {
                                                        if (!cachedNames.containsKey(clazz)) {
                                                            cachedNames.put(clazz, n);
                                                            extensionNames.add(name);
                                                        }
                                                        Class<?> c = extensionClasses.get(n);
                                                        if (c == null) {
                                                            extensionClasses.put(n, clazz);
                                                        } else if (c != clazz) {
                                                            // 对于同一个扩展名称，配置了多个扩展实现
                                                            throw new IllegalStateException("duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Throwable t) {
                                    IllegalStateException e = new IllegalStateException("failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                    exceptions.put(line, e);
                                }
                            }
                        }
                    } finally {
                        reader.close();
                    }
                } catch (Throwable t) {
                    log.error("load extension exception, class(interface: " + type + ", class file: " + url + ") in " + url, t);
                }
            }
        } catch (Throwable t) {
            log.error("load extension exception, class(interface: " + type + ", description file: " + fileName + ").", t);
        }
    }

    private String createExtensionName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        if (name.endsWith(type.getSimpleName())) {
            // 如果名称以 spi 接口 simple name 为后缀，则删除掉
            name = name.substring(0, name.length() - type.getSimpleName().length());
        }
        return name.toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            this.getExtensionClasses();
            if (null != cachedAdaptiveClass) {
                return this.injectExtension((T) cachedAdaptiveClass.newInstance());
            }
            return this.injectExtension(this.createAdaptiveExtensionInstance());
        } catch (Exception e) {
            throw new IllegalStateException("can not create adaptive extension " + type + ", cause : " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private T createAdaptiveExtensionInstance() {
        Method[] methods = type.getMethods();
        boolean withoutAdaptiveAnnotation = true;
        for (Method m : methods) {
            if (m.isAnnotationPresent(Adaptive.class)) {
                withoutAdaptiveAnnotation = false;
                break;
            }
        }
        if (withoutAdaptiveAnnotation) {
            throw new IllegalStateException("no adaptive method on extension " + type.getName() + ", refuse to create the adaptive class");
        }

        // 基于动态代理生成对应的 adaptive 类
        log.info("Create adaptive extension by cglib, type : " + type.getName());
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                Adaptive adaptive = AnnotationUtils.getInheritedAnnotation(Adaptive.class, method);
                if (null == adaptive) {
                    throw new UnsupportedOperationException("method is not adaptive, type : " + type.getName() + ", method : " + method.getName());
                }
                int index = adaptive.index();
                if (index < 0 || index >= args.length) {
                    throw new IllegalArgumentException("illegal adaptive index " + index + ", args length " + args.length + ", pointcut : " + type.getName() + "#" + method.getName());
                }
                String[] mapping = adaptive.mapping();
                if (ArrayUtils.isEmpty(mapping)) {
                    if (StringUtils.isNotBlank(cachedDefaultName)) {
                        log.info("No adaptive config found and use default extension '" + cachedDefaultName + "'");
                        Object instance = ExtensionLoader.getExtensionLoader(type).getExtension(cachedDefaultName);
                        if (null == instance) {
                            throw new IllegalStateException("no default extension found by name : " + cachedDefaultName + ", type : " + type.getName());
                        }
                        return method.invoke(instance, args);
                    }
                    throw new IllegalStateException("adaptive mapping is missing, index " + index + ", args length " + args.length + ", pointcut : " + type.getName() + "#" + method.getName());
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
                Object instance = ExtensionLoader.getExtensionLoader(type).getExtension(factor);
                if (null == instance) {
                    throw new IllegalStateException("no extension found by name : " + factor + ", type : " + type.getName());
                }
                return method.invoke(instance, args);
            }
        });
        return (T) enhancer.create();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + type.getName() + "]";
    }

    public ExtensionLoader<T> setFactorResolver(FactorResolver factorResolver) {
        this.factorResolver = factorResolver;
        return this;
    }
}