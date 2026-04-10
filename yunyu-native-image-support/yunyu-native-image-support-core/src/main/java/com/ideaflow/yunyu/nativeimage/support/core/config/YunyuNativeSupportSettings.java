package com.ideaflow.yunyu.nativeimage.support.core.config;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ideaflow.yunyu.nativeimage.support.core.aot.NativeImageClassScanner;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Yunyu Native 支撑配置对象类。
 * 作用：统一收口 AOT 阶段需要使用的主包扫描与扩展包扫描配置。
 */
public class YunyuNativeSupportSettings {

    /**
     * Native 构建时用于传递启动类的系统属性名。
     */
    public static final String APPLICATION_CLASS_PROPERTY = "yunyu.native.applicationClass";

    /**
     * Native 构建时用于补充扩展扫描包的系统属性名。
     */
    public static final String SCAN_PACKAGES_PROPERTY = "yunyu.native.scan-packages";

    /**
     * 启动类类型。
     */
    private final Class<?> applicationClass;

    /**
     * 需要参与扫描的包集合。
     */
    private final Set<String> scanPackages;

    /**
     * 使用扫描包集合创建配置对象。
     *
     * @param applicationClass 启动类
     * @param scanPackages 需要参与扫描的包集合
     */
    public YunyuNativeSupportSettings(Class<?> applicationClass, Set<String> scanPackages) {
        this.applicationClass = applicationClass;
        this.scanPackages = scanPackages;
    }

