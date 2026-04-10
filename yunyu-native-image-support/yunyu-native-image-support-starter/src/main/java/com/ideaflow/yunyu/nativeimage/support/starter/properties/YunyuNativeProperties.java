package com.ideaflow.yunyu.nativeimage.support.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Yunyu Native Starter 配置属性类。
 * 作用：向业务工程暴露 Native 支撑的开关、主包扫描和扩展扫描包配置。
 */
@ConfigurationProperties(prefix = "yunyu.native")
public class YunyuNativeProperties {

    /**
     * 是否启用 Yunyu Native Starter。
     */
    private boolean enabled = true;

    /**
     * 是否默认扫描启动类主包。
     */
    private boolean scanMainPackage = true;

    /**
     * 需要额外参与扫描的包集合。
     */
    private List<String> scanPackages = new ArrayList<>();

    /**
     * 返回 Starter 是否启用。
     *
     * @return Starter 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置 Starter 是否启用。
     *
     * @param enabled Starter 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 返回是否默认扫描启动类主包。
     *
     * @return 是否默认扫描主包
     */
    public boolean isScanMainPackage() {
        return scanMainPackage;
    }

    /**
     * 设置是否默认扫描启动类主包。
     *
     * @param scanMainPackage 是否默认扫描主包
     */
    public void setScanMainPackage(boolean scanMainPackage) {
        this.scanMainPackage = scanMainPackage;
    }

    /**
     * 返回额外扫描包集合。
     *
     * @return 额外扫描包集合
     */
    public List<String> getScanPackages() {
        return scanPackages;
    }

    /**
     * 设置额外扫描包集合。
     *
     * @param scanPackages 额外扫描包集合
     */
    public void setScanPackages(List<String> scanPackages) {
        this.scanPackages = scanPackages;
    }
}
