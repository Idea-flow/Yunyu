package com.ideaflow.yunyu.module.site.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.JsonNode;
import com.ideaflow.yunyu.module.contentaccess.model.ContentAccessArticleConfig;
import com.ideaflow.yunyu.module.contentaccess.model.ContentAccessConfig;
import com.ideaflow.yunyu.module.contentaccess.model.ContentAccessTailHiddenConfig;
import com.ideaflow.yunyu.module.contentaccess.dto.SiteContentAccessVerifyRequest;
import com.ideaflow.yunyu.module.contentaccess.service.ContentAccessGrantService;
import com.ideaflow.yunyu.module.contentaccess.vo.SiteContentAccessVerifyResponse;
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
import com.ideaflow.yunyu.module.site.vo.SiteBaseInfoResponse;
import com.ideaflow.yunyu.module.site.vo.SiteCategoryDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteCategoryItemResponse;
import com.ideaflow.yunyu.module.site.vo.SiteContentAccessStateResponse;
import com.ideaflow.yunyu.module.site.vo.SiteHeroVisualResponse;
import com.ideaflow.yunyu.module.site.vo.SiteHomeResponse;
import com.ideaflow.yunyu.module.site.vo.SiteHomepageConfigResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostListResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostSummaryResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTagDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTagItemResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTagLinkResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTopicDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTopicItemResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTopicLinkResponse;
import com.ideaflow.yunyu.module.tag.entity.TagEntity;
import com.ideaflow.yunyu.module.tag.mapper.TagMapper;
import com.ideaflow.yunyu.module.topic.entity.TopicEntity;
import com.ideaflow.yunyu.module.topic.mapper.TopicMapper;
import com.ideaflow.yunyu.module.user.entity.UserEntity;
import com.ideaflow.yunyu.module.user.mapper.UserMapper;
import com.ideaflow.yunyu.security.LoginUser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
    private final SiteConfigService siteConfigService;
    private final HomepageConfigService homepageConfigService;
    private final ContentAccessGrantService contentAccessGrantService;

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
     * @param siteConfigService 站点配置统一服务
     * @param homepageConfigService 首页配置服务
     * @param contentAccessGrantService 内容访问授权缓存服务
     */
    public SiteContentService(PostMapper postMapper,
                              PostContentMapper postContentMapper,
                              CategoryMapper categoryMapper,
                              TopicMapper topicMapper,
                              TagMapper tagMapper,
                              UserMapper userMapper,
                              PostTagMapper postTagMapper,
                              TopicPostMapper topicPostMapper,
                              SiteConfigService siteConfigService,
                              HomepageConfigService homepageConfigService,
                              ContentAccessGrantService contentAccessGrantService) {
        this.postMapper = postMapper;
        this.postContentMapper = postContentMapper;
        this.categoryMapper = categoryMapper;
        this.topicMapper = topicMapper;
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.postTagMapper = postTagMapper;
        this.topicPostMapper = topicPostMapper;
        this.siteConfigService = siteConfigService;
        this.homepageConfigService = homepageConfigService;
        this.contentAccessGrantService = contentAccessGrantService;
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
        response.setRecommendedPosts(listRecommendedPosts());
        response.setLatestPosts(listLatestPosts());
        response.setCategories(listCategories());
        response.setTopics(listTopics());
        return response;
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
    public SitePostDetailResponse getPostDetail(String slug, String visitorId) {
        PostEntity postEntity = findPublishedPostBySlug(slug);
        PostContentEntity postContentEntity = findPostContent(postEntity.getId());
        SitePostDetailResponse response = new SitePostDetailResponse();
        SitePostSummaryResponse summaryResponse = toSitePostSummaryResponse(postEntity);
        ContentAccessConfig contentAccessConfig = buildContentAccessConfig(postContentEntity);
        String visitorIdHash = contentAccessGrantService.hashVisitorId(visitorId);
        SiteContentAccessStateResponse contentAccessState = buildContentAccessState(postEntity.getId(), contentAccessConfig, visitorIdHash);

        response.setId(summaryResponse.getId());
        response.setSlug(summaryResponse.getSlug());
        response.setTitle(summaryResponse.getTitle());
        response.setSummary(summaryResponse.getSummary());
        response.setCoverUrl(summaryResponse.getCoverUrl());
        response.setVideoUrl(postContentEntity == null ? "" : defaultString(postContentEntity.getVideoUrl()));
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
        response.setContentMarkdown(Boolean.TRUE.equals(contentAccessState.getArticleAccessAllowed())
                ? postContentEntity == null ? "" : defaultString(postContentEntity.getContentMarkdown())
                : "");
        response.setContentHtml(Boolean.TRUE.equals(contentAccessState.getArticleAccessAllowed())
                ? postContentEntity == null ? "" : defaultString(postContentEntity.getContentHtml())
                : "");
        response.setContentTocJson(Boolean.TRUE.equals(contentAccessState.getArticleAccessAllowed())
                ? postContentEntity == null ? "[]" : defaultJsonArray(postContentEntity.getContentTocJson())
                : "[]");
        response.setContentAccessConfig(contentAccessConfig);
        response.setTailHiddenTitle(resolveTailHiddenTitle(contentAccessConfig));
        response.setTailHiddenContentHtml(Boolean.TRUE.equals(contentAccessState.getArticleAccessAllowed())
                && Boolean.TRUE.equals(contentAccessState.getTailHiddenAccessAllowed())
                ? defaultString(postContentEntity == null ? null : postContentEntity.getTailHiddenContentHtml())
                : "");
        response.setContentAccessState(contentAccessState);
        response.setAllowComment(Boolean.TRUE.equals(contentAccessState.getArticleAccessAllowed())
                && postEntity.getAllowComment() != null && postEntity.getAllowComment() == 1);
        response.setRelatedPosts(listRelatedPosts(postEntity));
        return response;
    }

    /**
     * 校验并写入内容访问授权。
     *
     * @param slug 文章 slug
     * @param request 校验请求
     * @param visitorId 访客标识
     * @return 最新内容访问状态
     */
    public SiteContentAccessVerifyResponse verifyContentAccess(String slug, SiteContentAccessVerifyRequest request, String visitorId) {
        PostEntity postEntity = findPublishedPostBySlug(slug);
        PostContentEntity postContentEntity = findPostContent(postEntity.getId());
        ContentAccessConfig contentAccessConfig = buildContentAccessConfig(postContentEntity);
        String visitorIdHash = contentAccessGrantService.hashVisitorId(visitorId);
        Long currentUserId = getCurrentUserId();
        String scopeType = request.getScopeType().trim();
        String ruleType = request.getRuleType().trim();
        String accessCode = request.getAccessCode().trim();

        validateRuleAllowed(scopeType, ruleType, contentAccessConfig);
        validateAccessCode(scopeType, ruleType, accessCode, contentAccessConfig);
        contentAccessGrantService.grantAccess(scopeType, postEntity.getId(), ruleType, currentUserId, visitorIdHash);

        SiteContentAccessVerifyResponse response = new SiteContentAccessVerifyResponse();
        response.setGranted(Boolean.TRUE);
        response.setContentAccessState(buildContentAccessState(postEntity.getId(), contentAccessConfig, visitorIdHash));
        return response;
    }

    /**
     * 构建内容访问控制配置。
     * 作用：从正文扩展表中解析文章访问控制和尾部隐藏内容配置，供前台详情页复用。
     *
     * @param postContentEntity 正文实体
     * @return 内容访问控制配置
     */
    private ContentAccessConfig buildContentAccessConfig(PostContentEntity postContentEntity) {
        if (postContentEntity == null || postContentEntity.getContentAccessConfigJson() == null || postContentEntity.getContentAccessConfigJson().isBlank()) {
            return createDefaultContentAccessConfig();
        }

        try {
            ContentAccessConfig contentAccessConfig = siteConfigService.getObjectMapper()
                    .readValue(postContentEntity.getContentAccessConfigJson(), ContentAccessConfig.class);
            return normalizeContentAccessConfig(contentAccessConfig);
        } catch (Exception exception) {
            return createDefaultContentAccessConfig();
        }
    }

    /**
     * 构建前台内容访问状态。
     * 作用：根据当前登录态、文章访问规则和站点级公众号配置，生成详情页可直接消费的状态对象。
     *
     * @param contentAccessConfig 内容访问配置
     * @return 当前访客内容访问状态
     */
    private SiteContentAccessStateResponse buildContentAccessState(Long postId, ContentAccessConfig contentAccessConfig, String visitorIdHash) {
        SiteContentAccessStateResponse response = new SiteContentAccessStateResponse();
        boolean loggedIn = isCurrentUserLoggedIn();
        JsonNode contentAccessConfigNode = siteConfigService.readJsonNode(readSiteContentAccessConfigJson());
        ContentAccessArticleConfig articleAccess = contentAccessConfig.getArticleAccess();
        ContentAccessTailHiddenConfig tailHiddenAccess = contentAccessConfig.getTailHiddenAccess();
        Long currentUserId = getCurrentUserId();
        List<String> articlePendingRules = resolvePendingRuleTypes("ARTICLE",
                postId,
                articleAccess.getEnabled(),
                articleAccess.getRuleTypes(),
                loggedIn,
                currentUserId,
                visitorIdHash,
                Set.of());
        boolean articleAccessAllowed = articlePendingRules.isEmpty();
        Set<String> inheritedSatisfiedRuleTypes = resolveSatisfiedRuleTypes(articleAccess.getEnabled(), articleAccess.getRuleTypes(), articlePendingRules);
        List<String> tailPendingRules = resolvePendingRuleTypes("TAIL_HIDDEN",
                postId,
                tailHiddenAccess.getEnabled(),
                tailHiddenAccess.getRuleTypes(),
                loggedIn,
                currentUserId,
                visitorIdHash,
                inheritedSatisfiedRuleTypes);

        response.setLoggedIn(loggedIn);
        response.setArticleAccessRuleTypes(articleAccess.getRuleTypes());
        response.setArticleAccessPendingRuleTypes(articlePendingRules);
        response.setTailHiddenAccessRuleTypes(tailHiddenAccess.getRuleTypes());
        response.setTailHiddenAccessPendingRuleTypes(tailPendingRules);
        response.setWechatAccessCodeEnabled(readBoolean(contentAccessConfigNode, "wechatAccessCodeEnabled"));
        response.setWechatAccessCodeHint(readText(contentAccessConfigNode, "wechatAccessCodeHint", "关注公众号后输入访问验证码"));
        response.setWechatQrCodeUrl(readText(contentAccessConfigNode, "wechatQrCodeUrl", ""));
        response.setArticleAccessAllowed(articleAccessAllowed);
        response.setTailHiddenAccessAllowed(tailPendingRules.isEmpty());
        return response;
    }

    /**
     * 解析当前仍未满足的规则列表。
     * 作用：按“多选规则全部满足才通过”的语义计算尚未满足的规则，供前台渲染解锁提示与动作入口。
     *
     * @param scopeType 授权范围类型
     * @param scopeId 授权范围 ID
     * @param enabled 是否启用规则
     * @param ruleTypes 规则列表
     * @param loggedIn 是否已登录
     * @param userId 用户 ID
     * @param visitorIdHash 访客标识哈希
     * @return 未满足的规则列表
     */
    private List<String> resolvePendingRuleTypes(String scopeType,
                                                 Long scopeId,
                                                 Boolean enabled,
                                                 List<String> ruleTypes,
                                                 boolean loggedIn,
                                                 Long userId,
                                                 String visitorIdHash,
                                                 Set<String> inheritedSatisfiedRuleTypes) {
        if (!Boolean.TRUE.equals(enabled) || ruleTypes == null || ruleTypes.isEmpty()) {
            return List.of();
        }

        List<String> pendingRuleTypes = new ArrayList<>();
        for (String ruleType : ruleTypes) {
            if (inheritedSatisfiedRuleTypes != null && inheritedSatisfiedRuleTypes.contains(ruleType)) {
                continue;
            }
            if ("LOGIN".equals(ruleType) && !loggedIn) {
                pendingRuleTypes.add(ruleType);
                continue;
            }
            if (("WECHAT_ACCESS_CODE".equals(ruleType) || "ACCESS_CODE".equals(ruleType))
                    && !contentAccessGrantService.hasValidGrant(scopeType, scopeId, ruleType, userId, visitorIdHash)) {
                pendingRuleTypes.add(ruleType);
            }
        }
        return pendingRuleTypes;
    }

    /**
     * 解析当前范围下已满足的规则类型集合。
     * 作用：让隐藏内容范围可以继承文章范围已经通过的同类型规则，
     * 避免文章和隐藏内容同时配置相同规则时重复要求用户再次校验。
     *
     * @param enabled 是否启用当前范围
     * @param ruleTypes 当前范围规则列表
     * @param pendingRuleTypes 当前范围未满足规则列表
     * @return 已满足规则类型集合
     */
    private Set<String> resolveSatisfiedRuleTypes(Boolean enabled,
                                                  List<String> ruleTypes,
                                                  List<String> pendingRuleTypes) {
        if (!Boolean.TRUE.equals(enabled) || ruleTypes == null || ruleTypes.isEmpty()) {
            return Set.of();
        }

        Set<String> pendingRuleTypeSet = pendingRuleTypes == null
                ? Set.of()
                : new HashSet<>(pendingRuleTypes);
        Set<String> satisfiedRuleTypes = new HashSet<>();
        for (String ruleType : ruleTypes) {
            if (!pendingRuleTypeSet.contains(ruleType)) {
                satisfiedRuleTypes.add(ruleType);
            }
        }
        return satisfiedRuleTypes;
    }

    /**
     * 校验指定规则是否允许在当前范围下使用。
     *
     * @param scopeType 范围类型
     * @param ruleType 规则类型
     * @param contentAccessConfig 内容访问配置
     */
    private void validateRuleAllowed(String scopeType, String ruleType, ContentAccessConfig contentAccessConfig) {
        List<String> supportedRuleTypes = "ARTICLE".equals(scopeType)
                ? contentAccessConfig.getArticleAccess().getRuleTypes()
                : contentAccessConfig.getTailHiddenAccess().getRuleTypes();

        if (!supportedRuleTypes.contains(ruleType)) {
            throw new BizException(ResultCode.BAD_REQUEST, "当前内容未启用该访问规则");
        }

    }

    /**
     * 校验访问码是否正确。
     *
     * @param scopeType 范围类型
     * @param ruleType 规则类型
     * @param accessCode 用户提交的访问码
     * @param contentAccessConfig 内容访问配置
     */
    private void validateAccessCode(String scopeType, String ruleType, String accessCode, ContentAccessConfig contentAccessConfig) {
        if ("ACCESS_CODE".equals(ruleType)) {
            String expectedCode = defaultString(contentAccessConfig.getArticleAccess().getArticleAccessCode()).trim();
            if (expectedCode.isEmpty() || !expectedCode.equals(accessCode)) {
                throw new BizException(ResultCode.BAD_REQUEST, "文章访问码不正确");
            }
            return;
        }

        if ("WECHAT_ACCESS_CODE".equals(ruleType)) {
            JsonNode contentAccessNode = siteConfigService.getConfigJsonNodeByKey("site.content-access");
            boolean enabled = readBoolean(contentAccessNode, "wechatAccessCodeEnabled");
            String expectedCode = readText(contentAccessNode, "wechatAccessCode", "");
            if (!enabled || expectedCode.isBlank()) {
                throw new BizException(ResultCode.BAD_REQUEST, "站点未启用公众号验证码");
            }
            if (!expectedCode.equals(accessCode)) {
                throw new BizException(ResultCode.BAD_REQUEST, "公众号验证码不正确");
            }
            return;
        }

        throw new BizException(ResultCode.BAD_REQUEST, "不支持的访问规则");
    }

    /**
     * 读取尾部隐藏内容标题。
     * 作用：前台详情页在需要展示隐藏内容模块时，优先复用配置中的业务标题。
     *
     * @param contentAccessConfig 内容访问配置
     * @return 模块标题
     */
    private String resolveTailHiddenTitle(ContentAccessConfig contentAccessConfig) {
        String title = contentAccessConfig.getTailHiddenAccess().getTitle();
        return title == null || title.isBlank() ? "隐藏内容" : title;
    }

    /**
     * 创建默认内容访问控制配置。
     * 作用：当正文尚未配置访问控制时，给前台返回稳定的默认对象结构。
     *
     * @return 默认内容访问控制配置
     */
    private ContentAccessConfig createDefaultContentAccessConfig() {
        return normalizeContentAccessConfig(new ContentAccessConfig());
    }

    /**
     * 规范化内容访问控制配置。
     *
     * @param contentAccessConfig 原始配置
     * @return 规范化后的配置
     */
    private ContentAccessConfig normalizeContentAccessConfig(ContentAccessConfig contentAccessConfig) {
        ContentAccessConfig normalizedConfig = contentAccessConfig == null ? new ContentAccessConfig() : contentAccessConfig;
        if (normalizedConfig.getVersion() == null || normalizedConfig.getVersion() < 1) {
            normalizedConfig.setVersion(1);
        }
        if (normalizedConfig.getArticleAccess() == null) {
            normalizedConfig.setArticleAccess(new ContentAccessArticleConfig());
        }
        if (normalizedConfig.getTailHiddenAccess() == null) {
            normalizedConfig.setTailHiddenAccess(new ContentAccessTailHiddenConfig());
        }
        normalizedConfig.getArticleAccess().setEnabled(Boolean.TRUE.equals(normalizedConfig.getArticleAccess().getEnabled()));
        normalizedConfig.getArticleAccess().setRuleTypes(normalizeRuleTypes(normalizedConfig.getArticleAccess().getRuleTypes()));
        normalizedConfig.getTailHiddenAccess().setEnabled(Boolean.TRUE.equals(normalizedConfig.getTailHiddenAccess().getEnabled()));
        normalizedConfig.getTailHiddenAccess().setRuleTypes(normalizeRuleTypes(normalizedConfig.getTailHiddenAccess().getRuleTypes()));
        return normalizedConfig;
    }

    /**
     * 规范化规则类型列表。
     *
     * @param ruleTypes 原始规则列表
     * @return 规范化后的规则列表
     */
    private List<String> normalizeRuleTypes(List<String> ruleTypes) {
        if (ruleTypes == null || ruleTypes.isEmpty()) {
            return List.of();
        }

        List<String> normalizedTypes = new ArrayList<>();
        for (String ruleType : ruleTypes) {
            if (ruleType != null && !ruleType.isBlank() && !normalizedTypes.contains(ruleType.trim())) {
                normalizedTypes.add(ruleType.trim());
            }
        }
        return normalizedTypes;
    }

    /**
     * 判断当前访问者是否已登录。
     * 作用：为前台详情页的最小访问裁决提供登录态依据，避免依赖前端传参。
     *
     * @return 是否已登录
     */
    private boolean isCurrentUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof LoginUser;
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID，未登录时返回 null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser.getUserId();
    }

    /**
     * 读取站点级内容访问配置 JSON。
     *
     * @return 配置 JSON
     */
    private String readSiteContentAccessConfigJson() {
        return siteConfigService.getConfigJsonTextByKey("site.content-access", "{}");
    }

    /**
     * 从 JSON 节点中读取布尔值。
     *
     * @param jsonNode JSON 节点
     * @param fieldName 字段名
     * @return 布尔值
     */
    private boolean readBoolean(JsonNode jsonNode, String fieldName) {
        return jsonNode != null && jsonNode.path(fieldName).asBoolean(false);
    }

    /**
     * 从 JSON 节点中读取文本值。
     *
     * @param jsonNode JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 文本值
     */
    private String readText(JsonNode jsonNode, String fieldName, String defaultValue) {
        if (jsonNode == null || jsonNode.path(fieldName).isMissingNode() || jsonNode.path(fieldName).isNull()) {
            return defaultValue;
        }
        String value = jsonNode.path(fieldName).asText(defaultValue);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * 增加文章浏览量。
     * 作用：在前台文章详情页完成客户端加载后，按文章 id 执行一次原子自增，
     * 避免详情读取接口与浏览量统计逻辑耦合。
     *
     * @param id 文章 id
     */
    public void increasePostViewCount(Long id) {
        if (id == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "文章不存在");
        }

        int affectedRows = postMapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                .setSql("view_count = COALESCE(view_count, 0) + 1")
                .eq(PostEntity::getId, id)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .eq(PostEntity::getDeleted, 0));

        if (affectedRows <= 0) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
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
     * 查询前台标签列表。
     *
     * @return 标签列表
     */
    public List<SiteTagItemResponse> listTags() {
        List<TagEntity> tagEntities = tagMapper.selectList(new LambdaQueryWrapper<TagEntity>()
                .eq(TagEntity::getDeleted, 0)
                .eq(TagEntity::getStatus, "ACTIVE")
                .orderByAsc(TagEntity::getName)
                .orderByAsc(TagEntity::getId));

        return tagEntities.stream()
                .map(this::toSiteTagItemResponse)
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
     * 查询前台标签详情。
     *
     * @param slug 标签 slug
     * @param request 查询请求
     * @return 标签详情
     */
    public SiteTagDetailResponse getTagDetail(String slug, SitePostQueryRequest request) {
        TagEntity tagEntity = findTagBySlug(slug);
        SitePostQueryRequest actualRequest = copyRequest(request);
        actualRequest.setTagSlug(tagEntity.getSlug());

        SiteTagDetailResponse response = new SiteTagDetailResponse();
        response.setTag(toSiteTagItemResponse(tagEntity));
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
        response.setTagItems(listTagItems(postEntity.getId()));
        response.setTagNames(listTagNames(postEntity.getId()));
        response.setTopicItems(listTopicItems(postEntity.getId()));
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
     * 构建首页首屏视觉块响应。
     * 作用：根据首页配置中指定的视觉文章，优先返回视频，其次返回封面图，供首页右侧主视觉直接展示。
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
     * 转换前台标签摘要响应。
     *
     * @param tagEntity 标签实体
     * @return 标签摘要响应
     */
    private SiteTagItemResponse toSiteTagItemResponse(TagEntity tagEntity) {
        SiteTagItemResponse response = new SiteTagItemResponse();
        response.setId(tagEntity.getId());
        response.setName(tagEntity.getName());
        response.setSlug(tagEntity.getSlug());
        response.setDescription(defaultString(tagEntity.getDescription()));
        response.setArticleCount(countPostsByTagId(tagEntity.getId()));
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
     * 通过 ID 查询已发布文章。
     *
     * @param postId 文章ID
     * @return 文章实体
     */
    private PostEntity findPublishedPostById(Long postId) {
        if (postId == null) {
            return null;
        }

        return postMapper.selectOne(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .last("LIMIT 1"));
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
        return listTagItems(postId).stream()
                .map(SiteTagLinkResponse::getName)
                .toList();
    }

    /**
     * 查询文章标签链接列表。
     *
     * @param postId 文章ID
     * @return 标签链接列表
     */
    private List<SiteTagLinkResponse> listTagItems(Long postId) {
        List<PostTagEntity> relations = postTagMapper.selectList(new LambdaQueryWrapper<PostTagEntity>()
                .eq(PostTagEntity::getPostId, postId));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<SiteTagLinkResponse> tagItems = new ArrayList<>();
        for (PostTagEntity relation : relations) {
            TagEntity tagEntity = tagMapper.selectById(relation.getTagId());
            if (tagEntity != null
                    && Objects.equals(tagEntity.getDeleted(), 0)
                    && Objects.equals(tagEntity.getStatus(), "ACTIVE")) {
                SiteTagLinkResponse response = new SiteTagLinkResponse();
                response.setName(tagEntity.getName());
                response.setSlug(tagEntity.getSlug());
                tagItems.add(response);
            }
        }
        return tagItems;
    }

    /**
     * 查询文章专题名称列表。
     *
     * @param postId 文章ID
     * @return 专题名称列表
     */
    private List<String> listTopicNames(Long postId) {
        return listTopicItems(postId).stream()
                .map(SiteTopicLinkResponse::getName)
                .toList();
    }

    /**
     * 查询文章专题链接列表。
     *
     * @param postId 文章ID
     * @return 专题链接列表
     */
    private List<SiteTopicLinkResponse> listTopicItems(Long postId) {
        List<TopicPostEntity> relations = topicPostMapper.selectList(new LambdaQueryWrapper<TopicPostEntity>()
                .eq(TopicPostEntity::getPostId, postId)
                .orderByAsc(TopicPostEntity::getSortOrder)
                .orderByAsc(TopicPostEntity::getId));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<SiteTopicLinkResponse> topicItems = new ArrayList<>();
        for (TopicPostEntity relation : relations) {
            TopicEntity topicEntity = topicMapper.selectById(relation.getTopicId());
            if (topicEntity != null
                    && Objects.equals(topicEntity.getDeleted(), 0)
                    && Objects.equals(topicEntity.getStatus(), "ACTIVE")) {
                SiteTopicLinkResponse response = new SiteTopicLinkResponse();
                response.setName(topicEntity.getName());
                response.setSlug(topicEntity.getSlug());
                topicItems.add(response);
            }
        }
        return topicItems;
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
     * 统计标签下已发布文章数量。
     *
     * @param tagId 标签ID
     * @return 文章数量
     */
    private Long countPostsByTagId(Long tagId) {
        Long count = postMapper.selectCount(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .inSql(PostEntity::getId, "SELECT post_id FROM post_tag WHERE tag_id = " + tagId));
        return count == null ? 0L : count;
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
