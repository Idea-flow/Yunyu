package com.ideaflow.yunyu.module.site.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.site.dto.SitePostQueryRequest;
import com.ideaflow.yunyu.module.site.service.SiteContentService;
import com.ideaflow.yunyu.module.site.vo.SiteCategoryDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteCategoryItemResponse;
import com.ideaflow.yunyu.module.site.vo.SiteHomeResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostListResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTagDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTagItemResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTopicDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTopicItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台内容公开控制器。
 * 作用：向前台页面暴露首页、文章、分类、标签和专题的公开只读接口。
 */
@Tag(name = "前台内容接口")
@RestController
@RequestMapping("/api/site")
public class SiteContentController {

    private final SiteContentService siteContentService;

    /**
     * 创建前台内容公开控制器。
     *
     * @param siteContentService 前台内容服务
     */
    public SiteContentController(SiteContentService siteContentService) {
        this.siteContentService = siteContentService;
    }

    /**
     * 获取前台首页聚合数据。
     *
     * @return 首页聚合数据
     */
    @Operation(summary = "获取前台首页聚合数据")
    @GetMapping("/home")
    public ApiResponse<SiteHomeResponse> getHome() {
        return ApiResponse.success(siteContentService.getHome());
    }

    /**
     * 查询前台文章列表。
     *
     * @param request 查询请求
     * @return 文章列表
     */
    @Operation(summary = "查询前台文章列表")
    @GetMapping("/posts")
    public ApiResponse<SitePostListResponse> listPosts(SitePostQueryRequest request) {
        return ApiResponse.success(siteContentService.listPosts(request));
    }

    /**
     * 查询前台文章详情。
     *
     * @param slug 文章 slug
     * @return 文章详情
     */
    @Operation(summary = "查询前台文章详情")
    @GetMapping("/posts/{slug}")
    public ApiResponse<SitePostDetailResponse> getPostDetail(@PathVariable String slug) {
        return ApiResponse.success(siteContentService.getPostDetail(slug));
    }

    /**
     * 查询前台分类列表。
     *
     * @return 分类列表
     */
    @Operation(summary = "查询前台分类列表")
    @GetMapping("/categories")
    public ApiResponse<List<SiteCategoryItemResponse>> listCategories() {
        return ApiResponse.success(siteContentService.listCategories());
    }

    /**
     * 查询前台分类详情。
     *
     * @param slug 分类 slug
     * @param request 查询请求
     * @return 分类详情
     */
    @Operation(summary = "查询前台分类详情")
    @GetMapping("/categories/{slug}")
    public ApiResponse<SiteCategoryDetailResponse> getCategoryDetail(@PathVariable String slug,
                                                                     SitePostQueryRequest request) {
        return ApiResponse.success(siteContentService.getCategoryDetail(slug, request));
    }

    /**
     * 查询前台专题列表。
     *
     * @return 专题列表
     */
    @Operation(summary = "查询前台专题列表")
    @GetMapping("/topics")
    public ApiResponse<List<SiteTopicItemResponse>> listTopics() {
        return ApiResponse.success(siteContentService.listTopics());
    }

    /**
     * 查询前台专题详情。
     *
     * @param slug 专题 slug
     * @param request 查询请求
     * @return 专题详情
     */
    @Operation(summary = "查询前台专题详情")
    @GetMapping("/topics/{slug}")
    public ApiResponse<SiteTopicDetailResponse> getTopicDetail(@PathVariable String slug,
                                                               SitePostQueryRequest request) {
        return ApiResponse.success(siteContentService.getTopicDetail(slug, request));
    }

    /**
     * 查询前台标签列表。
     *
     * @return 标签列表
     */
    @Operation(summary = "查询前台标签列表")
    @GetMapping("/tags")
    public ApiResponse<List<SiteTagItemResponse>> listTags() {
        return ApiResponse.success(siteContentService.listTags());
    }

    /**
     * 查询前台标签详情。
     *
     * @param slug 标签 slug
     * @param request 查询请求
     * @return 标签详情
     */
    @Operation(summary = "查询前台标签详情")
    @GetMapping("/tags/{slug}")
    public ApiResponse<SiteTagDetailResponse> getTagDetail(@PathVariable String slug,
                                                           SitePostQueryRequest request) {
        return ApiResponse.success(siteContentService.getTagDetail(slug, request));
    }
}
