package com.ideaflow.yunyu.module.site.publicapi.service;

import tools.jackson.databind.JsonNode;
import com.ideaflow.yunyu.module.category.site.service.CategorySiteService;
import com.ideaflow.yunyu.module.homepage.service.HomepageConfigService;
import com.ideaflow.yunyu.module.homepage.site.vo.SiteHeroVisualResponse;
import com.ideaflow.yunyu.module.homepage.site.vo.SiteHomepageConfigResponse;
import com.ideaflow.yunyu.module.post.entity.PostContentEntity;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostContentMapper;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.post.site.service.PostSiteService;
import com.ideaflow.yunyu.module.site.publicapi.vo.SiteBaseInfoResponse;
import com.ideaflow.yunyu.module.site.publicapi.vo.SiteHomeResponse;
import com.ideaflow.yunyu.module.siteconfig.service.SiteConfigService;
import com.ideaflow.yunyu.module.topic.site.service.TopicSiteService;
import org.springframework.stereotype.Service;

/**
 * 站点公开聚合服务类。
 * 作用：承接首页、站点基础配置等站点级公开接口，并编排文章、分类、专题等子域能力。
 */
@Service
public class SitePublicService {

    private final SiteConfigService siteConfigService;
    private final HomepageConfigService homepageConfigService;
    private final PostMapper postMapper;
    private final PostContentMapper postContentMapper;
    private final PostSiteService postSiteService;
    private final CategorySiteService categorySiteService;
    private final TopicSiteService topicSiteService;

    /**
     * 创建站点公开聚合服务。
     *
     * @param siteConfigService 站点配置统一服务
     * @param homepageConfigService 首页配置服务
     * @param postMapper 文章 Mapper
     * @param postContentMapper 文章内容 Mapper
     * @param postSiteService 前台文章服务
     * @param categorySiteService 前台分类服务
     * @param topicSiteService 前台专题服务
     */
    public SitePublicService(SiteConfigService siteConfigService,
                             HomepageConfigService homepageConfigService,
                             PostMapper postMapper,
                             PostContentMapper postContentMapper,
                             PostSiteService postSiteService,
                             CategorySiteService categorySiteService,
                             TopicSiteService topicSiteService) {
        this.siteConfigService = siteConfigService;
        this.homepageConfigService = homepageConfigService;
        this.postMapper = postMapper;
        this.postContentMapper = postContentMapper;
        this.postSiteService = postSiteService;
        this.categorySiteService = categorySiteService;
        this.topicSiteService = topicSiteService;
    }

    /**
     * 获取前台站点基础配置。
     *
     * @return 站点基础配置
     */
    public SiteBaseInfoResponse getSiteBaseInfo() {
        return buildSiteBaseInfo();
    }

    /**
     * 获取首页聚合数据。
     *
     * @return 首页聚合结果
     */
    public SiteHomeResponse getHome() {
        SiteHomeResponse response = new SiteHomeResponse();
        SiteHomepageConfigResponse homepageConfig = homepageConfigService.getSiteHomepageConfig();
        response.setSiteInfo(getSiteBaseInfo());
        response.setHomepageConfig(homepageConfig);
        response.setHeroVisual(buildHeroVisual(homepageConfig));
        response.setRecommendedPosts(postSiteService.listRecommendedPosts());
        response.setLatestPosts(postSiteService.listLatestPosts());
        response.setCategories(categorySiteService.listCategories());
        response.setTopics(topicSiteService.listTopics());
        return response;
    }

    /**
     * 构建站点基础信息。
     *
     * @return 站点基础信息
     */
    private SiteBaseInfoResponse buildSiteBaseInfo() {
        SiteBaseInfoResponse response = new SiteBaseInfoResponse();
        JsonNode baseNode = siteConfigService.getConfigJsonNodeByKey("site.base");
        JsonNode seoNode = siteConfigService.getConfigJsonNodeByKey("site.seo");
        JsonNode themeNode = siteConfigService.getConfigJsonNodeByKey("site.theme");

        response.setSiteName(readJsonText(baseNode, "siteName", "云屿"));
        response.setSiteSubTitle(readJsonText(baseNode, "siteSubTitle", "在二次元场景与情绪里漫游的内容站"));
        response.setFooterText(readJsonText(baseNode, "footerText", "云屿 Yunyu"));
        response.setLogoUrl(readJsonText(baseNode, "logoUrl", ""));
        response.setFaviconUrl(readJsonText(baseNode, "faviconUrl", ""));
        response.setDefaultTitle(readJsonText(seoNode, "defaultTitle", response.getSiteName()));
        response.setDefaultDescription(readJsonText(seoNode, "defaultDescription", response.getSiteSubTitle()));
        response.setPrimaryColor(readJsonText(themeNode, "primaryColor", "#38bdf8"));
        response.setSecondaryColor(readJsonText(themeNode, "secondaryColor", "#fb923c"));
        return response;
    }

    /**
     * 构建首页首屏视觉块响应。
     *
     * @param homepageConfig 首页配置
     * @return 首屏视觉块响应
     */
    private SiteHeroVisualResponse buildHeroVisual(SiteHomepageConfigResponse homepageConfig) {
        if (homepageConfig == null || homepageConfig.getHeroVisualPostId() == null) {
            return null;
        }

        PostEntity postEntity = findPublishedPostById(homepageConfig.getHeroVisualPostId());
        if (postEntity == null) {
            return null;
        }

        PostContentEntity postContentEntity = findPostContent(postEntity.getId());
        String videoUrl = postContentEntity == null ? "" : defaultString(postContentEntity.getVideoUrl());
        String imageUrl = defaultString(postEntity.getCoverUrl());

        if (videoUrl.isEmpty() && imageUrl.isEmpty()) {
            return null;
        }

        SiteHeroVisualResponse response = new SiteHeroVisualResponse();
        response.setMediaType(videoUrl.isEmpty() ? "image" : "video");
        response.setVideoUrl(videoUrl);
        response.setImageUrl(imageUrl);
        response.setPostId(postEntity.getId());
        response.setPostSlug(defaultString(postEntity.getSlug()));
        response.setPostTitle(defaultString(postEntity.getTitle()));
        response.setClickable(Boolean.TRUE.equals(homepageConfig.getHeroVisualClickable()));
        return response;
    }

    /**
     * 通过 ID 查询已发布文章。
     *
     * @param postId 文章 ID
     * @return 文章实体
     */
    private PostEntity findPublishedPostById(Long postId) {
        if (postId == null) {
            return null;
        }
        return postMapper.selectById(postId);
    }

    /**
     * 查询文章正文。
     *
     * @param postId 文章 ID
     * @return 正文实体
     */
    private PostContentEntity findPostContent(Long postId) {
        return postContentMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostContentEntity>()
                .eq(PostContentEntity::getPostId, postId)
                .last("LIMIT 1"));
    }

    /**
     * 读取 JSON 文本字段。
     *
     * @param node JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private String readJsonText(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return defaultValue;
        }

        String value = node.get(fieldName).asText();
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * 返回默认字符串。
     *
     * @param value 原始值
     * @return 默认化后的字符串
     */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
