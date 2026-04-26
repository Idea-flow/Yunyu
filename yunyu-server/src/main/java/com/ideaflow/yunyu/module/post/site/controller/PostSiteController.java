package com.ideaflow.yunyu.module.post.site.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.contentaccess.site.dto.SiteContentAccessVerifyRequest;
import com.ideaflow.yunyu.module.contentaccess.site.vo.SiteContentAccessVerifyResponse;
import com.ideaflow.yunyu.module.post.site.dto.SitePostQueryRequest;
import com.ideaflow.yunyu.module.post.site.service.PostSiteService;
import com.ideaflow.yunyu.module.post.site.vo.SitePostDetailResponse;
import com.ideaflow.yunyu.module.post.site.vo.SitePostListResponse;
import com.ideaflow.yunyu.security.jwt.JwtLoginUserAuthenticationConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.function.Supplier;
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
 * 前台文章控制器。
 * 作用：承接前台文章列表、详情、访问校验与浏览量上报等接口能力。
 */
@Tag(name = "前台文章接口")
@RestController
@RequestMapping("/api/site/posts")
public class PostSiteController {

    private final PostSiteService postSiteService;
    private final JwtDecoder jwtDecoder;
    private final JwtLoginUserAuthenticationConverter jwtAuthenticationConverter;

    /**
     * 创建前台文章控制器。
     *
     * @param postSiteService 前台文章服务
     * @param jwtDecoder JWT 解码器
     * @param jwtAuthenticationConverter JWT 登录用户认证转换器
     */
    public PostSiteController(PostSiteService postSiteService,
                              JwtDecoder jwtDecoder,
                              JwtLoginUserAuthenticationConverter jwtAuthenticationConverter) {
        this.postSiteService = postSiteService;
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    /**
     * 查询前台文章列表。
     *
     * @param request 查询请求
     * @return 文章列表
     */
    @Operation(summary = "查询前台文章列表")
    @GetMapping
    public ApiResponse<SitePostListResponse> listPosts(SitePostQueryRequest request) {
        return ApiResponse.success(postSiteService.listPosts(request));
    }

    /**
     * 查询前台文章详情。
     *
     * @param slug 文章 slug
     * @param httpServletRequest 当前请求
     * @return 文章详情
     */
    @Operation(summary = "查询前台文章详情")
    @GetMapping("/{slug}")
    public ApiResponse<SitePostDetailResponse> getPostDetail(@PathVariable String slug,
                                                             HttpServletRequest httpServletRequest) {
        return runWithOptionalAuthentication(httpServletRequest,
                () -> ApiResponse.success(postSiteService.getPostDetail(slug, resolveVisitorId(httpServletRequest))));
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
    @PostMapping("/{slug}/access/verify")
    public ApiResponse<SiteContentAccessVerifyResponse> verifyPostContentAccess(@PathVariable String slug,
                                                                                @Valid @RequestBody SiteContentAccessVerifyRequest request,
                                                                                HttpServletRequest httpServletRequest) {
        return runWithOptionalAuthentication(httpServletRequest,
                () -> ApiResponse.success(postSiteService.verifyContentAccess(slug, request, resolveVisitorId(httpServletRequest))));
    }

    /**
     * 上报文章浏览量。
     *
     * @param id 文章 id
     * @return 是否上报成功
     */
    @Operation(summary = "上报文章浏览量")
    @PostMapping("/{id}/view")
    public ApiResponse<Boolean> increasePostViewCount(@PathVariable Long id) {
        postSiteService.increasePostViewCount(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    /**
     * 解析当前请求的访客标识。
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
     *
     * @param request 当前请求
     * @param action 目标逻辑
     * @param <T> 返回值类型
     * @return 目标逻辑执行结果
     */
    private <T> T runWithOptionalAuthentication(HttpServletRequest request, Supplier<T> action) {
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
