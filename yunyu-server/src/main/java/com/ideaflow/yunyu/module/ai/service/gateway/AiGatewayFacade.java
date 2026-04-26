package com.ideaflow.yunyu.module.ai.service.gateway;

import tools.jackson.databind.JsonNode;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 网关门面接口。
 * 作用：为控制器层提供统一协议调用入口，屏蔽底层协议服务与适配器细节。
 */
public interface AiGatewayFacade {

    /**
     * 调用 Chat Completions 非流式接口。
     *
     * @param requestBody OpenAI Chat 请求体
     * @return OpenAI Chat 响应体
     */
    JsonNode invokeChat(JsonNode requestBody);

    /**
     * 调用 Chat Completions 流式接口。
     *
     * @param requestBody OpenAI Chat 请求体
     * @param emitter SSE 输出器
     */
    void streamChat(JsonNode requestBody, SseEmitter emitter);

    /**
     * 调用 Responses 非流式接口。
     *
     * @param requestBody OpenAI Responses 请求体
     * @return OpenAI Responses 响应体
     */
    JsonNode invokeResponses(JsonNode requestBody);

    /**
     * 调用 Responses 流式接口。
     *
     * @param requestBody OpenAI Responses 请求体
     * @param emitter SSE 输出器
     */
    void streamResponses(JsonNode requestBody, SseEmitter emitter);
}
