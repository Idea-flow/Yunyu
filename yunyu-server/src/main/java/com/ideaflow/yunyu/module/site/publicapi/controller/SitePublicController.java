package com.ideaflow.yunyu.module.site.publicapi.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.site.publicapi.service.SitePublicService;
import com.ideaflow.yunyu.module.site.publicapi.service.SiteVersionService;
import com.ideaflow.yunyu.module.site.publicapi.vo.SiteBaseInfoResponse;
import com.ideaflow.yunyu.module.site.publicapi.vo.SiteHomeResponse;
import com.ideaflow.yunyu.module.site.publicapi.vo.SiteVersionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站点公开聚合控制器。
 * 作用：承接首页、站点基础配置和公开版本信息等站点级公开接口。
 */
@Tag(name = "站点公开接口")
@RestController
@RequestMapping("/api/site")
public class SitePublicController {

    private final SitePublicService sitePublicService;
    private final SiteVersionService siteVersionService;

    /**
     * 创建站点公开聚合控制器。
     *
     * @param sitePublicService 站点公开聚合服务
     * @param siteVersionService 公开版本信息服务
     */
    public SitePublicController(SitePublicService sitePublicService,
                                SiteVersionService siteVersionService) {
        this.sitePublicService = sitePublicService;
        this.siteVersionService = siteVersionService;
    }

    /**
     * 获取当前服务公开版本信息。
     *
     * @return 服务版本信息
     */
    @Operation(summary = "获取当前服务公开版本信息")
    @GetMapping("/version")
    public ApiResponse<SiteVersionResponse> getVersion() {
        return ApiResponse.success(siteVersionService.getVersion());
    }

    /**
     * 获取前台站点基础配置。
     *
     * @return 站点基础配置
     */
    @Operation(summary = "获取前台站点基础配置")
    @GetMapping("/config")
    public ApiResponse<SiteBaseInfoResponse> getSiteConfig() {
        return ApiResponse.success(sitePublicService.getSiteBaseInfo());
    }

    /**
     * 获取前台首页聚合数据。
     *
     * @return 首页聚合数据
     */
    @Operation(summary = "获取前台首页聚合数据")
    @GetMapping("/home")
    public ApiResponse<SiteHomeResponse> getHome() {
        return ApiResponse.success(sitePublicService.getHome());
    }
}
