package com.ideaflow.yunyu.module.site.vo;

import lombok.Data;

/**
 * 前台标签条目响应类。
 * 作用：统一向标签列表页和标签详情页返回标签基础展示信息与文章数量。
 */
@Data
public class SiteTagItemResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long articleCount;
}
