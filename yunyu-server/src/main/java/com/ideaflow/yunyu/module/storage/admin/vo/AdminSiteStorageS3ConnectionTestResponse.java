package com.ideaflow.yunyu.module.storage.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台 S3 连接测试响应类。
 * 作用：向后台返回当前 S3 配置连接测试的结果状态和提示信息。
 */
@Data
@AllArgsConstructor
public class AdminSiteStorageS3ConnectionTestResponse {

    @Schema(description = "连接测试是否成功。", example = "true")
    private Boolean success;
    @Schema(description = "连接测试结果说明。", example = "连接成功，可访问目标 Bucket")
    private String message;
}
