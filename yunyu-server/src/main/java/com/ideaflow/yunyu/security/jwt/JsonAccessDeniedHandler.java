package com.ideaflow.yunyu.security.jwt;

import tools.jackson.databind.ObjectMapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 权限不足处理器。
 * 作用：当用户已登录但权限不足时，统一返回 JSON 格式的 403 响应。
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * 创建权限不足处理器。
     *
     * @param objectMapper JSON 序列化工具
     */
    public JsonAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 输出统一的无权限响应。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param accessDeniedException 权限异常
     * @throws IOException 输出异常
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ResultCode.FORBIDDEN));
    }
}
