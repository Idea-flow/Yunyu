package com.ideaflow.yunyu.common.exception;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * 作用：集中处理控制层抛出的异常，并统一转换为标准接口响应结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理请求参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 统一失败响应
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleValidationException(Exception exception) {
        return ApiResponse.fail(ResultCode.BAD_REQUEST, "请求参数错误");
    }

    /**
     * 处理兜底异常。
     *
     * @param exception 未分类异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        return ApiResponse.fail(ResultCode.INTERNAL_SERVER_ERROR, ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }
}
