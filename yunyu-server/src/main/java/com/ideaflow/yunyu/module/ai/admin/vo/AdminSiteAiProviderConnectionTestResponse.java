package com.ideaflow.yunyu.module.ai.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "连接测试是否成功。", example = "true")
    private Boolean success;
    @Schema(description = "连接测试结果说明。", example = "连接测试成功")
    private String message;
}
