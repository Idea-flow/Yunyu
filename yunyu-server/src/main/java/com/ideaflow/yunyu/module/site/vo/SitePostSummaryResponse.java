package com.ideaflow.yunyu.module.site.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 前台文章摘要响应类。
 * 作用：统一向首页、文章列表页、分类页和专题页返回文章卡片所需字段。
 */
@Data
public class SitePostSummaryResponse {

    private Long id;
    private String slug;
    private String title;
    private String summary;
    private String coverUrl;
    private String categoryName;
    private String categorySlug;
    private List<SiteTagLinkResponse> tagItems;
    private List<String> tagNames;
    private List<SiteTopicLinkResponse> topicItems;
    private List<String> topicNames;
    private String authorName;
    private String authorAvatarUrl;
    private LocalDateTime publishedAt;
    private Integer readingMinutes;
    private Long viewCount;
    private Long likeCount;
    private Boolean top;
    private Boolean recommend;
}
