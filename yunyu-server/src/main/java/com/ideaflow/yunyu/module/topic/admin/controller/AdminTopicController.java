package com.ideaflow.yunyu.module.topic.admin.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.topic.admin.dto.AdminTopicCreateRequest;
import com.ideaflow.yunyu.module.topic.admin.dto.AdminTopicQueryRequest;
import com.ideaflow.yunyu.module.topic.admin.dto.AdminTopicUpdateRequest;
import com.ideaflow.yunyu.module.topic.admin.service.AdminTopicService;
import com.ideaflow.yunyu.module.topic.admin.vo.AdminTopicItemResponse;
import com.ideaflow.yunyu.module.topic.admin.vo.AdminTopicListResponse;
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
 * 后台专题管理控制器。
 * 作用：向站长提供专题列表、详情、创建、更新和删除的后台管理接口。
 */
@Tag(name = "后台专题管理")
@RestController
@RequestMapping("/api/admin/topics")
public class AdminTopicController {

    private final AdminTopicService adminTopicService;

    /**
     * 创建后台专题管理控制器。
     *
     * @param adminTopicService 后台专题管理服务
     */
    public AdminTopicController(AdminTopicService adminTopicService) {
        this.adminTopicService = adminTopicService;
    }

    /**
     * 查询后台专题列表。
     *
     * @param request 查询请求
     * @return 专题列表
     */
    @Operation(summary = "查询后台专题列表")
    @GetMapping
    public ApiResponse<AdminTopicListResponse> listTopics(AdminTopicQueryRequest request) {
        return ApiResponse.success(adminTopicService.listTopics(request));
    }

    /**
     * 查询单个专题详情。
     *
     * @param topicId 专题ID
     * @return 专题详情
     */
    @Operation(summary = "查询单个专题详情")
    @GetMapping("/{topicId}")
    public ApiResponse<AdminTopicItemResponse> getTopic(@PathVariable Long topicId) {
        return ApiResponse.success(adminTopicService.getTopic(topicId));
    }

    /**
     * 创建专题。
     *
     * @param request 创建请求
     * @return 创建后的专题详情
     */
    @Operation(summary = "创建专题")
    @PostMapping
    public ApiResponse<AdminTopicItemResponse> createTopic(@Valid @RequestBody AdminTopicCreateRequest request) {
        return ApiResponse.success(adminTopicService.createTopic(request));
    }

    /**
     * 更新专题。
     *
     * @param topicId 专题ID
     * @param request 更新请求
     * @return 更新后的专题详情
     */
    @Operation(summary = "更新专题")
    @PutMapping("/{topicId}")
    public ApiResponse<AdminTopicItemResponse> updateTopic(@PathVariable Long topicId,
                                                           @Valid @RequestBody AdminTopicUpdateRequest request) {
        return ApiResponse.success(adminTopicService.updateTopic(topicId, request));
    }

    /**
     * 删除专题。
     *
     * @param topicId 专题ID
     * @return 成功响应
     */
    @Operation(summary = "删除专题")
    @DeleteMapping("/{topicId}")
    public ApiResponse<Void> deleteTopic(@PathVariable Long topicId) {
        adminTopicService.deleteTopic(topicId);
        return ApiResponse.success();
    }
}
