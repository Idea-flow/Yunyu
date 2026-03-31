package com.ideaflow.yunyu.module.tag.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.tag.dto.AdminTagCreateRequest;
import com.ideaflow.yunyu.module.tag.dto.AdminTagQueryRequest;
import com.ideaflow.yunyu.module.tag.dto.AdminTagUpdateRequest;
import com.ideaflow.yunyu.module.tag.service.AdminTagService;
import com.ideaflow.yunyu.module.tag.vo.AdminTagItemResponse;
import com.ideaflow.yunyu.module.tag.vo.AdminTagListResponse;
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
 * 后台标签管理控制器。
 * 作用：向站长提供标签列表、详情、创建、更新和删除的后台管理接口。
 */
@Tag(name = "后台标签管理")
@RestController
@RequestMapping("/api/admin/tags")
public class AdminTagController {

    private final AdminTagService adminTagService;

    /**
     * 创建后台标签管理控制器。
     *
     * @param adminTagService 后台标签管理服务
     */
    public AdminTagController(AdminTagService adminTagService) {
        this.adminTagService = adminTagService;
    }

    /**
     * 查询后台标签列表。
     *
     * @param request 查询请求
     * @return 标签列表
     */
    @Operation(summary = "查询后台标签列表")
    @GetMapping
    public ApiResponse<AdminTagListResponse> listTags(AdminTagQueryRequest request) {
        return ApiResponse.success(adminTagService.listTags(request));
    }

    /**
     * 查询单个标签详情。
     *
     * @param tagId 标签ID
     * @return 标签详情
     */
    @Operation(summary = "查询单个标签详情")
    @GetMapping("/{tagId}")
    public ApiResponse<AdminTagItemResponse> getTag(@PathVariable Long tagId) {
        return ApiResponse.success(adminTagService.getTag(tagId));
    }

    /**
     * 创建标签。
     *
     * @param request 创建请求
     * @return 创建后的标签详情
     */
    @Operation(summary = "创建标签")
    @PostMapping
    public ApiResponse<AdminTagItemResponse> createTag(@Valid @RequestBody AdminTagCreateRequest request) {
        return ApiResponse.success(adminTagService.createTag(request));
    }

    /**
     * 更新标签。
     *
     * @param tagId 标签ID
     * @param request 更新请求
     * @return 更新后的标签详情
     */
    @Operation(summary = "更新标签")
    @PutMapping("/{tagId}")
    public ApiResponse<AdminTagItemResponse> updateTag(@PathVariable Long tagId,
                                                       @Valid @RequestBody AdminTagUpdateRequest request) {
        return ApiResponse.success(adminTagService.updateTag(tagId, request));
    }

    /**
     * 删除标签。
     *
     * @param tagId 标签ID
     * @return 成功响应
     */
    @Operation(summary = "删除标签")
    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> deleteTag(@PathVariable Long tagId) {
        adminTagService.deleteTag(tagId);
        return ApiResponse.success();
    }
}
