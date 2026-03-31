package com.ideaflow.yunyu.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证失败处理器。
 * 作用：当请求未携带有效 JWT 或认证失效时，统一返回 JSON 格式的 401 响应。
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 创建认证失败处理器。
     *
     * @param objectMapper JSON 序列化工具
     */
    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 输出统一的未认证响应。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param authException 认证异常
     * @throws IOException 输出异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ResultCode.UNAUTHORIZED));
    }
}
