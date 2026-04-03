package com.ideaflow.yunyu.common.exception;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器。
 * 作用：集中处理控制层抛出的异常，并统一转换为标准接口响应结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @param request 当前请求
     * @return 统一失败响应
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException exception, HttpServletRequest request) {
        log.warn("业务异常，请求路径：{}，错误码：{}，错误信息：{}",
                request.getRequestURI(),
                exception.getCode(),
                exception.getMessage());
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理请求参数校验异常。
     *
     * @param exception 参数校验异常
     * @param request 当前请求
     * @return 统一失败响应
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleValidationException(Exception exception, HttpServletRequest request) {
        log.warn("参数校验异常，请求路径：{}，错误信息：{}",
                request.getRequestURI(),
                exception.getMessage());
        return ApiResponse.fail(ResultCode.BAD_REQUEST, resolveValidationMessage(exception));
    }

    /**
     * 处理请求方法不支持异常。
     *
     * @param exception 请求方法异常
     * @param request 当前请求
     * @return 统一失败响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException exception,
                                                               HttpServletRequest request) {
        log.warn("请求方法不支持，请求路径：{}，请求方法：{}",
                request.getRequestURI(),
                exception.getMethod());
        return ApiResponse.fail(ResultCode.BAD_REQUEST, "请求方法不正确");
    }

    /**
     * 处理资源不存在异常。
     *
     * @param exception 资源不存在异常
     * @param request 当前请求
     * @return 统一失败响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNoResourceFoundException(NoResourceFoundException exception, HttpServletRequest request) {
        return ApiResponse.fail(ResultCode.NOT_FOUND, "资源不存在：" + request.getRequestURI());
    }

    /**
     * 处理数据库唯一索引冲突异常。
     *
     * @param exception 唯一索引冲突异常
     * @param request 当前请求
     * @return 统一失败响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicateKeyException(DuplicateKeyException exception, HttpServletRequest request) {
        log.warn("数据库唯一索引冲突，请求路径：{}，错误信息：{}",
                request.getRequestURI(),
                exception.getMessage());
        return ApiResponse.fail(ResultCode.BAD_REQUEST, "数据已存在，请勿重复提交");
    }

    /**
     * 处理兜底异常。
     *
     * @param exception 未分类异常
     * @param request 当前请求
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception, HttpServletRequest request) {
        log.error("未处理异常，请求路径：{}", request.getRequestURI(), exception);
        return ApiResponse.fail(ResultCode.INTERNAL_SERVER_ERROR, ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    /**
     * 解析参数校验异常中的具体错误信息。
     *
     * @param exception 参数校验异常
     * @return 具体错误信息
     */
    private String resolveValidationMessage(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            return resolveBindingResultMessage(methodArgumentNotValidException.getBindingResult());
        }

        if (exception instanceof BindException bindException) {
            return resolveBindingResultMessage(bindException.getBindingResult());
        }

        if (exception instanceof ConstraintViolationException constraintViolationException) {
            return constraintViolationException.getConstraintViolations()
                    .stream()
                    .findFirst()
                    .map(violation -> violation.getMessage())
                    .orElse(ResultCode.BAD_REQUEST.getMessage());
        }

        if (exception instanceof HttpMessageNotReadableException) {
            return "请求体格式不正确";
        }

        return ResultCode.BAD_REQUEST.getMessage();
    }

    /**
     * 从绑定结果中提取首个可读错误文案。
     *
     * @param bindingResult 绑定结果
     * @return 错误文案
     */
    private String resolveBindingResultMessage(BindingResult bindingResult) {
        String message = bindingResult.getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);

        if (message != null) {
            return message;
        }

        return bindingResult.getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(ResultCode.BAD_REQUEST.getMessage());
    }
}
