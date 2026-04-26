package com.ideaflow.yunyu.module.category.site.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.category.site.service.CategorySiteService;
import com.ideaflow.yunyu.module.category.site.vo.SiteCategoryDetailResponse;
import com.ideaflow.yunyu.module.category.site.vo.SiteCategoryItemResponse;
import com.ideaflow.yunyu.module.post.site.dto.SitePostQueryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台分类控制器。
 * 作用：承接前台分类列表与分类详情查询接口。
 */
@Tag(name = "前台分类接口")
@RestController
@RequestMapping("/api/site/categories")
public class CategorySiteController {

    private final CategorySiteService categorySiteService;

    /**
     * 创建前台分类控制器。
     *
     * @param categorySiteService 前台分类服务
     */
    public CategorySiteController(CategorySiteService categorySiteService) {
        this.categorySiteService = categorySiteService;
    }

    /**
     * 查询前台分类列表。
     *
     * @return 分类列表
     */
    @Operation(summary = "查询前台分类列表")
    @GetMapping
    public ApiResponse<List<SiteCategoryItemResponse>> listCategories() {
        return ApiResponse.success(categorySiteService.listCategories());
    }

    /**
     * 查询前台分类详情。
     *
     * @param slug 分类 slug
     * @param request 查询请求
     * @return 分类详情
     */
    @Operation(summary = "查询前台分类详情")
    @GetMapping("/{slug}")
    public ApiResponse<SiteCategoryDetailResponse> getCategoryDetail(@PathVariable String slug,
                                                                     SitePostQueryRequest request) {
        return ApiResponse.success(categorySiteService.getCategoryDetail(slug, request));
    }
}
