package com.sinosoft.testdesign.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 配置拦截器、跨域等
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final ApiVersionInterceptor apiVersionInterceptor;
    
    /**
     * 配置跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有源（开发环境，生产环境应限制具体域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加API版本拦截器
        registry.addInterceptor(apiVersionInterceptor)
                .addPathPatterns("/v*/**")  // 匹配所有版本化路径
                .excludePathPatterns(
                        "/swagger-ui/**",      // 排除Swagger UI
                        "/v3/api-docs/**",     // 排除OpenAPI文档
                        "/actuator/**"         // 排除Actuator端点
                );
    }
}

