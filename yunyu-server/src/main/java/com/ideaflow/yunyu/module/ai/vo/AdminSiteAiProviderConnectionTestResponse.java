package com.ideaflow.yunyu.module.ai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 后台 AI 提供商连接测试响应类。
 * 作用：描述后台配置测试上游连接时的成功状态与提示信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSiteAiProviderConnectionTestResponse {

    private Boolean success;
    private String message;
}
