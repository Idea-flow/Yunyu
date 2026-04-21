package com.ideaflow.yunyu.module.attachment.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.attachment.dto.AdminAttachmentCompleteRequest;
import com.ideaflow.yunyu.module.attachment.dto.AdminAttachmentPresignRequest;
import com.ideaflow.yunyu.module.attachment.dto.AdminAttachmentQueryRequest;
import com.ideaflow.yunyu.module.attachment.service.AdminAttachmentService;
import com.ideaflow.yunyu.module.attachment.vo.AdminAttachmentItemResponse;
import com.ideaflow.yunyu.module.attachment.vo.AdminAttachmentListResponse;
import com.ideaflow.yunyu.module.attachment.vo.AdminAttachmentPresignResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台附件管理控制器。
 * 作用：向后台上传流程提供预签名、完成回执、列表查询和删除接口。
 */
@Tag(name = "后台附件管理")
@RestController
@RequestMapping("/api/admin/attachments")
public class AdminAttachmentController {

    private final AdminAttachmentService adminAttachmentService;

    /**
     * 创建后台附件管理控制器。
     *
     * @param adminAttachmentService 后台附件管理服务
     */
    public AdminAttachmentController(AdminAttachmentService adminAttachmentService) {
        this.adminAttachmentService = adminAttachmentService;
    }

    /**
     * 生成上传预签名。
     *
     * @param request 预签名请求
     * @return 预签名响应
     */
    @Operation(summary = "生成上传预签名")
    @PostMapping("/presign")
    public ApiResponse<AdminAttachmentPresignResponse> presign(@Valid @RequestBody AdminAttachmentPresignRequest request) {
        return ApiResponse.success(adminAttachmentService.createPresign(request));
    }

    /**
     * 提交上传完成回执。
     *
     * @param request 上传完成请求
     * @return 附件条目
     */
    @Operation(summary = "提交上传完成回执")
    @PostMapping("/complete")
    public ApiResponse<AdminAttachmentItemResponse> complete(@Valid @RequestBody AdminAttachmentCompleteRequest request) {
        return ApiResponse.success(adminAttachmentService.completeUpload(request));
    }

    /**
     * 查询附件列表。
     *
     * @param request 查询请求
     * @return 附件列表
     */
    @Operation(summary = "查询附件列表")
    @GetMapping
    public ApiResponse<AdminAttachmentListResponse> list(AdminAttachmentQueryRequest request) {
        return ApiResponse.success(adminAttachmentService.listAttachments(request));
    }

    /**
     * 删除附件。
     *
     * @param attachmentId 附件ID
     * @return 成功响应
     */
    @Operation(summary = "删除附件")
    @DeleteMapping("/{attachmentId}")
    public ApiResponse<Void> delete(@PathVariable Long attachmentId) {
        adminAttachmentService.deleteAttachment(attachmentId);
        return ApiResponse.success();
    }
}
