package com.ideaflow.yunyu.common.response;

import com.ideaflow.yunyu.common.constant.ResultCode;
import lombok.Data;

/**
 * 统一接口返回对象。
 * 作用：统一所有接口的响应结构，避免不同模块各自返回不一致的数据格式。
 *
 * @param <T> 数据载荷类型
 */
@Data
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构建成功响应，不携带业务数据。
     *
     * @return 成功响应对象
     * @param <T> 数据载荷类型
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 构建成功响应，并携带业务数据。
     *
     * @param data 业务数据
     * @return 成功响应对象
     * @param <T> 数据载荷类型
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 按统一返回码构建失败响应。
     *
     * @param resultCode 统一返回码
     * @return 失败响应对象
     * @param <T> 数据载荷类型
     */
    public static <T> ApiResponse<T> fail(ResultCode resultCode) {
        return new ApiResponse<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 按统一返回码和自定义消息构建失败响应。
     *
     * @param resultCode 统一返回码
     * @param message 自定义消息
     * @return 失败响应对象
     * @param <T> 数据载荷类型
     */
    public static <T> ApiResponse<T> fail(ResultCode resultCode, String message) {
        return new ApiResponse<>(resultCode.getCode(), message, null);
    }

    /**
     * 按自定义状态码和消息构建失败响应。
     *
     * @param code 业务状态码
     * @param message 自定义消息
     * @return 失败响应对象
     * @param <T> 数据载荷类型
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
