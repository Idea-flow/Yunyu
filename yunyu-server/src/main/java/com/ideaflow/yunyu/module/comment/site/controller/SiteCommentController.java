package com.ideaflow.yunyu.module.comment.site.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.comment.site.dto.SiteCommentCreateRequest;
import com.ideaflow.yunyu.module.comment.site.dto.SiteCommentQueryRequest;
import com.ideaflow.yunyu.module.comment.site.service.SiteCommentService;
import com.ideaflow.yunyu.module.comment.site.vo.SiteCommentCreateResponse;
import com.ideaflow.yunyu.module.comment.site.vo.SiteCommentListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台评论控制器。
 * 作用：向前台文章详情页提供评论列表查询和登录用户评论发布接口。
 */
@Tag(name = "前台评论接口")
@RestController
@RequestMapping("/api/site/posts/{slug}/comments")
public class SiteCommentController {

    private final SiteCommentService siteCommentService;

    /**
     * 创建前台评论控制器。
     *
     * @param siteCommentService 前台评论服务
     */
    public SiteCommentController(SiteCommentService siteCommentService) {
        this.siteCommentService = siteCommentService;
    }

    /**
     * 查询指定文章的评论列表。
     *
     * @param slug 文章 slug
     * @param request 查询请求
     * @return 评论列表
     */
    @Operation(summary = "查询文章评论列表")
    @GetMapping
    public ApiResponse<SiteCommentListResponse> listComments(@PathVariable String slug, SiteCommentQueryRequest request) {
        return ApiResponse.success(siteCommentService.listPostComments(slug, request));
    }

    /**
     * 发布指定文章的评论。
     *
     * @param slug 文章 slug
     * @param request 评论请求
     * @param httpServletRequest 当前请求
     * @return 发布结果
     */
    @Operation(summary = "发布文章评论")
    @PostMapping
    public ApiResponse<SiteCommentCreateResponse> createComment(@PathVariable String slug,
                                                                @Valid @RequestBody SiteCommentCreateRequest request,
                                                                HttpServletRequest httpServletRequest) {
        return ApiResponse.success(siteCommentService.createComment(slug, request, resolveClientIp(httpServletRequest)));
    }

    /**
     * 解析当前请求的客户端 IP。
     *
     * @param request 当前请求
     * @return 客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