    /**
     * 从当前 BeanFactory 和环境配置中解析 Native 支撑设置。
     *
     * @param beanFactory Spring BeanFactory
     * @return 解析后的 Native 支撑设置
     */
    public static YunyuNativeSupportSettings fromBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        LinkedHashSet<String> packages = new LinkedHashSet<>();
        Environment environment = resolveEnvironment(beanFactory);
        boolean scanMainPackage = true;
        if (environment != null) {
            scanMainPackage = Binder.get(environment)
                    .bind("yunyu.native.scan-main-package", Boolean.class)
                    .orElse(true);
        }
        if (scanMainPackage && AutoConfigurationPackages.has(beanFactory)) {
            packages.addAll(AutoConfigurationPackages.get(beanFactory));
        }
        if (environment != null) {
            List<String> extraPackages = Binder.get(environment)
                    .bind("yunyu.native.scan-packages", Bindable.listOf(String.class))
                    .orElse(List.of());
            for (String extraPackage : extraPackages) {
                if (extraPackage != null && !extraPackage.isBlank()) {
                    packages.add(extraPackage.trim());
                }
            }
        }
        return new YunyuNativeSupportSettings(null, packages);
    }

    /**
     * 根据启动类解析 Native 支撑配置。
     * 作用：在 GraalVM Feature 阶段为运行期注册逻辑提供统一配置入口。
     *
     * @param applicationClass 启动类
     * @return Native 支撑配置
     */
    public static YunyuNativeSupportSettings fromApplicationClass(Class<?> applicationClass) {
        return new YunyuNativeSupportSettings(
                applicationClass,
                resolveScanPackages(applicationClass, extractMapperScanPackages(applicationClass))
        );
    }

    /**
     * 根据 Native 构建系统属性解析配置。
     * 作用：兼容 GraalVM Feature 在 Spring 容器之外的构建期运行。
     *
     * @param classLoader Native 构建期类加载器
     * @return Native 支撑配置
     */
    public static YunyuNativeSupportSettings fromSystemProperty(ClassLoader classLoader) {
        String applicationClassName = System.getProperty(APPLICATION_CLASS_PROPERTY);
        if (applicationClassName == null || applicationClassName.isBlank()) {
            throw new IllegalStateException(
                    "缺少系统属性 " + APPLICATION_CLASS_PROPERTY
                            + "，请在 native-maven-plugin 的 buildArgs 中添加 "
                            + "-Dyunyu.native.applicationClass=<启动类全限定名>"
            );
        }
        Class<?> applicationClass = ClassUtils.resolveClassName(applicationClassName, classLoader);
        return fromApplicationClass(applicationClass);
    }

    /**
     * 返回需要参与扫描的包集合。
     *
     * @return 扫描包集合
     */
    public Set<String> getScanPackages() {
        return scanPackages;
    }

    /**
     * 返回启动类类型。
     *
     * @return 启动类类型
     */
    public Class<?> getApplicationClass() {
        return applicationClass;
    }

    /**
     * 解析需要参与 Native 构建期注册的 Mapper 接口。
     *
     * @param scanner Native 构建期类扫描器
     * @return Mapper 接口集合
     */
    public Set<Class<?>> resolveMapperInterfaces(NativeImageClassScanner scanner) {
        Set<Class<?>> mapperInterfaces = new LinkedHashSet<>();
        for (String scanPackage : scanPackages) {
            mapperInterfaces.addAll(scanner.collectClasses(
                    scanPackage,
                    candidate -> candidate.isInterface()
                            && ((BaseMapper.class.isAssignableFrom(candidate) && !BaseMapper.class.equals(candidate))
                            || candidate.isAnnotationPresent(Mapper.class))
            ));
        }
        return mapperInterfaces;
    }

    /**
     * 从 BeanFactory 中解析 Environment。
     *
     * @param beanFactory Spring BeanFactory
     * @return Spring Environment，不存在时返回 {@code null}
     */
    private static Environment resolveEnvironment(ConfigurableListableBeanFactory beanFactory) {
        try {
            return beanFactory.getBean(Environment.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 合并启动类主包、`@MapperScan` 和系统属性中的扩展扫描包。
     *
     * @param applicationClass 启动类
     * @param configuredPackages 注解中显式声明的扩展包
     * @return 扫描包集合
     */
    private static Set<String> resolveScanPackages(Class<?> applicationClass, String[] configuredPackages) {
        Set<String> packages = new LinkedHashSet<>();
        packages.add(applicationClass.getPackageName());
        for (String scanPackage : configuredPackages) {
            if (scanPackage != null && !scanPackage.isBlank()) {
                packages.add(scanPackage.trim());
            }
        }
        packages.addAll(parseScanPackages(System.getProperty(SCAN_PACKAGES_PROPERTY)));
        return packages;
    }

    /**
     * 从启动类上的 `@MapperScan` 中提取扩展扫描包。
     *
     * @param applicationClass 启动类
     * @return `@MapperScan` 声明的包数组
     */
    private static String[] extractMapperScanPackages(Class<?> applicationClass) {
        Set<String> mapperScanPackages = new LinkedHashSet<>();
        for (MapperScan mapperScan : applicationClass.getAnnotationsByType(MapperScan.class)) {
            for (String basePackage : mapperScan.basePackages()) {
                if (basePackage != null && !basePackage.isBlank()) {
                    mapperScanPackages.add(basePackage.trim());
                }
            }
            for (String valuePackage : mapperScan.value()) {
                if (valuePackage != null && !valuePackage.isBlank()) {
                    mapperScanPackages.add(valuePackage.trim());
                }
            }
            for (Class<?> basePackageClass : mapperScan.basePackageClasses()) {
                mapperScanPackages.add(basePackageClass.getPackageName());
            }
        }
        return mapperScanPackages.toArray(String[]::new);
    }

    /**
     * 解析系统属性中的扩展扫描包。
     *
     * @param configuredValue 系统属性值
     * @return 扩展扫描包集合
     */
    private static Set<String> parseScanPackages(String configuredValue) {
        Set<String> packages = new LinkedHashSet<>();
        if (configuredValue == null || configuredValue.isBlank()) {
            return packages;
        }
        for (String candidate : configuredValue.split(",")) {
            if (candidate != null && !candidate.isBlank()) {
                packages.add(candidate.trim());
            }
        }
        return packages;
    }
}
