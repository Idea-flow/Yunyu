package com.ideaflow.yunyu.module.ai.model;

import lombok.Data;

/**
 * AI 提供商配置项模型。
 * 作用：描述单个 AI 上游配置的可编辑字段，供配置中心、网关和协议服务统一使用。
 */
@Data
public class AiProviderProfile {

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
