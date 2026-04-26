package com.ideaflow.yunyu.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台 AI 提供商配置项响应类。
 * 作用：向后台配置页返回单个 AI 配置项的可展示字段。
 */
@Data
public class AdminSiteAiProviderProfileResponse {

    @Schema(description = "AI 配置键。", example = "openai-main")
    private String profileKey;
    @Schema(description = "AI 配置名称。", example = "OpenAI 主配置")
    private String name;
    @Schema(description = "该配置是否启用。", example = "true")
    private Boolean enabled;
    @Schema(description = "上游 AI 服务基础地址。", example = "https://api.openai.com")
    private String upstreamBaseUrl;
    @Schema(description = "上游 AI 服务 API Key。", example = "sk-xxxxxxxxxxxxxxxx")
    private String apiKey;
    @Schema(description = "默认模型名称。", example = "gpt-4.1-mini")
    private String model;
    @Schema(description = "上游协议类型。", example = "COMPLETIONS")
    private String upstreamProtocol;
    @Schema(description = "连接超时时间，单位毫秒。", example = "3000")
    private Integer connectTimeoutMs;
    @Schema(description = "读取超时时间，单位毫秒。", example = "15000")
    private Integer readTimeoutMs;
    @Schema(description = "写入超时时间，单位毫秒。", example = "15000")
    private Integer writeTimeoutMs;
    @Schema(description = "默认最大输出 token 数。", example = "800")
    private Integer maxTokens;
    @Schema(description = "默认采样温度。", example = "0.4")
    private Double temperature;
}
