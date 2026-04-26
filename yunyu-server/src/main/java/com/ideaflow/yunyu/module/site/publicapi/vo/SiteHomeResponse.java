package com.ideaflow.yunyu.module.site.publicapi.vo;

import com.ideaflow.yunyu.module.category.site.vo.SiteCategoryItemResponse;
import com.ideaflow.yunyu.module.homepage.site.vo.SiteHeroVisualResponse;
import com.ideaflow.yunyu.module.homepage.site.vo.SiteHomepageConfigResponse;
import com.ideaflow.yunyu.module.post.site.vo.SitePostSummaryResponse;
import com.ideaflow.yunyu.module.topic.site.vo.SiteTopicItemResponse;
import java.util.List;
import lombok.Data;

/**
 * 前台首页聚合响应类。
 * 作用：统一返回首页所需的站点信息、推荐文章、最新文章、分类和专题数据。
 */
@Data
public class SiteHomeResponse {

    private SiteBaseInfoResponse siteInfo;
    private SiteHomepageConfigResponse homepageConfig;
    private SiteHeroVisualResponse heroVisual;
    private List<SitePostSummaryResponse> recommendedPosts;
    private List<SitePostSummaryResponse> latestPosts;
    private List<SiteCategoryItemResponse> categories;
    private List<SiteTopicItemResponse> topics;
}
