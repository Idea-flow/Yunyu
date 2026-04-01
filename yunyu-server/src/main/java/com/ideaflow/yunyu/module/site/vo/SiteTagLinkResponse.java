package com.ideaflow.yunyu.module.site.vo;

import lombok.Data;

/**
 * 前台标签链接响应类。
 * 作用：统一向文章摘要和文章详情返回可展示、可跳转的标签信息。
 */
@Data
public class SiteTagLinkResponse {

    private String name;
    private String slug;
}
