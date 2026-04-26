package com.ideaflow.yunyu.module.friendlink.admin.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.friendlink.admin.dto.AdminFriendLinkCreateRequest;
import com.ideaflow.yunyu.module.friendlink.admin.dto.AdminFriendLinkQueryRequest;
import com.ideaflow.yunyu.module.friendlink.admin.dto.AdminFriendLinkStatusUpdateRequest;
import com.ideaflow.yunyu.module.friendlink.admin.dto.AdminFriendLinkUpdateRequest;
import com.ideaflow.yunyu.module.friendlink.admin.service.AdminFriendLinkService;
import com.ideaflow.yunyu.module.friendlink.admin.vo.AdminFriendLinkItemResponse;
import com.ideaflow.yunyu.module.friendlink.admin.vo.AdminFriendLinkListResponse;
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
 * 后台友链管理控制器。
 * 作用：向站长提供友链列表、详情、创建、编辑、审核和删除接口。
 */
@Tag(name = "后台友链管理")
@RestController
@RequestMapping("/api/admin/friend-links")
public class AdminFriendLinkController {

    private final AdminFriendLinkService adminFriendLinkService;

    /**
     * 创建后台友链管理控制器。
     *
     * @param adminFriendLinkService 后台友链管理服务
     */
    public AdminFriendLinkController(AdminFriendLinkService adminFriendLinkService) {
        this.adminFriendLinkService = adminFriendLinkService;
    }

    /**
     * 查询后台友链列表。
     *
     * @param request 查询请求
     * @return 友链列表
     */
    @Operation(summary = "查询后台友链列表")
    @GetMapping
    public ApiResponse<AdminFriendLinkListResponse> listFriendLinks(AdminFriendLinkQueryRequest request) {
        return ApiResponse.success(adminFriendLinkService.listFriendLinks(request));
    }

    /**
     * 查询后台友链详情。
     *
     * @param friendLinkId 友链ID
     * @return 友链详情
     */
    @Operation(summary = "查询后台友链详情")
    @GetMapping("/{friendLinkId}")
    public ApiResponse<AdminFriendLinkItemResponse> getFriendLink(@PathVariable Long friendLinkId) {
        return ApiResponse.success(adminFriendLinkService.getFriendLink(friendLinkId));
    }

    /**
     * 创建友链。
     *
     * @param request 创建请求
     * @return 创建后的友链详情
     */
    @Operation(summary = "创建友链")
    @PostMapping
    public ApiResponse<AdminFriendLinkItemResponse> createFriendLink(
            @Valid @RequestBody AdminFriendLinkCreateRequest request) {
        return ApiResponse.success(adminFriendLinkService.createFriendLink(request));
    }

    /**
     * 更新友链。
     *
     * @param friendLinkId 友链ID
     * @param request 更新请求
     * @return 更新后的友链详情
     */
    @Operation(summary = "更新友链")
    @PutMapping("/{friendLinkId}")
    public ApiResponse<AdminFriendLinkItemResponse> updateFriendLink(@PathVariable Long friendLinkId,
                                                                     @Valid @RequestBody AdminFriendLinkUpdateRequest request) {
        return ApiResponse.success(adminFriendLinkService.updateFriendLink(friendLinkId, request));
    }

    /**
     * 快速更新友链状态。
     *
     * @param friendLinkId 友链ID
     * @param request 状态更新请求
     * @return 更新后的友链详情
     */
    @Operation(summary = "快速更新友链状态")
    @PutMapping("/{friendLinkId}/status")
    public ApiResponse<AdminFriendLinkItemResponse> updateFriendLinkStatus(
            @PathVariable Long friendLinkId,
            @Valid @RequestBody AdminFriendLinkStatusUpdateRequest request) {
        return ApiResponse.success(adminFriendLinkService.updateFriendLinkStatus(friendLinkId, request));
    }

    /**
     * 删除友链。
     *
     * @param friendLinkId 友链ID
     * @return 成功响应
     */
    @Operation(summary = "删除友链")
    @DeleteMapping("/{friendLinkId}")
    public ApiResponse<Void> deleteFriendLink(@PathVariable Long friendLinkId) {
        adminFriendLinkService.deleteFriendLink(friendLinkId);
        return ApiResponse.success();
    }
}
