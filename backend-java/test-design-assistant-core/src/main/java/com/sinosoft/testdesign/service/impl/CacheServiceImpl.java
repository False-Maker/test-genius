package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 默认过期时间：1小时
    private static final long DEFAULT_TIMEOUT = 3600;
    
    @Override
    public void set(String key, Object value, long timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            log.debug("设置缓存成功: key={}, timeout={}秒", key, timeout);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
        }
    }
    
    @Override
    public void set(String key, Object value) {
        set(key, value, DEFAULT_TIMEOUT);
    }
    
    @Override
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            
            // 如果类型匹配，直接返回
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            
            // 否则尝试JSON转换
            return objectMapper.convertValue(value, type);
        } catch (Exception e) {
            log.error("获取缓存失败: key={}, type={}", key, type.getName(), e);
            return null;
        }
    }
    
    @Override
    public <T> List<T> getList(String key, Class<T> elementType) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            
            // 如果已经是List类型，直接转换
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                List<T> result = new ArrayList<>();
                for (Object item : list) {
                    if (elementType.isInstance(item)) {
                        result.add(elementType.cast(item));
                    } else {
                        result.add(objectMapper.convertValue(item, elementType));
                    }
                }
                return result;
            }
            
            // 否则尝试JSON转换
            return objectMapper.convertValue(value, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (Exception e) {
            log.error("获取列表缓存失败: key={}, elementType={}", key, elementType.getName(), e);
            return null;
        }
    }
    
    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("删除缓存成功: key={}", key);
        } catch (Exception e) {
            log.error("删除缓存失败: key={}", key, e);
        }
    }
    
    @Override
    public void delete(List<String> keys) {
        try {
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("批量删除缓存成功: count={}", keys.size());
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败", e);
        }
    }
    
    @Override
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("按模式删除缓存成功: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("按模式删除缓存失败: pattern={}", pattern, e);
        }
    }
    
    @Override
    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return result != null && result;
        } catch (Exception e) {
            log.error("检查缓存是否存在失败: key={}", key, e);
            return false;
        }
    }
    
    @Override
    public void expire(String key, long timeout) {
        try {
            redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
            log.debug("设置过期时间成功: key={}, timeout={}秒", key, timeout);
        } catch (Exception e) {
            log.error("设置过期时间失败: key={}", key, e);
        }
    }
    
    @Override
    public void clear() {
        try {
            // 获取所有键并删除（注意：生产环境应该使用更安全的方式，如指定前缀）
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清空所有缓存成功: count={}", keys.size());
            } else {
                log.debug("缓存已为空，无需清空");
            }
        } catch (Exception e) {
            log.error("清空缓存失败", e);
        }
    }
}

