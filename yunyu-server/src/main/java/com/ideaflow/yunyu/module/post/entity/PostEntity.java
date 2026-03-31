package com.ideaflow.yunyu.module.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文章实体类。
 * 作用：映射 `post` 表，为后台文章管理和后续前台文章读取提供统一的文章主记录对象。
 */
@Data
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
}
