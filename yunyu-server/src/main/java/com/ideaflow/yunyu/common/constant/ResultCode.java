package com.ideaflow.yunyu.common.constant;

/**
 * 统一返回码枚举。
 * 作用：约定接口层统一的业务状态码与默认提示语，便于前后端对齐响应语义。
 */
public enum ResultCode {

    SUCCESS(0, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或认证已失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取业务状态码。
     *
     * @return 业务状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取默认提示语。
     *
     * @return 默认提示语
     */
    public String getMessage() {
        return message;
    }
}
