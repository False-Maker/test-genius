package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.service.TestCaseQualityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用例质量评估服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
public class TestCaseQualityServiceImpl implements TestCaseQualityService {
    
    @Override
    public QualityScore assessQuality(TestCase testCase) {
        QualityScore score = new QualityScore();
        
        // 1. 完整性评分（30%）
        CompletenessScore completenessScore = checkCompleteness(testCase);
        score.setCompletenessScore(completenessScore.getTotalScore());
        
        // 2. 规范性评分（10%）
        StandardizationScore standardizationScore = checkStandardization(testCase);
        score.setStandardizationScore(standardizationScore.getTotalScore());
        
        // 3. 可执行性评分（20%）
        double executabilityScore = assessExecutability(testCase);
        score.setExecutabilityScore(executabilityScore);
        
        // 综合评分：完整性30% + 规范性10% + 可执行性20% = 60%（剩余40%需要人工评估）
        score.setTotalScore(
            completenessScore.getTotalScore() * 0.3 +
            standardizationScore.getTotalScore() * 0.1 +
            executabilityScore * 0.2
        );
        
        // 确定质量等级
        score.setQualityLevel(determineQualityLevel(score.getTotalScore()));
        
        return score;
    }
    
    @Override
    public CompletenessScore checkCompleteness(TestCase testCase) {
        CompletenessScore score = new CompletenessScore();
        
        // 1. 前置条件完整性（20%）
        double preConditionScore = assessPreCondition(testCase.getPreCondition());
        score.setPreConditionScore(preConditionScore);
        
        // 2. 测试步骤完整性（40%）
        double testStepScore = assessTestStep(testCase.getTestStep());
        score.setTestStepScore(testStepScore);
        
        // 3. 预期结果完整性（30%）
        double expectedResultScore = assessExpectedResult(testCase.getExpectedResult());
        score.setExpectedResultScore(expectedResultScore);
        
        // 4. 基本信息完整性（10%）
        double basicInfoScore = assessBasicInfo(testCase);
        score.setBasicInfoScore(basicInfoScore);
        
        // 综合完整性评分
        score.setTotalScore(
            preConditionScore * 0.2 +
            testStepScore * 0.4 +
            expectedResultScore * 0.3 +
            basicInfoScore * 0.1
        );
        
        return score;
    }
    
    @Override
    public StandardizationScore checkStandardization(TestCase testCase) {
        StandardizationScore score = new StandardizationScore();
        
        // 1. 命名规范性（30%）
        double namingScore = assessNaming(testCase);
        score.setNamingScore(namingScore);
        
        // 2. 格式规范性（40%）
        double formatScore = assessFormat(testCase);
        score.setFormatScore(formatScore);
        
        // 3. 内容规范性（30%）
        double contentScore = assessContent(testCase);
        score.setContentScore(contentScore);
        
        // 综合规范性评分
        score.setTotalScore(
            namingScore * 0.3 +
            formatScore * 0.4 +
            contentScore * 0.3
        );
        
        return score;
    }
    
    /**
     * 评估前置条件完整性
     */
    private double assessPreCondition(String preCondition) {
        if (!StringUtils.hasText(preCondition)) {
            return 50.0; // 前置条件可选，但如果有会更好
        }
        
        // 检查前置条件长度和内容
        if (preCondition.trim().length() < 5) {
            return 60.0; // 内容过短
        }
        
        if (preCondition.trim().length() > 500) {
            return 80.0; // 内容过长，但基本完整
        }
        
        return 100.0; // 前置条件完整
    }
    
    /**
     * 评估测试步骤完整性
     */
    private double assessTestStep(String testStep) {
        if (!StringUtils.hasText(testStep)) {
            return 0.0; // 测试步骤必须
        }
        
        // 检查测试步骤长度
        int length = testStep.trim().length();
        if (length < 10) {
            return 30.0; // 内容过短
        }
        
        if (length < 20) {
            return 60.0; // 内容较短
        }
        
        // 检查是否包含步骤编号（1. 2. 或 步骤1 步骤2）
        boolean hasStepNumbers = testStep.matches(".*[\\d一二三四五六七八九十][\\.、].*") ||
                                 testStep.matches(".*步骤[\\d一二三四五六七八九十].*");
        
        if (hasStepNumbers) {
            return 100.0; // 有步骤编号，结构清晰
        }
        
        return 85.0; // 内容完整但缺少步骤编号
    }
    
    /**
     * 评估预期结果完整性
     */
    private double assessExpectedResult(String expectedResult) {
        if (!StringUtils.hasText(expectedResult)) {
            return 0.0; // 预期结果必须
        }
        
        // 检查预期结果长度
        int length = expectedResult.trim().length();
        if (length < 5) {
            return 40.0; // 内容过短
        }
        
        if (length < 10) {
            return 70.0; // 内容较短
        }
        
        return 100.0; // 预期结果完整
    }
    
