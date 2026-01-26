package com.sinosoft.testdesign.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 日志MDC过滤器
 * 自动设置traceId和userId到MDC，用于日志追踪
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Component
@Order(1)
public class LoggingMDCFilter implements Filter {
    
    /**
     * TraceId请求头名称
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    
    /**
     * UserId请求头名称
     */
    private static final String USER_ID_HEADER = "X-User-Id";
    
    /**
     * MDC中traceId的key
     */
    private static final String MDC_TRACE_ID = "traceId";
    
    /**
     * MDC中userId的key
     */
    private static final String MDC_USER_ID = "userId";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            // 设置traceId：优先从请求头获取，否则生成新的UUID
            String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }
            MDC.put(MDC_TRACE_ID, traceId);
            
            // 设置userId：从请求头获取，如果没有则设置为"anonymous"
            String userId = httpRequest.getHeader(USER_ID_HEADER);
            if (userId == null || userId.isEmpty()) {
                // 注意：如果后续有认证系统（如Spring Security），可以从SecurityContext或JWT中获取userId
                // 示例代码（需要引入Spring Security）：
                // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                // if (authentication != null && authentication.isAuthenticated()) {
                //     userId = authentication.getName();
                // }
                userId = "anonymous";
            }
            MDC.put(MDC_USER_ID, userId);
            
            // 继续执行过滤器链
            chain.doFilter(request, response);
        } finally {
            // 请求结束后清除MDC，避免内存泄漏
            MDC.clear();
        }
    }
}

