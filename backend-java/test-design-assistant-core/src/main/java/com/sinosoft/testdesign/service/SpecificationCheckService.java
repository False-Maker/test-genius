package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestSpecification;

import java.util.List;

/**
 * 规约检查服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface SpecificationCheckService {
    
    /**
     * 自动匹配适用的规约
     * 根据用例的模块、测试分层、测试方法自动匹配适用的规约
     * 
     * @param testCase 测试用例
     * @return 匹配的规约列表（按优先级排序）
     */
    List<TestSpecification> matchSpecifications(TestCase testCase);
    
    /**
     * 检查用例是否符合规约要求
     * 
     * @param testCase 测试用例
     * @param specifications 要检查的规约列表（如果为空，则自动匹配）
     * @return 符合性检查结果
     */
    SpecificationComplianceResult checkCompliance(TestCase testCase, List<TestSpecification> specifications);
    
    /**
     * 注入规约内容到用例中
     * 在用例生成时将规约内容注入到用例中
     * 
     * @param testCase 测试用例
     * @param specifications 要应用的规约列表
     * @return 注入后的用例内容
     */
    SpecificationInjectionResult injectSpecification(TestCase testCase, List<TestSpecification> specifications);
    
    /**
     * 根据ID列表查询规约
     * 
     * @param ids 规约ID列表
     * @return 规约列表
     */
    List<TestSpecification> getSpecificationsByIds(List<Long> ids);
    
    /**
     * 规约符合性检查结果
     */
    class SpecificationComplianceResult {
        private boolean isCompliant;
        private double complianceScore; // 符合度评分（0-100）
        private int totalChecks; // 总检查项数
        private int passedChecks; // 通过的检查项数
        private int failedChecks; // 未通过的检查项数
        private List<ComplianceIssue> issues; // 不符合项列表
        
        // Getters and Setters
        public boolean isCompliant() {
            return isCompliant;
        }
        
        public void setCompliant(boolean compliant) {
            isCompliant = compliant;
        }
        
        public double getComplianceScore() {
            return complianceScore;
        }
        
        public void setComplianceScore(double complianceScore) {
            this.complianceScore = complianceScore;
        }
        
        public int getTotalChecks() {
            return totalChecks;
        }
        
        public void setTotalChecks(int totalChecks) {
            this.totalChecks = totalChecks;
        }
        
        public int getPassedChecks() {
            return passedChecks;
        }
        
        public void setPassedChecks(int passedChecks) {
            this.passedChecks = passedChecks;
        }
        
        public int getFailedChecks() {
            return failedChecks;
        }
        
        public void setFailedChecks(int failedChecks) {
            this.failedChecks = failedChecks;
        }
        
        public List<ComplianceIssue> getIssues() {
            return issues;
        }
        
        public void setIssues(List<ComplianceIssue> issues) {
            this.issues = issues;
        }
    }
    
    /**
     * 符合性检查问题
     */
    class ComplianceIssue {
        private String specCode; // 规约编码
        private String specName; // 规约名称
        private String issueType; // 问题类型：FIELD_REQUIREMENT/LOGIC_REQUIREMENT/FORMAT_REQUIREMENT等
        private String issueDescription; // 问题描述
        private String severity; // 严重程度：HIGH/MEDIUM/LOW
        private String suggestion; // 改进建议
        
        // Getters and Setters
        public String getSpecCode() {
            return specCode;
        }
        
        public void setSpecCode(String specCode) {
            this.specCode = specCode;
        }
        
        public String getSpecName() {
            return specName;
        }
        
        public void setSpecName(String specName) {
            this.specName = specName;
        }
        
        public String getIssueType() {
            return issueType;
        }
        
        public void setIssueType(String issueType) {
            this.issueType = issueType;
        }
        
        public String getIssueDescription() {
            return issueDescription;
        }
        
        public void setIssueDescription(String issueDescription) {
            this.issueDescription = issueDescription;
        }
        
        public String getSeverity() {
            return severity;
        }
        
        public void setSeverity(String severity) {
            this.severity = severity;
        }
        
        public String getSuggestion() {
            return suggestion;
        }
        
        public void setSuggestion(String suggestion) {
            this.suggestion = suggestion;
        }
    }
    
    /**
     * 规约注入结果
     */
    class SpecificationInjectionResult {
        private TestCase enhancedTestCase; // 增强后的用例
        private List<String> injectedContents; // 注入的内容列表
        private List<String> appliedSpecs; // 应用的规约列表
        
        // Getters and Setters
        public TestCase getEnhancedTestCase() {
            return enhancedTestCase;
        }
        
        public void setEnhancedTestCase(TestCase enhancedTestCase) {
            this.enhancedTestCase = enhancedTestCase;
        }
        
        public List<String> getInjectedContents() {
            return injectedContents;
        }
        
        public void setInjectedContents(List<String> injectedContents) {
            this.injectedContents = injectedContents;
        }
        
        public List<String> getAppliedSpecs() {
            return appliedSpecs;
        }
        
        public void setAppliedSpecs(List<String> appliedSpecs) {
            this.appliedSpecs = appliedSpecs;
        }
    }
}

