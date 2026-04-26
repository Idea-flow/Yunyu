package com.ideaflow.yunyu.module.topic.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.topic.admin.dto.AdminTopicCreateRequest;
import com.ideaflow.yunyu.module.topic.admin.dto.AdminTopicQueryRequest;
import com.ideaflow.yunyu.module.topic.admin.dto.AdminTopicUpdateRequest;
import com.ideaflow.yunyu.module.topic.entity.TopicEntity;
import com.ideaflow.yunyu.module.topic.mapper.TopicMapper;
import com.ideaflow.yunyu.module.topic.admin.vo.AdminTopicItemResponse;
import com.ideaflow.yunyu.module.topic.admin.vo.AdminTopicListResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台专题管理服务类。
 * 作用：处理后台专题的查询、创建、更新和删除逻辑，作为专题管理接口的核心业务层。
 */
@Service
public class AdminTopicService {

    private final TopicMapper topicMapper;

    /**
     * 创建后台专题管理服务。
     *
     * @param topicMapper 专题 Mapper
     */
    public AdminTopicService(TopicMapper topicMapper) {
        this.topicMapper = topicMapper;
    }

    /**
     * 查询后台专题列表。
     *
     * @param request 查询请求
     * @return 专题列表响应
     */
    public AdminTopicListResponse listTopics(AdminTopicQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        Page<TopicEntity> page = topicMapper.selectPage(new Page<>(pageNo, pageSize), buildTopicListQuery(request));

        List<AdminTopicItemResponse> items = page.getRecords()
                .stream()
                .map(this::toAdminTopicItemResponse)
                .toList();

        long totalPages = page.getPages() <= 0 ? 1 : page.getPages();
        return new AdminTopicListResponse(items, page.getTotal(), pageNo, pageSize, totalPages);
    }

    /**
     * 查询单个专题详情。
     *
     * @param topicId 专题ID
     * @return 专题详情
     */
    public AdminTopicItemResponse getTopic(Long topicId) {
        return toAdminTopicItemResponse(findTopicOrThrow(topicId));
    }

    /**
     * 创建专题。
     *
     * @param request 创建请求
     * @return 创建后的专题详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminTopicItemResponse createTopic(AdminTopicCreateRequest request) {
        String name = request.getName().trim();
        String slug = normalizeSlug(request.getSlug());
        ensureNameUnique(name, null);
        ensureSlugUnique(slug, null);

        TopicEntity topicEntity = new TopicEntity();
        topicEntity.setName(name);
        topicEntity.setSlug(slug);
        topicEntity.setSummary(normalizeOptionalValue(request.getDescription()));
        topicEntity.setCoverUrl(normalizeOptionalValue(request.getCoverUrl()));
        topicEntity.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        topicEntity.setStatus(resolveStatus(request.getStatus()));
        topicEntity.setCreatedTime(LocalDateTime.now());
        topicEntity.setUpdatedTime(LocalDateTime.now());
        topicEntity.setDeleted(0);
        topicMapper.insert(topicEntity);

        return getTopic(topicEntity.getId());
    }

    /**
     * 更新专题。
     *
     * @param topicId 专题ID
     * @param request 更新请求
     * @return 更新后的专题详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminTopicItemResponse updateTopic(Long topicId, AdminTopicUpdateRequest request) {
        TopicEntity topicEntity = findTopicOrThrow(topicId);
        String name = request.getName().trim();
        String slug = normalizeSlug(request.getSlug());
        ensureNameUnique(name, topicId);
        ensureSlugUnique(slug, topicId);

        topicEntity.setName(name);
        topicEntity.setSlug(slug);
        topicEntity.setSummary(normalizeOptionalValue(request.getDescription()));
        topicEntity.setCoverUrl(normalizeOptionalValue(request.getCoverUrl()));
        topicEntity.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        topicEntity.setStatus(resolveStatus(request.getStatus()));
        topicEntity.setUpdatedTime(LocalDateTime.now());
        topicMapper.updateById(topicEntity);

        return getTopic(topicId);
    }

    /**
     * 删除专题。
     *
     * @param topicId 专题ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long topicId) {
        findTopicOrThrow(topicId);
        topicMapper.update(null, new LambdaUpdateWrapper<TopicEntity>()
                .eq(TopicEntity::getId, topicId)
                .eq(TopicEntity::getDeleted, 0)
                .set(TopicEntity::getDeleted, 1)
                .set(TopicEntity::getUpdatedTime, LocalDateTime.now()));
    }

    /**
     * 构建专题列表查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<TopicEntity> buildTopicListQuery(AdminTopicQueryRequest request) {
        LambdaQueryWrapper<TopicEntity> queryWrapper = new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getDeleted, 0)
                .orderByAsc(TopicEntity::getSortOrder)
                .orderByDesc(TopicEntity::getId);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(TopicEntity::getName, keyword)
                    .or()
                    .like(TopicEntity::getSlug, keyword)
                    .or()
                    .like(TopicEntity::getSummary, keyword));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            queryWrapper.eq(TopicEntity::getStatus, request.getStatus().trim());
        }

        return queryWrapper;
    }

    /**
     * 查询专题，不存在时抛出异常。
     *
     * @param topicId 专题ID
     * @return 专题实体
     */
    private TopicEntity findTopicOrThrow(Long topicId) {
        TopicEntity topicEntity = topicMapper.selectOne(new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getId, topicId)
                .eq(TopicEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (topicEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "专题不存在");
        }
        return topicEntity;
    }

