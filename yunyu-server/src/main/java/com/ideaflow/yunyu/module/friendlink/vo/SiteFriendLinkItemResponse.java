package com.ideaflow.yunyu.module.friendlink.vo;

import lombok.Data;

/**
 * 前台友链条目响应类。
 * 作用：统一向前台友链展示页返回已通过审核的友链基础展示信息。
 */
@Data
public class SiteFriendLinkItemResponse {

    private Long id;
    private String siteName;
    private String siteUrl;
    private String logoUrl;
    private String description;
    private String themeColor;
}
