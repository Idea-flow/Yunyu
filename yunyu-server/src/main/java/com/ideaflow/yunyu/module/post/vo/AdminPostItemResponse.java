package com.ideaflow.yunyu.module.post.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台文章条目响应类。
 * 作用：向后台文章管理页返回文章展示和编辑所需字段，避免直接暴露实体对象。
 */
@Data
public class AdminPostItemResponse {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverUrl;
    private Long categoryId;
    private String topic;
    private String status;
    private String seoTitle;
    private String seoDescription;
    private Boolean coverReady;
    private Boolean summaryReady;
    private Integer readingMinutes;
    private Integer wordCount;
    private String contentMarkdown;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}
