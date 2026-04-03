package com.ideaflow.yunyu.module.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台首页首屏统计项请求类。
 * 作用：统一承接首页首屏统计项的标签和值配置。
 */
@Data
public class AdminHomepageHeroStatRequest {

    @NotBlank(message = "统计项标题不能为空")
    @Size(max = 32, message = "统计项标题长度不能超过32个字符")
    private String label;

    @NotBlank(message = "统计项数值不能为空")
    @Size(max = 32, message = "统计项数值长度不能超过32个字符")
    private String value;
}
