package com.ideaflow.yunyu.module.site.vo;

import lombok.Data;

/**
 * 前台首页首屏统计项响应类。
 * 作用：向前台首页返回轻量统计展示项，供品牌首屏下方展示。
 */
@Data
public class SiteHomepageHeroStatResponse {

    private String label;
    private String value;
}
