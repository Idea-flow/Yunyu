package com.ideaflow.yunyu.module.ai.adapter;

import tools.jackson.databind.JsonNode;
import com.ideaflow.yunyu.module.ai.model.AiProviderProfile;
import com.ideaflow.yunyu.module.ai.model.UpstreamProtocolType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 上游协议适配器接口。
 * 作用：为不同上游协议提供统一调用契约，供网关按协议动态路由。
 */
public interface UpstreamProtocolAdapter {

    /**
     * 返回当前适配器支持的协议类型。
     *
     * @return 协议类型
     */
    UpstreamProtocolType protocolType();

    /**
     * 以非流式方式调用上游接口。
     *
     * @param profile 提供商配置
     * @param endpointPath 上游接口路径
     * @param requestBody 请求体
     * @return 上游响应 JSON
     */
    JsonNode invoke(AiProviderProfile profile, String endpointPath, JsonNode requestBody);

    /**
     * 以流式方式调用上游接口并桥接到 SSE。
     *
     * @param profile 提供商配置
     * @param endpointPath 上游接口路径
     * @param requestBody 请求体
     * @param emitter SSE 输出器
     */
    void stream(AiProviderProfile profile, String endpointPath, JsonNode requestBody, SseEmitter emitter);
}
