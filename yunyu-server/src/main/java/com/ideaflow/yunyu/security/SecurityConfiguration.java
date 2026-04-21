package com.ideaflow.yunyu.security;

import com.ideaflow.yunyu.security.jwt.JwtLoginUserAuthenticationConverter;
import com.ideaflow.yunyu.security.jwt.JsonAccessDeniedHandler;
import com.ideaflow.yunyu.security.jwt.JsonAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

/**
 * Spring Security 基础配置类。
 * 作用：按公开匿名区、登录用户区与管理员区拆分 Spring Security 过滤链，
 * 让公开接口天然忽略无效 Bearer Token，同时把需要鉴权的接口收敛到专属安全链中。
 */
@Configuration
public class SecurityConfiguration {

    private final JwtLoginUserAuthenticationConverter jwtAuthenticationConverter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    /**
     * 创建安全配置对象。
     *
     * @param jwtAuthenticationConverter JWT 认证结果转换器
     * @param authenticationEntryPoint 认证失败处理器
     * @param accessDeniedHandler 权限不足处理器
     */
    public SecurityConfiguration(JwtLoginUserAuthenticationConverter jwtAuthenticationConverter,
                                 JsonAuthenticationEntryPoint authenticationEntryPoint,
                                 JsonAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * 构建公开匿名接口安全链。
     * 作用：为登录、注册、前台只读查询与健康检查接口提供匿名访问能力，
     * 并且不启用资源服务器 JWT 认证，避免公开接口被无效 Authorization 请求头误伤。
     *
     * @param http HttpSecurity 配置对象
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new OrRequestMatcher(
                        PathPatternRequestMatcher.pathPattern("/actuator/health"),
                        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/login"),
                        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/register"),
                        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/site/friend-links/applications"),
                        PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/site/**"),
                        PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/api/site/posts/{slug}/comments"),
                        PathPatternRequestMatcher.pathPattern("/v3/api-docs/**"),
                        PathPatternRequestMatcher.pathPattern("/swagger-ui/**"),
                        PathPatternRequestMatcher.pathPattern("/swagger-ui.html")
                ));
        applyBaseConfiguration(http);
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    /**
     * 构建登录用户接口安全链。
     * 作用：只对当前登录用户信息与前台评论发布等用户态接口启用 JWT 认证，
     * 把“需要登录”与“公开接口”清晰隔离。
     *
     * @param http HttpSecurity 配置对象
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    @Order(2)
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new OrRequestMatcher(
                        PathPatternRequestMatcher.pathPattern("/api/auth/me"),
                        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/site/posts/{slug}/comments")
                ));
        applyBaseConfiguration(http);
        applyJwtConfiguration(http);
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
        return http.build();
    }

    /**
     * 构建管理员接口安全链。
     * 作用：统一保护后台管理接口与受限 Actuator 端点，只允许站长角色访问。
     *
     * @param http HttpSecurity 配置对象
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    @Order(3)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new OrRequestMatcher(
                        PathPatternRequestMatcher.pathPattern("/api/admin/**"),
                        PathPatternRequestMatcher.pathPattern("/actuator/**")
                ));
        applyBaseConfiguration(http);
        applyJwtConfiguration(http);
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("SUPER_ADMIN"));
        return http.build();
    }

    /**
     * 构建兜底安全链。
     * 作用：兜住未被前三条安全链匹配到的请求，默认允许访问，
     * 避免遗漏路径时意外启用 JWT 认证。
     *
     * @param http HttpSecurity 配置对象
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    @Order(4)
    public SecurityFilterChain fallbackSecurityFilterChain(HttpSecurity http) throws Exception {
        applyBaseConfiguration(http);
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    /**
     * 应用所有安全链共享的基础配置。
     * 作用：统一关闭有状态认证方式，并保持当前项目使用无状态会话与统一异常返回。
     *
     * @param http HttpSecurity 配置对象
     * @throws Exception 配置异常
     */
    private void applyBaseConfiguration(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
    }

    /**
     * 应用 JWT 资源服务器配置。
     * 作用：为受保护接口启用 Bearer Token 认证，并复用统一的认证失败与鉴权失败处理器。
     *
     * @param http HttpSecurity 配置对象
     * @throws Exception 配置异常
     */
    private void applyJwtConfiguration(HttpSecurity http) throws Exception {
        http
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
    }
}
