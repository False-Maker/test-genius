package com.sinosoft.testdesign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.entity.AppLog;
import com.sinosoft.testdesign.service.AppLogService;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 环绕通知：拦截带有@AppLog注解的方法
     */
    // @Around("@annotation(com.sinosoft.testdesign.common.annotation.AppLog)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            // 执行方法
            Object result = joinPoint.proceed();

            // 记录成功日志
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // saveSuccessLog(joinPoint, duration, result);

            return result;
        } catch (Exception e) {
            // 记录失败日志
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // saveErrorLog(joinPoint, duration, e);

            throw e;
        }
    }

    /**
     * 保存成功日志
     */
    private void saveSuccessLog(ProceedingJoinPoint joinPoint, long duration, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            // 获取注解
            com.sinosoft.testdesign.common.annotation.AppLog appLog =
                method.getAnnotation(com.sinosoft.testdesign.common.annotation.AppLog.class);

            if (appLog == null || (!appLog.logRequest() && !appLog.logResponse())) {
                return;
            }

            AppLog logEntity = new AppLog();
            logEntity.setRequestId(UUID.randomUUID().toString());
            logEntity.setAppType(appLog.appType());

            // 获取请求信息
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                logEntity.setRequestMethod(request.getMethod());
                logEntity.setRequestUrl(request.getRequestURI());
                logEntity.setIpAddress(getClientIp(request));
            }

            // 执行结果
            logEntity.setStatus("success");
            logEntity.setResponseTime((int) duration);
            logEntity.setError(null);

            // 记录参数
            if (appLog.logRequest()) {
                logEntity.setRequestParams(getRequestParams(joinPoint));
            }

            // 记录响应数据
            if (appLog.logResponse() && result != null) {
                logEntity.setResponseData(objectMapper.writeValueAsString(result));
            }

            logEntity.setTimestamp(LocalDateTime.now());
            appLogService.log(logEntity);

        } catch (Exception e) {
            log.error("保存应用日志失败", e);
        }
    }

    /**
     * 保存错误日志
     */
    private void saveErrorLog(ProceedingJoinPoint joinPoint, long duration, Exception e) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            // 获取注解
            com.sinosoft.testdesign.common.annotation.AppLog appLog =
                method.getAnnotation(com.sinosoft.testdesign.common.annotation.AppLog.class);

            if (appLog == null || (!appLog.logRequest() && !appLog.logResponse())) {
                return;
            }

            AppLog logEntity = new AppLog();
            logEntity.setRequestId(UUID.randomUUID().toString());
            logEntity.setAppType(appLog.appType());

            // 获取请求信息
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                logEntity.setRequestMethod(request.getMethod());
                logEntity.setRequestUrl(request.getRequestURI());
                logEntity.setIpAddress(getClientIp(request));
            }

            // 执行结果
            logEntity.setStatus("failed");
            logEntity.setResponseTime((int) duration);
            logEntity.setError(e.getMessage());

            // 记录参数
            if (appLog.logRequest()) {
                logEntity.setRequestParams(getRequestParams(joinPoint));
            }

            logEntity.setTimestamp(LocalDateTime.now());
            appLogService.log(logEntity);

        } catch (Exception ex) {
            log.error("保存错误日志失败", ex);
        }
    }

    /**
     * 获取当前HTTP请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            Map<String, Object> params = new HashMap<>();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null) {
                    // 跳过HttpServletRequest等特殊类型
                    if (arg instanceof HttpServletRequest) {
                        continue;
                    }
                    params.put("arg" + i, arg);
                }
            }

            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.error("获取请求参数失败", e);
            return "{}";
        }
    }
}
