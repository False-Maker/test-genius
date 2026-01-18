package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 缓存服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("缓存服务测试")
class CacheServiceImplTest {
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @InjectMocks
    private CacheServiceImpl cacheService;
    
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    @Test
    @DisplayName("设置缓存-指定过期时间")
    void testSet_WithTimeout() {
        // Given
        String key = "test:key";
        String value = "test value";
        long timeout = 3600L;
        
        // When
        cacheService.set(key, value, timeout);
        
        // Then
        verify(valueOperations, times(1)).set(eq(key), eq(value), eq(timeout), eq(TimeUnit.SECONDS));
    }
    
    @Test
    @DisplayName("设置缓存-使用默认过期时间")
    void testSet_DefaultTimeout() {
        // Given
        String key = "test:key";
        String value = "test value";
        
        // When
        cacheService.set(key, value);
        
        // Then
        verify(valueOperations, times(1)).set(eq(key), eq(value), eq(3600L), eq(TimeUnit.SECONDS));
    }
    
    @Test
    @DisplayName("设置缓存-异常处理")
    void testSet_Exception() {
        // Given
        String key = "test:key";
        String value = "test value";
        doThrow(new RuntimeException("Redis连接失败")).when(valueOperations)
            .set(anyString(), any(), anyLong(), any(TimeUnit.class));
        
        // When & Then - 不应该抛出异常，应该被捕获
        assertDoesNotThrow(() -> {
            cacheService.set(key, value);
        });
    }
    
