package com.sinosoft.testdesign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.entity.AuditLog;
import com.sinosoft.testdesign.service.AuditLogService;
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
import java.util.Arrays;

/**
 * 审计日志切面
 * 自动记录Controller层的操作
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {
    
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    
    /**
     * 拦截所有Controller方法
     */
    @Around("execution(* com.sinosoft.testdesign.controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                recordAuditLog(joinPoint, result, exception, System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                log.error("记录审计日志失败", e);
            }
        }
    }
    
    /**
     * 记录审计日志
     */
    private void recordAuditLog(ProceedingJoinPoint joinPoint, Object result, Exception exception, long executionTime) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            
            HttpServletRequest request = attributes.getRequest();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取操作类型（根据HTTP方法）
            String operationType = getOperationType(request.getMethod(), method.getName());
            
            // 获取操作模块（根据Controller类名）
            String operationModule = getOperationModule(joinPoint.getTarget().getClass().getSimpleName());
            
            // 构建审计日志
            AuditLog auditLog = AuditLog.builder()
                    .userId(getCurrentUserId()) // 从SecurityContext或Session获取
                    .userName(getCurrentUserName())
                    .operationType(operationType)
                    .operationModule(operationModule)
                    .operationTarget(getOperationTarget(joinPoint.getArgs()))
                    .operationContent(getOperationContent(method, joinPoint.getArgs()))
                    .operationResult(exception == null ? "SUCCESS" : "FAILURE")
                    .errorMessage(exception != null ? exception.getMessage() : null)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .requestUrl(request.getRequestURI())
                    .requestMethod(request.getMethod())
                    .requestParams(getRequestParams(joinPoint.getArgs()))
                    .responseStatus(exception == null ? 200 : 500)
                    .executionTime(executionTime)
                    .createTime(LocalDateTime.now())
                    .build();
            
            // 异步记录审计日志
            auditLogService.logAsync(auditLog);
        } catch (Exception e) {
            log.error("构建审计日志失败", e);
        }
    }
    
    /**
     * 获取操作类型
     */
    private String getOperationType(String httpMethod, String methodName) {
        if (methodName.contains("create") || methodName.contains("add") || methodName.contains("save")) {
            return "CREATE";
        } else if (methodName.contains("update") || methodName.contains("modify") || methodName.contains("edit")) {
            return "UPDATE";
        } else if (methodName.contains("delete") || methodName.contains("remove")) {
            return "DELETE";
        } else if (methodName.contains("approve") || methodName.contains("audit")) {
            return "APPROVE";
        } else if (httpMethod.equals("POST")) {
            return "CREATE";
        } else if (httpMethod.equals("PUT") || httpMethod.equals("PATCH")) {
            return "UPDATE";
        } else if (httpMethod.equals("DELETE")) {
            return "DELETE";
        } else {
            return "QUERY";
        }
    }
    
    /**
     * 获取操作模块
     */
    private String getOperationModule(String controllerName) {
        if (controllerName.contains("Requirement")) {
            return "REQUIREMENT";
        } else if (controllerName.contains("TestCase")) {
            return "CASE";
        } else if (controllerName.contains("PromptTemplate")) {
            return "TEMPLATE";
        } else if (controllerName.contains("CaseGeneration")) {
            return "CASE_GENERATION";
        } else if (controllerName.contains("ModelConfig")) {
            return "MODEL_CONFIG";
        } else if (controllerName.contains("KnowledgeBase")) {
            return "KNOWLEDGE_BASE";
        } else if (controllerName.contains("CaseReuse")) {
            return "CASE_REUSE";
        } else {
            return "OTHER";
        }
    }
    
    /**
     * 获取操作目标
     */
    private String getOperationTarget(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        // 尝试从参数中提取ID或名称
        for (Object arg : args) {
            if (arg instanceof Long) {
                return "ID:" + arg;
            } else if (arg != null) {
                try {
                    Method getIdMethod = arg.getClass().getMethod("getId");
                    Object id = getIdMethod.invoke(arg);
                    if (id != null) {
                        return "ID:" + id;
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
        }
        return null;
    }
    
    /**
     * 获取操作内容
     */
    private String getOperationContent(Method method, Object[] args) {
        return method.getName() + "(" + Arrays.toString(args) + ")";
    }
    
    /**
     * 获取请求参数
     */
    private String getRequestParams(Object[] args) {
        try {
            if (args == null || args.length == 0) {
                return null;
            }
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return Arrays.toString(args);
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 实现用户认证后，从认证上下文获取
        return null;
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUserName() {
        // TODO: 实现用户认证后，从认证上下文获取
        return "SYSTEM";
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

