package com.sinosoft.testdesign.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 业务指标收集器
 * 收集用例生成、模型调用等业务指标
 * 
 * @author sinosoft
 * @date 2024-01-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    
    // 用例生成任务指标
    private Counter caseGenerationTaskTotal;
    private Counter caseGenerationTaskSuccess;
    private Counter caseGenerationTaskFailed;
    private Timer caseGenerationTaskDuration;
    
    // 模型调用指标
    private Counter llmCallTotal;
    private Counter llmCallSuccess;
    private Counter llmCallFailed;
    private Timer llmCallDuration;
    private Counter llmTokensUsed;
    
    // 任务队列长度（Gauge）
    private AtomicInteger taskQueueLength = new AtomicInteger(0);
    
    /**
     * 初始化指标
     */
    public void init() {
        // 用例生成任务指标
        caseGenerationTaskTotal = Counter.builder("case_generation_task_total")
                .description("用例生成任务总数")
                .tag("type", "task")
                .register(meterRegistry);
        
        caseGenerationTaskSuccess = Counter.builder("case_generation_task_success")
                .description("用例生成任务成功数")
                .tag("type", "task")
                .register(meterRegistry);
        
        caseGenerationTaskFailed = Counter.builder("case_generation_task_failed")
                .description("用例生成任务失败数")
                .tag("type", "task")
                .register(meterRegistry);
        
        caseGenerationTaskDuration = Timer.builder("case_generation_task_duration_seconds")
                .description("用例生成任务耗时（秒）")
                .register(meterRegistry);
        
        // 模型调用指标
        llmCallTotal = Counter.builder("llm_call_total")
                .description("模型调用总数")
                .tag("type", "llm")
                .register(meterRegistry);
        
        llmCallSuccess = Counter.builder("llm_call_success")
                .description("模型调用成功数")
                .tag("type", "llm")
                .register(meterRegistry);
        
        llmCallFailed = Counter.builder("llm_call_failed")
                .description("模型调用失败数")
                .tag("type", "llm")
                .register(meterRegistry);
        
        llmCallDuration = Timer.builder("llm_call_duration_seconds")
                .description("模型调用耗时（秒）")
                .register(meterRegistry);
        
        llmTokensUsed = Counter.builder("llm_tokens_used_total")
                .description("模型调用Token使用总量")
                .tag("type", "llm")
                .register(meterRegistry);
        
        // 任务队列长度（Gauge）
        Gauge.builder("case_generation_task_queue_length", taskQueueLength, AtomicInteger::get)
                .description("用例生成任务队列长度")
                .register(meterRegistry);
        
        log.info("业务指标收集器初始化完成");
    }
    
    /**
     * 记录用例生成任务创建
     */
    public void recordCaseGenerationTaskCreated() {
        caseGenerationTaskTotal.increment();
        taskQueueLength.incrementAndGet();
        log.debug("记录用例生成任务创建指标");
    }
    
    /**
     * 记录用例生成任务成功
     * 
     * @param durationSeconds 任务耗时（秒）
     */
    public void recordCaseGenerationTaskSuccess(double durationSeconds) {
        caseGenerationTaskSuccess.increment();
        caseGenerationTaskDuration.record(durationSeconds);
        taskQueueLength.decrementAndGet();
        log.debug("记录用例生成任务成功指标，耗时: {}秒", durationSeconds);
    }
    
    /**
     * 记录用例生成任务失败
     * 
     * @param durationSeconds 任务耗时（秒）
     * @param reason 失败原因
     */
    public void recordCaseGenerationTaskFailed(double durationSeconds, String reason) {
        caseGenerationTaskFailed.increment(
                io.micrometer.core.instrument.Tags.of("reason", reason != null ? reason : "unknown")
        );
        caseGenerationTaskDuration.record(durationSeconds);
        taskQueueLength.decrementAndGet();
        log.debug("记录用例生成任务失败指标，耗时: {}秒，原因: {}", durationSeconds, reason);
    }
    
    /**
     * 记录模型调用开始
     * 
     * @param modelCode 模型代码
     */
    public void recordLlmCallStart(String modelCode) {
        llmCallTotal.increment(
                io.micrometer.core.instrument.Tags.of("model", modelCode != null ? modelCode : "unknown")
        );
        log.debug("记录模型调用开始指标，模型: {}", modelCode);
    }
    
    /**
     * 记录模型调用成功
     * 
     * @param modelCode 模型代码
     * @param durationSeconds 调用耗时（秒）
     * @param tokensUsed Token使用量
     */
    public void recordLlmCallSuccess(String modelCode, double durationSeconds, Long tokensUsed) {
        llmCallSuccess.increment(
                io.micrometer.core.instrument.Tags.of("model", modelCode != null ? modelCode : "unknown")
        );
        llmCallDuration.record(durationSeconds);
        if (tokensUsed != null && tokensUsed > 0) {
            llmTokensUsed.increment(tokensUsed,
                    io.micrometer.core.instrument.Tags.of("model", modelCode != null ? modelCode : "unknown")
            );
        }
        log.debug("记录模型调用成功指标，模型: {}，耗时: {}秒，Token: {}", modelCode, durationSeconds, tokensUsed);
    }
    
    /**
     * 记录模型调用失败
     * 
     * @param modelCode 模型代码
     * @param durationSeconds 调用耗时（秒）
     * @param reason 失败原因
     */
    public void recordLlmCallFailed(String modelCode, double durationSeconds, String reason) {
        llmCallFailed.increment(
                io.micrometer.core.instrument.Tags.of(
                        "model", modelCode != null ? modelCode : "unknown",
                        "reason", reason != null ? reason : "unknown"
                )
        );
        llmCallDuration.record(durationSeconds);
        log.debug("记录模型调用失败指标，模型: {}，耗时: {}秒，原因: {}", modelCode, durationSeconds, reason);
    }
    
    /**
     * 更新任务队列长度
     * 
     * @param length 队列长度
     */
    public void updateTaskQueueLength(int length) {
        taskQueueLength.set(length);
        log.debug("更新任务队列长度: {}", length);
    }
}

