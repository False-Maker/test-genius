package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.PromptTemplate;
import com.sinosoft.testdesign.entity.PromptTemplateAbTest;
import com.sinosoft.testdesign.entity.PromptTemplateAbTestExecution;
import com.sinosoft.testdesign.entity.PromptTemplateVersion;
import com.sinosoft.testdesign.repository.PromptTemplateAbTestExecutionRepository;
import com.sinosoft.testdesign.repository.PromptTemplateAbTestRepository;
import com.sinosoft.testdesign.repository.PromptTemplateRepository;
import com.sinosoft.testdesign.repository.PromptTemplateVersionRepository;
import com.sinosoft.testdesign.service.PromptTemplateAbTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * 提示词模板A/B测试服务实现
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateAbTestServiceImpl implements PromptTemplateAbTestService {
    
    private final PromptTemplateAbTestRepository abTestRepository;
    private final PromptTemplateAbTestExecutionRepository executionRepository;
    private final PromptTemplateRepository templateRepository;
    private final PromptTemplateVersionRepository versionRepository;
    private final Random random = new Random();
    
    @Override
    @Transactional
    public PromptTemplateAbTest createAbTest(Long templateId, PromptTemplateAbTest abTest) {
        log.info("创建A/B测试: templateId={}", templateId);
        
        // 验证模板是否存在
        PromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
        
        // 验证版本是否存在
        PromptTemplateVersion versionA = versionRepository.findById(abTest.getVersionAId())
                .orElseThrow(() -> new RuntimeException("版本A不存在: " + abTest.getVersionAId()));
        PromptTemplateVersion versionB = versionRepository.findById(abTest.getVersionBId())
                .orElseThrow(() -> new RuntimeException("版本B不存在: " + abTest.getVersionBId()));
        
        // 验证版本属于该模板
        if (!versionA.getTemplateId().equals(templateId) || !versionB.getTemplateId().equals(templateId)) {
            throw new RuntimeException("版本不属于该模板");
        }
        
        // 验证流量分配比例
        if (abTest.getTrafficSplitA() + abTest.getTrafficSplitB() != 100) {
            throw new RuntimeException("流量分配比例之和必须等于100");
        }
        
        // 检查是否已有正在运行的测试
        Optional<PromptTemplateAbTest> runningTest = abTestRepository.findRunningTestByTemplateId(templateId);
        if (runningTest.isPresent()) {
            throw new RuntimeException("该模板已有正在运行的A/B测试，请先停止现有测试");
        }
        
        abTest.setTemplateId(templateId);
        abTest.setStatus("draft");
        abTest.setCreateTime(LocalDateTime.now());
        abTest.setUpdateTime(LocalDateTime.now());
        
        PromptTemplateAbTest saved = abTestRepository.save(abTest);
        log.info("创建A/B测试成功: id={}", saved.getId());
        
        return saved;
    }
    
    @Override
    public PromptTemplateAbTest getAbTestById(Long id) {
        return abTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("A/B测试不存在: " + id));
    }
    
    @Override
    public List<PromptTemplateAbTest> getAbTestsByTemplateId(Long templateId) {
        return abTestRepository.findByTemplateIdOrderByCreateTimeDesc(templateId);
    }
    
    @Override
    @Transactional
    public PromptTemplateAbTest startAbTest(Long id) {
        log.info("启动A/B测试: id={}", id);
        
        PromptTemplateAbTest abTest = getAbTestById(id);
        
        if ("running".equals(abTest.getStatus())) {
            throw new RuntimeException("A/B测试已在运行中");
        }
        
        // 检查是否已有正在运行的测试
        Optional<PromptTemplateAbTest> runningTest = abTestRepository.findRunningTestByTemplateId(abTest.getTemplateId());
        if (runningTest.isPresent() && !runningTest.get().getId().equals(id)) {
            throw new RuntimeException("该模板已有正在运行的A/B测试，请先停止现有测试");
        }
        
        abTest.setStatus("running");
        abTest.setStartTime(LocalDateTime.now());
        abTest.setUpdateTime(LocalDateTime.now());
        
        PromptTemplateAbTest saved = abTestRepository.save(abTest);
        log.info("启动A/B测试成功: id={}", saved.getId());
        
        return saved;
    }
    
    @Override
    @Transactional
    public PromptTemplateAbTest pauseAbTest(Long id) {
        log.info("暂停A/B测试: id={}", id);
        
        PromptTemplateAbTest abTest = getAbTestById(id);
        abTest.setStatus("paused");
        abTest.setUpdateTime(LocalDateTime.now());
        
        return abTestRepository.save(abTest);
    }
    
    @Override
    @Transactional
    public PromptTemplateAbTest stopAbTest(Long id) {
        log.info("停止A/B测试: id={}", id);
        
        PromptTemplateAbTest abTest = getAbTestById(id);
        abTest.setStatus("completed");
        abTest.setEndTime(LocalDateTime.now());
        abTest.setUpdateTime(LocalDateTime.now());
        
        // 如果启用了自动选择，执行自动选择
        if ("1".equals(abTest.getAutoSelectEnabled())) {
            try {
                autoSelectBestVersion(id);
            } catch (Exception e) {
                log.error("自动选择最优版本失败: {}", e.getMessage());
            }
        }
        
        return abTestRepository.save(abTest);
    }
    
    @Override
    @Transactional
    public void deleteAbTest(Long id) {
        log.info("删除A/B测试: id={}", id);
        
        PromptTemplateAbTest abTest = getAbTestById(id);
        if ("running".equals(abTest.getStatus())) {
            throw new RuntimeException("不能删除正在运行的A/B测试，请先停止测试");
        }
        
        abTestRepository.deleteById(id);
        log.info("删除A/B测试成功: id={}", id);
    }
    
    @Override
    public String selectVersion(Long abTestId, String requestId) {
        PromptTemplateAbTest abTest = getAbTestById(abTestId);
        
        if (!"running".equals(abTest.getStatus())) {
            // 如果测试未运行，返回版本A（默认版本）
            return "A";
        }
        
        // 根据流量分配比例随机选择版本
        int randomValue = random.nextInt(100);
        if (randomValue < abTest.getTrafficSplitA()) {
            return "A";
        } else {
            return "B";
        }
    }
    
    @Override
    @Transactional
    public PromptTemplateAbTestExecution recordExecution(PromptTemplateAbTestExecution execution) {
        execution.setExecutionTime(LocalDateTime.now());
        return executionRepository.save(execution);
    }
    
    @Override
    public Map<String, Object> getAbTestStatistics(Long abTestId) {
        PromptTemplateAbTest abTest = getAbTestById(abTestId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 版本A统计
        Long versionACount = executionRepository.countVersionAExecutions(abTestId);
        Long versionASuccess = executionRepository.countVersionASuccess(abTestId);
        Double versionAAvgResponseTime = executionRepository.getVersionAAvgResponseTime(abTestId);
        Double versionAAvgRating = executionRepository.getVersionAAvgRating(abTestId);
        
        double versionASuccessRate = versionACount > 0 ? (versionASuccess.doubleValue() / versionACount.doubleValue()) * 100 : 0;
        
        Map<String, Object> versionAStats = new HashMap<>();
        versionAStats.put("totalExecutions", versionACount);
        versionAStats.put("successCount", versionASuccess);
        versionAStats.put("successRate", Math.round(versionASuccessRate * 100.0) / 100.0);
        versionAStats.put("avgResponseTime", versionAAvgResponseTime != null ? Math.round(versionAAvgResponseTime) : null);
        versionAStats.put("avgRating", versionAAvgRating != null ? Math.round(versionAAvgRating * 10.0) / 10.0 : null);
        
        // 版本B统计
        Long versionBCount = executionRepository.countVersionBExecutions(abTestId);
        Long versionBSuccess = executionRepository.countVersionBSuccess(abTestId);
        Double versionBAvgResponseTime = executionRepository.getVersionBAvgResponseTime(abTestId);
        Double versionBAvgRating = executionRepository.getVersionBAvgRating(abTestId);
        
        double versionBSuccessRate = versionBCount > 0 ? (versionBSuccess.doubleValue() / versionBCount.doubleValue()) * 100 : 0;
        
        Map<String, Object> versionBStats = new HashMap<>();
        versionBStats.put("totalExecutions", versionBCount);
        versionBStats.put("successCount", versionBSuccess);
        versionBStats.put("successRate", Math.round(versionBSuccessRate * 100.0) / 100.0);
        versionBStats.put("avgResponseTime", versionBAvgResponseTime != null ? Math.round(versionBAvgResponseTime) : null);
        versionBStats.put("avgRating", versionBAvgRating != null ? Math.round(versionBAvgRating * 10.0) / 10.0 : null);
        
        stats.put("versionA", versionAStats);
        stats.put("versionB", versionBStats);
        stats.put("totalExecutions", versionACount + versionBCount);
        
        // 判断哪个版本更优
        String betterVersion = determineBetterVersion(versionAStats, versionBStats, abTest.getSelectionCriteria());
        stats.put("betterVersion", betterVersion);
        
        return stats;
    }
    
    /**
     * 判断哪个版本更优
     */
    private String determineBetterVersion(Map<String, Object> versionAStats, Map<String, Object> versionBStats, String criteria) {
        switch (criteria) {
            case "success_rate":
                double rateA = (Double) versionAStats.get("successRate");
                double rateB = (Double) versionBStats.get("successRate");
                return rateA >= rateB ? "A" : "B";
                
            case "response_time":
                Integer timeA = (Integer) versionAStats.get("avgResponseTime");
                Integer timeB = (Integer) versionBStats.get("avgResponseTime");
                if (timeA == null && timeB == null) return "A";
                if (timeA == null) return "B";
                if (timeB == null) return "A";
                return timeA <= timeB ? "A" : "B";
                
            case "user_rating":
                Double ratingA = (Double) versionAStats.get("avgRating");
                Double ratingB = (Double) versionBStats.get("avgRating");
                if (ratingA == null && ratingB == null) return "A";
                if (ratingA == null) return "B";
                if (ratingB == null) return "A";
                return ratingA >= ratingB ? "A" : "B";
                
            default:
                return "A";
        }
    }
    
    @Override
    public boolean shouldAutoSelect(Long abTestId) {
        PromptTemplateAbTest abTest = getAbTestById(abTestId);
        
        if (!"1".equals(abTest.getAutoSelectEnabled())) {
            return false;
        }
        
        if (!"running".equals(abTest.getStatus())) {
            return false;
        }
        
        // 检查是否达到最小样本数
        Long totalExecutions = executionRepository.countVersionAExecutions(abTestId) +
                              executionRepository.countVersionBExecutions(abTestId);
        
        return totalExecutions >= abTest.getMinSamples();
    }
    
    @Override
    @Transactional
    public PromptTemplateVersion autoSelectBestVersion(Long abTestId) {
        log.info("自动选择最优版本: abTestId={}", abTestId);
        
        PromptTemplateAbTest abTest = getAbTestById(abTestId);
        
        if (!shouldAutoSelect(abTestId)) {
            throw new RuntimeException("不满足自动选择条件");
        }
        
        // 获取统计信息
        Map<String, Object> stats = getAbTestStatistics(abTestId);
        String betterVersion = (String) stats.get("betterVersion");
        
        // 获取最优版本
        Long bestVersionId = "A".equals(betterVersion) ? abTest.getVersionAId() : abTest.getVersionBId();
        PromptTemplateVersion bestVersion = versionRepository.findById(bestVersionId)
                .orElseThrow(() -> new RuntimeException("最优版本不存在: " + bestVersionId));
        
        // 停止A/B测试
        stopAbTest(abTestId);
        
        // 将最优版本设为当前版本
        versionRepository.updateAllVersionsToNonCurrent(abTest.getTemplateId());
        bestVersion.setIsCurrent("1");
        versionRepository.save(bestVersion);
        
        // 更新模板内容
        PromptTemplate template = templateRepository.findById(abTest.getTemplateId())
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        template.setTemplateContent(bestVersion.getTemplateContent());
        template.setTemplateVariables(bestVersion.getTemplateVariables());
        template.setVersion(bestVersion.getVersionNumber());
        templateRepository.save(template);
        
        log.info("自动选择最优版本成功: version={}, versionNumber={}", betterVersion, bestVersion.getVersionNumber());
        
        return bestVersion;
    }
}
