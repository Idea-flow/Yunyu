package com.ideaflow.yunyu.module.ai.vo;

import java.util.List;
import lombok.Data;

/**
 * 后台 AI 提供商配置响应类。
 * 作用：向后台返回多配置集合与当前启用配置键，支持配置页回显和状态展示。
 */
@Data
public class AdminSiteAiProviderConfigResponse {

    private String activeProfileKey;
    private List<AdminSiteAiProviderProfileResponse> profiles;
}
