package com.ideaflow.yunyu.module.ai.vo;

import lombok.Data;

/**
 * 后台 AI 提供商配置项响应类。
 * 作用：向后台配置页返回单个 AI 配置项的可展示字段。
 */
@Data
public class AdminSiteAiProviderProfileResponse {

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
