package com.ideaflow.yunyu.module.post.vo;

import java.time.LocalDateTime;

/**
 * 后台文章条目响应类。
 * 作用：向后台文章管理页返回文章展示和编辑所需字段，避免直接暴露实体对象。
 */
public class AdminPostItemResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverUrl;
    private String topic;
    private String status;
    private Boolean coverReady;
    private Boolean summaryReady;
    private Integer readingMinutes;
    private Integer wordCount;
    private String contentMarkdown;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    /** 获取文章ID。 */
    public Long getId() { return id; }
    /** 设置文章ID。 */
    public void setId(Long id) { this.id = id; }
    /** 获取标题。 */
    public String getTitle() { return title; }
    /** 设置标题。 */
    public void setTitle(String title) { this.title = title; }
    /** 获取 slug。 */
    public String getSlug() { return slug; }
    /** 设置 slug。 */
    public void setSlug(String slug) { this.slug = slug; }
    /** 获取摘要。 */
    public String getSummary() { return summary; }
    /** 设置摘要。 */
    public void setSummary(String summary) { this.summary = summary; }
    /** 获取封面。 */
    public String getCoverUrl() { return coverUrl; }
    /** 设置封面。 */
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    /** 获取专题文案。 */
    public String getTopic() { return topic; }
    /** 设置专题文案。 */
    public void setTopic(String topic) { this.topic = topic; }
    /** 获取状态。 */
    public String getStatus() { return status; }
    /** 设置状态。 */
    public void setStatus(String status) { this.status = status; }
    /** 获取封面是否准备完成。 */
    public Boolean getCoverReady() { return coverReady; }
    /** 设置封面是否准备完成。 */
    public void setCoverReady(Boolean coverReady) { this.coverReady = coverReady; }
    /** 获取摘要是否准备完成。 */
    public Boolean getSummaryReady() { return summaryReady; }
    /** 设置摘要是否准备完成。 */
    public void setSummaryReady(Boolean summaryReady) { this.summaryReady = summaryReady; }
    /** 获取阅读时长。 */
    public Integer getReadingMinutes() { return readingMinutes; }
    /** 设置阅读时长。 */
    public void setReadingMinutes(Integer readingMinutes) { this.readingMinutes = readingMinutes; }
    /** 获取字数。 */
    public Integer getWordCount() { return wordCount; }
    /** 设置字数。 */
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
    /** 获取正文。 */
    public String getContentMarkdown() { return contentMarkdown; }
    /** 设置正文。 */
    public void setContentMarkdown(String contentMarkdown) { this.contentMarkdown = contentMarkdown; }
    /** 获取更新时间。 */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    /** 设置更新时间。 */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    /** 获取发布时间。 */
    public LocalDateTime getPublishedAt() { return publishedAt; }
    /** 设置发布时间。 */
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
