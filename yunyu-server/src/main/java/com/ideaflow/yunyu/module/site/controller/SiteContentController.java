package com.ideaflow.yunyu.module.site.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.contentaccess.dto.SiteContentAccessVerifyRequest;
import com.ideaflow.yunyu.module.contentaccess.vo.SiteContentAccessVerifyResponse;
import com.ideaflow.yunyu.module.site.dto.SitePostQueryRequest;
import com.ideaflow.yunyu.module.site.service.SiteContentService;
import com.ideaflow.yunyu.module.site.service.SiteVersionService;
import com.ideaflow.yunyu.module.site.vo.SiteBaseInfoResponse;
import com.ideaflow.yunyu.module.site.vo.SiteCategoryDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteCategoryItemResponse;
import com.ideaflow.yunyu.module.site.vo.SiteHomeResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SitePostListResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTagDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTagItemResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTopicDetailResponse;
import com.ideaflow.yunyu.module.site.vo.SiteTopicItemResponse;
import com.ideaflow.yunyu.module.site.vo.SiteVersionResponse;
import com.ideaflow.yunyu.security.jwt.JwtLoginUserAuthenticationConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final SiteVersionService siteVersionService;
    private final JwtDecoder jwtDecoder;
    private final JwtLoginUserAuthenticationConverter jwtAuthenticationConverter;

    /**
     * 创建前台内容公开控制器。
     *
     * @param siteContentService 前台内容服务
     * @param siteVersionService 公开版本信息服务
     * @param jwtDecoder JWT 解码器
     * @param jwtAuthenticationConverter JWT 登录用户认证转换器
     */
    public SiteContentController(SiteContentService siteContentService,
                                 SiteVersionService siteVersionService,
                                 JwtDecoder jwtDecoder,
                                 JwtLoginUserAuthenticationConverter jwtAuthenticationConverter) {
        this.siteContentService = siteContentService;
        this.siteVersionService = siteVersionService;
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
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
        return ApiResponse.success(siteContentService.getSiteBaseInfo());
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
    public ApiResponse<SitePostDetailResponse> getPostDetail(@PathVariable String slug,
                                                             HttpServletRequest httpServletRequest) {
        return runWithOptionalAuthentication(httpServletRequest,
                () -> ApiResponse.success(siteContentService.getPostDetail(slug, resolveVisitorId(httpServletRequest))));
    }

    /**
     * 校验文章内容访问规则。
     *
     * @param slug 文章 slug
     * @param request 校验请求
     * @param httpServletRequest 当前请求
     * @return 最新内容访问状态
     */
    @Operation(summary = "校验文章内容访问规则")
    @PostMapping("/posts/{slug}/access/verify")
    public ApiResponse<SiteContentAccessVerifyResponse> verifyPostContentAccess(@PathVariable String slug,
                                                                                @Valid @RequestBody SiteContentAccessVerifyRequest request,
                                                                                HttpServletRequest httpServletRequest) {
        return runWithOptionalAuthentication(httpServletRequest,
                () -> ApiResponse.success(siteContentService.verifyContentAccess(slug, request, resolveVisitorId(httpServletRequest))));
    }

    /**
     * 上报文章浏览量。
     *
     * @param id 文章 id
     * @return 是否上报成功
     */
    @Operation(summary = "上报文章浏览量")
    @PostMapping("/posts/{id}/view")
    public ApiResponse<Boolean> increasePostViewCount(@PathVariable Long id) {
        siteContentService.increasePostViewCount(id);
        return ApiResponse.success(Boolean.TRUE);
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

    /**
     * 解析当前请求的访客标识。
     * 作用：统一根据请求来源生成匿名访客标识，避免前端随机 UUID 持续漂移。
     *
     * @param request 当前请求
     * @return 访客标识
     */
    private String resolveVisitorId(HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        if (clientIp == null || clientIp.isBlank()) {
            return null;
        }

        String userAgent = request.getHeader("User-Agent");
        return md5Hex(clientIp + "|" + (userAgent == null ? "" : userAgent.trim()));
    }

    /**
     * 在当前请求线程中挂载可选登录态后执行目标逻辑。
     * 作用：公开接口仍允许匿名访问，但当请求头携带合法 Bearer Token 时，
     * 也能让服务层拿到登录用户信息，从而支持“登录后可见”这类服务端判定。
     *
     * @param request 当前请求
     * @param action 目标逻辑
     * @param <T> 返回值类型
     * @return 目标逻辑执行结果
     */
    private <T> T runWithOptionalAuthentication(HttpServletRequest request, java.util.function.Supplier<T> action) {
        SecurityContext originalContext = SecurityContextHolder.getContext();
        SecurityContext temporaryContext = SecurityContextHolder.createEmptyContext();
        temporaryContext.setAuthentication(resolveOptionalAuthentication(request));
        SecurityContextHolder.setContext(temporaryContext);
        try {
            return action.get();
        } finally {
            SecurityContextHolder.setContext(originalContext);
        }
    }

    /**
     * 尝试从请求头解析当前登录用户认证信息。
     * 作用：对公开接口上的 Bearer Token 做一次“尽力而为”的解析，
     * 解析失败时保持匿名，不让无 token 或脏 token 阻塞公开内容访问。
     *
     * @param request 当前请求
     * @return 认证对象，匿名时返回 null
     */
    private Authentication resolveOptionalAuthentication(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            return null;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwtAuthenticationConverter.convert(jwt);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * 解析客户端真实 IP。
     * 作用：优先兼容常见反向代理头，尽量拿到真实来源地址作为匿名访客指纹输入。
     *
     * @param request 当前请求
     * @return 客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] ipList = forwardedFor.split(",");
            if (ipList.length > 0 && ipList[0] != null && !ipList[0].isBlank()) {
                return ipList[0].trim();
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    /**
     * 计算 MD5 摘要。
     * 作用：把匿名访客原始指纹收敛为固定长度字符串，避免直接在链路中传播原始 IP 信息。
     *
     * @param value 原始文本
     * @return MD5 十六进制摘要
     */
    private String md5Hex(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digestBytes = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digestBytes);
        } catch (Exception exception) {
            return null;
        }
    }
}
