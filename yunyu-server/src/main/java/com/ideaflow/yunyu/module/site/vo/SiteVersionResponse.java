package com.ideaflow.yunyu.module.site.vo;

/**
 * 公开版本信息响应对象。
 * 作用：向部署环境暴露当前服务的名称、构建产物和版本号，便于上线后快速核对实际运行版本。
 */
public class SiteVersionResponse {

    private final String applicationName;
    private final String artifact;
    private final String version;
    private final String buildTime;

    /**
     * 创建公开版本信息响应对象。
     *
     * @param applicationName 应用名称
     * @param artifact 构建产物标识
     * @param version 当前服务版本号
     * @param buildTime 构建时间
     */
    public SiteVersionResponse(String applicationName, String artifact, String version, String buildTime) {
        this.applicationName = applicationName;
        this.artifact = artifact;
        this.version = version;
        this.buildTime = buildTime;
    }

    /**
     * 获取应用名称。
     *
     * @return 应用名称
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * 获取构建产物标识。
     *
     * @return 构建产物标识
     */
    public String getArtifact() {
        return artifact;
    }

    /**
     * 获取当前服务版本号。
     *
     * @return 当前服务版本号
     */
    public String getVersion() {
        return version;
    }

    /**
     * 获取构建时间。
     *
     * @return 构建时间
     */
    public String getBuildTime() {
        return buildTime;
    }
}
