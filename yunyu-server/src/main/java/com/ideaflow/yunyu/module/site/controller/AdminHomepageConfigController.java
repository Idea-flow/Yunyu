package com.ideaflow.yunyu.module.site.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.site.dto.AdminHomepageConfigUpdateRequest;
import com.ideaflow.yunyu.module.site.service.HomepageConfigService;
import com.ideaflow.yunyu.module.site.vo.AdminHomepageConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台首页配置控制器。
 * 作用：向后台提供首页品牌首屏和首页模块开关的独立读取与保存接口。
 */
@Tag(name = "后台首页配置")
@RestController
@RequestMapping("/api/admin/site/homepage-config")
public class AdminHomepageConfigController {

    private final HomepageConfigService homepageConfigService;

    /**
     * 创建后台首页配置控制器。
     *
     * @param homepageConfigService 首页配置服务
     */
    public AdminHomepageConfigController(HomepageConfigService homepageConfigService) {
        this.homepageConfigService = homepageConfigService;
    }

    /**
     * 查询首页配置。
     *
     * @return 首页配置
     */
    @Operation(summary = "查询首页配置")
    @GetMapping
    public ApiResponse<AdminHomepageConfigResponse> getHomepageConfig() {
        return ApiResponse.success(homepageConfigService.getAdminHomepageConfig());
    }

    /**
     * 更新首页配置。
     *
     * @param request 首页配置更新请求
     * @return 更新后的首页配置
     */
    @Operation(summary = "更新首页配置")
    @PutMapping
    public ApiResponse<AdminHomepageConfigResponse> updateHomepageConfig(@Valid @RequestBody AdminHomepageConfigUpdateRequest request) {
        return ApiResponse.success(homepageConfigService.updateHomepageConfig(request));
    }
}
