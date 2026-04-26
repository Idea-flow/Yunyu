package com.ideaflow.yunyu.module.ai.stream;

import java.util.Map;
import lombok.Data;

/**
 * AI 流式事件模型。
 * 作用：在协议服务、网关与控制器之间传递统一的流式事件载荷。
 */
@Data
public class AiStreamEvent {

    private AiStreamEventType type;
    private String traceId;
    private String content;
    private Map<String, Object> meta;
}
