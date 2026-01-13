package com.sinosoft.testdesign.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API版本拦截器
 * 在响应头中添加API版本信息和废弃警告
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {
    
    /**
     * API版本号正则表达式
     * 匹配 /v1/, /v2/ 等格式
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("/v(\\d+)/");
    
    /**
     * 当前API版本
     */
    private static final String CURRENT_API_VERSION = "v1";
    
    /**
     * 响应处理前
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, 
                            @NonNull HttpServletResponse response, 
                            @NonNull Object handler) {
        String requestURI = request.getRequestURI();
        
        // 从请求路径中提取版本号
        String apiVersion = extractApiVersion(requestURI);
        
        if (apiVersion != null) {
            // 添加API版本响应头
            response.setHeader("API-Version", apiVersion);
            
            // 检查是否需要添加废弃警告
            // 当前所有接口都是v1，暂无不废弃的接口
            // 将来如果需要废弃某个版本，可以在这里添加逻辑
            if (shouldDeprecate(apiVersion)) {
                addDeprecationHeaders(response, apiVersion);
            }
        }
        
        return true;
    }
    
    /**
     * 从请求路径中提取API版本号
     * 
     * @param requestURI 请求URI
     * @return API版本号，如 "v1", "v2"，如果未找到则返回null
     */
    private String extractApiVersion(String requestURI) {
        Matcher matcher = VERSION_PATTERN.matcher(requestURI);
        if (matcher.find()) {
            return "v" + matcher.group(1);
        }
        return null;
    }
    
    /**
     * 判断API版本是否应该废弃
     * 
     * @param apiVersion API版本号
     * @return true表示应该废弃，false表示不应该废弃
     */
    private boolean shouldDeprecate(String apiVersion) {
        // 当前逻辑：v1版本不废弃
        // 将来可以配置化，从配置文件或数据库读取废弃信息
        return false;
    }
    
    /**
     * 添加废弃警告响应头
     * 遵循 RFC 8594 标准
     * 
     * @param response HTTP响应
     * @param apiVersion API版本号
     */
    private void addDeprecationHeaders(HttpServletResponse response, String apiVersion) {
        // Deprecation: true 表示接口已废弃
        response.setHeader("Deprecation", "true");
        
        // Sunset: 废弃时间（RFC 8594格式）
        // 示例：Sunset: Sat, 31 Dec 2024 23:59:59 GMT
        // 这里可以根据配置设置具体时间
        response.setHeader("Sunset", "Sat, 31 Dec 2024 23:59:59 GMT");
        
        // Link: 迁移指南链接
        response.setHeader("Link", "<https://api.example.com/docs/v2/migration>; rel=\"deprecation\"");
        
        log.warn("API版本 {} 已标记为废弃", apiVersion);
    }
}

