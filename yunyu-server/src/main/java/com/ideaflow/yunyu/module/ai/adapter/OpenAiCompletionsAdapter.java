package com.ideaflow.yunyu.module.ai.adapter;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.ideaflow.yunyu.module.ai.model.AiProviderProfile;
import com.ideaflow.yunyu.module.ai.model.UpstreamProtocolType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * OpenAI Chat Completions 协议适配器。
 * 作用：承接 completions 协议下的非流式与流式请求转发。
 */
@Component
public class OpenAiCompletionsAdapter extends AbstractOpenAiAdapter implements UpstreamProtocolAdapter {

    /**
     * 创建 completions 协议适配器。
     *
     * @param objectMapper JSON 对象映射器
     */
    public OpenAiCompletionsAdapter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    /**
     * 返回当前适配器支持的协议类型。
     *
     * @return 协议类型
     */
    @Override
    public UpstreamProtocolType protocolType() {
        return UpstreamProtocolType.COMPLETIONS;
    }

    /**
     * 执行 completions 协议非流式调用。
     *
     * @param profile 提供商配置
     * @param endpointPath 上游接口路径
     * @param requestBody 请求体
     * @return 上游响应
     */
    @Override
    public JsonNode invoke(AiProviderProfile profile, String endpointPath, JsonNode requestBody) {
        return invokeJson(profile, endpointPath, requestBody);
    }

    /**
     * 执行 completions 协议流式调用。
     *
     * @param profile 提供商配置
     * @param endpointPath 上游接口路径
     * @param requestBody 请求体
     * @param emitter SSE 输出器
     */
    @Override
    public void stream(AiProviderProfile profile, String endpointPath, JsonNode requestBody, SseEmitter emitter) {
        streamJson(profile, endpointPath, requestBody, emitter);
    }
}
