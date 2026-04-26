package com.ideaflow.yunyu.module.site.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 后台 S3 配置响应类。
 * 作用：向后台站点设置页返回当前启用配置标识与多套 S3 配置项列表。
 */
@Data
public class AdminSiteStorageS3ConfigResponse {

    @Schema(description = "当前启用的 S3 配置键。", example = "r2-main")
    private String activeProfileKey;
    @Schema(description = "S3 配置列表。")
    private List<AdminSiteStorageS3ProfileResponse> profiles;
}
