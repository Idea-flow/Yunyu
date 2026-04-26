package com.ideaflow.yunyu.module.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.ObjectMapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.category.entity.CategoryEntity;
import com.ideaflow.yunyu.module.category.mapper.CategoryMapper;
import com.ideaflow.yunyu.module.contentaccess.service.ContentAccessGrantService;
import com.ideaflow.yunyu.module.contentaccess.model.ContentAccessArticleConfig;
import com.ideaflow.yunyu.module.contentaccess.model.ContentAccessConfig;
import com.ideaflow.yunyu.module.contentaccess.model.ContentAccessTailHiddenConfig;
import com.ideaflow.yunyu.module.post.dto.AdminPostCreateRequest;
import com.ideaflow.yunyu.module.post.dto.AdminPostQueryRequest;
import com.ideaflow.yunyu.module.post.dto.AdminPostUpdateRequest;
import com.ideaflow.yunyu.module.post.entity.PostContentEntity;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.entity.PostTagEntity;
import com.ideaflow.yunyu.module.post.entity.TopicPostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostContentMapper;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.post.mapper.PostTagMapper;
import com.ideaflow.yunyu.module.post.mapper.TopicPostMapper;
import com.ideaflow.yunyu.module.post.vo.AdminPostItemResponse;
import com.ideaflow.yunyu.module.post.vo.AdminPostListResponse;
import com.ideaflow.yunyu.module.tag.entity.TagEntity;
import com.ideaflow.yunyu.module.tag.mapper.TagMapper;
import com.ideaflow.yunyu.module.topic.entity.TopicEntity;
import com.ideaflow.yunyu.module.topic.mapper.TopicMapper;
import com.ideaflow.yunyu.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台文章管理服务类。
 * 作用：处理后台文章的查询、创建、更新、删除和正文同步逻辑，作为文章管理接口的核心业务层。
 */
@Service
public class AdminPostService {

    private final PostMapper postMapper;
    private final PostContentMapper postContentMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final TopicMapper topicMapper;
    private final PostTagMapper postTagMapper;
    private final TopicPostMapper topicPostMapper;
    private final ObjectMapper objectMapper;
    private final ContentAccessGrantService contentAccessGrantService;

    /**
     * 创建后台文章管理服务。
     *
     * @param postMapper 文章 Mapper
     * @param postContentMapper 文章内容 Mapper
     * @param categoryMapper 分类 Mapper
     * @param tagMapper 标签 Mapper
     * @param topicMapper 专题 Mapper
     * @param postTagMapper 文章标签关联 Mapper
     * @param topicPostMapper 专题文章关联 Mapper
     * @param objectMapper Jackson 对象映射器
     * @param contentAccessGrantService 内容访问授权缓存服务
     */
    public AdminPostService(PostMapper postMapper,
                            PostContentMapper postContentMapper,
                            CategoryMapper categoryMapper,
                            TagMapper tagMapper,
                            TopicMapper topicMapper,
                            PostTagMapper postTagMapper,
                            TopicPostMapper topicPostMapper,
                            ObjectMapper objectMapper,
                            ContentAccessGrantService contentAccessGrantService) {
        this.postMapper = postMapper;
        this.postContentMapper = postContentMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.topicMapper = topicMapper;
        this.postTagMapper = postTagMapper;
        this.topicPostMapper = topicPostMapper;
        this.objectMapper = objectMapper;
        this.contentAccessGrantService = contentAccessGrantService;
    }

    /**
     * 查询文章列表。
     *
     * @param request 查询请求
     * @return 文章列表响应
     */
    public AdminPostListResponse listPosts(AdminPostQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        Page<PostEntity> page = postMapper.selectPage(new Page<>(pageNo, pageSize), buildPostListQuery(request));

        List<AdminPostItemResponse> items = page.getRecords()
                .stream()
                .map(this::toAdminPostItemResponse)
                .toList();
        long totalPages = page.getPages() <= 0 ? 1 : page.getPages();
        return new AdminPostListResponse(items, page.getTotal(), pageNo, pageSize, totalPages);
    }

