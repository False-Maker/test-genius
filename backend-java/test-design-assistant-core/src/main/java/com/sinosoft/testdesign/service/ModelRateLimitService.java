package com.sinosoft.testdesign.service;

/**
 * 模型限流与配额服务（第四阶段 4.2）
 * 基于 Redis 的限流器与每日配额管理，避免模型资源滥用。
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
public interface ModelRateLimitService {

    /**
     * 尝试获取调用许可（同时校验限流与配额）
     *
     * @param userId   用户ID，可为 null（匿名按 IP 或默认 key）
     * @param modelCode 模型代码
     * @param dailyLimit 该模型每日限额，≤0 表示不限制
     * @return 是否允许调用
     */
    boolean tryAcquire(Long userId, String modelCode, int dailyLimit);

    /**
     * 记录一次模型调用（用于配额扣减）
     *
     * @param userId    用户ID
     * @param modelCode 模型代码
     */
    void recordUsage(Long userId, String modelCode);

    /**
     * 获取当日剩余配额
     *
     * @param userId    用户ID
     * @param modelCode 模型代码
     * @param dailyLimit 每日限额
     * @return 剩余次数，无限制时返回 -1
     */
    long getRemainingQuota(Long userId, String modelCode, int dailyLimit);

    /**
     * 获取当前限流窗口内已使用次数（用于监控）
     *
     * @param userId    用户ID
     * @param modelCode 模型代码
     * @return 当前窗口内调用次数
     */
    long getRateLimitCurrent(Long userId, String modelCode);
}
