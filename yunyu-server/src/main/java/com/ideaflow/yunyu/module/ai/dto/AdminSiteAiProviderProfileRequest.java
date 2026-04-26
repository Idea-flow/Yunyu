package com.ideaflow.yunyu.module.ai.dto;

import lombok.Data;

/**
 * 后台 AI 提供商配置项请求类。
 * 作用：承接后台保存 AI 配置时单个配置项的可编辑字段。
 */
@Data
public class AdminSiteAiProviderProfileRequest {

    private String profileKey;
    private String name;
    private Boolean enabled;
    private String upstreamBaseUrl;
    private String apiKey;
    private String model;
    private String upstreamProtocol;
    private Integer connectTimeoutMs;
    private Integer readTimeoutMs;
    private Integer writeTimeoutMs;
    private Integer maxTokens;
    private Double temperature;
}