    @Test
    @DisplayName("获取缓存-成功")
    void testGet_Success() {
        // Given
        String key = "test:key";
        String expectedValue = "test value";
        when(valueOperations.get(key)).thenReturn(expectedValue);
        
        // When
        String result = cacheService.get(key, String.class);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedValue, result);
        verify(valueOperations, times(1)).get(key);
    }
    
    @Test
    @DisplayName("获取缓存-键不存在")
    void testGet_KeyNotExists() {
        // Given
        String key = "test:key";
        when(valueOperations.get(key)).thenReturn(null);
        
        // When
        String result = cacheService.get(key, String.class);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("获取缓存-类型转换")
    void testGet_TypeConversion() {
        // Given
        String key = "test:key";
        Integer value = 123;
        when(valueOperations.get(key)).thenReturn(value);
        
        // When
        Integer result = cacheService.get(key, Integer.class);
        
        // Then
        assertNotNull(result);
        assertEquals(123, result);
    }
    
    @Test
    @DisplayName("获取缓存-异常处理")
    void testGet_Exception() {
        // Given
        String key = "test:key";
        when(valueOperations.get(key)).thenThrow(new RuntimeException("Redis连接失败"));
        
        // When
        String result = cacheService.get(key, String.class);
        
        // Then - 异常应该被捕获，返回null
        assertNull(result);
    }
    
    @Test
    @DisplayName("获取列表缓存-成功")
    void testGetList_Success() {
        // Given
        String key = "test:list";
        List<String> expectedList = new ArrayList<>();
        expectedList.add("item1");
        expectedList.add("item2");
        when(valueOperations.get(key)).thenReturn(expectedList);
        
        // When
        List<String> result = cacheService.getList(key, String.class);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
    }
    
    @Test
    @DisplayName("获取列表缓存-键不存在")
    void testGetList_KeyNotExists() {
        // Given
        String key = "test:list";
        when(valueOperations.get(key)).thenReturn(null);
        
        // When
        List<String> result = cacheService.getList(key, String.class);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("获取列表缓存-异常处理")
    void testGetList_Exception() {
        // Given
        String key = "test:list";
        when(valueOperations.get(key)).thenThrow(new RuntimeException("Redis连接失败"));
        
        // When
        List<String> result = cacheService.getList(key, String.class);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("删除缓存-成功")
    void testDelete_Success() {
        // Given
        String key = "test:key";
        when(redisTemplate.delete(key)).thenReturn(true);
        
        // When
        cacheService.delete(key);
        
        // Then
        verify(redisTemplate, times(1)).delete(key);
    }
    
    @Test
    @DisplayName("删除缓存-异常处理")
    void testDelete_Exception() {
        // Given
        String key = "test:key";
        doThrow(new RuntimeException("Redis连接失败")).when(redisTemplate).delete(key);
        
        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> {
            cacheService.delete(key);
        });
    }
    
    @Test
    @DisplayName("批量删除缓存-成功")
    void testDelete_List_Success() {
        // Given
        List<String> keys = new ArrayList<>();
        keys.add("key1");
        keys.add("key2");
        when(redisTemplate.delete(keys)).thenReturn(2L);
        
        // When
        cacheService.delete(keys);
        
        // Then
        verify(redisTemplate, times(1)).delete(keys);
    }
    
    @Test
    @DisplayName("批量删除缓存-空列表")
    void testDelete_List_Empty() {
        // Given
        List<String> keys = new ArrayList<>();
        
        // When
        cacheService.delete(keys);
        
        // Then - 不应该调用delete
        verify(redisTemplate, never()).delete(anyList());
    }
    
    @Test
    @DisplayName("批量删除缓存-null列表")
    void testDelete_List_Null() {
        // When
        cacheService.delete((List<String>) null);
        
        // Then - 不应该抛出异常
        assertDoesNotThrow(() -> {
            cacheService.delete((List<String>) null);
        });
    }
    
    @Test
    @DisplayName("批量删除缓存-异常处理")
    void testDelete_List_Exception() {
        // Given
        List<String> keys = new ArrayList<>();
        keys.add("key1");
        doThrow(new RuntimeException("Redis连接失败")).when(redisTemplate).delete(keys);
        
        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> {
            cacheService.delete(keys);
        });
    }
    
    @Test
    @DisplayName("按模式删除缓存-成功")
    void testDeleteByPattern_Success() {
        // Given
        String pattern = "test:*";
        Set<String> keys = Set.of("test:key1", "test:key2");
        when(redisTemplate.keys(pattern)).thenReturn(keys);
        when(redisTemplate.delete(keys)).thenReturn(2L);
        
        // When
        cacheService.deleteByPattern(pattern);
        
        // Then
        verify(redisTemplate, times(1)).keys(pattern);
        verify(redisTemplate, times(1)).delete(keys);
    }
    
    @Test
    @DisplayName("按模式删除缓存-无匹配键")
    void testDeleteByPattern_NoMatches() {
        // Given
        String pattern = "test:*";
        when(redisTemplate.keys(pattern)).thenReturn(null);
        
        // When
        cacheService.deleteByPattern(pattern);
        
        // Then
        verify(redisTemplate, times(1)).keys(pattern);
        verify(redisTemplate, never()).delete(anySet());
    }
    
    @Test
    @DisplayName("按模式删除缓存-异常处理")
    void testDeleteByPattern_Exception() {
        // Given
        String pattern = "test:*";
        when(redisTemplate.keys(pattern)).thenThrow(new RuntimeException("Redis连接失败"));
        
        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> {
            cacheService.deleteByPattern(pattern);
        });
    }
    
    @Test
    @DisplayName("检查缓存是否存在-存在")
    void testExists_True() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(true);
        
        // When
        boolean result = cacheService.exists(key);
        
        // Then
        assertTrue(result);
        verify(redisTemplate, times(1)).hasKey(key);
    }
    
    @Test
    @DisplayName("检查缓存是否存在-不存在")
    void testExists_False() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(false);
        
        // When
        boolean result = cacheService.exists(key);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    @DisplayName("检查缓存是否存在-返回null")
    void testExists_Null() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(null);
        
        // When
        boolean result = cacheService.exists(key);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    @DisplayName("检查缓存是否存在-异常处理")
    void testExists_Exception() {
        // Given
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenThrow(new RuntimeException("Redis连接失败"));
        
        // When
        boolean result = cacheService.exists(key);
        
        // Then - 异常应该被捕获，返回false
        assertFalse(result);
    }
    
    @Test
    @DisplayName("设置过期时间-成功")
    void testExpire_Success() {
        // Given
        String key = "test:key";
        long timeout = 1800L;
        when(redisTemplate.expire(eq(key), eq(timeout), eq(TimeUnit.SECONDS))).thenReturn(true);
        
        // When
        cacheService.expire(key, timeout);
        
        // Then
        verify(redisTemplate, times(1)).expire(eq(key), eq(timeout), eq(TimeUnit.SECONDS));
    }
    
    @Test
    @DisplayName("设置过期时间-异常处理")
    void testExpire_Exception() {
        // Given
        String key = "test:key";
        long timeout = 1800L;
        doThrow(new RuntimeException("Redis连接失败")).when(redisTemplate)
            .expire(anyString(), anyLong(), any(TimeUnit.class));
        
        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> {
            cacheService.expire(key, timeout);
        });
    }
}

