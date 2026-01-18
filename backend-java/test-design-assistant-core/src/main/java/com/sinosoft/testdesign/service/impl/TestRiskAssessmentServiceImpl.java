package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestCoverageAnalysis;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestExecutionRecord;
import com.sinosoft.testdesign.entity.TestExecutionTask;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.entity.TestRiskAssessment;
import com.sinosoft.testdesign.repository.TestCoverageAnalysisRepository;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.repository.TestExecutionRecordRepository;
import com.sinosoft.testdesign.repository.TestExecutionTaskRepository;
import com.sinosoft.testdesign.repository.TestRiskAssessmentRepository;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.service.TestRiskAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 风险评估服务实现
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestRiskAssessmentServiceImpl implements TestRiskAssessmentService {
    
    private final TestRiskAssessmentRepository riskRepository;
    private final RequirementRepository requirementRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestExecutionTaskRepository executionTaskRepository;
    private final TestExecutionRecordRepository executionRecordRepository;
    private final TestCoverageAnalysisRepository coverageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String RISK_CODE_PREFIX = "RISK";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // 风险等级阈值
    private static final BigDecimal HIGH_RISK_THRESHOLD = new BigDecimal("70");  // >=70 高风险
    private static final BigDecimal MEDIUM_RISK_THRESHOLD = new BigDecimal("40"); // 40-69 中风险
    // <40 低风险
    
    @Override
    @Transactional
    public TestRiskAssessment assessRisk(TestRiskAssessment assessment) {
        log.info("执行风险评估: {}", assessment.getAssessmentName());
        
        // 数据验证
        validateAssessment(assessment);
        
        // 自动生成评估编码（如果未提供）
        if (!StringUtils.hasText(assessment.getAssessmentCode())) {
            assessment.setAssessmentCode(generateAssessmentCode());
        } else {
            // 检查编码是否已存在
            if (riskRepository.findByAssessmentCode(assessment.getAssessmentCode()).isPresent()) {
                throw new BusinessException("评估编码已存在: " + assessment.getAssessmentCode());
            }
        }
        
        // 根据需求ID或执行任务ID执行相应的评估
        if (assessment.getRequirementId() != null) {
            return assessRequirementRisk(assessment.getRequirementId());
        } else if (assessment.getExecutionTaskId() != null) {
            return assessExecutionTaskRisk(assessment.getExecutionTaskId());
        } else {
            throw new BusinessException("必须提供需求ID或执行任务ID");
        }
    }
    
    @Override
    @Transactional
    public TestRiskAssessment assessRequirementRisk(Long requirementId) {
        log.info("评估需求风险: requirementId={}", requirementId);
        
        TestRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new BusinessException("需求不存在: " + requirementId));
        
        // 查询该需求下的所有用例
        List<TestCase> testCases = testCaseRepository.findByRequirementId(requirementId);
        
        // 查询覆盖分析数据
        List<TestCoverageAnalysis> coverageAnalyses = coverageRepository.findByRequirementId(requirementId);
        
        // 计算风险评分（0-100，分数越高风险越大）
        BigDecimal riskScore = calculateRequirementRiskScore(requirement, testCases, coverageAnalyses);
        
        // 确定风险等级
        String riskLevel = assessRiskLevel(riskScore);
        
        // 识别风险项
        String riskItemsJson = identifyRiskItems(requirementId, null);
        
        // 评估上线可行性（0-100，分数越高越可行）
        BigDecimal feasibilityScore = assessFeasibility(requirementId, null);
        
        // 生成上线建议
        String feasibilityRecommendation = generateFeasibilityRecommendation(riskScore, feasibilityScore, riskItemsJson);
        
        // 构建评估详情
        Map<String, Object> assessmentDetails = new HashMap<>();
        assessmentDetails.put("requirementId", requirementId);
        assessmentDetails.put("requirementCode", requirement.getRequirementCode());
        assessmentDetails.put("requirementName", requirement.getRequirementName());
        assessmentDetails.put("testCaseCount", testCases.size());
        assessmentDetails.put("riskScore", riskScore);
        assessmentDetails.put("riskLevel", riskLevel);
        assessmentDetails.put("feasibilityScore", feasibilityScore);
        assessmentDetails.put("coverageAnalysesCount", coverageAnalyses.size());
        
        // 创建评估记录
        TestRiskAssessment assessment = new TestRiskAssessment();
        assessment.setAssessmentCode(generateAssessmentCode());
        assessment.setAssessmentName(requirement.getRequirementName() + " - 风险评估");
        assessment.setRequirementId(requirementId);
        assessment.setRiskLevel(riskLevel);
        assessment.setRiskScore(riskScore);
        assessment.setRiskItems(riskItemsJson);
        assessment.setFeasibilityScore(feasibilityScore);
        assessment.setFeasibilityRecommendation(feasibilityRecommendation);
        assessment.setAssessmentDetails(toJson(assessmentDetails));
        assessment.setAssessmentTime(LocalDateTime.now());
        
        TestRiskAssessment saved = riskRepository.save(assessment);
        log.info("需求风险评估完成: assessmentCode={}, 风险评分={}, 风险等级={}", 
                saved.getAssessmentCode(), riskScore, riskLevel);
        
        return saved;
    }
    
    @Override
    @Transactional
    public TestRiskAssessment assessExecutionTaskRisk(Long executionTaskId) {
        log.info("评估执行任务风险: executionTaskId={}", executionTaskId);
        
        TestExecutionTask task = executionTaskRepository.findById(executionTaskId)
                .orElseThrow(() -> new BusinessException("执行任务不存在: " + executionTaskId));
        
        // 查询执行记录
        List<TestExecutionRecord> records = executionRecordRepository.findByTaskId(executionTaskId);
        
        // 计算风险评分
        BigDecimal riskScore = calculateExecutionTaskRiskScore(task, records);
        
        // 确定风险等级
        String riskLevel = assessRiskLevel(riskScore);
        
        // 识别风险项
        String riskItemsJson = identifyRiskItems(task.getRequirementId(), executionTaskId);
        
        // 评估上线可行性
        BigDecimal feasibilityScore = assessFeasibility(task.getRequirementId(), executionTaskId);
        
        // 生成上线建议
        String feasibilityRecommendation = generateFeasibilityRecommendation(riskScore, feasibilityScore, riskItemsJson);
        
        // 构建评估详情
        Map<String, Object> assessmentDetails = new HashMap<>();
        assessmentDetails.put("executionTaskId", executionTaskId);
        assessmentDetails.put("taskCode", task.getTaskCode());
        assessmentDetails.put("taskName", task.getTaskName());
        assessmentDetails.put("taskStatus", task.getTaskStatus());
        assessmentDetails.put("successCount", task.getSuccessCount());
        assessmentDetails.put("failCount", task.getFailCount());
        assessmentDetails.put("riskScore", riskScore);
        assessmentDetails.put("riskLevel", riskLevel);
        assessmentDetails.put("feasibilityScore", feasibilityScore);
        
        // 创建评估记录
        TestRiskAssessment assessment = new TestRiskAssessment();
        assessment.setAssessmentCode(generateAssessmentCode());
        assessment.setAssessmentName(task.getTaskName() + " - 风险评估");
        assessment.setRequirementId(task.getRequirementId());
        assessment.setExecutionTaskId(executionTaskId);
        assessment.setRiskLevel(riskLevel);
        assessment.setRiskScore(riskScore);
        assessment.setRiskItems(riskItemsJson);
        assessment.setFeasibilityScore(feasibilityScore);
        assessment.setFeasibilityRecommendation(feasibilityRecommendation);
        assessment.setAssessmentDetails(toJson(assessmentDetails));
        assessment.setAssessmentTime(LocalDateTime.now());
        
        TestRiskAssessment saved = riskRepository.save(assessment);
        log.info("执行任务风险评估完成: assessmentCode={}, 风险评分={}, 风险等级={}", 
                saved.getAssessmentCode(), riskScore, riskLevel);
        
        return saved;
    }
    
    @Override
    public String assessRiskLevel(BigDecimal riskScore) {
        if (riskScore == null) {
            return "LOW";
        }
        
        if (riskScore.compareTo(HIGH_RISK_THRESHOLD) >= 0) {
            return "HIGH";
        } else if (riskScore.compareTo(MEDIUM_RISK_THRESHOLD) >= 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    @Override
    public BigDecimal assessFeasibility(Long requirementId, Long executionTaskId) {
        log.info("评估上线可行性: requirementId={}, executionTaskId={}", requirementId, executionTaskId);
        
        // 基础可行性分数（100分）
        BigDecimal baseScore = new BigDecimal("100");
        BigDecimal deduction = BigDecimal.ZERO;
        
        // 1. 根据风险评分扣分（风险评分越高，扣分越多）
        BigDecimal riskScore = BigDecimal.ZERO;
        if (requirementId != null) {
            List<TestCase> testCases = testCaseRepository.findByRequirementId(requirementId);
            List<TestCoverageAnalysis> coverageAnalyses = coverageRepository.findByRequirementId(requirementId);
            riskScore = calculateRequirementRiskScore(
                    requirementRepository.findById(requirementId).orElse(null),
                    testCases, coverageAnalyses);
        } else if (executionTaskId != null) {
            TestExecutionTask task = executionTaskRepository.findById(executionTaskId).orElse(null);
            List<TestExecutionRecord> records = executionTaskId != null 
                    ? executionRecordRepository.findByTaskId(executionTaskId) 
                    : Collections.emptyList();
            riskScore = calculateExecutionTaskRiskScore(task, records);
        }
        
        // 风险评分越高，扣分越多（最多扣40分）
        deduction = deduction.add(riskScore.multiply(new BigDecimal("0.4")));
        
        // 2. 根据用例覆盖率扣分
        if (requirementId != null) {
            List<TestCoverageAnalysis> coverageAnalyses = coverageRepository.findByRequirementId(requirementId);
            for (TestCoverageAnalysis analysis : coverageAnalyses) {
                if (analysis.getCoverageRate() != null && analysis.getCoverageRate().compareTo(new BigDecimal("80")) < 0) {
                    // 覆盖率低于80%时扣分
                    BigDecimal gap = new BigDecimal("80").subtract(analysis.getCoverageRate());
                    deduction = deduction.add(gap.multiply(new BigDecimal("0.1"))); // 每低1%扣0.1分
                }
            }
        }
        
        // 3. 根据执行失败率扣分
        if (executionTaskId != null) {
            TestExecutionTask task = executionTaskRepository.findById(executionTaskId).orElse(null);
            if (task != null && task.getSuccessCount() + task.getFailCount() > 0) {
                int total = task.getSuccessCount() + task.getFailCount();
                double failRate = task.getFailCount() * 100.0 / total;
                if (failRate > 10) { // 失败率超过10%
                    deduction = deduction.add(BigDecimal.valueOf((failRate - 10) * 0.5)); // 每超1%扣0.5分
                }
            }
        }
        
        // 计算最终可行性分数（0-100）
        BigDecimal feasibilityScore = baseScore.subtract(deduction);
        if (feasibilityScore.compareTo(BigDecimal.ZERO) < 0) {
            feasibilityScore = BigDecimal.ZERO;
        }
        if (feasibilityScore.compareTo(new BigDecimal("100")) > 0) {
            feasibilityScore = new BigDecimal("100");
        }
        
        return feasibilityScore.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String identifyRiskItems(Long requirementId, Long executionTaskId) {
        log.info("识别风险项: requirementId={}, executionTaskId={}", requirementId, executionTaskId);
        
        List<Map<String, Object>> riskItems = new ArrayList<>();
        
        // 1. 用例数量风险
        if (requirementId != null) {
            List<TestCase> testCases = testCaseRepository.findByRequirementId(requirementId);
            if (testCases.isEmpty()) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "TEST_CASE_COUNT");
                item.put("level", "HIGH");
                item.put("description", "需求缺少测试用例");
                item.put("recommendation", "建议为该需求创建测试用例");
                riskItems.add(item);
            } else if (testCases.size() < 3) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "TEST_CASE_COUNT");
                item.put("level", "MEDIUM");
                item.put("description", "测试用例数量较少（" + testCases.size() + "个）");
                item.put("recommendation", "建议增加测试用例数量");
                riskItems.add(item);
            }
        }
        
        // 2. 覆盖率风险
        if (requirementId != null) {
            List<TestCoverageAnalysis> coverageAnalyses = coverageRepository.findByRequirementId(requirementId);
            for (TestCoverageAnalysis analysis : coverageAnalyses) {
                if (analysis.getCoverageRate() != null && analysis.getCoverageRate().compareTo(new BigDecimal("80")) < 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "COVERAGE");
                    item.put("level", analysis.getCoverageRate().compareTo(new BigDecimal("50")) < 0 ? "HIGH" : "MEDIUM");
                    item.put("description", String.format("%s覆盖率低于80%%（当前%.2f%%）", 
                            analysis.getCoverageType(), analysis.getCoverageRate()));
                    item.put("coverageType", analysis.getCoverageType());
                    item.put("coverageRate", analysis.getCoverageRate());
                    item.put("recommendation", "建议提高" + analysis.getCoverageType() + "覆盖率");
                    riskItems.add(item);
                }
            }
        }
        
        // 3. 执行失败风险
        if (executionTaskId != null) {
            TestExecutionTask task = executionTaskRepository.findById(executionTaskId).orElse(null);
            if (task != null && task.getSuccessCount() + task.getFailCount() > 0) {
                int total = task.getSuccessCount() + task.getFailCount();
                double failRate = task.getFailCount() * 100.0 / total;
                if (failRate > 10) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "EXECUTION_FAILURE");
                    item.put("level", failRate > 30 ? "HIGH" : "MEDIUM");
                    item.put("description", String.format("测试执行失败率过高（%.2f%%）", failRate));
                    item.put("failRate", failRate);
                    item.put("successCount", task.getSuccessCount());
                    item.put("failCount", task.getFailCount());
                    item.put("recommendation", "建议修复失败的测试用例");
                    riskItems.add(item);
                }
            }
        }
        
        return toJson(riskItems);
    }
    
    @Override
    public TestRiskAssessment getAssessmentById(Long id) {
        return riskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("风险评估不存在"));
    }
    
    @Override
    public TestRiskAssessment getAssessmentByCode(String assessmentCode) {
        return riskRepository.findByAssessmentCode(assessmentCode)
                .orElseThrow(() -> new BusinessException("风险评估不存在: " + assessmentCode));
    }
    
    @Override
    public Page<TestRiskAssessment> getAssessmentList(Pageable pageable) {
        return riskRepository.findAll(pageable);
    }
    
    @Override
    public List<TestRiskAssessment> getAssessmentByRequirementId(Long requirementId) {
        return riskRepository.findByRequirementId(requirementId);
    }
    
    @Override
    public List<TestRiskAssessment> getAssessmentByExecutionTaskId(Long executionTaskId) {
        return riskRepository.findByExecutionTaskId(executionTaskId);
    }
    
    @Override
    public List<TestRiskAssessment> getAssessmentByRiskLevel(String riskLevel) {
        return riskRepository.findByRiskLevel(riskLevel);
    }
    
    /**
     * 计算需求风险评分（0-100，分数越高风险越大）
     */
    private BigDecimal calculateRequirementRiskScore(TestRequirement requirement, 
                                                      List<TestCase> testCases,
                                                      List<TestCoverageAnalysis> coverageAnalyses) {
        BigDecimal riskScore = BigDecimal.ZERO;
        
        // 1. 用例数量风险（30%权重）
        if (testCases.isEmpty()) {
            riskScore = riskScore.add(new BigDecimal("30")); // 无用例，高风险
        } else if (testCases.size() < 3) {
            riskScore = riskScore.add(new BigDecimal("15")); // 用例少，中风险
        }
        
        // 2. 覆盖率风险（40%权重）
        if (!coverageAnalyses.isEmpty()) {
            BigDecimal avgCoverageRate = coverageAnalyses.stream()
                    .filter(a -> a.getCoverageRate() != null)
                    .map(TestCoverageAnalysis::getCoverageRate)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(coverageAnalyses.size()), 2, RoundingMode.HALF_UP);
            
            if (avgCoverageRate.compareTo(new BigDecimal("50")) < 0) {
                riskScore = riskScore.add(new BigDecimal("40")); // 覆盖率低于50%，高风险
            } else if (avgCoverageRate.compareTo(new BigDecimal("80")) < 0) {
                // 覆盖率50-80%，按比例扣分
                BigDecimal gap = new BigDecimal("80").subtract(avgCoverageRate);
                riskScore = riskScore.add(gap.multiply(new BigDecimal("1.33"))); // 每低1%加1.33分
            }
        } else {
            // 无覆盖分析数据，中风险
            riskScore = riskScore.add(new BigDecimal("20"));
        }
        
        // 3. 需求状态风险（30%权重）
        if (requirement != null && requirement.getRequirementStatus() != null) {
            String status = requirement.getRequirementStatus();
            if ("已关闭".equals(status) || "CLOSED".equals(status)) {
                riskScore = riskScore.add(new BigDecimal("30")); // 已关闭需求，高风险
            } else if ("审核中".equals(status) || "REVIEWING".equals(status)) {
                riskScore = riskScore.add(new BigDecimal("10")); // 审核中，低风险
            }
        }
        
        // 确保分数在0-100之间
        if (riskScore.compareTo(BigDecimal.ZERO) < 0) {
            riskScore = BigDecimal.ZERO;
        }
        if (riskScore.compareTo(new BigDecimal("100")) > 0) {
            riskScore = new BigDecimal("100");
        }
        
        return riskScore.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算执行任务风险评分（0-100，分数越高风险越大）
     */
    private BigDecimal calculateExecutionTaskRiskScore(TestExecutionTask task, 
                                                         List<TestExecutionRecord> records) {
        BigDecimal riskScore = BigDecimal.ZERO;
        
        if (task == null) {
            return new BigDecimal("50"); // 任务不存在，中等风险
        }
        
        // 1. 任务状态风险（20%权重）
        String taskStatus = task.getTaskStatus();
        if ("FAILED".equals(taskStatus)) {
            riskScore = riskScore.add(new BigDecimal("20")); // 任务失败，高风险
        } else if ("PROCESSING".equals(taskStatus)) {
            riskScore = riskScore.add(new BigDecimal("5")); // 任务进行中，低风险
        }
        
        // 2. 执行失败率风险（60%权重）
        int totalCount = task.getSuccessCount() + task.getFailCount();
        if (totalCount > 0) {
            double failRate = task.getFailCount() * 100.0 / totalCount;
            if (failRate > 50) {
                riskScore = riskScore.add(new BigDecimal("60")); // 失败率>50%，高风险
            } else if (failRate > 20) {
                // 失败率20-50%，按比例扣分
                riskScore = riskScore.add(BigDecimal.valueOf((failRate - 20) * 2)); // 每超1%加2分
            }
        } else {
            // 无执行记录，中等风险
            riskScore = riskScore.add(new BigDecimal("30"));
        }
        
        // 3. 记录失败风险（20%权重）
        long failedRecords = records.stream()
                .filter(r -> "FAILED".equals(r.getExecutionStatus()))
                .count();
        if (failedRecords > 0 && records.size() > 0) {
            double recordFailRate = failedRecords * 100.0 / records.size();
            riskScore = riskScore.add(BigDecimal.valueOf(recordFailRate * 0.2)); // 每1%失败率加0.2分
        }
        
        // 确保分数在0-100之间
        if (riskScore.compareTo(BigDecimal.ZERO) < 0) {
            riskScore = BigDecimal.ZERO;
        }
        if (riskScore.compareTo(new BigDecimal("100")) > 0) {
            riskScore = new BigDecimal("100");
        }
        
        return riskScore.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 生成上线建议
     */
    private String generateFeasibilityRecommendation(BigDecimal riskScore, 
                                                      BigDecimal feasibilityScore,
                                                      String riskItemsJson) {
        StringBuilder recommendation = new StringBuilder();
        
        // 根据可行性评分生成建议
        if (feasibilityScore.compareTo(new BigDecimal("80")) >= 0) {
            recommendation.append("✅ 上线可行性较高（").append(feasibilityScore).append("分），");
            recommendation.append("建议正常上线。");
        } else if (feasibilityScore.compareTo(new BigDecimal("60")) >= 0) {
            recommendation.append("⚠️ 上线可行性中等（").append(feasibilityScore).append("分），");
            recommendation.append("建议修复部分风险项后再上线。");
        } else {
            recommendation.append("❌ 上线可行性较低（").append(feasibilityScore).append("分），");
            recommendation.append("不建议上线，需修复所有高风险项。");
        }
        
        // 根据风险等级添加额外建议
        String riskLevel = assessRiskLevel(riskScore);
        if ("HIGH".equals(riskLevel)) {
            recommendation.append("\n高风险：建议重点关注并修复高风险项。");
        } else if ("MEDIUM".equals(riskLevel)) {
            recommendation.append("\n中风险：建议关注并修复中风险项。");
        }
        
        return recommendation.toString();
    }
    
    /**
     * 生成评估编码（RISK-YYYYMMDD-序号）
     */
    private String generateAssessmentCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = RISK_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天已生成的评估编码
        List<TestRiskAssessment> todayAssessments = riskRepository
                .findByAssessmentCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestRiskAssessment a : todayAssessments) {
            String code = a.getAssessmentCode();
            if (code != null && code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    log.warn("评估编码格式不正确: {}", code);
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        return prefix + String.format("%03d", newSequence);
    }
    
    /**
     * 验证评估数据
     */
    private void validateAssessment(TestRiskAssessment assessment) {
        if (!StringUtils.hasText(assessment.getAssessmentName())) {
            throw new BusinessException("评估名称不能为空");
        }
        if (assessment.getRequirementId() == null && assessment.getExecutionTaskId() == null) {
            throw new BusinessException("必须提供需求ID或执行任务ID");
        }
    }
    
    /**
     * 对象转JSON字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("对象转JSON失败: {}", e.getMessage(), e);
            return "{}";
        }
    }
}

