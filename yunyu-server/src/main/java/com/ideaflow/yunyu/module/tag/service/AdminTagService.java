package com.ideaflow.yunyu.module.tag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.tag.dto.AdminTagCreateRequest;
import com.ideaflow.yunyu.module.tag.dto.AdminTagQueryRequest;
import com.ideaflow.yunyu.module.tag.dto.AdminTagUpdateRequest;
import com.ideaflow.yunyu.module.tag.entity.TagEntity;
import com.ideaflow.yunyu.module.tag.mapper.TagMapper;
import com.ideaflow.yunyu.module.tag.vo.AdminTagItemResponse;
import com.ideaflow.yunyu.module.tag.vo.AdminTagListResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台标签管理服务类。
 * 作用：处理后台标签的查询、创建、更新和删除逻辑，作为标签管理接口的核心业务层。
 */
@Service
public class AdminTagService {

    private final TagMapper tagMapper;

    /**
     * 创建后台标签管理服务。
     *
     * @param tagMapper 标签 Mapper
     */
    public AdminTagService(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    /**
     * 查询后台标签列表。
     *
     * @param request 查询请求
     * @return 标签列表响应
     */
    public AdminTagListResponse listTags(AdminTagQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        long total = tagMapper.selectCount(buildTagListQuery(request));
        long offset = (pageNo - 1) * pageSize;

        List<AdminTagItemResponse> items = tagMapper.selectList(buildTagListQuery(request)
                        .last("LIMIT " + offset + "," + pageSize))
                .stream()
                .map(this::toAdminTagItemResponse)
                .toList();

        long totalPages = total == 0 ? 1 : (long) Math.ceil((double) total / pageSize);
        return new AdminTagListResponse(items, total, pageNo, pageSize, totalPages);
    }

    /**
     * 查询单个标签详情。
     *
     * @param tagId 标签ID
     * @return 标签详情
     */
    public AdminTagItemResponse getTag(Long tagId) {
        return toAdminTagItemResponse(findTagOrThrow(tagId));
    }

    /**
     * 创建标签。
     *
     * @param request 创建请求
     * @return 创建后的标签详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminTagItemResponse createTag(AdminTagCreateRequest request) {
        String name = request.getName().trim();
        String slug = normalizeSlug(request.getSlug());
        ensureNameUnique(name, null);
        ensureSlugUnique(slug, null);

        TagEntity tagEntity = new TagEntity();
        tagEntity.setName(name);
        tagEntity.setSlug(slug);
        tagEntity.setDescription(normalizeOptionalValue(request.getDescription()));
        tagEntity.setStatus(resolveStatus(request.getStatus()));
        tagEntity.setCreatedTime(LocalDateTime.now());
        tagEntity.setUpdatedTime(LocalDateTime.now());
        tagEntity.setDeleted(0);
        tagMapper.insert(tagEntity);

        return getTag(tagEntity.getId());
    }

    /**
     * 更新标签。
     *
     * @param tagId 标签ID
     * @param request 更新请求
     * @return 更新后的标签详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminTagItemResponse updateTag(Long tagId, AdminTagUpdateRequest request) {
        TagEntity tagEntity = findTagOrThrow(tagId);
        String name = request.getName().trim();
        String slug = normalizeSlug(request.getSlug());
        ensureNameUnique(name, tagId);
        ensureSlugUnique(slug, tagId);

        tagEntity.setName(name);
        tagEntity.setSlug(slug);
        tagEntity.setDescription(normalizeOptionalValue(request.getDescription()));
        tagEntity.setStatus(resolveStatus(request.getStatus()));
        tagEntity.setUpdatedTime(LocalDateTime.now());
        tagMapper.updateById(tagEntity);

        return getTag(tagId);
    }

    /**
     * 删除标签。
     *
     * @param tagId 标签ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId) {
        findTagOrThrow(tagId);
        tagMapper.update(null, new LambdaUpdateWrapper<TagEntity>()
                .eq(TagEntity::getId, tagId)
                .eq(TagEntity::getDeleted, 0)
                .set(TagEntity::getDeleted, 1)
                .set(TagEntity::getUpdatedTime, LocalDateTime.now()));
    }

    /**
     * 构建标签列表查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<TagEntity> buildTagListQuery(AdminTagQueryRequest request) {
        LambdaQueryWrapper<TagEntity> queryWrapper = new LambdaQueryWrapper<TagEntity>()
                .eq(TagEntity::getDeleted, 0)
                .orderByDesc(TagEntity::getCreatedTime)
                .orderByDesc(TagEntity::getId);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(TagEntity::getName, keyword)
                    .or()
                    .like(TagEntity::getSlug, keyword)
                    .or()
                    .like(TagEntity::getDescription, keyword));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            queryWrapper.eq(TagEntity::getStatus, request.getStatus().trim());
        }

        return queryWrapper;
    }

    /**
     * 查询标签，不存在时抛出异常。
     *
     * @param tagId 标签ID
     * @return 标签实体
     */
    private TagEntity findTagOrThrow(Long tagId) {
        TagEntity tagEntity = tagMapper.selectOne(new LambdaQueryWrapper<TagEntity>()
                .eq(TagEntity::getId, tagId)
                .eq(TagEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (tagEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "标签不存在");
        }
        return tagEntity;
    }

    /**
     * 校验标签名称唯一性。
     *
     * @param name 名称
     * @param excludeTagId 排除的标签ID
     */
    private void ensureNameUnique(String name, Long excludeTagId) {
        LambdaQueryWrapper<TagEntity> queryWrapper = new LambdaQueryWrapper<TagEntity>()
                .eq(TagEntity::getName, name)
                .eq(TagEntity::getDeleted, 0);
        if (excludeTagId != null) {
            queryWrapper.ne(TagEntity::getId, excludeTagId);
        }

        Long count = tagMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该标签名称已存在");
        }
    }

    /**
     * 校验标签 slug 唯一性。
     *
     * @param slug slug
     * @param excludeTagId 排除的标签ID
     */
    private void ensureSlugUnique(String slug, Long excludeTagId) {
        LambdaQueryWrapper<TagEntity> queryWrapper = new LambdaQueryWrapper<TagEntity>()
                .eq(TagEntity::getSlug, slug)
                .eq(TagEntity::getDeleted, 0);
        if (excludeTagId != null) {
            queryWrapper.ne(TagEntity::getId, excludeTagId);
        }

        Long count = tagMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该标签 Slug 已存在");
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
        return Math.min(pageSize, 50);
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
     * 转换后台标签响应对象。
     *
     * @param tagEntity 标签实体
     * @return 后台标签响应对象
     */
    private AdminTagItemResponse toAdminTagItemResponse(TagEntity tagEntity) {
        Long relatedPostCount = tagMapper.countRelatedPostByTagId(tagEntity.getId());

        AdminTagItemResponse response = new AdminTagItemResponse();
        response.setId(tagEntity.getId());
        response.setName(tagEntity.getName());
        response.setSlug(tagEntity.getSlug());
        response.setDescription(tagEntity.getDescription());
        response.setCoverUrl("");
        response.setStatus(tagEntity.getStatus());
        response.setSortOrder(0);
        response.setRelatedPostCount(relatedPostCount == null ? 0L : relatedPostCount);
        response.setCreatedTime(tagEntity.getCreatedTime());
        response.setUpdatedTime(tagEntity.getUpdatedTime());
        return response;
    }
}