    /**
     * 校验专题名称唯一性。
     *
     * @param name 名称
     * @param excludeTopicId 排除的专题ID
     */
    private void ensureNameUnique(String name, Long excludeTopicId) {
        LambdaQueryWrapper<TopicEntity> queryWrapper = new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getName, name)
                .eq(TopicEntity::getDeleted, 0);
        if (excludeTopicId != null) {
            queryWrapper.ne(TopicEntity::getId, excludeTopicId);
        }

        Long count = topicMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该专题名称已存在");
        }
    }

    /**
     * 校验专题 slug 唯一性。
     *
     * @param slug slug
     * @param excludeTopicId 排除的专题ID
     */
    private void ensureSlugUnique(String slug, Long excludeTopicId) {
        LambdaQueryWrapper<TopicEntity> queryWrapper = new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getSlug, slug)
                .eq(TopicEntity::getDeleted, 0);
        if (excludeTopicId != null) {
            queryWrapper.ne(TopicEntity::getId, excludeTopicId);
        }

        Long count = topicMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该专题 Slug 已存在");
        }
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
     * 标准化 slug。
     *
     * @param slug 原始 slug
     * @return 标准化后的 slug
     */
    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase();
    }

    /**
     * 标准化可选字段。
     *
     * @param value 原始值
     * @return 标准化后的值
     */
    private String normalizeOptionalValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 标准化排序值。
     *
     * @param sortOrder 原始排序值
     * @return 排序值
     */
    private Integer normalizeSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            return 0;
        }
        return sortOrder;
    }

    /**
     * 解析状态。
     *
     * @param status 原始状态
     * @return 状态
     */
    private String resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.trim();
    }

    /**
     * 转换后台专题响应对象。
     *
     * @param topicEntity 专题实体
     * @return 后台专题响应对象
     */
    private AdminTopicItemResponse toAdminTopicItemResponse(TopicEntity topicEntity) {
        Long relatedPostCount = topicMapper.countRelatedPostByTopicId(topicEntity.getId());

        AdminTopicItemResponse response = new AdminTopicItemResponse();
        response.setId(topicEntity.getId());
        response.setName(topicEntity.getName());
        response.setSlug(topicEntity.getSlug());
        response.setDescription(topicEntity.getSummary());
        response.setCoverUrl(topicEntity.getCoverUrl());
        response.setStatus(topicEntity.getStatus());
        response.setSortOrder(topicEntity.getSortOrder());
        response.setRelatedPostCount(relatedPostCount == null ? 0L : relatedPostCount);
        response.setCreatedTime(topicEntity.getCreatedTime());
        response.setUpdatedTime(topicEntity.getUpdatedTime());
        return response;
    }
}
