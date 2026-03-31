package com.ideaflow.yunyu.module.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 后台创建文章请求类。
 * 作用：承接后台创建文章时提交的主表与正文表字段，并在进入业务层前完成参数校验。
 */
public class AdminPostCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "Slug不能为空")
    @Size(max = 220, message = "Slug长度不能超过220个字符")
    private String slug;

    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String summary;

    @Size(max = 255, message = "封面地址长度不能超过255个字符")
    private String coverUrl;

    @Pattern(regexp = "DRAFT|PUBLISHED|OFFLINE", message = "状态不合法")
    private String status;

    private String contentMarkdown;

    /** 获取标题。 */
    public String getTitle() { return title; }
    /** 设置标题。 */
    public void setTitle(String title) { this.title = title; }
    /** 获取 Slug。 */
    public String getSlug() { return slug; }
    /** 设置 Slug。 */
    public void setSlug(String slug) { this.slug = slug; }
    /** 获取摘要。 */
    public String getSummary() { return summary; }
    /** 设置摘要。 */
    public void setSummary(String summary) { this.summary = summary; }
    /** 获取封面地址。 */
    public String getCoverUrl() { return coverUrl; }
    /** 设置封面地址。 */
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    /** 获取状态。 */
    public String getStatus() { return status; }
    /** 设置状态。 */
    public void setStatus(String status) { this.status = status; }
    /** 获取正文。 */
    public String getContentMarkdown() { return contentMarkdown; }
    /** 设置正文。 */
    public void setContentMarkdown(String contentMarkdown) { this.contentMarkdown = contentMarkdown; }
}
