package com.ideaflow.yunyu.module.user.admin.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.user.admin.dto.AdminUserCreateRequest;
import com.ideaflow.yunyu.module.user.admin.dto.AdminUserQueryRequest;
import com.ideaflow.yunyu.module.user.admin.dto.AdminUserUpdateRequest;
import com.ideaflow.yunyu.module.user.admin.service.AdminUserService;
import com.ideaflow.yunyu.module.user.admin.vo.AdminUserItemResponse;
import com.ideaflow.yunyu.module.user.admin.vo.AdminUserListResponse;
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
 * 后台用户管理控制器。
 * 作用：向站长提供用户列表、用户详情、创建用户、编辑用户和删除用户的后台管理接口。
 */
@Tag(name = "后台用户管理")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 创建后台用户管理控制器。
     *
     * @param adminUserService 后台用户管理服务
     */
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 查询后台用户列表。
     *
     * @param request 查询请求
     * @return 用户列表
     */
    @Operation(summary = "查询后台用户列表")
    @GetMapping
    public ApiResponse<AdminUserListResponse> listUsers(AdminUserQueryRequest request) {
        return ApiResponse.success(adminUserService.listUsers(request));
    }

    /**
     * 查询单个用户详情。
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    @Operation(summary = "查询单个用户详情")
    @GetMapping("/{userId}")
    public ApiResponse<AdminUserItemResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.success(adminUserService.getUser(userId));
    }

    /**
     * 创建用户。
     *
     * @param request 创建请求
     * @return 创建后的用户详情
     */
    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<AdminUserItemResponse> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return ApiResponse.success(adminUserService.createUser(request));
    }

    /**
     * 更新用户。
     *
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户详情
     */
    @Operation(summary = "更新用户")
    @PutMapping("/{userId}")
    public ApiResponse<AdminUserItemResponse> updateUser(@PathVariable Long userId,
                                                         @Valid @RequestBody AdminUserUpdateRequest request) {
        return ApiResponse.success(adminUserService.updateUser(userId, request));
    }

    /**
     * 删除用户。
     *
     * @param userId 用户ID
     * @return 成功响应
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ApiResponse.success();
    }
}