    /**
     * 获取文章详情。
     *
     * @param postId 文章ID
     * @return 文章详情
     */
    public AdminPostItemResponse getPost(Long postId) {
        return toAdminPostItemResponse(findPostOrThrow(postId));
    }

    /**
     * 创建文章。
     *
     * @param request 创建请求
     * @return 创建后的文章详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminPostItemResponse createPost(AdminPostCreateRequest request) {
        String slug = normalizeSlug(request.getSlug());
        ensureSlugUnique(slug, null);

        PostEntity postEntity = new PostEntity();
        postEntity.setTitle(request.getTitle().trim());
        postEntity.setSlug(slug);
        postEntity.setSummary(normalizeOptionalValue(request.getSummary()));
        postEntity.setCoverUrl(normalizeOptionalValue(request.getCoverUrl()));
        postEntity.setCategoryId(resolveCategoryId(request.getCategoryId()));
        postEntity.setSeoTitle(normalizeOptionalValue(request.getSeoTitle()));
        postEntity.setSeoDescription(normalizeOptionalValue(request.getSeoDescription()));
        postEntity.setUserId(SecurityUtils.getCurrentUser().getUserId());
        postEntity.setStatus(resolveStatus(request.getStatus()));
        postEntity.setIsTop(resolveFlagValue(request.getIsTop(), false));
        postEntity.setIsRecommend(resolveFlagValue(request.getIsRecommend(), false));
        postEntity.setHasVideo(hasVideo(request.getVideoUrl()) ? 1 : 0);
        postEntity.setAllowComment(resolveFlagValue(request.getAllowComment(), true));
        postEntity.setSortOrder(0);
        postEntity.setViewCount(0L);
        postEntity.setLikeCount(0L);
        postEntity.setCommentCount(0L);
        postEntity.setCreatedTime(LocalDateTime.now());
        postEntity.setUpdatedTime(LocalDateTime.now());
        postEntity.setDeleted(0);
        if ("PUBLISHED".equals(postEntity.getStatus())) {
            postEntity.setPublishedAt(LocalDateTime.now());
        }
        postMapper.insert(postEntity);

        saveOrUpdatePostContent(postEntity.getId(),
                request.getContentMarkdown(),
                request.getContentHtml(),
                request.getContentTocJson(),
                request.getVideoUrl(),
                request.getContentAccessConfig(),
                request.getTailHiddenContentMarkdown(),
                request.getTailHiddenContentHtml());
        savePostTags(postEntity.getId(), request.getTagIds());
        saveTopicPosts(postEntity.getId(), request.getTopicIds());
        return getPost(postEntity.getId());
    }

    /**
     * 更新文章。
     *
     * @param postId 文章ID
     * @param request 更新请求
     * @return 更新后的文章详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminPostItemResponse updatePost(Long postId, AdminPostUpdateRequest request) {
        PostEntity postEntity = findPostOrThrow(postId);
        String slug = normalizeSlug(request.getSlug());
        ensureSlugUnique(slug, postId);

        postEntity.setTitle(request.getTitle().trim());
        postEntity.setSlug(slug);
        postEntity.setSummary(normalizeOptionalValue(request.getSummary()));
        postEntity.setCoverUrl(normalizeOptionalValue(request.getCoverUrl()));
        postEntity.setCategoryId(resolveCategoryId(request.getCategoryId()));
        postEntity.setSeoTitle(normalizeOptionalValue(request.getSeoTitle()));
        postEntity.setSeoDescription(normalizeOptionalValue(request.getSeoDescription()));
        postEntity.setHasVideo(hasVideo(request.getVideoUrl()) ? 1 : 0);
        postEntity.setIsTop(resolveFlagValue(request.getIsTop(), postEntity.getIsTop() != null && postEntity.getIsTop() == 1));
        postEntity.setIsRecommend(resolveFlagValue(request.getIsRecommend(), postEntity.getIsRecommend() != null && postEntity.getIsRecommend() == 1));
        postEntity.setAllowComment(resolveFlagValue(request.getAllowComment(), postEntity.getAllowComment() == null || postEntity.getAllowComment() == 1));
        String nextStatus = resolveStatus(request.getStatus());
        postEntity.setStatus(nextStatus);
        postEntity.setUpdatedTime(LocalDateTime.now());
        if ("PUBLISHED".equals(nextStatus) && postEntity.getPublishedAt() == null) {
            postEntity.setPublishedAt(LocalDateTime.now());
        }
        if (!"PUBLISHED".equals(nextStatus)) {
            postEntity.setPublishedAt(null);
        }
        postMapper.updateById(postEntity);

        saveOrUpdatePostContent(postId,
                request.getContentMarkdown(),
                request.getContentHtml(),
                request.getContentTocJson(),
                request.getVideoUrl(),
                request.getContentAccessConfig(),
                request.getTailHiddenContentMarkdown(),
                request.getTailHiddenContentHtml());
        savePostTags(postId, request.getTagIds());
        saveTopicPosts(postId, request.getTopicIds());
        return getPost(postId);
    }

    /**
     * 删除文章。
     *
     * @param postId 文章ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId) {
        findPostOrThrow(postId);
        postMapper.update(null, new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getDeleted, 0)
                .set(PostEntity::getDeleted, 1)
                .set(PostEntity::getUpdatedTime, LocalDateTime.now()));
    }

    /**
     * 构建文章列表查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<PostEntity> buildPostListQuery(AdminPostQueryRequest request) {
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .orderByDesc(PostEntity::getUpdatedTime)
                .orderByDesc(PostEntity::getId);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(PostEntity::getTitle, keyword)
                    .or()
                    .like(PostEntity::getSlug, keyword));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            queryWrapper.eq(PostEntity::getStatus, request.getStatus().trim());
        }

        if (request.getCategoryId() != null && request.getCategoryId() > 0) {
            queryWrapper.eq(PostEntity::getCategoryId, request.getCategoryId());
        }

        if (request.getTagId() != null && request.getTagId() > 0) {
            queryWrapper.inSql(PostEntity::getId,
                    "SELECT post_id FROM post_tag WHERE tag_id = " + request.getTagId());
        }

        if (request.getTopicId() != null && request.getTopicId() > 0) {
            queryWrapper.inSql(PostEntity::getId,
                    "SELECT post_id FROM topic_post WHERE topic_id = " + request.getTopicId());
        }

        if (request.getIsTop() != null) {
            queryWrapper.eq(PostEntity::getIsTop, request.getIsTop());
        }

        if (request.getIsRecommend() != null) {
            queryWrapper.eq(PostEntity::getIsRecommend, request.getIsRecommend());
        }

        if (request.getAllowComment() != null) {
            queryWrapper.eq(PostEntity::getAllowComment, request.getAllowComment());
        }

        return queryWrapper;
    }

    /**
     * 保存或更新文章正文。
     *
     * @param postId 文章ID
     * @param contentMarkdown Markdown 正文
     * @param contentHtml HTML 正文
     * @param contentTocJson 目录 JSON
     * @param videoUrl 视频地址
     * @param contentAccessConfig 内容访问控制配置
     * @param tailHiddenContentMarkdown 尾部隐藏内容 Markdown
     * @param tailHiddenContentHtml 尾部隐藏内容 HTML
     */
    private void saveOrUpdatePostContent(Long postId,
                                         String contentMarkdown,
                                         String contentHtml,
                                         String contentTocJson,
                                         String videoUrl,
                                         ContentAccessConfig contentAccessConfig,
                                         String tailHiddenContentMarkdown,
                                         String tailHiddenContentHtml) {
        String markdown = contentMarkdown == null ? "" : contentMarkdown.trim();
        String html = contentHtml == null || contentHtml.isBlank() ? markdown : contentHtml.trim();
        String tocJson = contentTocJson == null || contentTocJson.isBlank() ? null : contentTocJson.trim();
        String normalizedVideoUrl = normalizeOptionalValue(videoUrl);
        ContentAccessConfig normalizedContentAccessConfig = normalizeContentAccessConfig(contentAccessConfig);
        String normalizedContentAccessConfigJson = writeContentAccessConfig(normalizedContentAccessConfig);
        String normalizedTailHiddenMarkdown = normalizeOptionalValue(tailHiddenContentMarkdown);
        String normalizedTailHiddenHtml = normalizeOptionalValue(tailHiddenContentHtml);
        String plainText = markdown.replaceAll("`{1,3}[^`]*`{1,3}", " ")
                .replaceAll("!\\[[^\\]]*\\]\\([^\\)]*\\)", " ")
                .replaceAll("\\[[^\\]]*\\]\\([^\\)]*\\)", " ")
                .replaceAll("[#>*_\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        int wordCount = plainText.isBlank() ? 0 : plainText.length();
        int readingTime = Math.max(1, (int) Math.ceil(wordCount / 500.0));

        PostContentEntity postContentEntity = postContentMapper.selectOne(new LambdaQueryWrapper<PostContentEntity>()
                .eq(PostContentEntity::getPostId, postId)
                .last("LIMIT 1"));

        if (postContentEntity == null) {
            postContentEntity = new PostContentEntity();
            postContentEntity.setPostId(postId);
            postContentEntity.setCreatedTime(LocalDateTime.now());
        }

        String previousContentAccessConfigJson = postContentEntity.getId() == null
                ? null
                : writeContentAccessConfig(readContentAccessConfig(postContentEntity.getContentAccessConfigJson()));

        postContentEntity.setContentMarkdown(markdown);
        postContentEntity.setContentHtml(html);
        postContentEntity.setContentTocJson(tocJson);
        postContentEntity.setContentAccessConfigJson(normalizedContentAccessConfigJson);
        postContentEntity.setTailHiddenContentMarkdown(normalizedTailHiddenMarkdown);
        postContentEntity.setTailHiddenContentHtml(normalizedTailHiddenHtml);
        postContentEntity.setVideoUrl(normalizedVideoUrl);
        postContentEntity.setContentPlainText(plainText);
        postContentEntity.setReadingTime(readingTime);
        postContentEntity.setUpdatedTime(LocalDateTime.now());

        if (postContentEntity.getId() == null) {
            postContentMapper.insert(postContentEntity);
            return;
        }
        postContentMapper.updateById(postContentEntity);

        if (!Objects.equals(previousContentAccessConfigJson, normalizedContentAccessConfigJson)) {
            contentAccessGrantService.clearPostAccessGrants(postId);
        }
    }

    /**
     * 保存文章标签关联。
     *
     * @param postId 文章ID
     * @param tagIds 标签ID列表
     */
    private void savePostTags(Long postId, List<Long> tagIds) {
        List<Long> normalizedTagIds = normalizeRelationIds(tagIds);
        validateTagIds(normalizedTagIds);

        postTagMapper.delete(new LambdaQueryWrapper<PostTagEntity>()
                .eq(PostTagEntity::getPostId, postId));

        for (Long tagId : normalizedTagIds) {
            PostTagEntity postTagEntity = new PostTagEntity();
            postTagEntity.setPostId(postId);
            postTagEntity.setTagId(tagId);
            postTagEntity.setCreatedTime(LocalDateTime.now());
            postTagMapper.insert(postTagEntity);
        }
    }

    /**
     * 保存文章专题关联。
     *
     * @param postId 文章ID
     * @param topicIds 专题ID列表
     */
    private void saveTopicPosts(Long postId, List<Long> topicIds) {
        List<Long> normalizedTopicIds = normalizeRelationIds(topicIds);
        validateTopicIds(normalizedTopicIds);

        topicPostMapper.delete(new LambdaQueryWrapper<TopicPostEntity>()
                .eq(TopicPostEntity::getPostId, postId));

        int sortOrder = 0;
        for (Long topicId : normalizedTopicIds) {
            TopicPostEntity topicPostEntity = new TopicPostEntity();
            topicPostEntity.setPostId(postId);
            topicPostEntity.setTopicId(topicId);
            topicPostEntity.setSortOrder(sortOrder);
            topicPostEntity.setCreatedTime(LocalDateTime.now());
            topicPostMapper.insert(topicPostEntity);
            sortOrder += 10;
        }
    }

    /**
     * 查询文章，不存在时抛出异常。
     *
     * @param postId 文章ID
     * @return 文章实体
     */
    private PostEntity findPostOrThrow(Long postId) {
        PostEntity postEntity = postMapper.selectOne(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (postEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        return postEntity;
    }

    /**
     * 校验 slug 唯一性。
     *
     * @param slug slug
     * @param excludePostId 排除的文章ID
     */
    private void ensureSlugUnique(String slug, Long excludePostId) {
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getSlug, slug)
                .eq(PostEntity::getDeleted, 0);
        if (excludePostId != null) {
            queryWrapper.ne(PostEntity::getId, excludePostId);
        }

        Long count = postMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该 slug 已被占用");
        }
    }

    /**
     * 解析分类ID。
     *
     * @param categoryId 原始分类ID
     * @return 合法分类ID
     */
    private Long resolveCategoryId(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        CategoryEntity categoryEntity = categoryMapper.selectOne(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getId, categoryId)
                .eq(CategoryEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (categoryEntity == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "所选分类不存在");
        }
        return categoryId;
    }

    /**
     * 标准化关联ID列表。
     *
     * @param relationIds 原始关联ID列表
     * @return 去重后的关联ID列表
     */
    private List<Long> normalizeRelationIds(List<Long> relationIds) {
        if (relationIds == null || relationIds.isEmpty()) {
            return List.of();
        }

        Set<Long> relationIdSet = new LinkedHashSet<>();
        for (Long relationId : relationIds) {
            if (relationId != null && relationId > 0) {
                relationIdSet.add(relationId);
            }
        }
        return new ArrayList<>(relationIdSet);
    }

    /**
     * 校验标签ID列表是否全部存在。
     *
     * @param tagIds 标签ID列表
     */
    private void validateTagIds(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }

        Long count = tagMapper.selectCount(new LambdaQueryWrapper<TagEntity>()
                .in(TagEntity::getId, tagIds)
                .eq(TagEntity::getDeleted, 0));
        if (count == null || count != tagIds.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "所选标签中存在无效项");
        }
    }

    /**
     * 校验专题ID列表是否全部存在。
     *
     * @param topicIds 专题ID列表
     */
    private void validateTopicIds(List<Long> topicIds) {
        if (topicIds.isEmpty()) {
            return;
        }

        Long count = topicMapper.selectCount(new LambdaQueryWrapper<TopicEntity>()
                .in(TopicEntity::getId, topicIds)
                .eq(TopicEntity::getDeleted, 0));
        if (count == null || count != topicIds.size()) {
            throw new BizException(ResultCode.BAD_REQUEST, "所选专题中存在无效项");
        }
    }

    /**
     * 规范化 slug。
     *
     * @param slug 原始 slug
     * @return 规范化后的 slug
     */
    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase();
    }

    /**
     * 规范化可选字段。
     *
     * @param value 原始值
     * @return 规范化后的值
     */
    private String normalizeOptionalValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 解析文章状态。
     *
     * @param status 原始状态
     * @return 文章状态
     */
    private String resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return "DRAFT";
        }
        return status.trim();
    }

    /**
     * 标准化页码。
     *
     * @param pageNo 原始页码
     * @return 页码
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
     * @return 分页大小
     */
    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    /**
     * 转换后台文章响应对象。
     *
     * @param postEntity 文章实体
     * @return 后台文章响应对象
     */
    private AdminPostItemResponse toAdminPostItemResponse(PostEntity postEntity) {
        PostContentEntity postContentEntity = postContentMapper.selectOne(new LambdaQueryWrapper<PostContentEntity>()
                .eq(PostContentEntity::getPostId, postEntity.getId())
                .last("LIMIT 1"));
        CategoryEntity categoryEntity = postEntity.getCategoryId() == null ? null : categoryMapper.selectOne(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getId, postEntity.getCategoryId())
                .eq(CategoryEntity::getDeleted, 0)
                .last("LIMIT 1"));
        List<Long> tagIds = postTagMapper.selectTagIdsByPostId(postEntity.getId());
        List<String> tagNames = postTagMapper.selectTagNamesByPostId(postEntity.getId());
        List<Long> topicIds = topicPostMapper.selectTopicIdsByPostId(postEntity.getId());
        List<String> topicNames = topicPostMapper.selectTopicNamesByPostId(postEntity.getId());

        AdminPostItemResponse response = new AdminPostItemResponse();
        response.setId(postEntity.getId());
        response.setTitle(postEntity.getTitle());
        response.setSlug(postEntity.getSlug());
        response.setSummary(postEntity.getSummary());
        response.setCoverUrl(postEntity.getCoverUrl());
        response.setVideoUrl(postContentEntity == null ? null : postContentEntity.getVideoUrl());
        response.setCategoryId(postEntity.getCategoryId());
        response.setCategoryName(categoryEntity == null ? null : categoryEntity.getName());
        response.setTagIds(tagIds == null ? List.of() : tagIds);
        response.setTagNames(tagNames == null ? List.of() : tagNames);
        response.setTopicIds(topicIds == null ? List.of() : topicIds);
        response.setTopicNames(topicNames == null ? List.of() : topicNames);
        response.setTopic(topicNames == null || topicNames.isEmpty() ? "未设置专题" : String.join(" / ", topicNames));
        response.setStatus(postEntity.getStatus());
        response.setIsTop(postEntity.getIsTop() != null && postEntity.getIsTop() == 1);
        response.setIsRecommend(postEntity.getIsRecommend() != null && postEntity.getIsRecommend() == 1);
        response.setAllowComment(postEntity.getAllowComment() == null || postEntity.getAllowComment() == 1);
        response.setSeoTitle(postEntity.getSeoTitle());
        response.setSeoDescription(postEntity.getSeoDescription());
        response.setCoverReady(postEntity.getCoverUrl() != null && !postEntity.getCoverUrl().isBlank());
        response.setVideoReady(postContentEntity != null
                && postContentEntity.getVideoUrl() != null
                && !postContentEntity.getVideoUrl().isBlank());
        response.setSummaryReady(postEntity.getSummary() != null && !postEntity.getSummary().isBlank());
        response.setReadingMinutes(postContentEntity == null || postContentEntity.getReadingTime() == null ? 1 : postContentEntity.getReadingTime());
        response.setWordCount(postContentEntity == null || postContentEntity.getContentPlainText() == null ? 0 : postContentEntity.getContentPlainText().length());
        response.setContentMarkdown(postContentEntity == null ? "" : postContentEntity.getContentMarkdown());
        response.setContentAccessConfig(readContentAccessConfig(postContentEntity == null ? null : postContentEntity.getContentAccessConfigJson()));
        response.setTailHiddenContentMarkdown(postContentEntity == null ? null : postContentEntity.getTailHiddenContentMarkdown());
        response.setUpdatedAt(postEntity.getUpdatedTime());
        response.setPublishedAt(postEntity.getPublishedAt());
        return response;
    }

    /**
     * 规范化内容访问控制配置。
     * 作用：为文章级访问控制和尾部隐藏内容提供稳定默认结构，避免前端回显时频繁做空值判断。
     *
     * @param config 原始配置
     * @return 规范化后的配置
     */
    private ContentAccessConfig normalizeContentAccessConfig(ContentAccessConfig config) {
        ContentAccessConfig normalizedConfig = config == null ? new ContentAccessConfig() : config;
        if (normalizedConfig.getVersion() == null || normalizedConfig.getVersion() < 1) {
            normalizedConfig.setVersion(1);
        }

        ContentAccessArticleConfig articleAccess = normalizedConfig.getArticleAccess();
        if (articleAccess == null) {
            articleAccess = new ContentAccessArticleConfig();
            normalizedConfig.setArticleAccess(articleAccess);
        }
        articleAccess.setEnabled(Boolean.TRUE.equals(articleAccess.getEnabled()));
        articleAccess.setRuleTypes(normalizeRuleTypes(articleAccess.getRuleTypes()));
        articleAccess.setArticleAccessCode(normalizeOptionalValue(articleAccess.getArticleAccessCode()));
        articleAccess.setArticleAccessCodeHint(normalizeOptionalValue(articleAccess.getArticleAccessCodeHint()));

        ContentAccessTailHiddenConfig tailHiddenAccess = normalizedConfig.getTailHiddenAccess();
        if (tailHiddenAccess == null) {
            tailHiddenAccess = new ContentAccessTailHiddenConfig();
            normalizedConfig.setTailHiddenAccess(tailHiddenAccess);
        }
        tailHiddenAccess.setEnabled(Boolean.TRUE.equals(tailHiddenAccess.getEnabled()));
        tailHiddenAccess.setTitle(normalizeOptionalValue(tailHiddenAccess.getTitle()));
        tailHiddenAccess.setRuleTypes(normalizeRuleTypes(tailHiddenAccess.getRuleTypes()));
        return normalizedConfig;
    }

    /**
     * 读取内容访问控制配置。
     * 作用：将正文表中的 JSON 配置转换为后台和前台可直接使用的结构化对象。
     *
     * @param contentAccessConfigJson 配置 JSON
     * @return 内容访问控制配置
     */
    private ContentAccessConfig readContentAccessConfig(String contentAccessConfigJson) {
        if (contentAccessConfigJson == null || contentAccessConfigJson.isBlank()) {
            return normalizeContentAccessConfig(null);
        }

        try {
            return normalizeContentAccessConfig(objectMapper.readValue(contentAccessConfigJson, ContentAccessConfig.class));
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "文章访问控制配置解析失败");
        }
    }

    /**
     * 写入内容访问控制配置。
     * 作用：统一序列化文章访问控制配置，避免在不同保存入口出现 JSON 结构漂移。
     *
     * @param contentAccessConfig 内容访问控制配置
     * @return 序列化后的 JSON 字符串
     */
    private String writeContentAccessConfig(ContentAccessConfig contentAccessConfig) {
        try {
            return objectMapper.writeValueAsString(normalizeContentAccessConfig(contentAccessConfig));
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "文章访问控制配置保存失败");
        }
    }

    /**
     * 规范化规则列表。
     * 作用：去除空值、重复值和前后空白，保持规则配置结构稳定。
     *
     * @param ruleTypes 原始规则列表
     * @return 规范化后的规则列表
     */
    private List<String> normalizeRuleTypes(List<String> ruleTypes) {
        if (ruleTypes == null || ruleTypes.isEmpty()) {
            return List.of();
        }

        Set<String> normalizedTypes = new LinkedHashSet<>();
        for (String ruleType : ruleTypes) {
            if (ruleType != null && !ruleType.isBlank()) {
                normalizedTypes.add(ruleType.trim());
            }
        }
        return new ArrayList<>(normalizedTypes);
    }

    /**
     * 判断当前视频地址是否有效。
     * 作用：统一生成文章主表中的视频存在标记，避免创建和更新逻辑重复判断。
     *
     * @param videoUrl 视频地址
     * @return 是否存在视频地址
     */
    private boolean hasVideo(String videoUrl) {
        return normalizeOptionalValue(videoUrl) != null;
    }

    /**
     * 将布尔开关转换为数据库中的 0/1 标记。
     * 作用：统一处理后台请求中的布尔配置，并在缺省时回落到业务默认值。
     *
     * @param value 原始布尔值
     * @param defaultValue 默认值
     * @return 数据库存储标记
     */
    private Integer resolveFlagValue(Boolean value, boolean defaultValue) {
        boolean resolvedValue = value == null ? defaultValue : value;
        return resolvedValue ? 1 : 0;
    }
}
