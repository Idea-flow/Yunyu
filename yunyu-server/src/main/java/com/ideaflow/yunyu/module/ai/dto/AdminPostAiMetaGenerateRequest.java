package com.ideaflow.yunyu.module.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台文章 AI 元信息生成请求类。
 * 作用：承接文章标题与正文等业务语义字段，由后端统一组装提示词并生成 slug、摘要和 SEO 元信息。
 */
@Data
public class AdminPostAiMetaGenerateRequest {

    @Schema(description = "文章标题。标题和正文至少填写一项；若两者都提供，标题会优先影响 slug 与 SEO 标题生成。", example = "Spring Boot 与 Nuxt 联调记录")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Schema(description = "文章正文 Markdown。标题和正文至少填写一项；后端在构造提示词时默认只截取前 6000 个字符参与生成。", example = "# 一、背景\\n\\n本文记录 Spring Boot 与 Nuxt 的联调过程。")
    private String contentMarkdown;

    @Schema(description = "是否使用 SSE 流式返回 OpenAI Chat 分片。true=流式，false=一次性返回；未传时默认 false。", example = "false")
    private Boolean stream;

    @Schema(description = "可选的模型覆盖值。未传时优先使用当前启用的 AI 提供商配置中的默认模型。", example = "gpt-4.1-mini")
    @Size(max = 200, message = "模型名称长度不能超过200个字符")
    private String model;

    @Schema(description = "可选的采样温度。未传时使用当前启用 AI 配置中的默认温度；建议文章元信息生成使用较低温度。", example = "0.2")
    @DecimalMin(value = "0.0", message = "temperature 不能小于0")
    @DecimalMax(value = "2.0", message = "temperature 不能大于2")
    private Double temperature;

    @Schema(description = "可选的最大输出 token 数。未传时使用当前启用 AI 配置中的默认值；通常无需超过 800。", example = "800")
    @Min(value = 1, message = "maxTokens 不能小于1")
    @Max(value = 128000, message = "maxTokens 不能大于128000")
    private Integer maxTokens;
}
