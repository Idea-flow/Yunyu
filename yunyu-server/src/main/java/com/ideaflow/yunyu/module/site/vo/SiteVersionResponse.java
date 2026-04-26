package com.ideaflow.yunyu.module.site.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 公开版本信息响应对象。
 * 作用：向部署环境暴露当前服务的名称、构建产物和版本号，便于上线后快速核对实际运行版本。
 */
@Data
@AllArgsConstructor
public class SiteVersionResponse {

    private final String applicationName;
    private final String artifact;
    private final String version;
    private final String buildTime;
}
