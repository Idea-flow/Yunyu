package com.ideaflow.yunyu.module.homepage.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 后台首页配置响应类。
 * 作用：向后台首页配置页返回首屏品牌信息和首页模块开关的统一结构。
 */
@Data
public class AdminHomepageConfigResponse {

    @Schema(description = "是否启用首页首屏模块。", example = "true")
    private Boolean heroEnabled;
    @Schema(description = "首屏布局。", example = "brand")
    private String heroLayout;
    @Schema(description = "首屏背景模式。", example = "gradient-grid")
    private String heroBackgroundMode;
    @Schema(description = "首页眉题。", example = "Personal Knowledge Base")
    private String heroEyebrow;
    @Schema(description = "首页主标题。", example = "在技术与生活之间，持续写作与整理")
    private String heroTitle;
    @Schema(description = "首页副标题。", example = "这里记录后端、前端、工程实践，以及一些慢慢沉淀下来的想法。")
    private String heroSubtitle;
    @Schema(description = "主按钮文案。", example = "查看文章")
    private String heroPrimaryButtonText;
    @Schema(description = "主按钮跳转地址。", example = "/posts")
    private String heroPrimaryButtonLink;
    @Schema(description = "次按钮文案。", example = "进入专题")
    private String heroSecondaryButtonText;
    @Schema(description = "次按钮跳转地址。", example = "/topics")
    private String heroSecondaryButtonLink;
    @Schema(description = "首屏视觉文章 ID。", example = "18")
    private Long heroVisualPostId;
    @Schema(description = "首屏视觉区是否允许点击跳转文章。", example = "true")
    private Boolean heroVisualClickable;
    @Schema(description = "首页关键词列表。", example = "[\"Spring Boot\",\"Nuxt\",\"架构\"]")
    private List<String> heroKeywords;
    @Schema(description = "是否展示首页关键词。", example = "true")
    private Boolean showHeroKeywords;
    @Schema(description = "是否展示首页统计项。", example = "true")
    private Boolean showHeroStats;
    @Schema(description = "首页统计项列表。")
    private List<AdminHomepageHeroStatResponse> heroStats;
    @Schema(description = "是否展示推荐文章区。", example = "true")
    private Boolean showFeaturedSection;
    @Schema(description = "推荐文章区标题。", example = "精选推荐")
    private String featuredSectionTitle;
    @Schema(description = "是否展示最新文章区。", example = "true")
    private Boolean showLatestSection;
    @Schema(description = "最新文章区标题。", example = "最新文章")
    private String latestSectionTitle;
    @Schema(description = "是否展示分类区。", example = "true")
    private Boolean showCategorySection;
    @Schema(description = "分类区标题。", example = "分类浏览")
    private String categorySectionTitle;
    @Schema(description = "是否展示专题区。", example = "true")
    private Boolean showTopicSection;
    @Schema(description = "专题区标题。", example = "专题阅读")
    private String topicSectionTitle;
}
