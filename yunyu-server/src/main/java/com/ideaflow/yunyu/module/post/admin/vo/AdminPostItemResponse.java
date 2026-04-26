package com.ideaflow.yunyu.module.post.admin.vo;

import com.ideaflow.yunyu.module.contentaccess.model.ContentAccessConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 后台文章条目响应类。
 * 作用：向后台文章管理页返回文章展示和编辑所需字段，避免直接暴露实体对象。
 */
@Data
public class AdminPostItemResponse {

    @Schema(description = "文章 ID。", example = "18")
    private Long id;
    @Schema(description = "文章标题。", example = "Spring Boot 与 Nuxt 联调记录")
    private String title;
    @Schema(description = "文章 slug。", example = "spring-boot-nuxt-integration-notes")
    private String slug;
    @Schema(description = "文章摘要。", example = "记录 Spring Boot 与 Nuxt 联调过程中的关键实践。")
    private String summary;
    @Schema(description = "封面图地址。", example = "https://cdn.example.com/covers/spring-nuxt.png")
    private String coverUrl;
    @Schema(description = "视频地址。", example = "https://cdn.example.com/videos/demo.mp4")
    private String videoUrl;
    @Schema(description = "分类 ID。", example = "12")
    private Long categoryId;
    @Schema(description = "分类名称。", example = "后端")
    private String categoryName;
    @Schema(description = "标签 ID 列表。", example = "[3,8]")
    private List<Long> tagIds;
    @Schema(description = "标签名称列表。", example = "[\"Spring Boot\",\"Nuxt\"]")
    private List<String> tagNames;
    @Schema(description = "专题 ID 列表。", example = "[5]")
    private List<Long> topicIds;
    @Schema(description = "专题名称列表。", example = "[\"全栈实践\"]")
    private List<String> topicNames;
    @Schema(description = "兼容旧前端的专题名称汇总字段。", example = "全栈实践")
    private String topic;
    @Schema(description = "文章状态。DRAFT=草稿，PUBLISHED=发布，OFFLINE=下线。", example = "PUBLISHED", allowableValues = {"DRAFT", "PUBLISHED", "OFFLINE"})
    private String status;
    @Schema(description = "是否置顶。", example = "false")
    private Boolean isTop;
    @Schema(description = "是否推荐。", example = "true")
    private Boolean isRecommend;
    @Schema(description = "是否允许评论。", example = "true")
    private Boolean allowComment;
    @Schema(description = "SEO 标题。", example = "Spring Boot 与 Nuxt 联调完整实践")
    private String seoTitle;
    @Schema(description = "SEO 描述。", example = "从接口设计到部署配置，完整记录 Spring Boot 与 Nuxt 的联调过程。")
    private String seoDescription;
    @Schema(description = "封面资源是否就绪。", example = "true")
    private Boolean coverReady;
    @Schema(description = "视频资源是否就绪。", example = "false")
    private Boolean videoReady;
    @Schema(description = "摘要是否已生成。", example = "true")
    private Boolean summaryReady;
    @Schema(description = "预计阅读时长，单位分钟。", example = "8")
    private Integer readingMinutes;
    @Schema(description = "正文字数。", example = "3560")
    private Integer wordCount;
    @Schema(description = "正文 Markdown。", example = "# 一、背景\\n\\n本文记录联调过程。")
    private String contentMarkdown;
    @Schema(description = "内容权限配置。")
    private ContentAccessConfig contentAccessConfig;
    @Schema(description = "尾部隐藏内容 Markdown。", example = "## 扩展阅读\\n\\n这里是隐藏内容。")
    private String tailHiddenContentMarkdown;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedAt;
    @Schema(description = "发布时间。未发布时可为空。", example = "2026-04-26T20:00:00")
    private LocalDateTime publishedAt;
}
