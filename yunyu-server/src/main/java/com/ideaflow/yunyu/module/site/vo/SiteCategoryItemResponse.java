package com.ideaflow.yunyu.module.site.vo;

import lombok.Data;

/**
 * 前台分类条目响应类。
 * 作用：统一向首页和分类页返回分类的基础展示信息与文章数量。
 */
@Data
public class SiteCategoryItemResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String coverUrl;
    private Long articleCount;
}
