package com.ideaflow.yunyu.module.ai.model;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import java.util.Locale;

/**
 * 上游协议类型枚举。
 * 作用：约束 AI 配置与调用链路中可选的上游协议类型，避免自由字符串导致路由漂移。
 */
public enum UpstreamProtocolType {

    COMPLETIONS,
    RESPONSES;

    /**
     * 将文本解析为上游协议枚举。
     *
     * @param value 原始文本
     * @return 上游协议枚举
     */
    public static UpstreamProtocolType fromText(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "上游协议类型不能为空");
        }

        String normalizedValue = value.trim().toUpperCase(Locale.ROOT);
        try {
            return UpstreamProtocolType.valueOf(normalizedValue);
        } catch (IllegalArgumentException exception) {
            throw new BizException(ResultCode.BAD_REQUEST, "不支持的上游协议类型：" + value);
        }
    }
}
