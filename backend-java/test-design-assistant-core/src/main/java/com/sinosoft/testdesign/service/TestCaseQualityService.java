package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestCase;

/**
 * 用例质量评估服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface TestCaseQualityService {
    
    /**
     * 评估用例质量
     * @param testCase 测试用例
     * @return 质量评分（0-100）
     */
    QualityScore assessQuality(TestCase testCase);
    
    /**
     * 检查用例完整性
     * @param testCase 测试用例
     * @return 完整性评分（0-100）
     */
    CompletenessScore checkCompleteness(TestCase testCase);
    
    /**
     * 检查用例规范性
     * @param testCase 测试用例
     * @return 规范性评分（0-100）
     */
    StandardizationScore checkStandardization(TestCase testCase);
    
    /**
     * 质量评分结果
     */
    class QualityScore {
        private double totalScore; // 总分（0-100）
        private double completenessScore; // 完整性评分
        private double standardizationScore; // 规范性评分
        private double executabilityScore; // 可执行性评分
        private String qualityLevel; // 质量等级：优秀/良好/一般/需改进
        
        // Getters and Setters
        public double getTotalScore() {
            return totalScore;
        }
        
        public void setTotalScore(double totalScore) {
            this.totalScore = totalScore;
        }
        
        public double getCompletenessScore() {
            return completenessScore;
        }
        
        public void setCompletenessScore(double completenessScore) {
            this.completenessScore = completenessScore;
        }
        
        public double getStandardizationScore() {
            return standardizationScore;
        }
        
        public void setStandardizationScore(double standardizationScore) {
            this.standardizationScore = standardizationScore;
        }
        
        public double getExecutabilityScore() {
            return executabilityScore;
        }
        
        public void setExecutabilityScore(double executabilityScore) {
            this.executabilityScore = executabilityScore;
        }
        
        public String getQualityLevel() {
            return qualityLevel;
        }
        
        public void setQualityLevel(String qualityLevel) {
            this.qualityLevel = qualityLevel;
        }
    }
    
    /**
     * 完整性评分结果
     */
    class CompletenessScore {
        private double totalScore; // 总分（0-100）
        private double preConditionScore; // 前置条件完整性
        private double testStepScore; // 测试步骤完整性
        private double expectedResultScore; // 预期结果完整性
        private double basicInfoScore; // 基本信息完整性
        
        // Getters and Setters
        public double getTotalScore() {
            return totalScore;
        }
        
        public void setTotalScore(double totalScore) {
            this.totalScore = totalScore;
        }
        
        public double getPreConditionScore() {
            return preConditionScore;
        }
        
        public void setPreConditionScore(double preConditionScore) {
            this.preConditionScore = preConditionScore;
        }
        
        public double getTestStepScore() {
            return testStepScore;
        }
        
        public void setTestStepScore(double testStepScore) {
            this.testStepScore = testStepScore;
        }
        
        public double getExpectedResultScore() {
            return expectedResultScore;
        }
        
        public void setExpectedResultScore(double expectedResultScore) {
            this.expectedResultScore = expectedResultScore;
        }
        
        public double getBasicInfoScore() {
            return basicInfoScore;
        }
        
        public void setBasicInfoScore(double basicInfoScore) {
            this.basicInfoScore = basicInfoScore;
        }
    }
    
    /**
     * 规范性评分结果
     */
    class StandardizationScore {
        private double totalScore; // 总分（0-100）
        private double namingScore; // 命名规范性
        private double formatScore; // 格式规范性
        private double contentScore; // 内容规范性
        
        // Getters and Setters
        public double getTotalScore() {
            return totalScore;
        }
        
        public void setTotalScore(double totalScore) {
            this.totalScore = totalScore;
        }
        
        public double getNamingScore() {
            return namingScore;
        }
        
        public void setNamingScore(double namingScore) {
            this.namingScore = namingScore;
        }
        
        public double getFormatScore() {
            return formatScore;
        }
        
        public void setFormatScore(double formatScore) {
            this.formatScore = formatScore;
        }
        
        public double getContentScore() {
            return contentScore;
        }
        
        public void setContentScore(double contentScore) {
            this.contentScore = contentScore;
        }
    }
}

