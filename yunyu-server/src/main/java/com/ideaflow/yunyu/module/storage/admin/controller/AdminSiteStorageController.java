package com.ideaflow.yunyu.module.storage.admin.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.storage.admin.dto.AdminSiteStorageS3ConfigUpdateRequest;
import com.ideaflow.yunyu.module.storage.admin.dto.AdminSiteStorageS3ProfileRequest;
import com.ideaflow.yunyu.module.storage.admin.service.SiteStorageS3ConfigService;
import com.ideaflow.yunyu.module.storage.admin.vo.AdminSiteStorageS3ConnectionTestResponse;
import com.ideaflow.yunyu.module.storage.admin.vo.AdminSiteStorageS3ConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台站点存储配置控制器。
 * 作用：向后台站点设置页提供 S3 多配置的读取与保存接口。
 */
@Tag(name = "后台站点存储配置")
@RestController
@RequestMapping("/api/admin/site/storage/s3")
public class AdminSiteStorageController {

    private final SiteStorageS3ConfigService siteStorageS3ConfigService;

    /**
     * 创建后台站点存储配置控制器。
     *
     * @param siteStorageS3ConfigService 站点 S3 配置服务
     */
    public AdminSiteStorageController(SiteStorageS3ConfigService siteStorageS3ConfigService) {
        this.siteStorageS3ConfigService = siteStorageS3ConfigService;
    }

    /**
     * 查询后台 S3 配置。
     *
     * @return S3 配置
     */
    @Operation(summary = "查询后台 S3 配置")
    @GetMapping
    public ApiResponse<AdminSiteStorageS3ConfigResponse> getS3Config() {
        return ApiResponse.success(siteStorageS3ConfigService.getAdminS3Config());
    }

    /**
     * 保存后台 S3 配置。
     *
     * @param request 更新请求
     * @return 保存后的配置
     */
    @Operation(summary = "保存后台 S3 配置")
    @PutMapping
    public ApiResponse<AdminSiteStorageS3ConfigResponse> updateS3Config(@RequestBody AdminSiteStorageS3ConfigUpdateRequest request) {
        return ApiResponse.success(siteStorageS3ConfigService.updateAdminS3Config(request));
    }

    /**
     * 测试 S3 配置连接。
     *
     * @param request 待测试配置
     * @return 连接测试结果
     */
    @Operation(summary = "测试 S3 配置连接")
    @PostMapping("/test")
    public ApiResponse<AdminSiteStorageS3ConnectionTestResponse> testS3Connection(@RequestBody AdminSiteStorageS3ProfileRequest request) {
        return ApiResponse.success(siteStorageS3ConfigService.testConnection(request));
    }
}
