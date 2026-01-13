package com.sinosoft.testdesign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.metrics.BusinessMetricsCollector;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 应用配置类
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Configuration
@RequiredArgsConstructor
public class AppConfig {
    
    private final BusinessMetricsCollector businessMetricsCollector;
    
    /**
     * RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    /**
     * ObjectMapper Bean
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    /**
     * 初始化业务指标收集器
     */
    @PostConstruct
    public void initMetrics() {
        businessMetricsCollector.init();
    }
}

