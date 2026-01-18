package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.*;
import com.sinosoft.testdesign.repository.*;
import com.sinosoft.testdesign.service.SpecificationCheckService;
import com.sinosoft.testdesign.service.TestSpecificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 规约检查服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecificationCheckServiceImpl implements SpecificationCheckService {
    
    private final TestSpecificationRepository specificationRepository;
    private final TestSpecificationService specificationService;
    private final FieldTestPointRepository fieldTestPointRepository;
    private final LogicTestPointRepository logicTestPointRepository;
    private final TestLayerRepository testLayerRepository;
    private final TestMethodRepository testMethodRepository;
    private final RequirementRepository requirementRepository;
    
    @Override
    public List<TestSpecification> matchSpecifications(TestCase testCase) {
        log.info("开始匹配适用的规约，用例ID: {}", testCase.getId());
        
        List<TestSpecification> matchedSpecs = new ArrayList<>();
        
        // 获取用例的模块信息（从关联的需求中获取）
        final String module;
        if (testCase.getRequirementId() != null) {
            Optional<TestRequirement> requirementOpt = requirementRepository.findById(testCase.getRequirementId());
            module = requirementOpt.map(TestRequirement::getBusinessModule).orElse(null);
        } else {
            module = null;
        }
        
        // 获取测试分层代码
        final String layerCode;
        if (testCase.getLayerId() != null) {
            Optional<TestLayer> layerOpt = testLayerRepository.findById(testCase.getLayerId());
            layerCode = layerOpt.map(TestLayer::getLayerCode).orElse(null);
        } else {
            layerCode = null;
        }
        
        // 获取测试方法代码
        final String methodCode;
        if (testCase.getMethodId() != null) {
            Optional<TestDesignMethod> methodOpt = testMethodRepository.findById(testCase.getMethodId());
            methodCode = methodOpt.map(TestDesignMethod::getMethodCode).orElse(null);
        } else {
            methodCode = null;
        }
        
        // 1. 首先尝试匹配应用级规约（优先级更高）
        List<TestSpecification> applicationSpecs = specificationService.getApplicationSpecifications();
        matchedSpecs.addAll(matchSpecificationsByCriteria(applicationSpecs, module, layerCode, methodCode));
        
        // 2. 然后匹配公共规约
        List<TestSpecification> publicSpecs = specificationService.getPublicSpecifications();
        matchedSpecs.addAll(matchSpecificationsByCriteria(publicSpecs, module, layerCode, methodCode));
        
        // 去重并排序（应用级规约在前，然后按适用度排序）
        final String finalModule = module;
        final String finalLayerCode = layerCode;
        final String finalMethodCode = methodCode;
        matchedSpecs = matchedSpecs.stream()
                .distinct()
                .sorted((TestSpecification s1, TestSpecification s2) -> {
                    // 先按类型排序（APPLICATION在前）
                    int typeCompare = s2.getSpecType().compareTo(s1.getSpecType());
                    if (typeCompare != 0) {
                        return typeCompare;
                    }
                    // 再按适用度排序（匹配项多的在前）
                    int score2 = getMatchScore(s2, finalModule, finalLayerCode, finalMethodCode);
                    int score1 = getMatchScore(s1, finalModule, finalLayerCode, finalMethodCode);
                    return Integer.compare(score2, score1);
                })
                .collect(Collectors.toList());
        
        log.info("匹配到 {} 个适用的规约", matchedSpecs.size());
        return matchedSpecs;
    }
    
    /**
     * 根据条件匹配规约
     */
    private List<TestSpecification> matchSpecificationsByCriteria(
            List<TestSpecification> specifications,
            String module, String layerCode, String methodCode) {
        return specifications.stream()
                .filter(spec -> {
                    // 检查规约是否启用
                    if (!"1".equals(spec.getIsActive())) {
                        return false;
                    }
                    
                    // 检查生效日期
                    if (spec.getEffectiveDate() != null && spec.getEffectiveDate().isAfter(LocalDate.now())) {
                        return false;
                    }
                    
                    // 检查失效日期
                    if (spec.getExpireDate() != null && spec.getExpireDate().isBefore(LocalDate.now())) {
                        return false;
                    }
                    
                    // 检查模块匹配
                    if (module != null && StringUtils.hasText(spec.getApplicableModules())) {
                        if (!spec.getApplicableModules().contains(module)) {
                            return false;
                        }
                    }
                    
                    // 检查测试分层匹配
                    if (layerCode != null && StringUtils.hasText(spec.getApplicableLayers())) {
                        if (!spec.getApplicableLayers().contains(layerCode)) {
                            return false;
                        }
                    }
                    
                    // 检查测试方法匹配
                    if (methodCode != null && StringUtils.hasText(spec.getApplicableMethods())) {
                        if (!spec.getApplicableMethods().contains(methodCode)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 计算规约的匹配度评分
     */
    private int getMatchScore(TestSpecification spec, String module, String layerCode, String methodCode) {
        int score = 0;
        
        // 模块匹配 +10分
        if (module != null && StringUtils.hasText(spec.getApplicableModules())) {
            if (spec.getApplicableModules().contains(module)) {
                score += 10;
            }
        }
        
        // 测试分层匹配 +5分
        if (layerCode != null && StringUtils.hasText(spec.getApplicableLayers())) {
            if (spec.getApplicableLayers().contains(layerCode)) {
                score += 5;
            }
        }
        
        // 测试方法匹配 +5分
        if (methodCode != null && StringUtils.hasText(spec.getApplicableMethods())) {
            if (spec.getApplicableMethods().contains(methodCode)) {
                score += 5;
            }
        }
        
        return score;
    }
    
    @Override
    public SpecificationComplianceResult checkCompliance(TestCase testCase, List<TestSpecification> specifications) {
        log.info("开始检查用例规约符合性，用例ID: {}", testCase.getId());
        
        SpecificationComplianceResult result = new SpecificationComplianceResult();
        result.setTotalChecks(0);
        result.setPassedChecks(0);
        result.setFailedChecks(0);
        result.setIssues(new ArrayList<>());
        
        // 如果未指定规约，则自动匹配
        if (specifications == null || specifications.isEmpty()) {
            specifications = matchSpecifications(testCase);
        }
        
        if (specifications.isEmpty()) {
            log.warn("未找到适用的规约，跳过符合性检查");
            result.setCompliant(true);
            result.setComplianceScore(100.0);
            return result;
        }
        
        // 对每个规约进行检查
        for (TestSpecification spec : specifications) {
            checkSpecificationCompliance(testCase, spec, result);
        }
        
        // 计算符合度评分
        if (result.getTotalChecks() > 0) {
            result.setComplianceScore(
                (double) result.getPassedChecks() / result.getTotalChecks() * 100.0
            );
            result.setCompliant(result.getComplianceScore() >= 80.0); // 80分以上视为符合
        } else {
            result.setComplianceScore(100.0);
            result.setCompliant(true);
        }
        
        log.info("规约符合性检查完成，符合度评分: {}, 是否符合: {}", 
                result.getComplianceScore(), result.isCompliant());
        
        return result;
    }
    
    /**
     * 检查单个规约的符合性
     */
    private void checkSpecificationCompliance(
            TestCase testCase, TestSpecification spec, SpecificationComplianceResult result) {
        log.debug("检查规约符合性，规约编码: {}", spec.getSpecCode());
        
        // 1. 检查字段测试要点
        List<FieldTestPoint> fieldPoints = fieldTestPointRepository
                .findBySpecIdAndIsActiveOrderByDisplayOrderAsc(spec.getId(), "1");
        
        for (FieldTestPoint point : fieldPoints) {
            result.setTotalChecks(result.getTotalChecks() + 1);
            
            // 检查必填字段
            if ("1".equals(point.getIsRequired())) {
                // 这里可以根据字段名称检查用例内容是否包含该字段
                // 简化实现：检查用例名称或描述中是否包含字段名称
                if (!containsField(testCase, point.getFieldName())) {
                    ComplianceIssue issue = new ComplianceIssue();
                    issue.setSpecCode(spec.getSpecCode());
                    issue.setSpecName(spec.getSpecName());
                    issue.setIssueType("FIELD_REQUIREMENT");
                    issue.setIssueDescription(String.format("缺少必填字段测试要点: %s", point.getFieldName()));
                    issue.setSeverity("HIGH");
                    issue.setSuggestion(point.getTestRequirement());
                    result.getIssues().add(issue);
                    result.setFailedChecks(result.getFailedChecks() + 1);
                } else {
                    result.setPassedChecks(result.getPassedChecks() + 1);
                }
            } else {
                result.setPassedChecks(result.getPassedChecks() + 1);
            }
        }
        
        // 2. 检查逻辑测试要点
        List<LogicTestPoint> logicPoints = logicTestPointRepository
                .findBySpecIdAndIsActiveOrderByDisplayOrderAsc(spec.getId(), "1");
        
        for (LogicTestPoint point : logicPoints) {
            result.setTotalChecks(result.getTotalChecks() + 1);
            
            // 检查逻辑测试要点
            if (!containsLogic(testCase, point.getLogicName())) {
                ComplianceIssue issue = new ComplianceIssue();
                issue.setSpecCode(spec.getSpecCode());
                issue.setSpecName(spec.getSpecName());
                issue.setIssueType("LOGIC_REQUIREMENT");
                issue.setIssueDescription(String.format("缺少逻辑测试要点: %s", point.getLogicName()));
                issue.setSeverity("MEDIUM");
                issue.setSuggestion(point.getTestRequirement());
                result.getIssues().add(issue);
                result.setFailedChecks(result.getFailedChecks() + 1);
            } else {
                result.setPassedChecks(result.getPassedChecks() + 1);
            }
        }
        
        // 3. 检查格式要求（从规约内容中提取）
        if (StringUtils.hasText(spec.getSpecContent())) {
            // 这里可以解析规约内容中的格式要求进行检查
            // 简化实现：检查用例的基本格式要求
            checkFormatRequirements(testCase, spec, result);
        }
    }
    
    /**
     * 检查用例是否包含字段
     */
    private boolean containsField(TestCase testCase, String fieldName) {
        String caseContent = (testCase.getCaseName() != null ? testCase.getCaseName() : "") +
                            " " + (testCase.getPreCondition() != null ? testCase.getPreCondition() : "") +
                            " " + (testCase.getTestStep() != null ? testCase.getTestStep() : "") +
                            " " + (testCase.getExpectedResult() != null ? testCase.getExpectedResult() : "");
        return caseContent.toLowerCase().contains(fieldName.toLowerCase());
    }
    
    /**
     * 检查用例是否包含逻辑
     */
    private boolean containsLogic(TestCase testCase, String logicName) {
        String caseContent = (testCase.getCaseName() != null ? testCase.getCaseName() : "") +
                            " " + (testCase.getPreCondition() != null ? testCase.getPreCondition() : "") +
                            " " + (testCase.getTestStep() != null ? testCase.getTestStep() : "") +
                            " " + (testCase.getExpectedResult() != null ? testCase.getExpectedResult() : "");
        return caseContent.toLowerCase().contains(logicName.toLowerCase());
    }
    
    /**
     * 检查格式要求
     */
    private void checkFormatRequirements(TestCase testCase, TestSpecification spec, SpecificationComplianceResult result) {
        // 检查前置条件是否为空
        if (!StringUtils.hasText(testCase.getPreCondition())) {
            result.setTotalChecks(result.getTotalChecks() + 1);
            ComplianceIssue issue = new ComplianceIssue();
            issue.setSpecCode(spec.getSpecCode());
            issue.setSpecName(spec.getSpecName());
            issue.setIssueType("FORMAT_REQUIREMENT");
            issue.setIssueDescription("前置条件为空，不符合规约要求");
            issue.setSeverity("MEDIUM");
            issue.setSuggestion("请填写前置条件");
            result.getIssues().add(issue);
            result.setFailedChecks(result.getFailedChecks() + 1);
        } else {
            result.setTotalChecks(result.getTotalChecks() + 1);
            result.setPassedChecks(result.getPassedChecks() + 1);
        }
        
        // 检查测试步骤是否为空
        if (!StringUtils.hasText(testCase.getTestStep())) {
            result.setTotalChecks(result.getTotalChecks() + 1);
            ComplianceIssue issue = new ComplianceIssue();
            issue.setSpecCode(spec.getSpecCode());
            issue.setSpecName(spec.getSpecName());
            issue.setIssueType("FORMAT_REQUIREMENT");
            issue.setIssueDescription("测试步骤为空，不符合规约要求");
            issue.setSeverity("HIGH");
            issue.setSuggestion("请填写测试步骤");
            result.getIssues().add(issue);
            result.setFailedChecks(result.getFailedChecks() + 1);
        } else {
            result.setTotalChecks(result.getTotalChecks() + 1);
            result.setPassedChecks(result.getPassedChecks() + 1);
        }
        
        // 检查预期结果是否为空
        if (!StringUtils.hasText(testCase.getExpectedResult())) {
            result.setTotalChecks(result.getTotalChecks() + 1);
            ComplianceIssue issue = new ComplianceIssue();
            issue.setSpecCode(spec.getSpecCode());
            issue.setSpecName(spec.getSpecName());
            issue.setIssueType("FORMAT_REQUIREMENT");
            issue.setIssueDescription("预期结果为空，不符合规约要求");
            issue.setSeverity("HIGH");
            issue.setSuggestion("请填写预期结果");
            result.getIssues().add(issue);
            result.setFailedChecks(result.getFailedChecks() + 1);
        } else {
            result.setTotalChecks(result.getTotalChecks() + 1);
            result.setPassedChecks(result.getPassedChecks() + 1);
        }
    }
    
    @Override
    public SpecificationInjectionResult injectSpecification(
            TestCase testCase, List<TestSpecification> specifications) {
        log.info("开始注入规约内容到用例，用例ID: {}", testCase.getId());
        
        SpecificationInjectionResult result = new SpecificationInjectionResult();
        result.setEnhancedTestCase(new TestCase());
        result.setInjectedContents(new ArrayList<>());
        result.setAppliedSpecs(new ArrayList<>());
        
        // 复制原用例
        TestCase enhanced = copyTestCase(testCase);
        
        // 如果未指定规约，则自动匹配
        if (specifications == null || specifications.isEmpty()) {
            specifications = matchSpecifications(testCase);
        }
        
        if (specifications.isEmpty()) {
            log.warn("未找到适用的规约，跳过规约注入");
            result.setEnhancedTestCase(enhanced);
            return result;
        }
        
        // 对每个规约进行内容注入
        for (TestSpecification spec : specifications) {
            injectSpecificationContent(enhanced, spec, result);
            result.getAppliedSpecs().add(spec.getSpecCode());
        }
        
        result.setEnhancedTestCase(enhanced);
        
        log.info("规约内容注入完成，应用了 {} 个规约", result.getAppliedSpecs().size());
        
        return result;
    }
    
    /**
     * 注入单个规约的内容
     */
    private void injectSpecificationContent(
            TestCase testCase, TestSpecification spec, SpecificationInjectionResult result) {
        log.debug("注入规约内容，规约编码: {}", spec.getSpecCode());
        
        // 1. 注入字段测试要点到前置条件或测试步骤
        List<FieldTestPoint> fieldPoints = fieldTestPointRepository
                .findBySpecIdAndIsActiveOrderByDisplayOrderAsc(spec.getId(), "1");
        
        StringBuilder preConditionBuilder = new StringBuilder(
                testCase.getPreCondition() != null ? testCase.getPreCondition() : "");
        
        for (FieldTestPoint point : fieldPoints) {
            if ("1".equals(point.getIsRequired())) {
                if (StringUtils.hasText(point.getTestRequirement())) {
                    if (preConditionBuilder.length() > 0) {
                        preConditionBuilder.append("\n");
                    }
                    preConditionBuilder.append(String.format("[字段测试要点] %s: %s", 
                            point.getFieldName(), point.getTestRequirement()));
                    result.getInjectedContents().add(String.format("字段测试要点: %s", point.getFieldName()));
                }
            }
        }
        
        if (preConditionBuilder.length() > 0) {
            testCase.setPreCondition(preConditionBuilder.toString());
        }
        
        // 2. 注入逻辑测试要点到测试步骤
        List<LogicTestPoint> logicPoints = logicTestPointRepository
                .findBySpecIdAndIsActiveOrderByDisplayOrderAsc(spec.getId(), "1");
        
        StringBuilder testStepBuilder = new StringBuilder(
                testCase.getTestStep() != null ? testCase.getTestStep() : "");
        
        for (LogicTestPoint point : logicPoints) {
            if (StringUtils.hasText(point.getTestRequirement())) {
                if (testStepBuilder.length() > 0) {
                    testStepBuilder.append("\n");
                }
                testStepBuilder.append(String.format("[逻辑测试要点] %s: %s", 
                        point.getLogicName(), point.getTestRequirement()));
                result.getInjectedContents().add(String.format("逻辑测试要点: %s", point.getLogicName()));
            }
        }
        
        if (testStepBuilder.length() > 0) {
            testCase.setTestStep(testStepBuilder.toString());
        }
    }
    
    /**
     * 复制用例对象
     */
    private TestCase copyTestCase(TestCase source) {
        TestCase target = new TestCase();
        target.setCaseCode(source.getCaseCode());
        target.setCaseName(source.getCaseName());
        target.setRequirementId(source.getRequirementId());
        target.setLayerId(source.getLayerId());
        target.setMethodId(source.getMethodId());
        target.setCaseType(source.getCaseType());
        target.setCasePriority(source.getCasePriority());
        target.setPreCondition(source.getPreCondition());
        target.setTestStep(source.getTestStep());
        target.setExpectedResult(source.getExpectedResult());
        target.setCaseStatus(source.getCaseStatus());
        target.setCreatorId(source.getCreatorId());
        target.setCreatorName(source.getCreatorName());
        return target;
    }
}

