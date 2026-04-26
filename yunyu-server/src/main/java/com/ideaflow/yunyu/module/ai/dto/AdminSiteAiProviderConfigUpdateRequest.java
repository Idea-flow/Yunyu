package com.ideaflow.yunyu.module.ai.dto;

import java.util.List;
import lombok.Data;

/**
 * 后台 AI 提供商配置更新请求类。
 * 作用：承接后台配置页提交的多配置集合与当前启用配置键。
 */
@Data
public class AdminSiteAiProviderConfigUpdateRequest {

    private String activeProfileKey;
    private List<AdminSiteAiProviderProfileRequest> profiles;
}
