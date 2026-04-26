package com.ideaflow.yunyu.module.ai.internal.service.protocol;

import tools.jackson.databind.JsonNode;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Responses 协议服务接口。
 * 作用：定义 Responses 协议的非流式与流式调用能力，供网关与兼容接口复用。
 */
public interface ResponsesService {

    /**
     * 执行 Responses 非流式调用。
     *
     * @param requestBody OpenAI Responses 请求体
     * @return OpenAI Responses 响应体
     */
    JsonNode create(JsonNode requestBody);

    /**
     * 执行 Responses 流式调用。
     *
     * @param requestBody OpenAI Responses 请求体
     * @param emitter SSE 输出器
     */
    void stream(JsonNode requestBody, SseEmitter emitter);
}
