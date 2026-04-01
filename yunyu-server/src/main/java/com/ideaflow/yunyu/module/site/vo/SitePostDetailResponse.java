package com.ideaflow.yunyu.module.site.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 前台文章详情响应类。
 * 作用：统一向前台文章详情页返回正文、目录、SEO 与相关推荐展示所需字段。
 */
@Data
public class SitePostDetailResponse {

    private Long id;
    private String slug;
    private String title;
    private String summary;
    private String coverUrl;
    private String categoryName;
    private String categorySlug;
    private List<String> tagNames;
    private List<String> topicNames;
    private String authorName;
    private String authorAvatarUrl;
    private LocalDateTime publishedAt;
    private Integer readingMinutes;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private String seoTitle;
    private String seoDescription;
    private String contentMarkdown;
    private String contentHtml;
    private String contentTocJson;
    private Boolean allowComment;
    private List<SitePostSummaryResponse> relatedPosts;
}
