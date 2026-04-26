package com.ideaflow.yunyu.module.site.publicapi.service;

import com.ideaflow.yunyu.YunyuServerApplication;
import com.ideaflow.yunyu.module.site.publicapi.vo.SiteVersionResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

/**
 * 公开版本信息服务。
 * 作用：统一汇总应用名称、构建产物、版本号和构建时间，供部署后的公开查询接口使用。
 */
@Service
public class SiteVersionService {

    private static final String UNKNOWN_VERSION = "unknown";
    private static final String DEV_BUILD_TIME = "dev";

    private final BuildProperties buildProperties;
    private final String applicationName;
    private final String artifactName;
    private final String environmentVersion;

    /**
     * 创建公开版本信息服务。
     *
     * @param buildPropertiesProvider 构建信息提供器
     * @param applicationName 应用名称
     * @param artifactName 构建产物标识
     * @param environmentVersion 环境中声明的版本号
     */
    public SiteVersionService(ObjectProvider<BuildProperties> buildPropertiesProvider,
                              @Value("${spring.application.name:yunyu-server}") String applicationName,
                              @Value("${spring.application.name:yunyu-server}") String artifactName,
                              @Value("${APP_VERSION:}") String environmentVersion) {
        this.buildProperties = buildPropertiesProvider.getIfAvailable();
        this.applicationName = applicationName;
        this.artifactName = artifactName;
        this.environmentVersion = environmentVersion;
    }

    /**
     * 获取当前服务的公开版本信息。
     *
     * @return 版本信息响应
     */
    public SiteVersionResponse getVersion() {
        if (buildProperties != null) {
            return new SiteVersionResponse(
                    buildProperties.getName(),
                    buildProperties.getArtifact(),
                    buildProperties.getVersion(),
                    buildProperties.getTime() == null ? DEV_BUILD_TIME : buildProperties.getTime().toString()
            );
        }

        return new SiteVersionResponse(
                applicationName,
                artifactName,
                resolveFallbackVersion(),
                resolveFallbackBuildTime()
        );
    }

    /**
     * 解析构建信息缺失时的回退版本号。
     *
     * @return 回退版本号
     */
    private String resolveFallbackVersion() {
        if (environmentVersion != null && !environmentVersion.isBlank()) {
            return environmentVersion;
        }

        Package applicationPackage = YunyuServerApplication.class.getPackage();
        if (applicationPackage != null && applicationPackage.getImplementationVersion() != null) {
            return applicationPackage.getImplementationVersion();
        }

        return UNKNOWN_VERSION;
    }

    /**
     * 解析构建信息缺失时的回退构建时间。
     *
     * @return 回退构建时间
     */
    private String resolveFallbackBuildTime() {
        return buildProperties != null && buildProperties.getTime() != null
                ? buildProperties.getTime().toString()
                : DEV_BUILD_TIME;
    }
}
