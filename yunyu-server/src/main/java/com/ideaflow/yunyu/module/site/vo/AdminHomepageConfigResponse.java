package com.ideaflow.yunyu.module.site.vo;

import java.util.List;
import lombok.Data;

/**
 * 后台首页配置响应类。
 * 作用：向后台首页配置页返回首屏品牌信息和首页模块开关的统一结构。
 */
@Data
public class AdminHomepageConfigResponse {

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
    private List<AdminHomepageHeroStatResponse> heroStats;
    private Boolean showFeaturedSection;
    private String featuredSectionTitle;
    private Boolean showLatestSection;
    private String latestSectionTitle;
    private Boolean showCategorySection;
    private String categorySectionTitle;
    private Boolean showTopicSection;
    private String topicSectionTitle;
}
