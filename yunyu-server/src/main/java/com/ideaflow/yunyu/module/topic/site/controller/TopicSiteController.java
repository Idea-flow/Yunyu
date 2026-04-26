package com.ideaflow.yunyu.module.topic.site.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.post.site.dto.SitePostQueryRequest;
import com.ideaflow.yunyu.module.topic.site.service.TopicSiteService;
import com.ideaflow.yunyu.module.topic.site.vo.SiteTopicDetailResponse;
import com.ideaflow.yunyu.module.topic.site.vo.SiteTopicItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台专题控制器。
 * 作用：承接前台专题列表与专题详情查询接口。
 */
@Tag(name = "前台专题接口")
@RestController
@RequestMapping("/api/site/topics")
public class TopicSiteController {

    private final TopicSiteService topicSiteService;

    /**
     * 创建前台专题控制器。
     *
     * @param topicSiteService 前台专题服务
     */
    public TopicSiteController(TopicSiteService topicSiteService) {
        this.topicSiteService = topicSiteService;
    }

    /**
     * 查询前台专题列表。
     *
     * @return 专题列表
     */
    @Operation(summary = "查询前台专题列表")
    @GetMapping
    public ApiResponse<List<SiteTopicItemResponse>> listTopics() {
        return ApiResponse.success(topicSiteService.listTopics());
    }

    /**
     * 查询前台专题详情。
     *
     * @param slug 专题 slug
     * @param request 查询请求
     * @return 专题详情
     */
    @Operation(summary = "查询前台专题详情")
    @GetMapping("/{slug}")
    public ApiResponse<SiteTopicDetailResponse> getTopicDetail(@PathVariable String slug,
                                                               SitePostQueryRequest request) {
        return ApiResponse.success(topicSiteService.getTopicDetail(slug, request));
    }
}
