package com.ideaflow.yunyu.module.ai.adapter;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.ai.model.AiProviderProfile;
import com.ideaflow.yunyu.module.ai.stream.SseBridge;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * OpenAI 协议适配抽象基类。
 * 作用：统一封装 HTTP 调用、错误解析与 SSE 桥接流程，降低具体协议适配器重复代码。
 */
public abstract class AbstractOpenAiAdapter {

    private final ObjectMapper objectMapper;
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();

    /**
     * 创建 OpenAI 适配器抽象基类。
     *
     * @param objectMapper JSON 对象映射器
     */
    protected AbstractOpenAiAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 执行上游非流式请求。
     *
     * @param profile 提供商配置
     * @param endpointPath 上游接口路径
     * @param requestBody 请求体
     * @return 上游响应 JSON
     */
    protected JsonNode invokeJson(AiProviderProfile profile, String endpointPath, JsonNode requestBody) {
        try {
            HttpClient httpClient = createHttpClient(profile);
            HttpRequest request = buildJsonRequest(profile, endpointPath, requestBody);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return parseResponseBody(response.body());
            }
            throw new BizException(ResultCode.BAD_REQUEST, resolveErrorMessage(response.statusCode(), response.body()));
        } catch (BizException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "请求上游 AI 服务时被中断");
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "请求上游 AI 服务失败：" + exception.getMessage());
        }
    }

    /**
     * 执行上游流式请求。
     *
     * @param profile 提供商配置
     * @param endpointPath 上游接口路径
     * @param requestBody 请求体
     * @param emitter SSE 输出器
     */
    protected void streamJson(AiProviderProfile profile, String endpointPath, JsonNode requestBody, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> doStream(profile, endpointPath, requestBody, emitter), streamExecutor);
    }

    /**
     * 关闭流式执行线程池。
     *
     * @throws InterruptedException 线程中断异常
     */
    public void shutdown() throws InterruptedException {
        streamExecutor.shutdown();
        streamExecutor.awaitTermination(3, TimeUnit.SECONDS);
    }

    /**
     * 真正执行流式请求并透传 SSE 数据。
     *
     * @param profile 提供商配置
     * @param endpointPath 上游接口路径
     * @param requestBody 请求体
     * @param emitter SSE 输出器
     */
    private void doStream(AiProviderProfile profile, String endpointPath, JsonNode requestBody, SseEmitter emitter) {
        try {
            HttpClient httpClient = createHttpClient(profile);
            HttpRequest request = buildJsonRequest(profile, endpointPath, requestBody);
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody = readStreamBody(response.body());
                SseBridge.sendErrorAndComplete(emitter, buildOpenAiErrorJson(resolveErrorMessage(response.statusCode(), errorBody)));
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith(":")) {
                        continue;
                    }

                    if (line.startsWith("data:")) {
                        String payload = line.substring(5).trim();
                        SseBridge.sendData(emitter, payload);
                        continue;
                    }

                    SseBridge.sendData(emitter, line);
                }
            }
            emitter.complete();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            SseBridge.sendErrorAndComplete(emitter, buildOpenAiErrorJson("请求上游 AI 流式服务时被中断"));
        } catch (Exception exception) {
            SseBridge.sendErrorAndComplete(emitter, buildOpenAiErrorJson("请求上游 AI 流式服务失败：" + exception.getMessage()));
        }
    }

    /**
     * 创建 HTTP 客户端。
     *
     * @param profile 提供商配置
     * @return HTTP 客户端
     */
    private HttpClient createHttpClient(AiProviderProfile profile) {
        int connectTimeoutMs = resolveTimeout(profile.getConnectTimeoutMs(), 3000);
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
    }

    /**
     * 构建 JSON 请求对象。
     *
     * @param profile 提供商配置
     * @param endpointPath 上游接口路径
     * @param requestBody 请求体
     * @return HTTP 请求
     */
    private HttpRequest buildJsonRequest(AiProviderProfile profile, String endpointPath, JsonNode requestBody) {
        int requestTimeoutMs = resolveTimeout(profile.getReadTimeoutMs(), 15000);
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(requestBody == null ? objectMapper.createObjectNode() : requestBody);
        } catch (Exception exception) {
            throw new BizException(ResultCode.BAD_REQUEST, "AI 请求体序列化失败");
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(buildEndpointUri(profile.getUpstreamBaseUrl(), endpointPath))
                .timeout(Duration.ofMillis(requestTimeoutMs))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8));

        if (profile.getApiKey() != null && !profile.getApiKey().isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + profile.getApiKey().trim());
        }
        return requestBuilder.build();
    }

    /**
     * 构建上游接口 URI。
     *
     * @param baseUrl 上游基础地址
     * @param endpointPath 上游接口路径
     * @return URI
     */
    private URI buildEndpointUri(String baseUrl, String endpointPath) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "上游服务地址不能为空");
        }

        String normalizedBaseUrl = baseUrl.trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }

        String normalizedPath = endpointPath == null || endpointPath.isBlank()
                ? ""
                : (endpointPath.startsWith("/") ? endpointPath.trim() : "/" + endpointPath.trim());
        return URI.create(normalizedBaseUrl + normalizedPath);
    }

    /**
     * 解析上游响应正文。
     *
     * @param responseBody 原始正文
     * @return JSON 节点
     */
    private JsonNode parseResponseBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            return objectMapper.readTree(responseBody);
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "上游 AI 响应解析失败");
        }
    }

    /**
     * 读取流式响应体。
     *
     * @param inputStream 输入流
     * @return 全量字符串
     */
    private String readStreamBody(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (Exception exception) {
            return "";
        }
    }

    /**
     * 解析上游错误信息。
     *
     * @param statusCode HTTP 状态码
     * @param responseBody 响应体
     * @return 错误提示
     */
    private String resolveErrorMessage(int statusCode, String responseBody) {
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode errorNode = rootNode.path("error");
                if (!errorNode.isMissingNode()) {
                    String message = errorNode.path("message").asText("");
                    if (!message.isBlank()) {
                        return message;
                    }
                }
            } catch (Exception ignored) {
                // 忽略解析异常，回退通用提示。
            }
        }
        return "上游 AI 服务调用失败，HTTP 状态码：" + statusCode;
    }

    /**
     * 构建 OpenAI 风格错误 JSON。
     *
     * @param message 错误提示
     * @return 错误 JSON 字符串
     */
    private String buildOpenAiErrorJson(String message) {
        ObjectNode rootNode = objectMapper.createObjectNode();
        ObjectNode errorNode = rootNode.putObject("error");
        errorNode.put("message", message == null || message.isBlank() ? "AI 服务调用失败" : message);
        errorNode.put("type", "api_error");
        errorNode.putNull("param");
        errorNode.putNull("code");
        return rootNode.toString();
    }

    /**
     * 解析超时参数并应用默认值。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 超时毫秒值
     */
    private int resolveTimeout(Integer value, int defaultValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return value;
    }
}
