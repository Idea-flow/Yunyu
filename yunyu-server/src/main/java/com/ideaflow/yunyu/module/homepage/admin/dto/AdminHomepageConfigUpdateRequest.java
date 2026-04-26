package com.ideaflow.yunyu.module.homepage.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * 后台首页配置更新请求类。
 * 作用：承接首页品牌首屏和首页模块开关相关的可编辑字段，并在进入业务层前完成基础校验。
 */
@Data
public class AdminHomepageConfigUpdateRequest {

    @Schema(description = "是否启用首页首屏模块。", example = "true")
    private Boolean heroEnabled;

    @Schema(description = "首屏布局。当前仅支持 brand。", example = "brand", allowableValues = {"brand"})
    @Pattern(regexp = "^(brand)$", message = "首屏布局仅支持 brand")
    private String heroLayout;

    @Schema(description = "首屏背景模式。", example = "gradient-grid", allowableValues = {"gradient-grid", "soft-glow", "minimal-lines", "keyword-cloud"})
    @Pattern(regexp = "^(gradient-grid|soft-glow|minimal-lines|keyword-cloud)$", message = "首屏背景模式不合法")
    private String heroBackgroundMode;

    @Schema(description = "首页眉题。可选。", example = "Personal Knowledge Base")
    @Size(max = 64, message = "首页眉题长度不能超过64个字符")
    private String heroEyebrow;

    @Schema(description = "首页主标题。", example = "在技术与生活之间，持续写作与整理")
    @NotBlank(message = "首页主标题不能为空")
    @Size(max = 120, message = "首页主标题长度不能超过120个字符")
    private String heroTitle;

    @Schema(description = "首页副标题。", example = "这里记录后端、前端、工程实践，以及一些慢慢沉淀下来的想法。")
    @NotBlank(message = "首页副标题不能为空")
    @Size(max = 255, message = "首页副标题长度不能超过255个字符")
    private String heroSubtitle;

    @Schema(description = "主按钮文案。", example = "查看文章")
    @NotBlank(message = "主按钮文案不能为空")
    @Size(max = 32, message = "主按钮文案长度不能超过32个字符")
    private String heroPrimaryButtonText;

    @Schema(description = "主按钮跳转地址。", example = "/posts")
    @NotBlank(message = "主按钮跳转地址不能为空")
    @Size(max = 255, message = "主按钮跳转地址长度不能超过255个字符")
    private String heroPrimaryButtonLink;

    @Schema(description = "次按钮文案。可选。", example = "进入专题")
    @Size(max = 32, message = "次按钮文案长度不能超过32个字符")
    private String heroSecondaryButtonText;

    @Schema(description = "次按钮跳转地址。可选。", example = "/topics")
    @Size(max = 255, message = "次按钮跳转地址长度不能超过255个字符")
    private String heroSecondaryButtonLink;

    @Schema(description = "首屏视觉文章 ID。可选；填写后用于从文章中取视觉内容。", example = "18")
    @Positive(message = "首屏视觉文章 ID 必须大于 0")
    private Long heroVisualPostId;

    @Schema(description = "首屏视觉区是否允许点击跳转文章。", example = "true")
    private Boolean heroVisualClickable;

    @Schema(description = "首页关键词列表，最多 6 个。", example = "[\"Spring Boot\", \"Nuxt\", \"架构\"]")
    @Size(max = 6, message = "首页关键词最多6个")
    private List<@Size(max = 20, message = "首页关键词长度不能超过20个字符") String> heroKeywords;

    @Schema(description = "是否展示首页关键词。", example = "true")
    private Boolean showHeroKeywords;

    @Schema(description = "是否展示首页统计项。", example = "true")
    private Boolean showHeroStats;

    @Schema(description = "首页统计项列表，最多 4 个。")
    @Valid
    @Size(max = 4, message = "首页统计项最多4个")
    private List<AdminHomepageHeroStatRequest> heroStats;

    @Schema(description = "是否展示推荐文章区。", example = "true")
    private Boolean showFeaturedSection;

    @Schema(description = "推荐文章区标题。", example = "精选推荐")
    @Size(max = 32, message = "推荐标题长度不能超过32个字符")
    private String featuredSectionTitle;

    @Schema(description = "是否展示最新文章区。", example = "true")
    private Boolean showLatestSection;

    @Schema(description = "最新文章区标题。", example = "最新文章")
    @Size(max = 32, message = "最新文章标题长度不能超过32个字符")
    private String latestSectionTitle;

    @Schema(description = "是否展示分类区。", example = "true")
    private Boolean showCategorySection;

    @Schema(description = "分类区标题。", example = "分类浏览")
    @Size(max = 32, message = "分类标题长度不能超过32个字符")
    private String categorySectionTitle;

    @Schema(description = "是否展示专题区。", example = "true")
    private Boolean showTopicSection;

    @Schema(description = "专题区标题。", example = "专题阅读")
    @Size(max = 32, message = "专题标题长度不能超过32个字符")
    private String topicSectionTitle;
}
