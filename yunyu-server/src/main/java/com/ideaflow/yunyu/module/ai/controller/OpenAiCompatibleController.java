package com.ideaflow.yunyu.module.ai.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.ai.service.gateway.AiGatewayFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * OpenAI 兼容接口控制器。
 * 作用：对外提供与 OpenAI 协议字段和交互尽量一致的 chat 与 responses 接口。
 */
@Tag(name = "OpenAI 兼容接口")
@RestController
@RequestMapping("/v1")
public class OpenAiCompatibleController {

    private final AiGatewayFacade aiGatewayFacade;
    private final ObjectMapper objectMapper;

    /**
     * 创建 OpenAI 兼容接口控制器。
     *
     * @param aiGatewayFacade AI 网关门面
     * @param objectMapper JSON 对象映射器
     */
    public OpenAiCompatibleController(AiGatewayFacade aiGatewayFacade,
                                      ObjectMapper objectMapper) {
        this.aiGatewayFacade = aiGatewayFacade;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用 Chat Completions 接口。
     *
     * @param requestBody OpenAI Chat 请求体
     * @return OpenAI Chat 响应或流式响应
     */
    @Operation(summary = "OpenAI Chat Completions 兼容接口")
    @PostMapping("/chat/completions")
    public ResponseEntity<?> chatCompletions(@RequestBody(required = false) JsonNode requestBody) {
        try {
            JsonNode payload = requestBody == null ? objectMapper.createObjectNode() : requestBody;
            if (isStream(payload)) {
                SseEmitter emitter = new SseEmitter(0L);
                aiGatewayFacade.streamChat(payload, emitter);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(emitter);
            }
            return ResponseEntity.ok(aiGatewayFacade.invokeChat(payload));
        } catch (Exception exception) {
            return buildOpenAiErrorResponse(exception);
        }
    }

    /**
     * 调用 Responses 接口（单数路径）。
     *
     * @param requestBody OpenAI Responses 请求体
     * @return OpenAI Responses 响应或流式响应
     */
    @Operation(summary = "OpenAI Responses 兼容接口（/v1/response）")
    @PostMapping("/response")
    public ResponseEntity<?> responsesSingle(@RequestBody(required = false) JsonNode requestBody) {
        return responsesInternal(requestBody);
    }

    /**
     * 调用 Responses 接口（标准复数路径）。
     *
     * @param requestBody OpenAI Responses 请求体
     * @return OpenAI Responses 响应或流式响应
     */
    @Operation(summary = "OpenAI Responses 兼容接口（/v1/responses）")
    @PostMapping("/responses")
    public ResponseEntity<?> responsesPlural(@RequestBody(required = false) JsonNode requestBody) {
        return responsesInternal(requestBody);
    }

    /**
     * 处理 Responses 协议调用。
     *
     * @param requestBody 请求体
     * @return 响应实体
     */
    private ResponseEntity<?> responsesInternal(JsonNode requestBody) {
        try {
            JsonNode payload = requestBody == null ? objectMapper.createObjectNode() : requestBody;
            if (isStream(payload)) {
                SseEmitter emitter = new SseEmitter(0L);
                aiGatewayFacade.streamResponses(payload, emitter);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(emitter);
            }
            return ResponseEntity.ok(aiGatewayFacade.invokeResponses(payload));
        } catch (Exception exception) {
            return buildOpenAiErrorResponse(exception);
        }
    }

    /**
     * 判断请求是否要求流式响应。
     *
     * @param requestBody 请求体
     * @return 是否流式
     */
    private boolean isStream(JsonNode requestBody) {
        return requestBody != null && requestBody.path("stream").asBoolean(false);
    }

    /**
     * 构建 OpenAI 风格错误响应。
     *
     * @param exception 异常对象
     * @return OpenAI 风格错误响应
     */
    private ResponseEntity<JsonNode> buildOpenAiErrorResponse(Exception exception) {
        String message = exception.getMessage();
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

        if (exception instanceof BizException bizException) {
            statusCode = mapBizStatusCode(bizException.getCode());
            message = bizException.getMessage();
        }

        ObjectNode rootNode = objectMapper.createObjectNode();
        ObjectNode errorNode = rootNode.putObject("error");
        errorNode.put("message", message == null || message.isBlank() ? "AI 服务调用失败" : message);
        errorNode.put("type", "api_error");
        errorNode.putNull("param");
        errorNode.putNull("code");
        return ResponseEntity.status(statusCode).body(rootNode);
    }

    /**
     * 将业务状态码映射到 HTTP 状态码。
     *
     * @param bizCode 业务状态码
     * @return HTTP 状态码
     */
    private int mapBizStatusCode(int bizCode) {
        if (bizCode == 400) {
            return HttpStatus.BAD_REQUEST.value();
        }
        if (bizCode == 401) {
            return HttpStatus.UNAUTHORIZED.value();
        }
        if (bizCode == 403) {
            return HttpStatus.FORBIDDEN.value();
        }
        if (bizCode == 404) {
            return HttpStatus.NOT_FOUND.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
