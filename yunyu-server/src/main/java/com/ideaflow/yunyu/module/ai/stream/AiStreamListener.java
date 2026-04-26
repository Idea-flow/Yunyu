package com.ideaflow.yunyu.module.ai.stream;

/**
 * AI 流式监听器接口。
 * 作用：为协议层与网关层提供统一的流式事件回调能力，便于桥接到 SSE 输出。
 */
public interface AiStreamListener {

    /**
     * 处理流式事件。
     *
     * @param event 流式事件
     */
    void onEvent(AiStreamEvent event);
}
