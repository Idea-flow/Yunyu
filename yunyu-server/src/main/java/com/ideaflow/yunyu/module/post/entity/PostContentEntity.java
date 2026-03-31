package com.ideaflow.yunyu.module.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 文章内容实体类。
 * 作用：映射 `post_content` 表，用于存放文章正文和阅读时长等扩展内容信息。
 */
@TableName("post_content")
public class PostContentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String contentMarkdown;
    private String contentHtml;
    private String contentPlainText;
    private String contentTocJson;
    private String videoUrl;
    private Integer readingTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    /** 获取内容ID。 */
    public Long getId() {
        return id;
    }

    /** 设置内容ID。 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 获取文章ID。 */
    public Long getPostId() {
        return postId;
    }

    /** 设置文章ID。 */
    public void setPostId(Long postId) {
        this.postId = postId;
    }

    /** 获取 Markdown 内容。 */
    public String getContentMarkdown() {
        return contentMarkdown;
    }

    /** 设置 Markdown 内容。 */
    public void setContentMarkdown(String contentMarkdown) {
        this.contentMarkdown = contentMarkdown;
    }

    /** 获取 HTML 内容。 */
    public String getContentHtml() {
        return contentHtml;
    }

    /** 设置 HTML 内容。 */
    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    /** 获取纯文本内容。 */
    public String getContentPlainText() {
        return contentPlainText;
    }

    /** 设置纯文本内容。 */
    public void setContentPlainText(String contentPlainText) {
        this.contentPlainText = contentPlainText;
    }

    /** 获取目录 JSON。 */
    public String getContentTocJson() {
        return contentTocJson;
    }

    /** 设置目录 JSON。 */
    public void setContentTocJson(String contentTocJson) {
        this.contentTocJson = contentTocJson;
    }

    /** 获取视频地址。 */
    public String getVideoUrl() {
        return videoUrl;
    }

    /** 设置视频地址。 */
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    /** 获取阅读时长。 */
    public Integer getReadingTime() {
        return readingTime;
    }

    /** 设置阅读时长。 */
    public void setReadingTime(Integer readingTime) {
        this.readingTime = readingTime;
    }

    /** 获取创建时间。 */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    /** 设置创建时间。 */
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /** 获取更新时间。 */
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    /** 设置更新时间。 */
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
