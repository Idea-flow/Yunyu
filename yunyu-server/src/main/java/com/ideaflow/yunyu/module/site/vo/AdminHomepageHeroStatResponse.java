package com.ideaflow.yunyu.module.site.vo;

import lombok.Data;

/**
 * 后台首页首屏统计项响应类。
 * 作用：向后台页面返回首页首屏统计项的标签和值配置。
 */
@Data
public class AdminHomepageHeroStatResponse {

    private String label;
    private String value;
}
