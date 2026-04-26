package com.ideaflow.yunyu.module.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 后台 AI 提供商配置更新请求类。
 * 作用：承接后台配置页提交的多配置集合与当前启用配置键。
 */
@Data
public class AdminSiteAiProviderConfigUpdateRequest {

    @Schema(description = "当前启用的 AI 配置键。可不传；不传时后端会自动取 `enabled=true` 的那一项。若传入，必须与唯一启用的配置项一致。", example = "openai-main")
    private String activeProfileKey;

    @Schema(description = "AI 提供商配置列表。保存时至少提供一个配置项，且必须且只能有一个 `enabled=true`。")
    private List<AdminSiteAiProviderProfileRequest> profiles;
}
