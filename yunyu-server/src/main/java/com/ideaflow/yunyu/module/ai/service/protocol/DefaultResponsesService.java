package com.ideaflow.yunyu.module.ai.service.protocol;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.ai.adapter.UpstreamProtocolAdapter;
import com.ideaflow.yunyu.module.ai.adapter.UpstreamProtocolAdapterRegistry;
import com.ideaflow.yunyu.module.ai.model.AiProviderProfile;
import com.ideaflow.yunyu.module.ai.model.UpstreamProtocolType;
import com.ideaflow.yunyu.module.ai.service.config.AiProviderConfigService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Responses 协议服务实现类。
 * 作用：负责 Responses 协议请求标准化、协议匹配校验与上游 responses 调用。
 */
@Service
public class DefaultResponsesService implements ResponsesService {

    private static final String RESPONSES_ENDPOINT = "/v1/responses";

    private final AiProviderConfigService aiProviderConfigService;
    private final UpstreamProtocolAdapterRegistry adapterRegistry;
    private final ObjectMapper objectMapper;

    /**
     * 创建 Responses 协议服务实现。
     *
     * @param aiProviderConfigService AI 提供商配置服务
     * @param adapterRegistry 协议适配器注册表
     * @param objectMapper JSON 对象映射器
     */
    public DefaultResponsesService(AiProviderConfigService aiProviderConfigService,
                                   UpstreamProtocolAdapterRegistry adapterRegistry,
                                   ObjectMapper objectMapper) {
        this.aiProviderConfigService = aiProviderConfigService;
        this.adapterRegistry = adapterRegistry;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行 Responses 非流式调用。
     *
     * @param requestBody 请求体
     * @return 响应 JSON
     */
    @Override
    public JsonNode create(JsonNode requestBody) {
        AiProviderProfile profile = requireResponsesProfile();
        ObjectNode normalizedRequest = normalizeRequest(profile, requestBody, false);
        UpstreamProtocolAdapter adapter = adapterRegistry.getAdapter(UpstreamProtocolType.RESPONSES);
        return adapter.invoke(profile, RESPONSES_ENDPOINT, normalizedRequest);
    }

    /**
     * 执行 Responses 流式调用。
     *
     * @param requestBody 请求体
     * @param emitter SSE 输出器
     */
    @Override
    public void stream(JsonNode requestBody, SseEmitter emitter) {
        AiProviderProfile profile = requireResponsesProfile();
        ObjectNode normalizedRequest = normalizeRequest(profile, requestBody, true);
        UpstreamProtocolAdapter adapter = adapterRegistry.getAdapter(UpstreamProtocolType.RESPONSES);
        adapter.stream(profile, RESPONSES_ENDPOINT, normalizedRequest, emitter);
    }

    /**
     * 校验并获取 responses 协议配置。
     *
     * @return 启用的 responses 配置
     */
    private AiProviderProfile requireResponsesProfile() {
        AiProviderProfile profile = aiProviderConfigService.getActiveProfileOrThrow();
        UpstreamProtocolType protocolType = UpstreamProtocolType.fromText(profile.getUpstreamProtocol());
        if (protocolType != UpstreamProtocolType.RESPONSES) {
            throw new BizException(ResultCode.BAD_REQUEST, "当前启用配置不支持 Responses 协议，请切换 RESPONSES 配置");
        }
        return profile;
    }

    /**
     * 规范化 Responses 请求体。
     *
     * @param profile 配置项
     * @param requestBody 原始请求体
     * @param stream 是否流式
     * @return 规范化后的请求体
     */
    private ObjectNode normalizeRequest(AiProviderProfile profile, JsonNode requestBody, boolean stream) {
        ObjectNode requestNode = requestBody instanceof ObjectNode
                ? ((ObjectNode) requestBody).deepCopy()
                : objectMapper.createObjectNode();
        requestNode.put("stream", stream);

        if (!requestNode.hasNonNull("model") && profile.getModel() != null && !profile.getModel().isBlank()) {
            requestNode.put("model", profile.getModel());
        }

        if (!requestNode.hasNonNull("max_output_tokens") && profile.getMaxTokens() != null && profile.getMaxTokens() > 0) {
            requestNode.put("max_output_tokens", profile.getMaxTokens());
        }

        if (!requestNode.hasNonNull("temperature") && profile.getTemperature() != null) {
            requestNode.put("temperature", profile.getTemperature());
        }

        if (!requestNode.hasNonNull("model")) {
            throw new BizException(ResultCode.BAD_REQUEST, "Responses 请求缺少 model 字段，且启用配置未提供默认模型");
        }
        return requestNode;
    }
}
