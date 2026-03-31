package com.ideaflow.yunyu.module.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 文章实体类。
 * 作用：映射 `post` 表，为后台文章管理和后续前台文章读取提供统一的文章主记录对象。
 */
@TableName("post")
public class PostEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverUrl;
    private Long userId;
    private Long categoryId;
    private String status;
    private Integer isTop;
    private Integer isRecommend;
    private Integer hasVideo;
    private Integer allowComment;
    private String seoTitle;
    private String seoDescription;
    private LocalDateTime publishedAt;
    private Integer sortOrder;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;

    /** 获取文章ID。 */
    public Long getId() {
        return id;
    }

    /** 设置文章ID。 */
    public void setId(Long id) {
        this.id = id;
    }

    /** 获取标题。 */
    public String getTitle() {
        return title;
    }

    /** 设置标题。 */
    public void setTitle(String title) {
        this.title = title;
    }

    /** 获取唯一标识。 */
    public String getSlug() {
        return slug;
    }

    /** 设置唯一标识。 */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /** 获取摘要。 */
    public String getSummary() {
        return summary;
    }

    /** 设置摘要。 */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /** 获取封面地址。 */
    public String getCoverUrl() {
        return coverUrl;
    }

    /** 设置封面地址。 */
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    /** 获取作者ID。 */
    public Long getUserId() {
        return userId;
    }

    /** 设置作者ID。 */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** 获取分类ID。 */
    public Long getCategoryId() {
        return categoryId;
    }

    /** 设置分类ID。 */
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    /** 获取状态。 */
    public String getStatus() {
        return status;
    }

    /** 设置状态。 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 获取置顶标记。 */
    public Integer getIsTop() {
        return isTop;
    }

    /** 设置置顶标记。 */
    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    /** 获取推荐标记。 */
    public Integer getIsRecommend() {
        return isRecommend;
    }

    /** 设置推荐标记。 */
    public void setIsRecommend(Integer isRecommend) {
        this.isRecommend = isRecommend;
    }

    /** 获取视频标记。 */
    public Integer getHasVideo() {
        return hasVideo;
    }

    /** 设置视频标记。 */
    public void setHasVideo(Integer hasVideo) {
        this.hasVideo = hasVideo;
    }

    /** 获取评论开关。 */
    public Integer getAllowComment() {
        return allowComment;
    }

    /** 设置评论开关。 */
    public void setAllowComment(Integer allowComment) {
        this.allowComment = allowComment;
    }

    /** 获取SEO标题。 */
    public String getSeoTitle() {
        return seoTitle;
    }

    /** 设置SEO标题。 */
    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
    }

    /** 获取SEO描述。 */
    public String getSeoDescription() {
        return seoDescription;
    }

    /** 设置SEO描述。 */
    public void setSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
    }

    /** 获取发布时间。 */
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    /** 设置发布时间。 */
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    /** 获取排序值。 */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /** 设置排序值。 */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /** 获取浏览量。 */
    public Long getViewCount() {
        return viewCount;
    }

    /** 设置浏览量。 */
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    /** 获取点赞数。 */
    public Long getLikeCount() {
        return likeCount;
    }

    /** 设置点赞数。 */
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    /** 获取评论数。 */
    public Long getCommentCount() {
        return commentCount;
    }

    /** 设置评论数。 */
    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
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

    /** 获取删除标记。 */
    public Integer getDeleted() {
        return deleted;
    }

    /** 设置删除标记。 */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
