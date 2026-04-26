package com.ideaflow.yunyu.module.siteconfig.admin.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.siteconfig.admin.dto.AdminSiteConfigUpdateRequest;
import com.ideaflow.yunyu.module.siteconfig.admin.service.AdminSiteConfigService;
import com.ideaflow.yunyu.module.siteconfig.admin.vo.AdminSiteConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台站点配置控制器。
 * 作用：向后台站点设置页面提供配置读取与保存接口，让站长可直接维护站点基础信息、SEO 与主题配置。
 */
@Tag(name = "后台站点配置")
@RestController
@RequestMapping("/api/admin/site-config")
public class AdminSiteConfigController {

    private final AdminSiteConfigService adminSiteConfigService;

    /**
     * 创建后台站点配置控制器。
     *
     * @param adminSiteConfigService 后台站点配置服务
     */
    public AdminSiteConfigController(AdminSiteConfigService adminSiteConfigService) {
        this.adminSiteConfigService = adminSiteConfigService;
    }

    /**
     * 查询站点配置。
     *
     * @return 站点配置
     */
    @Operation(summary = "查询站点配置")
    @GetMapping
    public ApiResponse<AdminSiteConfigResponse> getSiteConfig() {
        return ApiResponse.success(adminSiteConfigService.getSiteConfig());
    }

    /**
     * 更新站点配置。
     *
     * @param request 更新请求
     * @return 更新后的站点配置
     */
    @Operation(summary = "更新站点配置")
    @PutMapping
    public ApiResponse<AdminSiteConfigResponse> updateSiteConfig(@Valid @RequestBody AdminSiteConfigUpdateRequest request) {
        return ApiResponse.success(adminSiteConfigService.updateSiteConfig(request));
    }
}
