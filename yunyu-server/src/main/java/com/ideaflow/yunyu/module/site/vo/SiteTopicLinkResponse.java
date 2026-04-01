package com.ideaflow.yunyu.module.site.vo;

import lombok.Data;

/**
 * 前台专题链接响应类。
 * 作用：统一向文章摘要和文章详情返回可展示、可跳转的专题信息。
 */
@Data
public class SiteTopicLinkResponse {

    private String name;
    private String slug;
}
