package com.ideaflow.yunyu.module.site.vo;

import lombok.Data;

/**
 * 前台站点基础信息响应类。
 * 作用：统一向前台返回站点名称、副标题、SEO 和主题相关的基础配置。
 */
@Data
public class SiteBaseInfoResponse {

    private String siteName;
    private String siteSubTitle;
    private String footerText;
    private String logoUrl;
    private String faviconUrl;
    private String defaultTitle;
    private String defaultDescription;
    private String defaultShareImage;
    private String primaryColor;
    private String secondaryColor;
    private String homeStyle;
}
