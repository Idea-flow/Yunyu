package com.ideaflow.yunyu.module.site.dto;

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

    private Boolean heroEnabled;

    @Pattern(regexp = "^(brand)$", message = "首屏布局仅支持 brand")
    private String heroLayout;

    @Pattern(regexp = "^(gradient-grid|soft-glow|minimal-lines|keyword-cloud)$", message = "首屏背景模式不合法")
    private String heroBackgroundMode;

    @Size(max = 64, message = "首页眉题长度不能超过64个字符")
    private String heroEyebrow;

    @NotBlank(message = "首页主标题不能为空")
    @Size(max = 120, message = "首页主标题长度不能超过120个字符")
    private String heroTitle;

    @NotBlank(message = "首页副标题不能为空")
    @Size(max = 255, message = "首页副标题长度不能超过255个字符")
    private String heroSubtitle;

    @NotBlank(message = "主按钮文案不能为空")
    @Size(max = 32, message = "主按钮文案长度不能超过32个字符")
    private String heroPrimaryButtonText;

    @NotBlank(message = "主按钮跳转地址不能为空")
    @Size(max = 255, message = "主按钮跳转地址长度不能超过255个字符")
    private String heroPrimaryButtonLink;

    @Size(max = 32, message = "次按钮文案长度不能超过32个字符")
    private String heroSecondaryButtonText;

    @Size(max = 255, message = "次按钮跳转地址长度不能超过255个字符")
    private String heroSecondaryButtonLink;

    @Positive(message = "首屏视觉文章 ID 必须大于 0")
    private Long heroVisualPostId;

    private Boolean heroVisualClickable;

    @Size(max = 6, message = "首页关键词最多6个")
    private List<@Size(max = 20, message = "首页关键词长度不能超过20个字符") String> heroKeywords;

    private Boolean showHeroKeywords;
    private Boolean showHeroStats;

    @Valid
    @Size(max = 4, message = "首页统计项最多4个")
    private List<AdminHomepageHeroStatRequest> heroStats;

    private Boolean showFeaturedSection;

    @Size(max = 32, message = "主打内容标题长度不能超过32个字符")
    private String featuredSectionTitle;

    private Boolean showLatestSection;

    @Size(max = 32, message = "最新文章标题长度不能超过32个字符")
    private String latestSectionTitle;

    private Boolean showCategorySection;

    @Size(max = 32, message = "分类标题长度不能超过32个字符")
    private String categorySectionTitle;

    private Boolean showTopicSection;

    @Size(max = 32, message = "专题标题长度不能超过32个字符")
    private String topicSectionTitle;
}
