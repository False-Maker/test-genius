package com.sinosoft.testdesign.common;

import com.sinosoft.testdesign.entity.*;
import com.sinosoft.testdesign.enums.CaseStatus;
import com.sinosoft.testdesign.enums.RequirementStatus;

import java.time.LocalDateTime;

/**
 * 测试数据构建器
 * 使用Builder模式构建测试数据
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
public class TestDataBuilder {
    
    /**
     * 构建测试需求
     */
    public static class RequirementBuilder {
        private TestRequirement requirement = new TestRequirement();
        
        public RequirementBuilder withId(Long id) {
            requirement.setId(id);
            return this;
        }
        
        public RequirementBuilder withCode(String code) {
            requirement.setRequirementCode(code);
            return this;
        }
        
        public RequirementBuilder withName(String name) {
            requirement.setRequirementName(name);
            return this;
        }
        
        public RequirementBuilder withType(String type) {
            requirement.setRequirementType(type);
            return this;
        }
        
        public RequirementBuilder withStatus(RequirementStatus status) {
            requirement.setRequirementStatus(status.name());
            return this;
        }
        
        public RequirementBuilder withDescription(String description) {
            requirement.setRequirementDescription(description);
            return this;
        }
        
        public RequirementBuilder withModule(String module) {
            requirement.setBusinessModule(module);
            return this;
        }
        
        public TestRequirement build() {
            // 设置默认值
            if (requirement.getRequirementCode() == null) {
                requirement.setRequirementCode("REQ-20240101-001");
            }
            if (requirement.getRequirementName() == null) {
                requirement.setRequirementName("测试需求");
            }
            if (requirement.getRequirementType() == null) {
                requirement.setRequirementType("新功能");
            }
            if (requirement.getRequirementStatus() == null) {
                requirement.setRequirementStatus(RequirementStatus.DRAFT.name());
            }
            if (requirement.getVersion() == null) {
                requirement.setVersion(1);
            }
            return requirement;
        }
    }
    
    /**
     * 构建测试用例
     */
    public static class TestCaseBuilder {
        private TestCase testCase = new TestCase();
        
        public TestCaseBuilder withId(Long id) {
            testCase.setId(id);
            return this;
        }
        
        public TestCaseBuilder withCode(String code) {
            testCase.setCaseCode(code);
            return this;
        }
        
        public TestCaseBuilder withName(String name) {
            testCase.setCaseName(name);
            return this;
        }
        
        public TestCaseBuilder withStatus(CaseStatus status) {
            testCase.setCaseStatus(status.name());
            return this;
        }
        
        public TestCaseBuilder withRequirementId(Long requirementId) {
            TestRequirement requirement = new TestRequirement();
            requirement.setId(requirementId);
            testCase.setRequirement(requirement);
            return this;
        }
        
        public TestCaseBuilder withSteps(String steps) {
            testCase.setTestSteps(steps);
            return this;
        }
        
        public TestCaseBuilder withExpectedResult(String result) {
            testCase.setExpectedResult(result);
            return this;
        }
        
        public TestCase build() {
            // 设置默认值
            if (testCase.getCaseCode() == null) {
                testCase.setCaseCode("CASE-20240101-001");
            }
            if (testCase.getCaseName() == null) {
                testCase.setCaseName("测试用例");
            }
            if (testCase.getCaseStatus() == null) {
                testCase.setCaseStatus(CaseStatus.DRAFT.name());
            }
            if (testCase.getVersion() == null) {
                testCase.setVersion(1);
            }
            return testCase;
        }
    }
    
    /**
     * 构建提示词模板
     */
    public static class PromptTemplateBuilder {
        private PromptTemplate template = new PromptTemplate();
        
        public PromptTemplateBuilder withId(Long id) {
            template.setId(id);
            return this;
        }
        
        public PromptTemplateBuilder withCode(String code) {
            template.setTemplateCode(code);
            return this;
        }
        
        public PromptTemplateBuilder withName(String name) {
            template.setTemplateName(name);
            return this;
        }
        
        public PromptTemplateBuilder withContent(String content) {
            template.setTemplateContent(content);
            return this;
        }
        
        public PromptTemplateBuilder withVariables(String variables) {
            template.setTemplateVariables(variables);
            return this;
        }
        
        public PromptTemplateBuilder withActive(String isActive) {
            template.setIsActive(isActive);
            return this;
        }
        
        public PromptTemplate build() {
            // 设置默认值
            if (template.getTemplateCode() == null) {
                template.setTemplateCode("TMP-20240101-001");
            }
            if (template.getTemplateName() == null) {
                template.setTemplateName("测试模板");
            }
            if (template.getTemplateContent() == null) {
                template.setTemplateContent("这是一个测试模板，包含变量{var1}和{var2}");
            }
            if (template.getIsActive() == null) {
                template.setIsActive("1");
            }
            if (template.getVersion() == null) {
                template.setVersion(1);
            }
            return template;
        }
    }
    
    /**
     * 创建需求构建器
     */
    public static RequirementBuilder requirement() {
        return new RequirementBuilder();
    }
    
    /**
     * 创建用例构建器
     */
    public static TestCaseBuilder testCase() {
        return new TestCaseBuilder();
    }
    
    /**
     * 创建模板构建器
     */
    public static PromptTemplateBuilder promptTemplate() {
        return new PromptTemplateBuilder();
    }
}

