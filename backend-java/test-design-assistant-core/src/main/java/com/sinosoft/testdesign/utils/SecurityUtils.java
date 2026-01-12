package com.sinosoft.testdesign.utils;

import java.util.regex.Pattern;

/**
 * 安全工具类
 * 提供SQL注入防护、XSS防护等功能
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public class SecurityUtils {
    
    // SQL注入危险字符模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript|onload|onerror)"
    );
    
    // XSS危险字符模式
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<script|</script>|<iframe|</iframe>|<object|</object>|<embed|</embed>|javascript:|vbscript:|onload=|onerror=|onclick=)"
    );
    
    // 危险SQL关键字
    private static final String[] SQL_KEYWORDS = {
        "union", "select", "insert", "update", "delete", "drop", "create", 
        "alter", "exec", "execute", "script", "javascript", "vbscript"
    };
    
    /**
     * 检查是否包含SQL注入风险
     * 
     * @param input 输入字符串
     * @return 如果包含SQL注入风险返回true
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        // 检查是否包含危险SQL关键字
        String lowerInput = input.toLowerCase();
        for (String keyword : SQL_KEYWORDS) {
            if (lowerInput.contains(keyword)) {
                // 进一步检查是否是完整的SQL语句模式
                if (SQL_INJECTION_PATTERN.matcher(input).find()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否包含XSS风险
     * 
     * @param input 输入字符串
     * @return 如果包含XSS风险返回true
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        return XSS_PATTERN.matcher(input).find();
    }
    
    /**
     * 清理SQL注入风险字符
     * 
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String sanitizeSql(String input) {
        if (input == null) {
            return null;
        }
        
        // 移除或转义危险字符
        String sanitized = input
            .replace("'", "''")  // 转义单引号
            .replace(";", "")   // 移除分号
            .replace("--", "")  // 移除SQL注释
            .replace("/*", "")  // 移除SQL注释
            .replace("*/", ""); // 移除SQL注释
        
        return sanitized;
    }
    
    /**
     * 清理XSS风险字符
     * 
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String sanitizeXss(String input) {
        if (input == null) {
            return null;
        }
        
        // 移除或转义危险HTML标签和脚本
        String sanitized = input
            .replace("<script", "&lt;script")
            .replace("</script>", "&lt;/script&gt;")
            .replace("<iframe", "&lt;iframe")
            .replace("</iframe>", "&lt;/iframe&gt;")
            .replace("<object", "&lt;object")
            .replace("</object>", "&lt;/object&gt;")
            .replace("<embed", "&lt;embed")
            .replace("javascript:", "")
            .replace("vbscript:", "")
            .replace("onload=", "")
            .replace("onerror=", "")
            .replace("onclick=", "");
        
        return sanitized;
    }
    
    /**
     * 验证输入字符串（同时检查SQL注入和XSS）
     * 
     * @param input 输入字符串
     * @param fieldName 字段名称（用于错误提示）
     * @throws IllegalArgumentException 如果包含安全风险
     */
    public static void validateInput(String input, String fieldName) {
        if (input == null || input.isEmpty()) {
            return;
        }
        
        if (containsSqlInjection(input)) {
            throw new IllegalArgumentException(fieldName + "包含SQL注入风险，请检查输入内容");
        }
        
        if (containsXss(input)) {
            throw new IllegalArgumentException(fieldName + "包含XSS风险，请检查输入内容");
        }
    }
    
    /**
     * 验证并清理输入字符串
     * 
     * @param input 输入字符串
     * @param fieldName 字段名称（用于错误提示）
     * @return 清理后的字符串
     * @throws IllegalArgumentException 如果包含严重安全风险
     */
    public static String validateAndSanitize(String input, String fieldName) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 先检查严重风险
        if (containsSqlInjection(input)) {
            throw new IllegalArgumentException(fieldName + "包含SQL注入风险，请检查输入内容");
        }
        
        // 清理XSS风险
        return sanitizeXss(input);
    }
}

