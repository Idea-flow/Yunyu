package com.ideaflow.yunyu.module.ai.admin.controller;

import com.ideaflow.yunyu.module.ai.admin.dto.AdminPostAiMetaGenerateRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.ai.admin.scene.post.PostMetaGenerateService;
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
 * 后台文章 AI 能力控制器。
 * 作用：提供文章元信息生成接口，并基于 stream 开关分流非流式与流式调用链路。
 */
@Tag(name = "后台文章 AI 能力")
@RestController
@RequestMapping("/api/admin/posts/ai")
public class AdminPostAiController {

    private final PostMetaGenerateService postMetaGenerateService;
    private final ObjectMapper objectMapper;

    /**
     * 创建后台文章 AI 控制器。
     *
     * @param postMetaGenerateService 文章元信息场景服务
     * @param objectMapper JSON 对象映射器
     */
    public AdminPostAiController(PostMetaGenerateService postMetaGenerateService,
                                 ObjectMapper objectMapper) {
        this.postMetaGenerateService = postMetaGenerateService;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成文章元信息。
     * 该接口底层固定走 Chat Completions 协议服务，并由后端根据标题与正文统一构造提示词。
     *
     * @param request 元信息生成请求
     * @return OpenAI Chat 风格响应或流式响应
     */
    @Operation(summary = "生成文章 Slug/摘要/SEO 元信息")
    @PostMapping("/meta/generate")
    public ResponseEntity<?> generateMeta(@RequestBody(required = false) AdminPostAiMetaGenerateRequest request) {
        try {
            if (isStream(request)) {
                SseEmitter emitter = new SseEmitter(0L);
                postMetaGenerateService.streamGenerate(request, emitter);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(emitter);
            }
            return ResponseEntity.ok(postMetaGenerateService.generate(request));
        } catch (Exception exception) {
            return buildOpenAiErrorResponse(exception);
        }
    }

    /**
     * 判断请求是否要求流式响应。
     *
     * @param request 请求对象
     * @return 是否流式
     */
    private boolean isStream(AdminPostAiMetaGenerateRequest request) {
        return request != null && Boolean.TRUE.equals(request.getStream());
    }

    /**
     * 构建 OpenAI 风格错误响应。
     *
     * @param exception 异常对象
     * @return 错误响应
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
