package com.ideaflow.yunyu.module.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.category.dto.AdminCategoryCreateRequest;
import com.ideaflow.yunyu.module.category.dto.AdminCategoryQueryRequest;
import com.ideaflow.yunyu.module.category.dto.AdminCategoryUpdateRequest;
import com.ideaflow.yunyu.module.category.entity.CategoryEntity;
import com.ideaflow.yunyu.module.category.mapper.CategoryMapper;
import com.ideaflow.yunyu.module.category.vo.AdminCategoryItemResponse;
import com.ideaflow.yunyu.module.category.vo.AdminCategoryListResponse;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台分类管理服务类。
 * 作用：处理后台分类的查询、创建、更新和删除逻辑，作为分类管理接口的核心业务层。
 */
@Service
public class AdminCategoryService {

    private final CategoryMapper categoryMapper;
    private final PostMapper postMapper;

    /**
     * 创建后台分类管理服务。
     *
     * @param categoryMapper 分类 Mapper
     * @param postMapper 文章 Mapper
     */
    public AdminCategoryService(CategoryMapper categoryMapper, PostMapper postMapper) {
        this.categoryMapper = categoryMapper;
        this.postMapper = postMapper;
    }

    /**
     * 查询后台分类列表。
     *
     * @param request 查询请求
     * @return 分类列表响应
     */
    public AdminCategoryListResponse listCategories(AdminCategoryQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        Page<CategoryEntity> page = categoryMapper.selectPage(new Page<>(pageNo, pageSize), buildCategoryListQuery(request));

        List<AdminCategoryItemResponse> items = page.getRecords()
                .stream()
                .map(this::toAdminCategoryItemResponse)
                .toList();

        long totalPages = page.getPages() <= 0 ? 1 : page.getPages();
        return new AdminCategoryListResponse(items, page.getTotal(), pageNo, pageSize, totalPages);
    }

    /**
     * 查询单个分类详情。
     *
     * @param categoryId 分类ID
     * @return 分类详情
     */
    public AdminCategoryItemResponse getCategory(Long categoryId) {
        return toAdminCategoryItemResponse(findCategoryOrThrow(categoryId));
    }

    /**
     * 创建分类。
     *
     * @param request 创建请求
     * @return 创建后的分类详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminCategoryItemResponse createCategory(AdminCategoryCreateRequest request) {
        String name = request.getName().trim();
        String slug = normalizeSlug(request.getSlug());
        ensureNameUnique(name, null);
        ensureSlugUnique(slug, null);

        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName(name);
        categoryEntity.setSlug(slug);
        categoryEntity.setDescription(normalizeOptionalValue(request.getDescription()));
        categoryEntity.setCoverUrl(normalizeOptionalValue(request.getCoverUrl()));
        categoryEntity.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        categoryEntity.setStatus(resolveStatus(request.getStatus()));
        categoryEntity.setCreatedTime(LocalDateTime.now());
        categoryEntity.setUpdatedTime(LocalDateTime.now());
        categoryEntity.setDeleted(0);
        categoryMapper.insert(categoryEntity);

        return getCategory(categoryEntity.getId());
    }

    /**
     * 更新分类。
     *
     * @param categoryId 分类ID
     * @param request 更新请求
     * @return 更新后的分类详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminCategoryItemResponse updateCategory(Long categoryId, AdminCategoryUpdateRequest request) {
        CategoryEntity categoryEntity = findCategoryOrThrow(categoryId);
        String name = request.getName().trim();
        String slug = normalizeSlug(request.getSlug());
        ensureNameUnique(name, categoryId);
        ensureSlugUnique(slug, categoryId);

        categoryEntity.setName(name);
        categoryEntity.setSlug(slug);
        categoryEntity.setDescription(normalizeOptionalValue(request.getDescription()));
        categoryEntity.setCoverUrl(normalizeOptionalValue(request.getCoverUrl()));
        categoryEntity.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        categoryEntity.setStatus(resolveStatus(request.getStatus()));
        categoryEntity.setUpdatedTime(LocalDateTime.now());
        categoryMapper.updateById(categoryEntity);

        return getCategory(categoryId);
    }

    /**
     * 删除分类。
     *
     * @param categoryId 分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        findCategoryOrThrow(categoryId);
        categoryMapper.update(null, new LambdaUpdateWrapper<CategoryEntity>()
                .eq(CategoryEntity::getId, categoryId)
                .eq(CategoryEntity::getDeleted, 0)
                .set(CategoryEntity::getDeleted, 1)
                .set(CategoryEntity::getUpdatedTime, LocalDateTime.now()));
    }

    /**
     * 构建分类列表查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<CategoryEntity> buildCategoryListQuery(AdminCategoryQueryRequest request) {
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getDeleted, 0)
                .orderByAsc(CategoryEntity::getSortOrder)
                .orderByDesc(CategoryEntity::getId);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(CategoryEntity::getName, keyword)
                    .or()
                    .like(CategoryEntity::getSlug, keyword)
                    .or()
                    .like(CategoryEntity::getDescription, keyword));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            queryWrapper.eq(CategoryEntity::getStatus, request.getStatus().trim());
        }

        return queryWrapper;
    }

    /**
     * 查询分类，不存在时抛出异常。
     *
     * @param categoryId 分类ID
     * @return 分类实体
     */
    private CategoryEntity findCategoryOrThrow(Long categoryId) {
        CategoryEntity categoryEntity = categoryMapper.selectOne(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getId, categoryId)
                .eq(CategoryEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (categoryEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "分类不存在");
        }
        return categoryEntity;
    }

    /**
     * 校验分类名称唯一性。
     *
     * @param name 名称
     * @param excludeCategoryId 排除的分类ID
     */
    private void ensureNameUnique(String name, Long excludeCategoryId) {
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getName, name)
                .eq(CategoryEntity::getDeleted, 0);
        if (excludeCategoryId != null) {
            queryWrapper.ne(CategoryEntity::getId, excludeCategoryId);
        }

        Long count = categoryMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该分类名称已存在");
        }
    }

    /**
     * 校验分类 slug 唯一性。
     *
     * @param slug slug
     * @param excludeCategoryId 排除的分类ID
     */
    private void ensureSlugUnique(String slug, Long excludeCategoryId) {
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getSlug, slug)
                .eq(CategoryEntity::getDeleted, 0);
        if (excludeCategoryId != null) {
            queryWrapper.ne(CategoryEntity::getId, excludeCategoryId);
        }

        Long count = categoryMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该分类 Slug 已存在");
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
     * 转换后台分类响应对象。
     *
     * @param categoryEntity 分类实体
     * @return 后台分类响应对象
     */
    private AdminCategoryItemResponse toAdminCategoryItemResponse(CategoryEntity categoryEntity) {
        Long relatedPostCount = postMapper.selectCount(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getCategoryId, categoryEntity.getId())
                .eq(PostEntity::getDeleted, 0));

        AdminCategoryItemResponse response = new AdminCategoryItemResponse();
        response.setId(categoryEntity.getId());
        response.setName(categoryEntity.getName());
        response.setSlug(categoryEntity.getSlug());
        response.setDescription(categoryEntity.getDescription());
        response.setCoverUrl(categoryEntity.getCoverUrl());
        response.setStatus(categoryEntity.getStatus());
        response.setSortOrder(categoryEntity.getSortOrder());
        response.setRelatedPostCount(relatedPostCount == null ? 0L : relatedPostCount);
        response.setCreatedTime(categoryEntity.getCreatedTime());
        response.setUpdatedTime(categoryEntity.getUpdatedTime());
        return response;
    }
}
