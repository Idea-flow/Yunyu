package com.ideaflow.yunyu.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 后台 AI 提供商配置响应类。
 * 作用：向后台返回多配置集合与当前启用配置键，支持配置页回显和状态展示。
 */
@Data
public class AdminSiteAiProviderConfigResponse {

    @Schema(description = "当前启用的 AI 配置键。", example = "openai-main")
    private String activeProfileKey;
    @Schema(description = "AI 提供商配置列表。")
    private List<AdminSiteAiProviderProfileResponse> profiles;
}
