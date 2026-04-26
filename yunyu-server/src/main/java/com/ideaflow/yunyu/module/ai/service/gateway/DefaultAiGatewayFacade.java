package com.ideaflow.yunyu.module.ai.service.gateway;

import tools.jackson.databind.JsonNode;
import com.ideaflow.yunyu.module.ai.service.protocol.ChatCompletionsService;
import com.ideaflow.yunyu.module.ai.service.protocol.ResponsesService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 网关门面实现类。
 * 作用：聚合 Chat 与 Responses 协议服务，向上层提供统一入口。
 */
@Service
public class DefaultAiGatewayFacade implements AiGatewayFacade {

    private final ChatCompletionsService chatCompletionsService;
    private final ResponsesService responsesService;

    /**
     * 创建 AI 网关门面实现。
     *
     * @param chatCompletionsService Chat 协议服务
     * @param responsesService Responses 协议服务
     */
    public DefaultAiGatewayFacade(ChatCompletionsService chatCompletionsService,
                                  ResponsesService responsesService) {
        this.chatCompletionsService = chatCompletionsService;
        this.responsesService = responsesService;
    }

    /**
     * 调用 Chat 非流式能力。
     *
     * @param requestBody 请求体
     * @return 响应 JSON
     */
    @Override
    public JsonNode invokeChat(JsonNode requestBody) {
        return chatCompletionsService.create(requestBody);
    }

    /**
     * 调用 Chat 流式能力。
     *
     * @param requestBody 请求体
     * @param emitter SSE 输出器
     */
    @Override
    public void streamChat(JsonNode requestBody, SseEmitter emitter) {
        chatCompletionsService.stream(requestBody, emitter);
    }

    /**
     * 调用 Responses 非流式能力。
     *
     * @param requestBody 请求体
     * @return 响应 JSON
     */
    @Override
    public JsonNode invokeResponses(JsonNode requestBody) {
        return responsesService.create(requestBody);
    }

    /**
     * 调用 Responses 流式能力。
     *
     * @param requestBody 请求体
     * @param emitter SSE 输出器
     */
    @Override
    public void streamResponses(JsonNode requestBody, SseEmitter emitter) {
        responsesService.stream(requestBody, emitter);
    }
}
