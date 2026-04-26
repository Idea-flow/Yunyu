package com.ideaflow.yunyu.module.post.dto;

import com.ideaflow.yunyu.module.contentaccess.model.ContentAccessConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * 后台更新文章请求类。
 * 作用：承接后台编辑文章时提交的文章字段，用于更新文章主记录和正文内容。
 */
@Data
public class AdminPostUpdateRequest {

    @Schema(description = "文章标题。更新时必填。", example = "Spring Boot 与 Nuxt 联调记录")
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Schema(description = "文章唯一 slug。更新时必填，通常应在原有值基础上调整。", example = "spring-boot-nuxt-integration-notes")
    @NotBlank(message = "Slug不能为空")
    @Size(max = 220, message = "Slug长度不能超过220个字符")
    private String slug;

    @Schema(description = "文章摘要。可由 AI 根据正文自动生成或重写。", example = "记录 Spring Boot 后端与 Nuxt 前端联调过程中的接口联调、鉴权和部署注意事项。")
    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String summary;

    @Schema(description = "文章封面图片地址。没有封面时可留空。", example = "https://cdn.example.com/covers/spring-nuxt.png")
    @Size(max = 255, message = "封面地址长度不能超过255个字符")
    private String coverUrl;

    @Schema(description = "文章视频地址。仅在需要视频首屏或视频正文时填写。", example = "https://cdn.example.com/videos/demo.mp4")
    @Size(max = 500, message = "视频地址长度不能超过500个字符")
    private String videoUrl;

    @Schema(description = "所属分类 ID。可为空；如需修改应先查询可用分类列表。", example = "12")
    private Long categoryId;

    @Schema(description = "标签 ID 列表。更新前建议先读取当前文章详情，再合并目标标签。", example = "[3,8]")
    private List<Long> tagIds;

    @Schema(description = "专题 ID 列表。更新前建议先读取当前文章详情，再合并目标专题。", example = "[5]")
    private List<Long> topicIds;

    @Schema(
            description = "文章状态。DRAFT=草稿，PUBLISHED=发布，OFFLINE=下线。更新时若用户未明确要改状态，应保留原值。",
            example = "DRAFT",
            allowableValues = {"DRAFT", "PUBLISHED", "OFFLINE"}
    )
    @Pattern(regexp = "DRAFT|PUBLISHED|OFFLINE", message = "状态不合法")
    private String status;

    @Schema(description = "是否置顶。默认 false。", example = "false")
    private Boolean isTop;

    @Schema(description = "是否推荐。默认 false。", example = "false")
    private Boolean isRecommend;

    @Schema(description = "是否允许评论。默认 true。", example = "true")
    private Boolean allowComment;

    @Schema(description = "SEO 标题。可由 AI 根据标题和正文自动生成。", example = "Spring Boot 与 Nuxt 联调完整实践")
    @Size(max = 255, message = "SEO标题长度不能超过255个字符")
    private String seoTitle;

    @Schema(description = "SEO 描述。可由 AI 根据正文自动生成。", example = "从接口设计、JWT 鉴权到部署配置，完整记录 Spring Boot 与 Nuxt 的联调过程。")
    @Size(max = 500, message = "SEO描述长度不能超过500个字符")
    private String seoDescription;

    @Schema(description = "文章正文 Markdown。更新文章时必填。", example = "# 一、背景\\n\\n本文记录 Spring Boot 与 Nuxt 的联调过程。")
    private String contentMarkdown;

    @Schema(description = "文章正文 HTML。通常由编辑器或服务端渲染生成，不建议手工维护。")
    private String contentHtml;

    @Schema(description = "文章目录 JSON。通常由 Markdown 渲染结果自动生成，不建议手工维护。")
    private String contentTocJson;

    @Schema(description = "内容权限配置。默认不启用；若启用应先明确询问用户是否需要访问控制。")
    private ContentAccessConfig contentAccessConfig;

    @Schema(description = "尾部隐藏内容 Markdown。只有启用了尾部隐藏内容时才需要填写。", example = "## 扩展阅读\\n\\n这里是需要解锁后查看的内容。")
    private String tailHiddenContentMarkdown;

    @Schema(description = "尾部隐藏内容 HTML。通常由 Markdown 渲染自动生成，不建议手工维护。")
    private String tailHiddenContentHtml;
}
