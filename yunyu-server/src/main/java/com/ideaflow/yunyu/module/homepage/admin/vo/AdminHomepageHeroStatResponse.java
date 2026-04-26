package com.ideaflow.yunyu.module.homepage.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台首页首屏统计项响应类。
 * 作用：向后台页面返回首页首屏统计项的标签和值配置。
 */
@Data
public class AdminHomepageHeroStatResponse {

    @Schema(description = "统计项标签。", example = "文章")
    private String label;
    @Schema(description = "统计项值。", example = "128+")
    private String value;
}
