package com.flowmeal.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.flowmeal.common.result.Result;
import com.flowmeal.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常拦截器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Valid 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理 BindException（GET表单参数校验）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * Sa-Token: 未登录
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录: {}", e.getMessage());
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * Sa-Token: 无角色权限
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("无权限访问: {}", e.getMessage());
        return Result.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail("系统繁忙，请稍后重试");
    }
}
