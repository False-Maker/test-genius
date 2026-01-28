package com.sinosoft.testdesign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
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
     * 配置连接超时和读取超时
     */
    @Bean
    public RestTemplate restTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5秒连接超时
        factory.setReadTimeout(30000);   // 30秒读取超时
        return new RestTemplate(factory);
    }
    
    /**
     * ObjectMapper Bean
     * 配置支持 Java 8 时间类型序列化
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
    /**
     * 初始化业务指标收集器
     */
    @PostConstruct
    public void initMetrics() {
        businessMetricsCollector.init();
    }
}

