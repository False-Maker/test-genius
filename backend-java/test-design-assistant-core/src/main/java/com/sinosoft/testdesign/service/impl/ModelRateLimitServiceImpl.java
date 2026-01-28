package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.service.ModelRateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * 模型限流与配额服务实现（第四阶段 4.2）
 * Redis 限流：固定窗口，每窗口最大请求数；配额：按自然日累计，支持每日限额。
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelRateLimitServiceImpl implements ModelRateLimitService {

    private static final String PREFIX_RATE = "model:rate:";
    private static final String PREFIX_QUOTA = "model:quota:";
    private static final long RATE_WINDOW_SECONDS = 1L;
    private static final long QUOTA_TTL_SECONDS = 25 * 3600L; // 略超 24h，避免 key 提前过期

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.model-rate-limit.per-window:10}")
    private int rateLimitPerWindow;

    @Override
    public boolean tryAcquire(Long userId, String modelCode, int dailyLimit) {
        String uid = userId != null ? String.valueOf(userId) : "anon";
        String rateKey = PREFIX_RATE + uid + ":" + modelCode;
        String quotaKey = PREFIX_QUOTA + uid + ":" + modelCode + ":" + LocalDate.now());

        try {
            if (dailyLimit > 0) {
                String q = stringRedisTemplate.opsForValue().get(quotaKey);
                long used = q != null ? Long.parseLong(q) : 0;
                if (used >= dailyLimit) {
                    log.warn("模型配额用尽: userId={}, modelCode={}, used={}, limit={}",
                            userId, modelCode, used, dailyLimit);
                    return false;
                }
            }

            Long rate = stringRedisTemplate.opsForValue().increment(rateKey);
            if (rate != null && rate == 1) {
                stringRedisTemplate.expire(rateKey, RATE_WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (rate != null && rate > rateLimitPerWindow) {
                log.warn("模型限流触发: userId={}, modelCode={}, current={}", userId, modelCode, rate);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("限流/配额检查异常，放行: userId={}, modelCode={}", userId, modelCode, e);
            return true;
        }
    }

    @Override
    public void recordUsage(Long userId, String modelCode) {
        String uid = userId != null ? String.valueOf(userId) : "anon";
        String quotaKey = PREFIX_QUOTA + uid + ":" + modelCode + ":" + LocalDate.now();

        try {
            Long n = stringRedisTemplate.opsForValue().increment(quotaKey);
            if (n != null && n == 1) {
                stringRedisTemplate.expire(quotaKey, QUOTA_TTL_SECONDS, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error("记录配额使用失败: userId={}, modelCode={}", userId, modelCode, e);
        }
    }

    @Override
    public long getRemainingQuota(Long userId, String modelCode, int dailyLimit) {
        if (dailyLimit <= 0) {
            return -1;
        }
        String uid = userId != null ? String.valueOf(userId) : "anon";
        String quotaKey = PREFIX_QUOTA + uid + ":" + modelCode + ":" + LocalDate.now();
        try {
            String v = stringRedisTemplate.opsForValue().get(quotaKey);
            long used = v != null ? Long.parseLong(v) : 0;
            return Math.max(0, dailyLimit - used);
        } catch (Exception e) {
            return dailyLimit;
        }
    }

    @Override
    public long getRateLimitCurrent(Long userId, String modelCode) {
        String uid = userId != null ? String.valueOf(userId) : "anon";
        String rateKey = PREFIX_RATE + uid + ":" + modelCode;
        try {
            String v = stringRedisTemplate.opsForValue().get(rateKey);
            return v != null ? Long.parseLong(v) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
