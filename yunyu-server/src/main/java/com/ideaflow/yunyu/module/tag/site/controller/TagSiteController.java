package com.ideaflow.yunyu.module.tag.site.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.post.site.dto.SitePostQueryRequest;
import com.ideaflow.yunyu.module.tag.site.service.TagSiteService;
import com.ideaflow.yunyu.module.tag.site.vo.SiteTagDetailResponse;
import com.ideaflow.yunyu.module.tag.site.vo.SiteTagItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台标签控制器。
 * 作用：承接前台标签列表与标签详情查询接口。
 */
@Tag(name = "前台标签接口")
@RestController
@RequestMapping("/api/site/tags")
public class TagSiteController {

    private final TagSiteService tagSiteService;

    /**
     * 创建前台标签控制器。
     *
     * @param tagSiteService 前台标签服务
     */
    public TagSiteController(TagSiteService tagSiteService) {
        this.tagSiteService = tagSiteService;
    }

    /**
     * 查询前台标签列表。
     *
     * @return 标签列表
     */
    @Operation(summary = "查询前台标签列表")
    @GetMapping
    public ApiResponse<List<SiteTagItemResponse>> listTags() {
        return ApiResponse.success(tagSiteService.listTags());
    }

    /**
     * 查询前台标签详情。
     *
     * @param slug 标签 slug
     * @param request 查询请求
     * @return 标签详情
     */
    @Operation(summary = "查询前台标签详情")
    @GetMapping("/{slug}")
    public ApiResponse<SiteTagDetailResponse> getTagDetail(@PathVariable String slug,
                                                           SitePostQueryRequest request) {
        return ApiResponse.success(tagSiteService.getTagDetail(slug, request));
    }
}
