package com.ideaflow.yunyu.nativeimage.support.core.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

/**
 * Yunyu Native 支撑配置对象类。
 * 作用：统一收口 AOT 阶段需要使用的主包扫描与扩展包扫描配置。
 */
public class YunyuNativeSupportSettings {

    /**
     * 需要参与扫描的包集合。
     */
    private final Set<String> scanPackages;

    /**
     * 使用扫描包集合创建配置对象。
     *
     * @param scanPackages 需要参与扫描的包集合
     */
    public YunyuNativeSupportSettings(Set<String> scanPackages) {
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
        return new YunyuNativeSupportSettings(packages);
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
}
