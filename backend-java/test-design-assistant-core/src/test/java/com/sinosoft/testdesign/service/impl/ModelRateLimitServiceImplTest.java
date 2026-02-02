package com.sinosoft.testdesign.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * 模型限流与配额服务单元测试（第四阶段 4.2）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("模型限流与配额服务测试")
class ModelRateLimitServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private ModelRateLimitServiceImpl modelRateLimitService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        ReflectionTestUtils.setField(modelRateLimitService, "rateLimitPerWindow", 10);
    }

    @Test
    @DisplayName("tryAcquire-无限额时允许")
    void tryAcquire_NoQuotaLimit_Allowed() {
        when(valueOps.increment(anyString())).thenReturn(1L);
        when(stringRedisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        boolean allowed = modelRateLimitService.tryAcquire(1L, "deepseek", 0);

        assertTrue(allowed);
    }

    @Test
    @DisplayName("tryAcquire-限额内允许")
    void tryAcquire_WithinQuota_Allowed() {
        when(valueOps.increment(anyString())).thenReturn(1L);
        when(valueOps.get(anyString())).thenReturn("5");
        when(stringRedisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        boolean allowed = modelRateLimitService.tryAcquire(1L, "deepseek", 100);

        assertTrue(allowed);
    }

    @Test
    @DisplayName("tryAcquire-配额用尽拒绝")
    void tryAcquire_QuotaExceeded_Rejected() {
        when(valueOps.get(argThat((String s) -> s != null && s.contains("quota")))).thenReturn("100");

        boolean allowed = modelRateLimitService.tryAcquire(1L, "deepseek", 100);

        assertFalse(allowed);
        verify(valueOps, never()).increment(anyString());
    }

    @Test
    @DisplayName("recordUsage-成功")
    void recordUsage_Success() {
        when(valueOps.increment(anyString())).thenReturn(1L);
        when(stringRedisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        assertDoesNotThrow(() -> modelRateLimitService.recordUsage(1L, "deepseek"));
        verify(valueOps).increment(anyString());
    }

    @Test
    @DisplayName("getRemainingQuota-无限额返回-1")
    void getRemainingQuota_NoLimit_ReturnsNegativeOne() {
        long remaining = modelRateLimitService.getRemainingQuota(1L, "deepseek", 0);

        assertEquals(-1, remaining);
        verify(valueOps, never()).get(anyString());
    }

    @Test
    @DisplayName("getRemainingQuota-有剩余")
    void getRemainingQuota_HasRemaining() {
        when(valueOps.get(anyString())).thenReturn("20");

        long remaining = modelRateLimitService.getRemainingQuota(1L, "deepseek", 100);

        assertEquals(80, remaining);
    }

    @Test
    @DisplayName("getRateLimitCurrent-无键返回0")
    void getRateLimitCurrent_NoKey_ReturnsZero() {
        when(valueOps.get(anyString())).thenReturn(null);

        long current = modelRateLimitService.getRateLimitCurrent(1L, "deepseek");

        assertEquals(0, current);
    }
}
