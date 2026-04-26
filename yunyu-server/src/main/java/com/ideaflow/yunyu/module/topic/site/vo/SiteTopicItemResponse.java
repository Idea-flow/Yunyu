package com.ideaflow.yunyu.module.topic.site.vo;

import lombok.Data;

/**
 * 前台专题条目响应类。
 * 作用：统一向首页和专题页返回专题的基础展示信息与文章数量。
 */
@Data
public class SiteTopicItemResponse {

    private Long id;
    private String name;
    private String slug;
    private String summary;
    private String coverUrl;
    private Long articleCount;
}
