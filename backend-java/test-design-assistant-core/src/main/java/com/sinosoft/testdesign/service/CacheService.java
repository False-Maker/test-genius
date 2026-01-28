package com.sinosoft.testdesign.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface CacheService {
    
    /**
     * 设置缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间（秒）
     */
    void set(String key, Object value, long timeout);
    
    /**
     * 设置缓存（默认过期时间1小时）
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);
    
    /**
     * 获取缓存
     * 
     * @param key 缓存键
     * @return 缓存值
     */
    <T> T get(String key, Class<T> type);
    
    /**
     * 获取列表缓存
     * 
     * @param key 缓存键
     * @param elementType 元素类型
     * @return 列表
     */
    <T> List<T> getList(String key, Class<T> elementType);
    
    /**
     * 删除缓存
     * 
     * @param key 缓存键
     */
    void delete(String key);
    
    /**
     * 删除多个缓存
     * 
     * @param keys 缓存键列表
     */
    void delete(List<String> keys);
    
    /**
     * 删除匹配模式的缓存
     * 
     * @param pattern 匹配模式（如 "cache:user:*"）
     */
    void deleteByPattern(String pattern);
    
    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);
    
    /**
     * 设置过期时间
     * 
     * @param key 缓存键
     * @param timeout 过期时间（秒）
     */
    void expire(String key, long timeout);
    
    /**
     * 清空所有缓存
     */
    void clear();
}

