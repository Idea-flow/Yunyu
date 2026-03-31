package com.ideaflow.yunyu.common.exception;

import com.ideaflow.yunyu.common.constant.ResultCode;

/**
 * 业务异常类。
 * 作用：在业务流程中主动抛出明确的业务错误，并映射到统一接口返回结构。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(ResultCode.BAD_REQUEST, message);
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 获取业务异常对应的状态码。
     *
     * @return 业务状态码
     */
    public int getCode() {
        return code;
    }
}
