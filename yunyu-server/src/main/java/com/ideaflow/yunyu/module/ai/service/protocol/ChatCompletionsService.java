package com.ideaflow.yunyu.module.ai.service.protocol;

import tools.jackson.databind.JsonNode;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Chat Completions 协议服务接口。
 * 作用：定义 Chat 协议的非流式与流式调用能力，供网关与业务场景复用。
 */
public interface ChatCompletionsService {

    /**
     * 执行 Chat Completions 非流式调用。
     *
     * @param requestBody OpenAI Chat 请求体
     * @return OpenAI Chat 响应体
     */
    JsonNode create(JsonNode requestBody);

    /**
     * 执行 Chat Completions 流式调用。
     *
     * @param requestBody OpenAI Chat 请求体
     * @param emitter SSE 输出器
     */
    void stream(JsonNode requestBody, SseEmitter emitter);
}
