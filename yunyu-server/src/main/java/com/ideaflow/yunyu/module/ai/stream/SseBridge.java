package com.ideaflow.yunyu.module.ai.stream;

import java.io.IOException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 事件桥接工具类。
 * 作用：将统一流式事件转换为 SSE 输出，保持控制器层发送逻辑一致。
 */
public final class SseBridge {

    private SseBridge() {
    }

    /**
     * 向 SSE 连接发送普通数据。
     *
     * @param emitter SSE 输出器
     * @param data 输出数据
     */
    public static void sendData(SseEmitter emitter, String data) {
        try {
            emitter.send(data == null ? "" : data);
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
    }

    /**
     * 向 SSE 连接发送错误数据并结束连接。
     *
     * @param emitter SSE 输出器
     * @param data 错误数据
     */
    public static void sendErrorAndComplete(SseEmitter emitter, String data) {
        sendData(emitter, data);
        emitter.complete();
    }
}
