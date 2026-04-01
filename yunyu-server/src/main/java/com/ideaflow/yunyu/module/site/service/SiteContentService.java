package com.ideaflow.yunyu.module.site.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.category.entity.CategoryEntity;
import com.ideaflow.yunyu.module.category.mapper.CategoryMapper;
import com.ideaflow.yunyu.module.post.entity.PostContentEntity;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.entity.PostTagEntity;
import com.ideaflow.yunyu.module.post.entity.TopicPostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostContentMapper;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.post.mapper.PostTagMapper;
import com.ideaflow.yunyu.module.post.mapper.TopicPostMapper;
import com.ideaflow.yunyu.module.site.dto.SitePostQueryRequest;
import com.ideaflow.yunyu.module.site.entity.SiteConfigEntity;
import com.ideaflow.yunyu.module.site.mapper.SiteConfigMapper;
import com.ideaflow.yunyu.module.site.vo.SiteBaseInfoResponse;
import com.ideaflow.yunyu.module.site.vo.SiteCategoryDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteCategoryItemResponse;
import com.ideaflow.yunyu.module.site.vo.SiteHomeResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostListResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostSummaryResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTopicDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTopicItemResponse;
import com.ideaflow.yunyu.module.tag.entity.TagEntity;
import com.ideaflow.yunyu.module.tag.mapper.TagMapper;
import com.ideaflow.yunyu.module.topic.entity.TopicEntity;
import com.ideaflow.yunyu.module.topic.mapper.TopicMapper;
import com.ideaflow.yunyu.module.user.entity.UserEntity;
import com.ideaflow.yunyu.module.user.mapper.UserMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * 前台内容服务类。
 * 作用：聚合站点配置、文章、分类、专题和标签数据，为前台首页、列表页和详情页提供公开接口能力。
 */
@Service
public class SiteContentService {

