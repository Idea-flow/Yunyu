package com.ideaflow.yunyu.module.homepage.site.vo;

import lombok.Data;

/**
 * 首页首屏视觉块响应类。
 * 作用：向前台首页返回右侧视觉区域所需的媒体类型、媒体地址和点击跳转信息。
 */
@Data
public class SiteHeroVisualResponse {

    private String mediaType;
    private String videoUrl;
    private String imageUrl;
    private Long postId;
    private String postSlug;
    private String postTitle;
    private Boolean clickable;
}
