package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestCoverageAnalysis;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.repository.TestCoverageAnalysisRepository;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.service.TestCoverageService;
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
import java.util.stream.Collectors;

/**
 * 测试覆盖分析服务实现
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCoverageServiceImpl implements TestCoverageService {
    
    private final TestCoverageAnalysisRepository coverageRepository;
    private final TestCaseRepository testCaseRepository;
    private final RequirementRepository requirementRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String COVERAGE_CODE_PREFIX = "COV";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public TestCoverageAnalysis analyzeCoverage(TestCoverageAnalysis analysis) {
        log.info("分析测试覆盖: {}, 类型: {}", analysis.getAnalysisName(), analysis.getCoverageType());
        
        // 数据验证
        validateAnalysis(analysis);
        
        // 自动生成分析编码（如果未提供）
        if (!StringUtils.hasText(analysis.getAnalysisCode())) {
            analysis.setAnalysisCode(generateAnalysisCode());
        } else {
            // 检查编码是否已存在
            if (coverageRepository.findByAnalysisCode(analysis.getAnalysisCode()).isPresent()) {
                throw new BusinessException("分析编码已存在: " + analysis.getAnalysisCode());
            }
        }
        
        // 根据覆盖类型执行相应的分析
        switch (analysis.getCoverageType()) {
            case "REQUIREMENT":
                return analyzeRequirementCoverage(analysis.getRequirementId());
            case "FUNCTION":
                return analyzeFunctionCoverage(analysis.getRequirementId());
            case "SCENARIO":
                return analyzeScenarioCoverage(analysis.getRequirementId());
            case "CODE":
                // 代码覆盖需要外部数据
                throw new BusinessException("代码覆盖分析需要提供覆盖数据，请使用 analyzeCodeCoverage 方法");
            default:
                throw new BusinessException("不支持的覆盖类型: " + analysis.getCoverageType());
        }
    }
    
    @Override
    @Transactional
    public TestCoverageAnalysis analyzeRequirementCoverage(Long requirementId) {
        log.info("分析需求覆盖: requirementId={}", requirementId);
        
        TestRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new BusinessException("需求不存在: " + requirementId));
        
        // 查询该需求下的所有用例
        List<TestCase> testCases = testCaseRepository.findByRequirementId(requirementId);
        
        // 需求覆盖统计：所有需求都应该有至少一个用例
        int totalRequirements = 1; // 当前需求
        int coveredRequirements = testCases.isEmpty() ? 0 : 1;
        
        // 计算覆盖率
        BigDecimal coverageRate = totalRequirements > 0 
                ? BigDecimal.valueOf(coveredRequirements * 100.0 / totalRequirements)
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // 构建覆盖详情
        Map<String, Object> coverageDetails = new HashMap<>();
        coverageDetails.put("requirementId", requirementId);
        coverageDetails.put("requirementCode", requirement.getRequirementCode());
        coverageDetails.put("requirementName", requirement.getRequirementName());
        coverageDetails.put("testCaseCount", testCases.size());
        coverageDetails.put("covered", coveredRequirements > 0);
        
        // 未覆盖项列表
        List<Map<String, Object>> uncoveredItems = new ArrayList<>();
        if (coveredRequirements == 0) {
            Map<String, Object> item = new HashMap<>();
            item.put("requirementId", requirementId);
            item.put("requirementCode", requirement.getRequirementCode());
            item.put("requirementName", requirement.getRequirementName());
            uncoveredItems.add(item);
        }
        
        // 创建分析记录
        TestCoverageAnalysis analysis = new TestCoverageAnalysis();
        analysis.setAnalysisCode(generateAnalysisCode());
        analysis.setAnalysisName(requirement.getRequirementName() + " - 需求覆盖分析");
        analysis.setRequirementId(requirementId);
        analysis.setCoverageType("REQUIREMENT");
        analysis.setTotalItems(totalRequirements);
        analysis.setCoveredItems(coveredRequirements);
        analysis.setCoverageRate(coverageRate);
        analysis.setCoverageDetails(toJson(coverageDetails));
        analysis.setUncoveredItems(toJson(uncoveredItems));
        analysis.setAnalysisTime(LocalDateTime.now());
        
        TestCoverageAnalysis saved = coverageRepository.save(analysis);
        log.info("需求覆盖分析完成: analysisCode={}, 覆盖率={}%", saved.getAnalysisCode(), coverageRate);
        
        return saved;
    }
    
    @Override
    @Transactional
    public TestCoverageAnalysis analyzeFunctionCoverage(Long requirementId) {
        log.info("分析功能覆盖: requirementId={}", requirementId);
        
        TestRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new BusinessException("需求不存在: " + requirementId));
        
        // 查询该需求下的所有用例
        List<TestCase> testCases = testCaseRepository.findByRequirementId(requirementId);
        
        // 功能覆盖统计：从需求描述中提取功能点，然后检查用例是否覆盖这些功能点
        // 简化实现：基于用例数量评估功能覆盖
        // 实际应该从需求文档或需求描述中解析功能点列表
        Set<String> functions = extractFunctionsFromRequirement(requirement);
        int totalFunctions = functions.size();
        
        // 统计覆盖的功能点（从用例中提取功能相关关键词）
        Set<String> coveredFunctions = extractFunctionsFromTestCases(testCases);
        int coveredCount = 0;
        for (String func : functions) {
            if (coveredFunctions.contains(func) || isFunctionCovered(func, testCases)) {
                coveredCount++;
            }
        }
        
        // 如果需求没有明确的功能点，则基于用例数量进行评估
        if (totalFunctions == 0) {
            // 假设至少需要3个用例才能覆盖基本功能
            totalFunctions = 1;
            coveredCount = testCases.size() >= 3 ? 1 : 0;
        }
        
        // 计算覆盖率
        BigDecimal coverageRate = totalFunctions > 0
                ? BigDecimal.valueOf(coveredCount * 100.0 / totalFunctions)
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // 构建覆盖详情
        Map<String, Object> coverageDetails = new HashMap<>();
        coverageDetails.put("requirementId", requirementId);
        coverageDetails.put("totalFunctions", totalFunctions);
        coverageDetails.put("coveredFunctions", coveredCount);
        coverageDetails.put("functionList", functions);
        coverageDetails.put("coveredFunctionList", coveredFunctions);
        
        // 未覆盖项列表
        List<String> uncoveredFunctionList = new ArrayList<>();
        for (String func : functions) {
            if (!coveredFunctions.contains(func) && !isFunctionCovered(func, testCases)) {
                uncoveredFunctionList.add(func);
            }
        }
        
        // 创建分析记录
        TestCoverageAnalysis analysis = new TestCoverageAnalysis();
        analysis.setAnalysisCode(generateAnalysisCode());
        analysis.setAnalysisName(requirement.getRequirementName() + " - 功能覆盖分析");
        analysis.setRequirementId(requirementId);
        analysis.setCoverageType("FUNCTION");
        analysis.setTotalItems(totalFunctions);
        analysis.setCoveredItems(coveredCount);
        analysis.setCoverageRate(coverageRate);
        analysis.setCoverageDetails(toJson(coverageDetails));
        analysis.setUncoveredItems(toJson(uncoveredFunctionList));
        analysis.setAnalysisTime(LocalDateTime.now());
        
        TestCoverageAnalysis saved = coverageRepository.save(analysis);
        log.info("功能覆盖分析完成: analysisCode={}, 覆盖率={}%", saved.getAnalysisCode(), coverageRate);
        
        return saved;
    }
    
    @Override
    @Transactional
    public TestCoverageAnalysis analyzeScenarioCoverage(Long requirementId) {
        log.info("分析场景覆盖: requirementId={}", requirementId);
        
        TestRequirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new BusinessException("需求不存在: " + requirementId));
        
        // 查询该需求下的所有用例
        List<TestCase> testCases = testCaseRepository.findByRequirementId(requirementId);
        
        // 场景覆盖统计：基于用例类型（正常/异常/边界）评估场景覆盖
        // 理想情况下应该有正常场景、异常场景、边界场景
        int totalScenarios = 3; // 正常、异常、边界
        Set<String> scenarioTypes = new HashSet<>();
        
        for (TestCase testCase : testCases) {
            if (StringUtils.hasText(testCase.getCaseType())) {
                String caseType = testCase.getCaseType().toLowerCase();
                if (caseType.contains("正常") || caseType.contains("normal") || caseType.equals("正常")) {
                    scenarioTypes.add("正常");
                } else if (caseType.contains("异常") || caseType.contains("exception") || caseType.contains("异常")) {
                    scenarioTypes.add("异常");
                } else if (caseType.contains("边界") || caseType.contains("boundary") || caseType.equals("边界")) {
                    scenarioTypes.add("边界");
                }
            }
        }
        
        int coveredScenarios = scenarioTypes.size();
        
        // 计算覆盖率
        BigDecimal coverageRate = BigDecimal.valueOf(coveredScenarios * 100.0 / totalScenarios)
                .setScale(2, RoundingMode.HALF_UP);
        
        // 构建覆盖详情
        Map<String, Object> coverageDetails = new HashMap<>();
        coverageDetails.put("requirementId", requirementId);
        coverageDetails.put("totalScenarios", totalScenarios);
        coverageDetails.put("coveredScenarios", coveredScenarios);
        coverageDetails.put("scenarioTypes", scenarioTypes);
        coverageDetails.put("testCaseCount", testCases.size());
        
        // 未覆盖项列表
        List<String> uncoveredScenarios = new ArrayList<>();
        if (!scenarioTypes.contains("正常")) {
            uncoveredScenarios.add("正常场景");
        }
        if (!scenarioTypes.contains("异常")) {
            uncoveredScenarios.add("异常场景");
        }
        if (!scenarioTypes.contains("边界")) {
            uncoveredScenarios.add("边界场景");
        }
        
        // 创建分析记录
        TestCoverageAnalysis analysis = new TestCoverageAnalysis();
        analysis.setAnalysisCode(generateAnalysisCode());
        analysis.setAnalysisName(requirement.getRequirementName() + " - 场景覆盖分析");
        analysis.setRequirementId(requirementId);
        analysis.setCoverageType("SCENARIO");
        analysis.setTotalItems(totalScenarios);
        analysis.setCoveredItems(coveredScenarios);
        analysis.setCoverageRate(coverageRate);
        analysis.setCoverageDetails(toJson(coverageDetails));
        analysis.setUncoveredItems(toJson(uncoveredScenarios));
        analysis.setAnalysisTime(LocalDateTime.now());
        
        TestCoverageAnalysis saved = coverageRepository.save(analysis);
        log.info("场景覆盖分析完成: analysisCode={}, 覆盖率={}%", saved.getAnalysisCode(), coverageRate);
        
        return saved;
    }
    
    @Override
    @Transactional
    public TestCoverageAnalysis analyzeCodeCoverage(Long requirementId, String coverageData) {
        log.info("分析代码覆盖: requirementId={}, hasData={}", requirementId, StringUtils.hasText(coverageData));
        
        if (!StringUtils.hasText(coverageData)) {
            throw new BusinessException("代码覆盖数据不能为空");
        }
        
        // 解析代码覆盖数据（JSON格式）
        Map<String, Object> coverageMap;
        try {
            coverageMap = objectMapper.readValue(coverageData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析代码覆盖数据失败: {}", e.getMessage(), e);
            throw new BusinessException("代码覆盖数据格式错误: " + e.getMessage());
        }
        
        // 提取覆盖率信息
        Double lineCoverage = getDoubleValue(coverageMap, "lineCoverage", 0.0);
        Double branchCoverage = getDoubleValue(coverageMap, "branchCoverage", 0.0);
        Integer totalLines = getIntegerValue(coverageMap, "totalLines", 0);
        Integer coveredLines = getIntegerValue(coverageMap, "coveredLines", 0);
        
        // 计算综合覆盖率（行覆盖率和分支覆盖率的平均值）
        BigDecimal coverageRate = BigDecimal.valueOf((lineCoverage + branchCoverage) / 2.0)
                .setScale(2, RoundingMode.HALF_UP);
        
        // 构建覆盖详情
        Map<String, Object> coverageDetails = new HashMap<>();
        coverageDetails.put("requirementId", requirementId);
        coverageDetails.put("lineCoverage", lineCoverage);
        coverageDetails.put("branchCoverage", branchCoverage);
        coverageDetails.put("totalLines", totalLines);
        coverageDetails.put("coveredLines", coveredLines);
        coverageDetails.put("rawData", coverageMap);
        
        // 未覆盖项列表（覆盖率低于80%时标记）
        List<Map<String, Object>> uncoveredItems = new ArrayList<>();
        if (lineCoverage < 80.0) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "line");
            item.put("coverage", lineCoverage);
            item.put("threshold", 80.0);
            uncoveredItems.add(item);
        }
        if (branchCoverage < 80.0) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "branch");
            item.put("coverage", branchCoverage);
            item.put("threshold", 80.0);
            uncoveredItems.add(item);
        }
        
        // 创建分析记录
        TestCoverageAnalysis analysis = new TestCoverageAnalysis();
        analysis.setAnalysisCode(generateAnalysisCode());
        analysis.setAnalysisName("代码覆盖分析" + (requirementId != null ? " - 需求ID: " + requirementId : ""));
        analysis.setRequirementId(requirementId);
        analysis.setCoverageType("CODE");
        analysis.setTotalItems(totalLines);
        analysis.setCoveredItems(coveredLines);
        analysis.setCoverageRate(coverageRate);
        analysis.setCoverageDetails(toJson(coverageDetails));
        analysis.setUncoveredItems(toJson(uncoveredItems));
        analysis.setAnalysisTime(LocalDateTime.now());
        
        TestCoverageAnalysis saved = coverageRepository.save(analysis);
        log.info("代码覆盖分析完成: analysisCode={}, 行覆盖率={}%, 分支覆盖率={}%", 
                saved.getAnalysisCode(), lineCoverage, branchCoverage);
        
        return saved;
    }
    
    @Override
    public TestCoverageAnalysis getAnalysisById(Long id) {
        return coverageRepository.findById(id)
                .orElseThrow(() -> new BusinessException("覆盖分析不存在"));
    }
    
    @Override
    public TestCoverageAnalysis getAnalysisByCode(String analysisCode) {
        return coverageRepository.findByAnalysisCode(analysisCode)
                .orElseThrow(() -> new BusinessException("覆盖分析不存在: " + analysisCode));
    }
    
    @Override
    public Page<TestCoverageAnalysis> getAnalysisList(Pageable pageable) {
        return coverageRepository.findAll(pageable);
    }
    
    @Override
    public List<TestCoverageAnalysis> getAnalysisByRequirementId(Long requirementId) {
        return coverageRepository.findByRequirementId(requirementId);
    }
    
    @Override
    public List<TestCoverageAnalysis> getAnalysisByCoverageType(String coverageType) {
        return coverageRepository.findByCoverageType(coverageType);
    }
    
    @Override
    public String getCoverageTrend(Long requirementId, String coverageType, Integer days) {
        log.info("获取覆盖趋势: requirementId={}, coverageType={}, days={}", requirementId, coverageType, days);
        
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        // 查询指定时间范围内的分析记录
        List<TestCoverageAnalysis> analyses;
        if (requirementId != null && StringUtils.hasText(coverageType)) {
            analyses = coverageRepository.findByRequirementIdAndCoverageType(requirementId, coverageType);
        } else if (requirementId != null) {
            analyses = coverageRepository.findByRequirementId(requirementId);
        } else if (StringUtils.hasText(coverageType)) {
            analyses = coverageRepository.findByCoverageType(coverageType);
        } else {
            analyses = coverageRepository.findAll();
        }
        
        // 过滤时间范围并按时间排序
        List<Map<String, Object>> trendData = analyses.stream()
                .filter(a -> a.getAnalysisTime() != null && a.getAnalysisTime().isAfter(startTime))
                .sorted(Comparator.comparing(TestCoverageAnalysis::getAnalysisTime))
                .map(a -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", a.getAnalysisTime().toString());
                    point.put("coverageRate", a.getCoverageRate());
                    point.put("analysisCode", a.getAnalysisCode());
                    point.put("coverageType", a.getCoverageType());
                    return point;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("days", days);
        result.put("requirementId", requirementId);
        result.put("coverageType", coverageType);
        result.put("dataPoints", trendData);
        result.put("dataPointCount", trendData.size());
        
        return toJson(result);
    }
    
    @Override
    public String checkCoverageInsufficiency(Long requirementId, Double threshold) {
        log.info("检查覆盖不足: requirementId={}, threshold={}", requirementId, threshold);
        
        // 使用final变量，确保Lambda表达式可以访问
        final double finalThreshold;
        if (threshold == null || threshold <= 0) {
            finalThreshold = 80.0; // 默认阈值80%
        } else {
            finalThreshold = threshold;
        }
        
        // 查询覆盖分析记录
        List<TestCoverageAnalysis> analyses;
        if (requirementId != null) {
            analyses = coverageRepository.findByRequirementId(requirementId);
        } else {
            analyses = coverageRepository.findAll();
        }
        
        // 过滤覆盖率低于阈值的记录
        List<Map<String, Object>> insufficientItems = analyses.stream()
                .filter(a -> a.getCoverageRate() != null && 
                        a.getCoverageRate().doubleValue() < finalThreshold)
                .map(a -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("analysisCode", a.getAnalysisCode());
                    item.put("analysisName", a.getAnalysisName());
                    item.put("requirementId", a.getRequirementId());
                    item.put("coverageType", a.getCoverageType());
                    item.put("coverageRate", a.getCoverageRate());
                    item.put("threshold", finalThreshold);
                    item.put("gap", finalThreshold - a.getCoverageRate().doubleValue());
                    return item;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("threshold", finalThreshold);
        result.put("requirementId", requirementId);
        result.put("insufficientCount", insufficientItems.size());
        result.put("items", insufficientItems);
        
        return toJson(result);
    }
    
    @Override
    public String generateCoverageReport(Long requirementId, String coverageType) {
        log.info("生成覆盖报告: requirementId={}, coverageType={}", requirementId, coverageType);
        
        // 查询覆盖分析记录
        List<TestCoverageAnalysis> analyses;
        if (requirementId != null && StringUtils.hasText(coverageType)) {
            analyses = coverageRepository.findByRequirementIdAndCoverageType(requirementId, coverageType);
        } else if (requirementId != null) {
            analyses = coverageRepository.findByRequirementId(requirementId);
        } else {
            analyses = coverageRepository.findAll();
        }
        
        // 构建报告数据
        Map<String, Object> report = new HashMap<>();
        report.put("requirementId", requirementId);
        report.put("coverageType", coverageType);
        report.put("generateTime", LocalDateTime.now().toString());
        report.put("analysisCount", analyses.size());
        
        // 按类型汇总
        Map<String, Object> summary = new HashMap<>();
        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, BigDecimal> typeAvgCoverage = new HashMap<>();
        
        for (TestCoverageAnalysis analysis : analyses) {
            String type = analysis.getCoverageType();
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
            
            if (analysis.getCoverageRate() != null) {
                BigDecimal currentAvg = typeAvgCoverage.getOrDefault(type, BigDecimal.ZERO);
                int count = typeCount.get(type);
                // 累加计算平均值
                typeAvgCoverage.put(type, currentAvg.add(analysis.getCoverageRate()));
            }
        }
        
        // 计算平均值
        for (String type : typeAvgCoverage.keySet()) {
            int count = typeCount.get(type);
            BigDecimal total = typeAvgCoverage.get(type);
            typeAvgCoverage.put(type, total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        }
        
        summary.put("typeCount", typeCount);
        summary.put("typeAvgCoverage", typeAvgCoverage);
        report.put("summary", summary);
        
        // 详细信息
        List<Map<String, Object>> details = analyses.stream()
                .map(a -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("analysisCode", a.getAnalysisCode());
                    detail.put("analysisName", a.getAnalysisName());
                    detail.put("coverageType", a.getCoverageType());
                    detail.put("coverageRate", a.getCoverageRate());
                    detail.put("totalItems", a.getTotalItems());
                    detail.put("coveredItems", a.getCoveredItems());
                    detail.put("analysisTime", a.getAnalysisTime() != null ? a.getAnalysisTime().toString() : null);
                    return detail;
                })
                .collect(Collectors.toList());
        report.put("details", details);
        
        return toJson(report);
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 生成分析编码
     * 格式：COV-YYYYMMDD-序号（如 COV-20240117-001）
     */
    private String generateAnalysisCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = COVERAGE_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天已生成的分析编码
        List<TestCoverageAnalysis> todayAnalyses = coverageRepository
                .findByAnalysisCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestCoverageAnalysis a : todayAnalyses) {
            String code = a.getAnalysisCode();
            if (code != null && code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    log.warn("分析编码格式不正确: {}", code);
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        return prefix + String.format("%03d", newSequence);
    }
    
    /**
     * 验证分析数据
     */
    private void validateAnalysis(TestCoverageAnalysis analysis) {
        if (!StringUtils.hasText(analysis.getAnalysisName())) {
            throw new BusinessException("分析名称不能为空");
        }
        if (!StringUtils.hasText(analysis.getCoverageType())) {
            throw new BusinessException("覆盖类型不能为空");
        }
        if (!Arrays.asList("REQUIREMENT", "FUNCTION", "SCENARIO", "CODE").contains(analysis.getCoverageType())) {
            throw new BusinessException("覆盖类型必须是 REQUIREMENT/FUNCTION/SCENARIO/CODE 之一");
        }
    }
    
    /**
     * 从需求中提取功能点
     */
    private Set<String> extractFunctionsFromRequirement(TestRequirement requirement) {
        Set<String> functions = new HashSet<>();
        
        // 从需求描述中提取功能点（简化实现）
        // 实际应该使用NLP或正则表达式提取
        String description = requirement.getRequirementDescription();
        if (StringUtils.hasText(description)) {
            // 简单的关键词提取
            String[] keywords = {"功能", "模块", "需求", "业务"};
            for (String keyword : keywords) {
                if (description.contains(keyword)) {
                    functions.add(keyword);
                }
            }
        }
        
        return functions;
    }
    
    /**
     * 从用例中提取功能点
     */
    private Set<String> extractFunctionsFromTestCases(List<TestCase> testCases) {
        Set<String> functions = new HashSet<>();
        
        for (TestCase testCase : testCases) {
            // 从用例名称和步骤中提取功能关键词
            String caseName = testCase.getCaseName();
            String testStep = testCase.getTestStep();
            
            if (StringUtils.hasText(caseName)) {
                // 简单的关键词提取
                if (caseName.contains("功能") || caseName.contains("模块")) {
                    functions.add("功能");
                }
            }
        }
        
        return functions;
    }
    
    /**
     * 判断功能是否被用例覆盖
     */
    private boolean isFunctionCovered(String function, List<TestCase> testCases) {
        for (TestCase testCase : testCases) {
            String content = (testCase.getCaseName() != null ? testCase.getCaseName() : "") +
                    " " + (testCase.getTestStep() != null ? testCase.getTestStep() : "");
            if (content.toLowerCase().contains(function.toLowerCase())) {
                return true;
            }
        }
        return false;
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
    
    /**
     * 从Map中获取Double值
     */
    private Double getDoubleValue(Map<String, Object> map, String key, Double defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 从Map中获取Integer值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

