package com.ideaflow.yunyu.module.friendlink.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.friendlink.dto.SiteFriendLinkApplyRequest;
import com.ideaflow.yunyu.module.friendlink.service.SiteFriendLinkService;
import com.ideaflow.yunyu.module.friendlink.vo.SiteFriendLinkApplyResponse;
import com.ideaflow.yunyu.module.friendlink.vo.SiteFriendLinkItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台友链公开控制器。
 * 作用：向前台页面提供友链展示和匿名友链申请接口。
 */
@Tag(name = "前台友链接口")
@RestController
@RequestMapping("/api/site/friend-links")
public class SiteFriendLinkController {

    private final SiteFriendLinkService siteFriendLinkService;

    /**
     * 创建前台友链公开控制器。
     *
     * @param siteFriendLinkService 前台友链服务
     */
    public SiteFriendLinkController(SiteFriendLinkService siteFriendLinkService) {
        this.siteFriendLinkService = siteFriendLinkService;
    }

    /**
     * 查询前台友链列表。
     *
     * @return 已通过审核的友链列表
     */
    @Operation(summary = "查询前台友链列表")
    @GetMapping
    public ApiResponse<List<SiteFriendLinkItemResponse>> listFriendLinks() {
        return ApiResponse.success(siteFriendLinkService.listApprovedFriendLinks());
    }

    /**
     * 提交友链申请。
     *
     * @param request 友链申请请求
     * @return 申请结果
     */
    @Operation(summary = "提交友链申请")
    @PostMapping("/applications")
    public ApiResponse<SiteFriendLinkApplyResponse> applyFriendLink(
            @Valid @RequestBody SiteFriendLinkApplyRequest request) {
        return ApiResponse.success(siteFriendLinkService.applyFriendLink(request));
    }
}
