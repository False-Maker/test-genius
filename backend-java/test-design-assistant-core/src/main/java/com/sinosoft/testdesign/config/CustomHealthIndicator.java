package com.sinosoft.testdesign.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/**
 * 自定义健康检查指示器
 * 检查数据库、Redis和AI服务的连接状态
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Component("customHealthIndicator")
@RequiredArgsConstructor
public class CustomHealthIndicator implements org.springframework.boot.actuate.health.HealthIndicator {
    
    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        // 检查数据库连接
        boolean dbHealthy = checkDatabase();
        builder.withDetail("database", dbHealthy ? "UP" : "DOWN");
        
        // 检查Redis连接
        boolean redisHealthy = checkRedis();
        builder.withDetail("redis", redisHealthy ? "UP" : "DOWN");
        
        // 检查AI服务连接
        boolean aiServiceHealthy = checkAIService();
        builder.withDetail("ai-service", aiServiceHealthy ? "UP" : "DOWN");
        
        if (dbHealthy && redisHealthy && aiServiceHealthy) {
            return builder.up().build();
        } else {
            return builder.down().build();
        }
    }
    
    /**
     * 检查数据库连接
     */
    private boolean checkDatabase() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                return connection.isValid(2); // 2秒超时
            }
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            return false;
        }
    }
    
    /**
     * 检查Redis连接
     */
    private boolean checkRedis() {
        try {
            redisTemplate.opsForValue().get("health:check");
            return true;
        } catch (Exception e) {
            log.error("Redis健康检查失败", e);
            return false;
        }
    }
    
    /**
     * 检查AI服务连接
     */
    private boolean checkAIService() {
        try {
            String url = aiServiceUrl + "/health";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null && "healthy".equals(response.get("status"));
        } catch (Exception e) {
            log.warn("AI服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }
}