    private static final int DEFAULT_HOME_POST_LIMIT = 6;
    private final PostMapper postMapper;
    private final PostContentMapper postContentMapper;
    private final CategoryMapper categoryMapper;
    private final TopicMapper topicMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final PostTagMapper postTagMapper;
    private final TopicPostMapper topicPostMapper;
    private final SiteConfigMapper siteConfigMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建前台内容服务。
     *
     * @param postMapper 文章 Mapper
     * @param postContentMapper 文章内容 Mapper
     * @param categoryMapper 分类 Mapper
     * @param topicMapper 专题 Mapper
     * @param tagMapper 标签 Mapper
     * @param userMapper 用户 Mapper
     * @param postTagMapper 文章标签关联 Mapper
     * @param topicPostMapper 专题文章关联 Mapper
     * @param siteConfigMapper 站点配置 Mapper
     * @param objectMapper Jackson 对象映射器
     */
    public SiteContentService(PostMapper postMapper,
                              PostContentMapper postContentMapper,
                              CategoryMapper categoryMapper,
                              TopicMapper topicMapper,
                              TagMapper tagMapper,
                              UserMapper userMapper,
                              PostTagMapper postTagMapper,
                              TopicPostMapper topicPostMapper,
                              SiteConfigMapper siteConfigMapper,
                              ObjectMapper objectMapper) {
        this.postMapper = postMapper;
        this.postContentMapper = postContentMapper;
        this.categoryMapper = categoryMapper;
        this.topicMapper = topicMapper;
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.postTagMapper = postTagMapper;
        this.topicPostMapper = topicPostMapper;
        this.siteConfigMapper = siteConfigMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取首页聚合数据。
     *
     * @return 首页聚合结果
     */
    public SiteHomeResponse getHome() {
        SiteHomeResponse response = new SiteHomeResponse();
        response.setSiteInfo(buildSiteBaseInfo());
        response.setFeaturedPosts(listRecommendedPosts());
        response.setLatestPosts(listLatestPosts());
        response.setCategories(listCategories());
        response.setTopics(listTopics());
        return response;
    }

    /**
     * 查询前台文章列表。
     *
     * @param request 查询请求
     * @return 文章分页结果
     */
    public SitePostListResponse listPosts(SitePostQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        Page<PostEntity> page = postMapper.selectPage(new Page<>(pageNo, pageSize), buildPublishedPostQuery(request));
        return toSitePostListResponse(page, pageNo, pageSize);
    }

    /**
     * 查询前台文章详情。
     *
     * @param slug 文章 slug
     * @return 文章详情
     */
    public SitePostDetailResponse getPostDetail(String slug) {
        PostEntity postEntity = findPublishedPostBySlug(slug);
        PostContentEntity postContentEntity = findPostContent(postEntity.getId());
        SitePostDetailResponse response = new SitePostDetailResponse();
        SitePostSummaryResponse summaryResponse = toSitePostSummaryResponse(postEntity);

        response.setId(summaryResponse.getId());
        response.setSlug(summaryResponse.getSlug());
        response.setTitle(summaryResponse.getTitle());
        response.setSummary(summaryResponse.getSummary());
        response.setCoverUrl(summaryResponse.getCoverUrl());
        response.setCategoryName(summaryResponse.getCategoryName());
        response.setCategorySlug(summaryResponse.getCategorySlug());
        response.setTagNames(summaryResponse.getTagNames());
        response.setTopicNames(summaryResponse.getTopicNames());
        response.setAuthorName(summaryResponse.getAuthorName());
        response.setAuthorAvatarUrl(summaryResponse.getAuthorAvatarUrl());
        response.setPublishedAt(summaryResponse.getPublishedAt());
        response.setReadingMinutes(summaryResponse.getReadingMinutes());
        response.setViewCount(summaryResponse.getViewCount());
        response.setLikeCount(summaryResponse.getLikeCount());
        response.setCommentCount(postEntity.getCommentCount());
        response.setSeoTitle(postEntity.getSeoTitle());
        response.setSeoDescription(postEntity.getSeoDescription());
        response.setContentMarkdown(postContentEntity == null ? "" : defaultString(postContentEntity.getContentMarkdown()));
        response.setContentHtml(postContentEntity == null ? "" : defaultString(postContentEntity.getContentHtml()));
        response.setContentTocJson(postContentEntity == null ? "[]" : defaultJsonArray(postContentEntity.getContentTocJson()));
        response.setAllowComment(postEntity.getAllowComment() != null && postEntity.getAllowComment() == 1);
        response.setRelatedPosts(listRelatedPosts(postEntity));
        return response;
    }

    /**
     * 查询前台分类列表。
     *
     * @return 分类列表
     */
    public List<SiteCategoryItemResponse> listCategories() {
        List<CategoryEntity> categoryEntities = categoryMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getDeleted, 0)
                .eq(CategoryEntity::getStatus, "ACTIVE")
                .orderByAsc(CategoryEntity::getSortOrder)
                .orderByAsc(CategoryEntity::getId));

