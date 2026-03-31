package com.ideaflow.yunyu.security.jwt;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.auth.service.AuthService;
import com.ideaflow.yunyu.security.LoginUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 * 作用：从 Authorization 请求头中解析 Bearer Token，完成用户认证并写入 Spring Security 上下文。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AuthService authService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;

    /**
     * 创建 JWT 认证过滤器。
     *
     * @param jwtTokenService JWT 服务
     * @param authService 认证服务
     * @param authenticationEntryPoint 认证失败处理器
     * @param accessDeniedHandler 权限不足处理器
     */
    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   AuthService authService,
                                   AuthenticationEntryPoint authenticationEntryPoint,
                                   AccessDeniedHandler accessDeniedHandler) {
        this.jwtTokenService = jwtTokenService;
        this.authService = authService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * 执行 JWT 认证逻辑。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring(7);
            Claims claims = jwtTokenService.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            LoginUser loginUser = authService.loadLoginUser(userId);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    loginUser,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + loginUser.getRole()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (BizException exception) {
            SecurityContextHolder.clearContext();
            if (exception.getCode() == ResultCode.FORBIDDEN.getCode()) {
                accessDeniedHandler.handle(
                        request,
                        response,
                        new org.springframework.security.access.AccessDeniedException(exception.getMessage(), exception)
                );
            } else {
                authenticationEntryPoint.commence(
                        request,
                        response,
                        new org.springframework.security.authentication.BadCredentialsException(ResultCode.UNAUTHORIZED.getMessage(), exception)
                );
            }
        }
    }
}
