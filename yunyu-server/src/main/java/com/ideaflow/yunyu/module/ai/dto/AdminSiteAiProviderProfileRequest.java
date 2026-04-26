package com.ideaflow.yunyu.module.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台 AI 提供商配置项请求类。
 * 作用：承接后台保存 AI 配置时单个配置项的可编辑字段。
 */
@Data
public class AdminSiteAiProviderProfileRequest {

    @Schema(description = "AI 配置键。建议使用稳定且可读的英文标识。", example = "openai-main")
    private String profileKey;

    @Schema(description = "AI 配置名称。用于后台展示与人工区分。", example = "OpenAI 主配置")
    private String name;

    @Schema(description = "该配置是否启用。保存整组配置时必须且只能有一个为 true。", example = "true")
    private Boolean enabled;

    @Schema(description = "上游 AI 服务基础地址。通常不包含具体接口路径。", example = "https://api.openai.com")
    private String upstreamBaseUrl;

    @Schema(description = "上游 AI 服务 API Key。保存或测试连接时必须提供完整明文值。", example = "sk-xxxxxxxxxxxxxxxx")
    private String apiKey;

    @Schema(description = "默认模型名称。文章元信息生成等场景未显式覆盖模型时，会优先使用这里的值。", example = "gpt-4.1-mini")
    private String model;

    @Schema(description = "上游协议类型。COMPLETIONS=走 `/v1/chat/completions`，RESPONSES=走 `/v1/responses`。文章元信息生成当前要求使用 COMPLETIONS。", example = "COMPLETIONS", allowableValues = {"COMPLETIONS", "RESPONSES"})
    private String upstreamProtocol;

    @Schema(description = "连接超时时间，单位毫秒。有效范围 100-120000；未传时默认 3000。", example = "3000")
    private Integer connectTimeoutMs;

    @Schema(description = "读取超时时间，单位毫秒。有效范围 100-120000；未传时默认 15000。", example = "15000")
    private Integer readTimeoutMs;

    @Schema(description = "写入超时时间，单位毫秒。有效范围 100-120000；未传时默认 15000。", example = "15000")
    private Integer writeTimeoutMs;

    @Schema(description = "默认最大输出 token 数。有效范围 1-128000；未传时默认 800。", example = "800")
    private Integer maxTokens;

    @Schema(description = "默认采样温度。有效范围 0-2；未传时默认 0.4。", example = "0.4")
    private Double temperature;
}
