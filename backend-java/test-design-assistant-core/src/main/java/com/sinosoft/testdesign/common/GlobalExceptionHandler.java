package com.sinosoft.testdesign.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(Exception e) {
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        log.warn("参数校验失败: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParameterException(MissingServletRequestParameterException e) {
        String message = String.format("缺少必需参数: %s", e.getParameterName());
        log.warn("缺少请求参数: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    /**
     * 处理请求体解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = "请求体格式错误，请检查JSON格式";
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    /**
     * 处理HTTP方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String message = String.format("不支持的HTTP方法: %s", e.getMethod());
        log.warn("HTTP方法不支持: {}", message);
        return Result.fail(ResultCode.FORBIDDEN.getCode(), message);
    }
    
    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleDataAccessException(DataAccessException e) {
        log.error("数据库访问异常", e);
        return Result.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "数据库操作失败，请稍后重试");
    }
    
    /**
     * 处理外部服务调用异常（RestTemplate相关）
     */
    @ExceptionHandler({RestClientException.class, ResourceAccessException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRestClientException(Exception e) {
        log.error("外部服务调用异常", e);
        String message = "外部服务调用失败";
        if (e instanceof ResourceAccessException) {
            message = "无法连接到外部服务，请检查服务是否可用";
        }
        return Result.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message);
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: [{}] {}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode() != null ? e.getCode() : ResultCode.BUSINESS_ERROR.getCode(), e.getMessage());
    }
    
    /**
     * 处理安全异常（SQL注入、XSS等）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleSecurityException(IllegalArgumentException e) {
        String message = e.getMessage();
        if (message != null && (message.contains("SQL注入") || message.contains("XSS"))) {
            log.warn("安全风险检测: {}", message);
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), message);
        }
        // 其他IllegalArgumentException按普通参数错误处理
        log.warn("参数错误: {}", message);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), message != null ? message : "参数错误");
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return Result.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "系统异常，请联系管理员");
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "系统异常，请联系管理员");
    }
}

