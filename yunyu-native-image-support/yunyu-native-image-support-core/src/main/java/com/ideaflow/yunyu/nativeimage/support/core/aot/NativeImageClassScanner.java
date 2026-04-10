package com.ideaflow.yunyu.nativeimage.support.core.aot;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.core.ResolvableType;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Native Image 构建期类扫描器类。
 * 作用：在 AOT 阶段按业务主包扫描 Mapper、实体和 Lambda 捕获类，为 RuntimeHints 注册提供输入。
 */
public class NativeImageClassScanner {

    /**
     * Native support 自身包前缀。
     * 作用：避免把仅在 GraalVM 构建期使用的 support 类再次当成业务类扫描并注册到运行期元数据中。
     */
    private static final String NATIVE_SUPPORT_PACKAGE_PREFIX = "com.ideaflow.yunyu.nativeimage.support";

    /**
     * 当前扫描器使用的类加载器。
     */
    private final ClassLoader classLoader;

    /**
     * 使用指定类加载器创建扫描器。
     *
     * @param classLoader AOT 阶段类加载器
     */
    public NativeImageClassScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 扫描指定包下满足条件的类集合。
     *
     * @param basePackage 基础包名
     * @param predicate 类过滤条件
     * @return 满足条件的类集合
     */
    public Set<Class<?>> collectClasses(String basePackage, Predicate<Class<?>> predicate) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        try {
            for (String className : findClassNames(basePackage)) {
                if (shouldSkipClass(className)) {
                    continue;
                }
                Class<?> candidateClass = loadClass(className);
                if (candidateClass != null && predicate.test(candidateClass)) {
                    classes.add(candidateClass);
                }
            }
        } catch (IOException ignored) {
            return classes;
        }
        return classes;
    }

    /**
     * 扫描业务包中的 Mapper 接口。
     *
     * @param scanPackages 扫描包集合
     * @return Mapper 接口集合
     */
    public Set<Class<?>> findMapperInterfaces(Set<String> scanPackages) {
        Set<Class<?>> mapperInterfaces = new LinkedHashSet<>();
        for (String scanPackage : scanPackages) {
            mapperInterfaces.addAll(collectClasses(scanPackage, candidate ->
                    candidate.isInterface()
                            && (candidate.isAnnotationPresent(Mapper.class)
                            || (BaseMapper.class.isAssignableFrom(candidate) && !BaseMapper.class.equals(candidate)))));
        }
        return mapperInterfaces;
    }

    /**
     * 根据 Mapper 泛型信息解析实体类集合。
     *
     * @param mapperInterfaces Mapper 接口集合
     * @return 实体类集合
     */
    public Set<Class<?>> findEntityClasses(Set<Class<?>> mapperInterfaces) {
        Set<Class<?>> entityClasses = new LinkedHashSet<>();
        for (Class<?> mapperInterface : mapperInterfaces) {
            Class<?> entityClass = ResolvableType.forClass(mapperInterface)
                    .as(BaseMapper.class)
                    .resolveGeneric(0);
            if (entityClass != null) {
                entityClasses.add(entityClass);
            }
        }
        return entityClasses;
    }

    /**
     * 扫描业务包中的 Lambda 捕获类。
     *
     * @param scanPackages 扫描包集合
     * @return Lambda 捕获类集合
     */
    public Set<Class<?>> findLambdaCapturingClasses(Set<String> scanPackages) {
        Set<Class<?>> lambdaCapturingClasses = new LinkedHashSet<>();
        for (String scanPackage : scanPackages) {
            lambdaCapturingClasses.addAll(collectClasses(scanPackage, this::isLambdaCapturingClass));
        }
        return lambdaCapturingClasses;
    }

    /**
     * 扫描业务主包下的全部项目类。
     * 作用：为 Spring MVC 参数绑定、Jackson 序列化和其他运行期反射场景统一补齐业务类型元数据。
     *
     * @param scanPackages 扫描包集合
     * @return 业务项目类集合
     */
    public Set<Class<?>> findProjectClasses(Set<String> scanPackages) {
        Set<Class<?>> projectClasses = new LinkedHashSet<>();
        for (String scanPackage : scanPackages) {
            projectClasses.addAll(collectClasses(scanPackage, candidate -> true));
        }
        return projectClasses;
    }

    /**
     * 判断类是否包含 Lambda 反序列化入口。
     *
     * @param candidateClass 待判断类
     * @return 是否为 Lambda 捕获类
     */
    public boolean isLambdaCapturingClass(Class<?> candidateClass) {
        for (var method : candidateClass.getDeclaredMethods()) {
            if (method.getName().contains("$deserializeLambda$")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按类名尝试加载类。
     *
     * @param className 全限定类名
     * @return 成功加载的类，失败时返回 {@code null}
     */
    public Class<?> loadClass(String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    /**
     * 查找基础包下全部类名。
     *
     * @param basePackage 基础包
     * @return 类名集合
     * @throws IOException 扫描资源异常
     */
    private Set<String> findClassNames(String basePackage) throws IOException {
        Set<String> classNames = new LinkedHashSet<>();
        String path = basePackage.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            if ("file".equals(protocol)) {
                File directory = new File(URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8));
                if (directory.exists()) {
                    findClassesInDirectory(directory, basePackage, classNames);
                }
                continue;
            }
            if ("jar".equals(protocol)) {
                String jarPath = resource.getFile().substring(5, resource.getFile().indexOf('!'));
                try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (!entry.isDirectory() && entryName.endsWith(".class") && entryName.startsWith(path)) {
                            classNames.add(entryName.substring(0, entryName.length() - 6).replace('/', '.'));
                        }
                    }
                }
            }
        }
        return classNames;
    }

    /**
     * 递归扫描目录中的类文件。
     *
     * @param directory 当前目录
     * @param packageName 当前包名
     * @param classNames 类名收集容器
     */
    private void findClassesInDirectory(File directory, String packageName, Set<String> classNames) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                String nextPackageName = packageName.isEmpty()
                        ? file.getName()
                        : packageName + "." + file.getName();
                findClassesInDirectory(file, nextPackageName, classNames);
                continue;
            }
            if (file.getName().endsWith(".class")) {
                String simpleClassName = file.getName().substring(0, file.getName().length() - 6);
                classNames.add(packageName + "." + simpleClassName);
            }
        }
    }

    /**
     * 判断当前类名是否应该跳过扫描。
     * 作用：过滤掉 Lambda 合成类、`package-info` 以及 Native support 自身的构建期类，避免污染业务扫描结果。
     *
     * @param className 待判断的类名
     * @return 是否跳过
     */
    private boolean shouldSkipClass(String className) {
        return className.contains("$$")
                || className.contains("package-info")
                || className.startsWith(NATIVE_SUPPORT_PACKAGE_PREFIX);
    }
}
