package com.ideaflow.yunyu.module.ai.stream;

/**
 * AI 流式事件类型枚举。
 * 作用：统一描述流式调用中开始、增量、完成与异常四类事件语义。
 */
public enum AiStreamEventType {

    START,
    DELTA,
    COMPLETE,
    ERROR
}
