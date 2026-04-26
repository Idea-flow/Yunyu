package com.ideaflow.yunyu.module.ai.admin.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.ai.admin.dto.AdminSiteAiProviderConfigUpdateRequest;
import com.ideaflow.yunyu.module.ai.admin.dto.AdminSiteAiProviderProfileRequest;
import com.ideaflow.yunyu.module.ai.admin.service.AiProviderConfigService;
import com.ideaflow.yunyu.module.ai.admin.vo.AdminSiteAiProviderConfigResponse;
import com.ideaflow.yunyu.module.ai.admin.vo.AdminSiteAiProviderConnectionTestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 AI 提供商配置控制器。
 * 作用：向后台站点配置页提供 AI 多配置读取、保存与连接测试接口。
 */
@Tag(name = "后台 AI 提供商配置")
@RestController
@RequestMapping("/api/admin/site/ai/providers")
public class AdminSiteAiProviderController {

    private final AiProviderConfigService aiProviderConfigService;

    /**
     * 创建后台 AI 配置控制器。
     *
     * @param aiProviderConfigService AI 提供商配置服务
     */
    public AdminSiteAiProviderController(AiProviderConfigService aiProviderConfigService) {
        this.aiProviderConfigService = aiProviderConfigService;
    }

    /**
     * 查询后台 AI 配置。
     *
     * @return 后台 AI 配置
     */
    @Operation(summary = "查询后台 AI 提供商配置")
    @GetMapping
    public ApiResponse<AdminSiteAiProviderConfigResponse> getConfig() {
        return ApiResponse.success(aiProviderConfigService.getAdminConfig());
    }

    /**
     * 保存后台 AI 配置。
     *
     * @param request 配置更新请求
     * @return 保存后的配置
     */
    @Operation(summary = "保存后台 AI 提供商配置")
    @PutMapping
    public ApiResponse<AdminSiteAiProviderConfigResponse> updateConfig(@RequestBody AdminSiteAiProviderConfigUpdateRequest request) {
        return ApiResponse.success(aiProviderConfigService.updateAdminConfig(request));
    }

    /**
     * 测试 AI 配置连接。
     *
     * @param request 待测试配置项
     * @return 连接测试结果
     */
    @Operation(summary = "测试 AI 提供商连接")
    @PostMapping("/test")
    public ApiResponse<AdminSiteAiProviderConnectionTestResponse> testConnection(@RequestBody AdminSiteAiProviderProfileRequest request) {
        return ApiResponse.success(aiProviderConfigService.testConnection(request));
    }
}
