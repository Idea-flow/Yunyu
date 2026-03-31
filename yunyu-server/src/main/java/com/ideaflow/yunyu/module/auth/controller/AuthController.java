package com.ideaflow.yunyu.module.auth.controller;

import com.ideaflow.yunyu.common.response.ApiResponse;
import com.ideaflow.yunyu.module.auth.dto.LoginRequest;
import com.ideaflow.yunyu.module.auth.service.AuthService;
import com.ideaflow.yunyu.module.auth.vo.CurrentUserResponse;
import com.ideaflow.yunyu.module.auth.vo.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器。
 * 作用：提供本地登录和当前登录用户信息查询等基础认证接口。
 */
@Tag(name = "认证模块")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 创建认证控制器。
     *
     * @param authService 认证服务
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 执行本地账号密码登录。
     *
     * @param request 登录请求
     * @param httpServletRequest 当前请求
     * @return 登录响应
     */
    @Operation(summary = "本地账号密码登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.success(authService.login(request, resolveClientIp(httpServletRequest)));
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前用户响应
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> currentUser() {
        return ApiResponse.success(authService.getCurrentUser());
    }

    /**
     * 解析当前请求的客户端 IP。
     *
     * @param request 当前请求
     * @return 客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