        return categoryEntities.stream()
                .map(this::toSiteCategoryItemResponse)
                .toList();
    }

    /**
     * 查询前台分类详情。
     *
     * @param slug 分类 slug
     * @param request 查询请求
     * @return 分类详情
     */
    public SiteCategoryDetailResponse getCategoryDetail(String slug, SitePostQueryRequest request) {
        CategoryEntity categoryEntity = findCategoryBySlug(slug);
        SitePostQueryRequest actualRequest = copyRequest(request);
        actualRequest.setCategorySlug(categoryEntity.getSlug());

        SiteCategoryDetailResponse response = new SiteCategoryDetailResponse();
        response.setCategory(toSiteCategoryItemResponse(categoryEntity));
        response.setPosts(listPosts(actualRequest));
        return response;
    }

    /**
     * 查询前台专题列表。
     *
     * @return 专题列表
     */
    public List<SiteTopicItemResponse> listTopics() {
        List<TopicEntity> topicEntities = topicMapper.selectList(new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getDeleted, 0)
                .eq(TopicEntity::getStatus, "ACTIVE")
                .orderByAsc(TopicEntity::getSortOrder)
                .orderByAsc(TopicEntity::getId));

        return topicEntities.stream()
                .map(this::toSiteTopicItemResponse)
                .toList();
    }

    /**
     * 查询前台专题详情。
     *
     * @param slug 专题 slug
     * @param request 查询请求
     * @return 专题详情
     */
    public SiteTopicDetailResponse getTopicDetail(String slug, SitePostQueryRequest request) {
        TopicEntity topicEntity = findTopicBySlug(slug);
        SitePostQueryRequest actualRequest = copyRequest(request);
        actualRequest.setTopicSlug(topicEntity.getSlug());

        SiteTopicDetailResponse response = new SiteTopicDetailResponse();
        response.setTopic(toSiteTopicItemResponse(topicEntity));
        response.setPosts(listPosts(actualRequest));
        return response;
    }

    /**
     * 构建站点基础信息。
     *
     * @return 站点基础信息
     */
    private SiteBaseInfoResponse buildSiteBaseInfo() {
        SiteBaseInfoResponse response = new SiteBaseInfoResponse();
        SiteConfigEntity baseConfig = findSiteConfig("site.base");
        SiteConfigEntity seoConfig = findSiteConfig("site.seo");
        SiteConfigEntity themeConfig = findSiteConfig("site.theme");

        JsonNode baseNode = readJsonNode(baseConfig == null ? null : baseConfig.getConfigJson());
        JsonNode seoNode = readJsonNode(seoConfig == null ? null : seoConfig.getConfigJson());
        JsonNode themeNode = readJsonNode(themeConfig == null ? null : themeConfig.getConfigJson());

        response.setSiteName(readJsonText(baseNode, "siteName", "云屿"));
        response.setSiteSubTitle(readJsonText(baseNode, "siteSubTitle", "在二次元场景与情绪里漫游的内容站"));
        response.setFooterText(readJsonText(baseNode, "footerText", "云屿 Yunyu"));
        response.setLogoUrl(readJsonText(baseNode, "logoUrl", ""));
        response.setFaviconUrl(readJsonText(baseNode, "faviconUrl", ""));
        response.setDefaultTitle(readJsonText(seoNode, "defaultTitle", response.getSiteName()));
        response.setDefaultDescription(readJsonText(seoNode, "defaultDescription", response.getSiteSubTitle()));
        response.setDefaultShareImage(readJsonText(seoNode, "defaultShareImage", ""));
        response.setPrimaryColor(readJsonText(themeNode, "primaryColor", "#38bdf8"));
        response.setSecondaryColor(readJsonText(themeNode, "secondaryColor", "#fb923c"));
        response.setHomeStyle(readJsonText(themeNode, "homeStyle", "default"));
        return response;
    }

    /**
     * 查询推荐文章。
     *
     * @return 推荐文章列表
     */
    private List<SitePostSummaryResponse> listRecommendedPosts() {
        List<PostEntity> postEntities = postMapper.selectList(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .eq(PostEntity::getIsRecommend, 1)
                .orderByDesc(PostEntity::getIsTop)
                .orderByDesc(PostEntity::getPublishedAt)
                .orderByDesc(PostEntity::getId)
                .last("LIMIT " + DEFAULT_HOME_POST_LIMIT));

        if (postEntities.isEmpty()) {
            postEntities = postMapper.selectList(new LambdaQueryWrapper<PostEntity>()
                    .eq(PostEntity::getDeleted, 0)
                    .eq(PostEntity::getStatus, "PUBLISHED")
                    .orderByDesc(PostEntity::getIsTop)
                    .orderByDesc(PostEntity::getPublishedAt)
                    .orderByDesc(PostEntity::getId)
                    .last("LIMIT " + DEFAULT_HOME_POST_LIMIT));
        }

        return postEntities.stream()
                .map(this::toSitePostSummaryResponse)
                .toList();
    }

    /**
     * 查询最新文章。
     *
     * @return 最新文章列表
     */
    private List<SitePostSummaryResponse> listLatestPosts() {
        List<PostEntity> postEntities = postMapper.selectList(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .orderByDesc(PostEntity::getPublishedAt)
                .orderByDesc(PostEntity::getId)
                .last("LIMIT " + DEFAULT_HOME_POST_LIMIT));

        return postEntities.stream()
                .map(this::toSitePostSummaryResponse)
                .toList();
    }

    /**
     * 构建前台已发布文章查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<PostEntity> buildPublishedPostQuery(SitePostQueryRequest request) {
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .orderByDesc(PostEntity::getIsTop)
                .orderByAsc(PostEntity::getSortOrder)
                .orderByDesc(PostEntity::getPublishedAt)
                .orderByDesc(PostEntity::getId);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(PostEntity::getTitle, keyword)
                    .or()
                    .like(PostEntity::getSummary, keyword));
        }

        if (request.getCategorySlug() != null && !request.getCategorySlug().isBlank()) {
            CategoryEntity categoryEntity = findCategoryBySlug(request.getCategorySlug().trim());
            queryWrapper.eq(PostEntity::getCategoryId, categoryEntity.getId());
        }

        if (request.getTopicSlug() != null && !request.getTopicSlug().isBlank()) {
            TopicEntity topicEntity = findTopicBySlug(request.getTopicSlug().trim());
            queryWrapper.inSql(PostEntity::getId, "SELECT post_id FROM topic_post WHERE topic_id = " + topicEntity.getId());
        }

        if (request.getTagSlug() != null && !request.getTagSlug().isBlank()) {
            TagEntity tagEntity = findTagBySlug(request.getTagSlug().trim());
            queryWrapper.inSql(PostEntity::getId, "SELECT post_id FROM post_tag WHERE tag_id = " + tagEntity.getId());
        }

        return queryWrapper;
    }

    /**
     * 转换文章分页响应。
     *
     * @param page 文章分页对象
     * @param pageNo 当前页码
     * @param pageSize 当前页大小
     * @return 前台文章分页响应
     */
    private SitePostListResponse toSitePostListResponse(Page<PostEntity> page, long pageNo, long pageSize) {
        SitePostListResponse response = new SitePostListResponse();
        response.setList(page.getRecords().stream().map(this::toSitePostSummaryResponse).toList());
        response.setTotal(page.getTotal());
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setTotalPages(page.getPages() <= 0 ? 1 : page.getPages());
        return response;
    }

    /**
     * 转换前台文章摘要响应。
     *
     * @param postEntity 文章实体
     * @return 文章摘要响应
     */
    private SitePostSummaryResponse toSitePostSummaryResponse(PostEntity postEntity) {
        SitePostSummaryResponse response = new SitePostSummaryResponse();
        CategoryEntity categoryEntity = postEntity.getCategoryId() == null ? null : categoryMapper.selectById(postEntity.getCategoryId());
        UserEntity userEntity = userMapper.selectById(postEntity.getUserId());
        PostContentEntity postContentEntity = findPostContent(postEntity.getId());

        response.setId(postEntity.getId());
        response.setSlug(postEntity.getSlug());
        response.setTitle(postEntity.getTitle());
        response.setSummary(defaultString(postEntity.getSummary()));
        response.setCoverUrl(defaultString(postEntity.getCoverUrl()));
        response.setCategoryName(categoryEntity == null ? "" : defaultString(categoryEntity.getName()));
        response.setCategorySlug(categoryEntity == null ? "" : defaultString(categoryEntity.getSlug()));
        response.setTagNames(listTagNames(postEntity.getId()));
        response.setTopicNames(listTopicNames(postEntity.getId()));
        response.setAuthorName(userEntity == null ? "" : defaultString(userEntity.getUserName()));
        response.setAuthorAvatarUrl(userEntity == null ? "" : defaultString(userEntity.getAvatarUrl()));
        response.setPublishedAt(postEntity.getPublishedAt());
        response.setReadingMinutes(postContentEntity == null || postContentEntity.getReadingTime() == null ? 1 : postContentEntity.getReadingTime());
        response.setViewCount(defaultLong(postEntity.getViewCount()));
        response.setLikeCount(defaultLong(postEntity.getLikeCount()));
        response.setTop(postEntity.getIsTop() != null && postEntity.getIsTop() == 1);
        response.setRecommend(postEntity.getIsRecommend() != null && postEntity.getIsRecommend() == 1);
        return response;
    }

    /**
     * 转换前台分类摘要响应。
     *
     * @param categoryEntity 分类实体
     * @return 分类摘要响应
     */
    private SiteCategoryItemResponse toSiteCategoryItemResponse(CategoryEntity categoryEntity) {
        SiteCategoryItemResponse response = new SiteCategoryItemResponse();
        response.setId(categoryEntity.getId());
        response.setName(categoryEntity.getName());
        response.setSlug(categoryEntity.getSlug());
        response.setDescription(defaultString(categoryEntity.getDescription()));
        response.setCoverUrl(defaultString(categoryEntity.getCoverUrl()));
        response.setArticleCount(countPostsByCategoryId(categoryEntity.getId()));
        return response;
    }

    /**
     * 转换前台专题摘要响应。
     *
     * @param topicEntity 专题实体
     * @return 专题摘要响应
     */
    private SiteTopicItemResponse toSiteTopicItemResponse(TopicEntity topicEntity) {
        SiteTopicItemResponse response = new SiteTopicItemResponse();
        response.setId(topicEntity.getId());
        response.setName(topicEntity.getName());
        response.setSlug(topicEntity.getSlug());
        response.setSummary(defaultString(topicEntity.getSummary()));
        response.setCoverUrl(defaultString(topicEntity.getCoverUrl()));
        response.setArticleCount(countPostsByTopicId(topicEntity.getId()));
        return response;
    }

    /**
     * 查询相关文章。
     *
     * @param postEntity 当前文章
     * @return 相关文章
     */
    private List<SitePostSummaryResponse> listRelatedPosts(PostEntity postEntity) {
        List<PostEntity> relatedPostEntities = postMapper.selectList(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .ne(PostEntity::getId, postEntity.getId())
                .and(wrapper -> wrapper.eq(postEntity.getCategoryId() != null, PostEntity::getCategoryId, postEntity.getCategoryId())
                        .or()
                        .eq(PostEntity::getIsRecommend, 1))
                .orderByDesc(PostEntity::getIsRecommend)
                .orderByDesc(PostEntity::getPublishedAt)
                .orderByDesc(PostEntity::getId)
                .last("LIMIT 3"));

        return relatedPostEntities.stream()
                .map(this::toSitePostSummaryResponse)
                .toList();
    }

    /**
     * 通过 slug 查询已发布文章。
     *
     * @param slug 文章 slug
     * @return 文章实体
     */
    private PostEntity findPublishedPostBySlug(String slug) {
        PostEntity postEntity = postMapper.selectOne(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getSlug, slug)
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .last("LIMIT 1"));
        if (postEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        return postEntity;
    }

    /**
     * 通过 slug 查询前台可见分类。
     *
     * @param slug 分类 slug
     * @return 分类实体
     */
    private CategoryEntity findCategoryBySlug(String slug) {
        CategoryEntity categoryEntity = categoryMapper.selectOne(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getSlug, slug)
                .eq(CategoryEntity::getDeleted, 0)
                .eq(CategoryEntity::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (categoryEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "分类不存在");
        }
        return categoryEntity;
    }

    /**
     * 通过 slug 查询前台可见专题。
     *
     * @param slug 专题 slug
     * @return 专题实体
     */
    private TopicEntity findTopicBySlug(String slug) {
        TopicEntity topicEntity = topicMapper.selectOne(new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getSlug, slug)
                .eq(TopicEntity::getDeleted, 0)
                .eq(TopicEntity::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (topicEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "专题不存在");
        }
        return topicEntity;
    }

    /**
     * 通过 slug 查询前台可见标签。
     *
     * @param slug 标签 slug
     * @return 标签实体
     */
    private TagEntity findTagBySlug(String slug) {
        TagEntity tagEntity = tagMapper.selectOne(new LambdaQueryWrapper<TagEntity>()
                .eq(TagEntity::getSlug, slug)
                .eq(TagEntity::getDeleted, 0)
                .eq(TagEntity::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (tagEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "标签不存在");
        }
        return tagEntity;
    }

    /**
     * 查询文章正文。
     *
     * @param postId 文章ID
     * @return 正文实体
     */
    private PostContentEntity findPostContent(Long postId) {
        return postContentMapper.selectOne(new LambdaQueryWrapper<PostContentEntity>()
                .eq(PostContentEntity::getPostId, postId)
                .last("LIMIT 1"));
    }

    /**
     * 查询文章标签名称列表。
     *
     * @param postId 文章ID
     * @return 标签名称列表
     */
    private List<String> listTagNames(Long postId) {
        List<PostTagEntity> relations = postTagMapper.selectList(new LambdaQueryWrapper<PostTagEntity>()
                .eq(PostTagEntity::getPostId, postId));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> tagNames = new ArrayList<>();
        for (PostTagEntity relation : relations) {
            TagEntity tagEntity = tagMapper.selectById(relation.getTagId());
            if (tagEntity != null && Objects.equals(tagEntity.getDeleted(), 0)) {
                tagNames.add(tagEntity.getName());
            }
        }
        return tagNames;
    }

    /**
     * 查询文章专题名称列表。
     *
     * @param postId 文章ID
     * @return 专题名称列表
     */
    private List<String> listTopicNames(Long postId) {
        List<TopicPostEntity> relations = topicPostMapper.selectList(new LambdaQueryWrapper<TopicPostEntity>()
                .eq(TopicPostEntity::getPostId, postId)
                .orderByAsc(TopicPostEntity::getSortOrder)
                .orderByAsc(TopicPostEntity::getId));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> topicNames = new ArrayList<>();
        for (TopicPostEntity relation : relations) {
            TopicEntity topicEntity = topicMapper.selectById(relation.getTopicId());
            if (topicEntity != null && Objects.equals(topicEntity.getDeleted(), 0)) {
                topicNames.add(topicEntity.getName());
            }
        }
        return topicNames;
    }

    /**
     * 统计分类下已发布文章数量。
     *
     * @param categoryId 分类ID
     * @return 文章数量
     */
    private Long countPostsByCategoryId(Long categoryId) {
        Long count = postMapper.selectCount(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .eq(PostEntity::getCategoryId, categoryId));
        return count == null ? 0L : count;
    }

    /**
     * 统计专题下已发布文章数量。
     *
     * @param topicId 专题ID
     * @return 文章数量
     */
    private Long countPostsByTopicId(Long topicId) {
        Long count = postMapper.selectCount(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .inSql(PostEntity::getId, "SELECT post_id FROM topic_post WHERE topic_id = " + topicId));
        return count == null ? 0L : count;
    }

    /**
     * 查询站点配置。
     *
     * @param configKey 配置键
     * @return 站点配置
     */
    private SiteConfigEntity findSiteConfig(String configKey) {
        return siteConfigMapper.selectOne(new LambdaQueryWrapper<SiteConfigEntity>()
                .eq(SiteConfigEntity::getConfigKey, configKey)
                .last("LIMIT 1"));
    }

    /**
     * 读取 JSON 节点。
     *
     * @param jsonText JSON 文本
     * @return JSON 节点
     */
    private JsonNode readJsonNode(String jsonText) {
        if (jsonText == null || jsonText.isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            return objectMapper.readTree(jsonText);
        } catch (Exception exception) {
            return objectMapper.createObjectNode();
        }
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
     * 复制查询参数。
     *
     * @param request 原始请求
     * @return 复制后的请求
     */
    private SitePostQueryRequest copyRequest(SitePostQueryRequest request) {
        SitePostQueryRequest copiedRequest = new SitePostQueryRequest();
        if (request == null) {
            return copiedRequest;
        }
        copiedRequest.setKeyword(request.getKeyword());
        copiedRequest.setCategorySlug(request.getCategorySlug());
        copiedRequest.setTopicSlug(request.getTopicSlug());
        copiedRequest.setTagSlug(request.getTagSlug());
        copiedRequest.setPageNo(request.getPageNo());
        copiedRequest.setPageSize(request.getPageSize());
        return copiedRequest;
    }

    /**
     * 标准化页码。
     *
     * @param pageNo 原始页码
     * @return 标准化后的页码
     */
    private long normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo < 1) {
            return 1;
        }
        return pageNo;
    }

    /**
     * 标准化分页大小。
     *
     * @param pageSize 原始分页大小
     * @return 标准化后的分页大小
     */
    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
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

    /**
     * 返回默认长整型。
     *
     * @param value 原始值
     * @return 默认化后的长整型
     */
    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    /**
     * 返回默认 JSON 数组文本。
     *
     * @param value 原始值
     * @return 默认化后的 JSON 数组文本
     */
    private String defaultJsonArray(String value) {
        return value == null || value.isBlank() ? "[]" : value;
    }
}