    /**
     * 评估基本信息完整性
     */
    private double assessBasicInfo(TestCase testCase) {
        double score = 0.0;
        
        // 用例名称（必须）
        if (StringUtils.hasText(testCase.getCaseName())) {
            score += 30.0;
        }
        
        // 用例类型（可选）
        if (StringUtils.hasText(testCase.getCaseType())) {
            score += 20.0;
        }
        
        // 用例优先级（可选）
        if (StringUtils.hasText(testCase.getCasePriority())) {
            score += 20.0;
        }
        
        // 关联需求（可选）
        if (testCase.getRequirementId() != null) {
            score += 15.0;
        }
        
        // 测试分层和方法（可选）
        if (testCase.getLayerId() != null && testCase.getMethodId() != null) {
            score += 15.0;
        }
        
        return score;
    }
    
    /**
     * 评估命名规范性
     */
    private double assessNaming(TestCase testCase) {
        if (!StringUtils.hasText(testCase.getCaseName())) {
            return 0.0;
        }
        
        String caseName = testCase.getCaseName().trim();
        
        // 检查用例名称长度（5-200字符）
        if (caseName.length() < 5) {
            return 50.0; // 名称过短
        }
        
        if (caseName.length() > 200) {
            return 70.0; // 名称过长
        }
        
        // 检查命名格式（建议包含模块名、功能点等）
        // 这里使用简单的启发式规则
        boolean hasModule = caseName.contains("_") || caseName.contains("-") || 
                           caseName.matches(".*[模块|功能|测试].*");
        
        if (hasModule) {
            return 100.0; // 命名规范
        }
        
        return 80.0; // 命名基本规范
    }
    
    /**
     * 评估格式规范性
     */
    private double assessFormat(TestCase testCase) {
        double score = 0.0;
        
        // 检查测试步骤格式
        if (StringUtils.hasText(testCase.getTestStep())) {
            String testStep = testCase.getTestStep();
            // 检查是否包含换行或步骤分隔符
            if (testStep.contains("\n") || testStep.matches(".*[\\d一二三四五六七八九十][\\.、].*")) {
                score += 50.0; // 格式规范
            } else {
                score += 30.0; // 格式基本规范
            }
        }
        
        // 检查预期结果格式
        if (StringUtils.hasText(testCase.getExpectedResult())) {
            String expectedResult = testCase.getExpectedResult();
            // 检查是否包含换行或列表格式
            if (expectedResult.contains("\n") || expectedResult.matches(".*[\\d一二三四五六七八九十][\\.、].*")) {
                score += 50.0; // 格式规范
            } else {
                score += 30.0; // 格式基本规范
            }
        }
        
        return score;
    }
    
    /**
     * 评估内容规范性
     */
    private double assessContent(TestCase testCase) {
        double score = 100.0;
        
        // 检查是否包含明显的错误字符或格式问题
        String testStep = testCase.getTestStep();
        if (StringUtils.hasText(testStep)) {
            // 检查是否包含过多的特殊字符
            long specialCharCount = testStep.chars()
                    .filter(c -> !Character.isLetterOrDigit(c) && 
                                !Character.isWhitespace(c) && 
                                c < 128) // 排除中文等
                    .count();
            
            if (specialCharCount > testStep.length() * 0.3) {
                score -= 20.0; // 特殊字符过多
            }
        }
        
        return Math.max(0.0, score);
    }
    
    /**
     * 评估可执行性
     */
    private double assessExecutability(TestCase testCase) {
        double score = 0.0;
        
        // 测试步骤清晰度（40%）
        if (StringUtils.hasText(testCase.getTestStep())) {
            String testStep = testCase.getTestStep();
            // 检查步骤是否清晰（包含动作动词）
            boolean hasActionVerbs = testStep.matches(".*[输入|点击|选择|填写|提交|验证|检查|确认].*");
            if (hasActionVerbs) {
                score += 40.0;
            } else {
                score += 20.0;
            }
        }
        
        // 数据准备难度（30%）
        if (StringUtils.hasText(testCase.getPreCondition())) {
            score += 30.0; // 有前置条件，数据准备更清晰
        } else {
            score += 15.0; // 无前置条件，数据准备不明确
        }
        
        // 环境依赖（30%）
        // 这里简化处理，实际可以根据用例内容判断
        score += 30.0; // 假设环境依赖明确
        
        return score;
    }
    
    /**
     * 确定质量等级
     */
    private String determineQualityLevel(double totalScore) {
        if (totalScore >= 90.0) {
            return "优秀";
        } else if (totalScore >= 75.0) {
            return "良好";
        } else if (totalScore >= 60.0) {
            return "一般";
        } else {
            return "需改进";
        }
    }
}

