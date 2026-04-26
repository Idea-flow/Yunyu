package com.ideaflow.yunyu.module.category.site.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.category.entity.CategoryEntity;
import com.ideaflow.yunyu.module.category.mapper.CategoryMapper;
import com.ideaflow.yunyu.module.category.site.vo.SiteCategoryDetailResponse;
import com.ideaflow.yunyu.module.category.site.vo.SiteCategoryItemResponse;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.post.site.dto.SitePostQueryRequest;
import com.ideaflow.yunyu.module.post.site.service.PostSiteService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 前台分类服务类。
 * 作用：承接前台分类列表与分类详情查询，并复用前台文章服务完成分类页文章分页。
 */
@Service
public class CategorySiteService {

    private final CategoryMapper categoryMapper;
    private final PostMapper postMapper;
    private final PostSiteService postSiteService;

    /**
     * 创建前台分类服务。
     *
     * @param categoryMapper 分类 Mapper
     * @param postMapper 文章 Mapper
     * @param postSiteService 前台文章服务
     */
    public CategorySiteService(CategoryMapper categoryMapper,
                               PostMapper postMapper,
                               PostSiteService postSiteService) {
        this.categoryMapper = categoryMapper;
        this.postMapper = postMapper;
        this.postSiteService = postSiteService;
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
        response.setPosts(postSiteService.listPosts(actualRequest));
        return response;
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
     * 统计分类下已发布文章数量。
     *
     * @param categoryId 分类 ID
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
     * 返回默认字符串。
     *
     * @param value 原始值
     * @return 默认化后的字符串
     */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
