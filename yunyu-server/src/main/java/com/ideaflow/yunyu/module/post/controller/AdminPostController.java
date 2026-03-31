package com.ideaflow.yunyu.module.post.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.post.dto.AdminPostCreateRequest;
import com.ideaflow.yunyu.module.post.dto.AdminPostQueryRequest;
import com.ideaflow.yunyu.module.post.dto.AdminPostUpdateRequest;
import com.ideaflow.yunyu.module.post.service.AdminPostService;
import com.ideaflow.yunyu.module.post.vo.AdminPostItemResponse;
import com.ideaflow.yunyu.module.post.vo.AdminPostListResponse;
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
 * 后台文章管理控制器。
 * 作用：向站长提供后台文章列表、详情、创建、更新和删除接口。
 */
@Tag(name = "后台文章管理")
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final AdminPostService adminPostService;

    /**
     * 创建后台文章管理控制器。
     *
     * @param adminPostService 后台文章管理服务
     */
    public AdminPostController(AdminPostService adminPostService) {
        this.adminPostService = adminPostService;
    }

    /**
     * 查询后台文章列表。
     *
     * @param request 查询请求
     * @return 文章列表
     */
    @Operation(summary = "查询后台文章列表")
    @GetMapping
    public ApiResponse<AdminPostListResponse> listPosts(AdminPostQueryRequest request) {
        return ApiResponse.success(adminPostService.listPosts(request));
    }

    /**
     * 查询后台文章详情。
     *
     * @param postId 文章ID
     * @return 文章详情
     */
    @Operation(summary = "查询后台文章详情")
    @GetMapping("/{postId}")
    public ApiResponse<AdminPostItemResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.success(adminPostService.getPost(postId));
    }

    /**
     * 创建文章。
     *
     * @param request 创建请求
     * @return 创建后的文章详情
     */
    @Operation(summary = "创建文章")
    @PostMapping
    public ApiResponse<AdminPostItemResponse> createPost(@Valid @RequestBody AdminPostCreateRequest request) {
        return ApiResponse.success(adminPostService.createPost(request));
    }

    /**
     * 更新文章。
     *
     * @param postId 文章ID
     * @param request 更新请求
     * @return 更新后的文章详情
     */
    @Operation(summary = "更新文章")
    @PutMapping("/{postId}")
    public ApiResponse<AdminPostItemResponse> updatePost(@PathVariable Long postId,
                                                         @Valid @RequestBody AdminPostUpdateRequest request) {
        return ApiResponse.success(adminPostService.updatePost(postId, request));
    }

    /**
     * 删除文章。
     *
     * @param postId 文章ID
     * @return 成功响应
     */
    @Operation(summary = "删除文章")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId) {
        adminPostService.deletePost(postId);
        return ApiResponse.success();
    }
}
