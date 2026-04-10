package com.ideaflow.yunyu.security;

import com.ideaflow.yunyu.security.jwt.JwtLoginUserAuthenticationConverter;
import com.ideaflow.yunyu.security.jwt.JsonAccessDeniedHandler;
import com.ideaflow.yunyu.security.jwt.JsonAuthenticationEntryPoint;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 基础配置类。
 * 作用：统一配置 Spring Security 过滤链、资源服务器 Bearer Token 认证入口以及接口访问规则。
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
     * 构建当前阶段的安全过滤链。
     *
     * @param http HttpSecurity 配置对象
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/actuator/health",
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/site/version"
                        ).permitAll()
                        .requestMatchers(
                                "/actuator/**"
                        ).hasRole("SUPER_ADMIN")
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/site/posts/*/comments").authenticated()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                        .anyRequest().permitAll());
        return http.build();
    }
}
