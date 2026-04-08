package com.ideaflow.yunyu.module.comment.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.comment.dto.AdminCommentQueryRequest;
import com.ideaflow.yunyu.module.comment.dto.AdminCommentStatusUpdateRequest;
import com.ideaflow.yunyu.module.comment.service.AdminCommentService;
import com.ideaflow.yunyu.module.comment.vo.AdminCommentItemResponse;
import com.ideaflow.yunyu.module.comment.vo.AdminCommentListResponse;
import com.ideaflow.yunyu.module.comment.vo.AdminCommentThreadGroupListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台评论管理控制器。
 * 作用：向站长提供评论列表查询、审核状态调整与删除操作接口。
 */
@Tag(name = "后台评论管理")
@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    /**
     * 创建后台评论管理控制器。
     *
     * @param adminCommentService 后台评论管理服务
     */
    public AdminCommentController(AdminCommentService adminCommentService) {
        this.adminCommentService = adminCommentService;
    }

    /**
     * 查询后台评论列表。
     *
     * @param request 查询请求
     * @return 评论列表
     */
    @Operation(summary = "查询后台评论列表")
    @GetMapping
    public ApiResponse<AdminCommentListResponse> listComments(AdminCommentQueryRequest request) {
        return ApiResponse.success(adminCommentService.listComments(request));
    }

    /**
     * 查询后台评论树形审核列表。
     *
     * @param request 查询请求
     * @return 按文章分组的评论树形审核列表
     */
    @Operation(summary = "查询后台评论树形审核列表")
    @GetMapping("/thread-groups")
    public ApiResponse<AdminCommentThreadGroupListResponse> listCommentThreadGroups(AdminCommentQueryRequest request) {
        return ApiResponse.success(adminCommentService.listCommentThreadGroups(request));
    }

    /**
     * 查询单条评论详情。
     *
     * @param commentId 评论ID
     * @return 评论详情
     */
    @Operation(summary = "查询单条评论详情")
    @GetMapping("/{commentId}")
    public ApiResponse<AdminCommentItemResponse> getComment(@PathVariable Long commentId) {
        return ApiResponse.success(adminCommentService.getComment(commentId));
    }

    /**
     * 更新评论状态。
     *
     * @param commentId 评论ID
     * @param request 状态请求
     * @return 更新后的评论详情
     */
    @Operation(summary = "更新评论状态")
    @PutMapping("/{commentId}/status")
    public ApiResponse<AdminCommentItemResponse> updateCommentStatus(@PathVariable Long commentId,
                                                                     @Valid @RequestBody AdminCommentStatusUpdateRequest request) {
        return ApiResponse.success(adminCommentService.updateCommentStatus(commentId, request.getStatus()));
    }

    /**
     * 删除评论。
     *
     * @param commentId 评论ID
     * @return 成功响应
     */
    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
        adminCommentService.deleteComment(commentId);
        return ApiResponse.success();
    }
}
