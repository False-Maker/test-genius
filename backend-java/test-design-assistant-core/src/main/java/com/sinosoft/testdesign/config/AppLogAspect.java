package com.sinosoft.testdesign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.annotation.AppLog;
import com.sinosoft.testdesign.entity.AppLog;
import com.sinosoft.testdesign.service.AppLogService;
import com.sinosoft.testdesign.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 应用日志切面
 * 自动记录模型调用和应用操作的日志
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AppLogAspect {
    
    private final AppLogService appLogService;
    private final ObjectMapper objectMapper;
    
    /**
     * 拦截标记了@AppLog注解的方法
     */
    @Around("@annotation(com.sinosoft.testdesign.common.annotation.AppLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.sinosoft.testdesign.common.annotation.AppLog appLogAnnotation = method.getAnnotation(com.sinosoft.testdesign.common.annotation.AppLog.class);
        
        if (appLogAnnotation == null) {
            return joinPoint.proceed();
        }
        
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        Object result = null;
        Exception exception = null;
        String appType = appLogAnnotation.appType();
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                recordAppLog(joinPoint, appLogAnnotation, requestId, result, exception, 
                    System.currentTimeMillis() - startTime, appType);
            } catch (Exception e) {
                log.error("记录应用日志失败", e);
            }
        }
    }
    
    /**
     * 记录应用日志
     */
    private void recordAppLog(ProceedingJoinPoint joinPoint, com.sinosoft.testdesign.common.annotation.AppLog appLogAnnotation, 
                             String requestId, Object result, Exception exception, 
                             long executionTime, String appType) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
            
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 构建应用日志
            AppLog.AppLogBuilder builder = AppLog.builder()
                    .requestId(requestId)
                    .userId(getCurrentUserId())
                    .userName(getCurrentUserName())
                    .appType(appType.isEmpty() ? getAppTypeFromMethod(method) : appType)
                    .responseTime((int) executionTime)
                    .status(exception == null ? "success" : "failed")
                    .timestamp(LocalDateTime.now());
            
            // 从请求参数中提取模型相关信息
            extractModelInfo(joinPoint.getArgs(), builder);
            
            // 记录请求信息
            if (request != null) {
                builder.ipAddress(getClientIpAddress(request))
                       .userAgent(request.getHeader("User-Agent"))
                       .requestUrl(request.getRequestURI())
                       .requestMethod(request.getMethod());
            }
            
            // 记录请求参数
            if (appLogAnnotation.logRequest()) {
                String requestParams = getRequestParams(joinPoint.getArgs());
                builder.requestParams(requestParams);
                
                // 尝试从请求参数中提取prompt
                String prompt = extractPrompt(joinPoint.getArgs());
                if (prompt != null) {
                    builder.prompt(prompt)
                           .promptLength(prompt.length());
                }
            }
            
            // 记录响应结果
            if (appLogAnnotation.logResponse() && result != null) {
                try {
                    String responseData = objectMapper.writeValueAsString(result);
                    builder.responseData(responseData);
                    
                    // 尝试从响应中提取模型相关信息
                    if (result instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> resultMap = (Map<String, Object>) result;
                        
                        // 提取模型代码
                        if (resultMap.containsKey("model_code")) {
                            builder.modelCode((String) resultMap.get("model_code"));
                        }
                        
                        // 提取响应内容
                        if (resultMap.containsKey("content")) {
                            String content = String.valueOf(resultMap.get("content"));
                            builder.response(content)
                                   .responseLength(content.length());
                        }
                        
                        // 提取token信息
                        if (resultMap.containsKey("tokens_used")) {
                            Object tokensObj = resultMap.get("tokens_used");
                            if (tokensObj instanceof Number) {
                                builder.tokensTotal(((Number) tokensObj).intValue());
                            }
                        }
                        
                        // 提取响应时间
                        if (resultMap.containsKey("response_time")) {
                            Object timeObj = resultMap.get("response_time");
                            if (timeObj instanceof Number) {
                                builder.responseTime(((Number) timeObj).intValue());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("序列化响应结果失败", e);
                }
            }
            
            // 记录错误信息
            if (exception != null) {
                builder.error(exception.getMessage())
                       .errorCode(exception.getClass().getSimpleName());
            }
            
            // 异步记录日志
            appLogService.logAsync(builder.build());
            
        } catch (Exception e) {
            log.error("构建应用日志失败", e);
        }
    }
    
    /**
     * 从方法参数中提取模型信息
     */
    private void extractModelInfo(Object[] args, AppLog.AppLogBuilder builder) {
        if (args == null) {
            return;
        }
        
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            
            // 如果是Map类型，尝试提取模型代码
            if (arg instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) arg;
                if (map.containsKey("model_code")) {
                    builder.modelCode((String) map.get("model_code"));
                }
                if (map.containsKey("model_name")) {
                    builder.modelName((String) map.get("model_name"));
                }
            }
        }
    }
    
    /**
     * 从方法参数中提取prompt
     */
    private String extractPrompt(Object[] args) {
        if (args == null) {
            return null;
        }
        
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            
            // 如果是Map类型，尝试提取prompt
            if (arg instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) arg;
                if (map.containsKey("prompt")) {
                    return String.valueOf(map.get("prompt"));
                }
            }
            
            // 如果是String类型，可能是prompt
            if (arg instanceof String && ((String) arg).length() > 50) {
                return (String) arg;
            }
        }
        
        return null;
    }
    
    /**
     * 获取应用类型（从方法名推断）
     */
    private String getAppTypeFromMethod(Method method) {
        String methodName = method.getName().toLowerCase();
        String className = method.getDeclaringClass().getSimpleName().toLowerCase();
        
        if (methodName.contains("case") || className.contains("case")) {
            return "CASE_GENERATION";
        } else if (methodName.contains("script") || className.contains("script")) {
            return "UI_SCRIPT_GENERATION";
        } else if (methodName.contains("document") || className.contains("document")) {
            return "DOCUMENT_PARSING";
        } else if (methodName.contains("llm") || methodName.contains("model")) {
            return "MODEL_CALL";
        } else {
            return "OTHER";
        }
    }
    
    /**
     * 获取请求参数
     */
    private String getRequestParams(Object[] args) {
        try {
            if (args == null || args.length == 0) {
                return null;
            }
            // 只记录前几个参数，避免日志过大
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < Math.min(args.length, 5); i++) {
                params.put("arg" + i, args[i]);
            }
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
    
    /**
     * 获取当前用户名
     */
    private String getCurrentUserName() {
        return SecurityUtils.getCurrentUserName();
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
