package com.ideaflow.yunyu.module.site.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台 S3 连接测试响应类。
 * 作用：向后台返回当前 S3 配置连接测试的结果状态和提示信息。
 */
@Data
@AllArgsConstructor
public class AdminSiteStorageS3ConnectionTestResponse {

    private Boolean success;
    private String message;
}
