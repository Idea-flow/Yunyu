package com.ideaflow.yunyu.module.category.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.category.dto.AdminCategoryCreateRequest;
import com.ideaflow.yunyu.module.category.dto.AdminCategoryQueryRequest;
import com.ideaflow.yunyu.module.category.dto.AdminCategoryUpdateRequest;
import com.ideaflow.yunyu.module.category.service.AdminCategoryService;
import com.ideaflow.yunyu.module.category.vo.AdminCategoryItemResponse;
import com.ideaflow.yunyu.module.category.vo.AdminCategoryListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台分类管理控制器。
 * 作用：向站长提供分类列表、详情、创建、更新和删除的后台管理接口。
 */
@Tag(name = "后台分类管理")
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    /**
     * 创建后台分类管理控制器。
     *
     * @param adminCategoryService 后台分类管理服务
     */
    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    /**
     * 查询后台分类列表。
     *
     * @param request 查询请求
     * @return 分类列表
     */
    @Operation(summary = "查询后台分类列表")
    @GetMapping
    public ApiResponse<AdminCategoryListResponse> listCategories(AdminCategoryQueryRequest request) {
        return ApiResponse.success(adminCategoryService.listCategories(request));
    }

    /**
     * 查询单个分类详情。
     *
     * @param categoryId 分类ID
     * @return 分类详情
     */
    @Operation(summary = "查询单个分类详情")
    @GetMapping("/{categoryId}")
    public ApiResponse<AdminCategoryItemResponse> getCategory(@PathVariable Long categoryId) {
        return ApiResponse.success(adminCategoryService.getCategory(categoryId));
    }

    /**
     * 创建分类。
     *
     * @param request 创建请求
     * @return 创建后的分类详情
     */
    @Operation(summary = "创建分类")
    @PostMapping
    public ApiResponse<AdminCategoryItemResponse> createCategory(@Valid @RequestBody AdminCategoryCreateRequest request) {
        return ApiResponse.success(adminCategoryService.createCategory(request));
    }

    /**
     * 更新分类。
     *
     * @param categoryId 分类ID
     * @param request 更新请求
     * @return 更新后的分类详情
     */
    @Operation(summary = "更新分类")
    @PutMapping("/{categoryId}")
    public ApiResponse<AdminCategoryItemResponse> updateCategory(@PathVariable Long categoryId,
                                                                 @Valid @RequestBody AdminCategoryUpdateRequest request) {
        return ApiResponse.success(adminCategoryService.updateCategory(categoryId, request));
    }

    /**
     * 删除分类。
     *
     * @param categoryId 分类ID
     * @return 成功响应
     */
    @Operation(summary = "删除分类")
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long categoryId) {
        adminCategoryService.deleteCategory(categoryId);
        return ApiResponse.success();
    }
}
