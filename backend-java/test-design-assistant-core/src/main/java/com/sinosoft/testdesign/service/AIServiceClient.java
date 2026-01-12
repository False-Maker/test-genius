package com.sinosoft.testdesign.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * AI服务客户端
 * 封装AI服务调用，统一处理限流、熔断和重试
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface AIServiceClient {
    
    /**
     * 调用AI服务（POST请求）
     * 
     * @param url 请求URL
     * @param request 请求体
     * @return 响应结果
     */
    @CircuitBreaker(name = "aiService", fallbackMethod = "fallback")
    @RateLimiter(name = "aiService")
    @Retry(name = "aiService")
    Map<String, Object> post(String url, Object request);
    
    /**
     * 调用AI服务（GET请求）
     * 
     * @param url 请求URL
     * @return 响应结果
     */
    @CircuitBreaker(name = "aiService", fallbackMethod = "fallback")
    @RateLimiter(name = "aiService")
    @Retry(name = "aiService")
    Map<String, Object> get(String url);
    
    /**
     * 降级方法
     */
    default Map<String, Object> fallback(String url, Object request, Exception e) {
        return Map.of(
            "status", "error",
            "message", "AI服务暂时不可用，请稍后重试",
            "error", e.getMessage()
        );
    }
    
    /**
     * 降级方法（GET）
     */
    default Map<String, Object> fallback(String url, Exception e) {
        return Map.of(
            "status", "error",
            "message", "AI服务暂时不可用，请稍后重试",
            "error", e.getMessage()
        );
    }
}

