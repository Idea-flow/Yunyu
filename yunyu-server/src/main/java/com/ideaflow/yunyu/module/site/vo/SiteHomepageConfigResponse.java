package com.ideaflow.yunyu.module.site.vo;

import java.util.List;
import lombok.Data;

/**
 * 前台首页配置响应类。
 * 作用：向前台首页返回品牌首屏和首页模块显隐配置，支持无封面首屏渲染。
 */
@Data
public class SiteHomepageConfigResponse {

    private Boolean heroEnabled;
    private String heroLayout;
    private String heroBackgroundMode;
    private String heroEyebrow;
    private String heroTitle;
    private String heroSubtitle;
    private String heroPrimaryButtonText;
    private String heroPrimaryButtonLink;
    private String heroSecondaryButtonText;
    private String heroSecondaryButtonLink;
    private List<String> heroKeywords;
    private Boolean showHeroKeywords;
    private Boolean showHeroStats;
    private List<SiteHomepageHeroStatResponse> heroStats;
    private Boolean showFeaturedSection;
    private String featuredSectionTitle;
    private Boolean showLatestSection;
    private String latestSectionTitle;
    private Boolean showCategorySection;
    private String categorySectionTitle;
    private Boolean showTopicSection;
    private String topicSectionTitle;
}
