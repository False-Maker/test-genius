package com.sinosoft.testdesign.scheduled;

import com.sinosoft.testdesign.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 告警定时任务
 * 定时检查告警规则并触发告警
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {
    
    private final AlertService alertService;
    
    /**
     * 定时检查告警规则
     * 默认每60秒执行一次
     * 可以通过配置文件修改执行频率
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void checkAlertRules() {
        try {
            log.debug("开始执行告警规则检查");
            alertService.checkAlertRules();
            log.debug("告警规则检查完成");
        } catch (Exception e) {
            log.error("告警规则检查失败", e);
        }
    }
}
