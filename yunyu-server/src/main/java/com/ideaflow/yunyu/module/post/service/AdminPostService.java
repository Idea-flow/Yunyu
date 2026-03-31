package com.ideaflow.yunyu.module.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.post.dto.AdminPostCreateRequest;
import com.ideaflow.yunyu.module.post.dto.AdminPostQueryRequest;
import com.ideaflow.yunyu.module.post.dto.AdminPostUpdateRequest;
import com.ideaflow.yunyu.module.post.entity.PostContentEntity;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostContentMapper;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.post.vo.AdminPostItemResponse;
import com.ideaflow.yunyu.module.post.vo.AdminPostListResponse;
import com.ideaflow.yunyu.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * 创建后台文章管理服务。
     *
     * @param postMapper 文章 Mapper
     * @param postContentMapper 文章内容 Mapper
     */
    public AdminPostService(PostMapper postMapper, PostContentMapper postContentMapper) {
        this.postMapper = postMapper;
        this.postContentMapper = postContentMapper;
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
        long total = postMapper.selectCount(buildPostListQuery(request));
        long offset = (pageNo - 1) * pageSize;

        List<AdminPostItemResponse> items = postMapper.selectList(buildPostListQuery(request)
                        .last("LIMIT " + offset + "," + pageSize))
                .stream()
                .map(this::toAdminPostItemResponse)
                .toList();

        long totalPages = total == 0 ? 1 : (long) Math.ceil((double) total / pageSize);
        return new AdminPostListResponse(items, total, pageNo, pageSize, totalPages);
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
        postEntity.setUserId(SecurityUtils.getCurrentUser().getUserId());
        postEntity.setStatus(resolveStatus(request.getStatus()));
        postEntity.setIsTop(0);
        postEntity.setIsRecommend(0);
        postEntity.setHasVideo(0);
        postEntity.setAllowComment(1);
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

        saveOrUpdatePostContent(postEntity.getId(), request.getContentMarkdown());
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

        saveOrUpdatePostContent(postId, request.getContentMarkdown());
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

        return queryWrapper;
    }

    /**
     * 保存或更新文章正文。
     *
     * @param postId 文章ID
     * @param contentMarkdown Markdown 正文
     */
    private void saveOrUpdatePostContent(Long postId, String contentMarkdown) {
        String markdown = contentMarkdown == null ? "" : contentMarkdown.trim();
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

        postContentEntity.setContentMarkdown(markdown);
        postContentEntity.setContentHtml(markdown);
        postContentEntity.setContentPlainText(plainText);
        postContentEntity.setReadingTime(readingTime);
        postContentEntity.setUpdatedTime(LocalDateTime.now());

        if (postContentEntity.getId() == null) {
            postContentMapper.insert(postContentEntity);
            return;
        }
        postContentMapper.updateById(postContentEntity);
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
        return Math.min(pageSize, 50);
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

        AdminPostItemResponse response = new AdminPostItemResponse();
        response.setId(postEntity.getId());
        response.setTitle(postEntity.getTitle());
        response.setSlug(postEntity.getSlug());
        response.setSummary(postEntity.getSummary());
        response.setCoverUrl(postEntity.getCoverUrl());
        response.setTopic("未设置专题");
        response.setStatus(postEntity.getStatus());
        response.setCoverReady(postEntity.getCoverUrl() != null && !postEntity.getCoverUrl().isBlank());
        response.setSummaryReady(postEntity.getSummary() != null && !postEntity.getSummary().isBlank());
        response.setReadingMinutes(postContentEntity == null || postContentEntity.getReadingTime() == null ? 1 : postContentEntity.getReadingTime());
        response.setWordCount(postContentEntity == null || postContentEntity.getContentPlainText() == null ? 0 : postContentEntity.getContentPlainText().length());
        response.setContentMarkdown(postContentEntity == null ? "" : postContentEntity.getContentMarkdown());
        response.setUpdatedAt(postEntity.getUpdatedTime());
        response.setPublishedAt(postEntity.getPublishedAt());
        return response;
    }
}
