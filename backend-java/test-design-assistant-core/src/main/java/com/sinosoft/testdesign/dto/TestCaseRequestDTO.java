package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 测试用例请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class TestCaseRequestDTO {
    
    /**
     * 用例名称
     */
    @NotBlank(message = "用例名称不能为空")
    @Size(max = 500, message = "用例名称长度不能超过500个字符")
    private String caseName;
    
    /**
     * 需求ID
     */
    private Long requirementId;
    
    /**
     * 测试分层ID
     */
    private Long layerId;
    
    /**
     * 测试方法ID
     */
    private Long methodId;
    
    /**
     * 用例类型：正常/异常/边界
     */
    @Size(max = 50, message = "用例类型长度不能超过50个字符")
    private String caseType;
    
    /**
     * 用例优先级：高/中/低
     */
    @Size(max = 50, message = "用例优先级长度不能超过50个字符")
    private String casePriority;
    
    /**
     * 前置条件
     */
    private String preCondition;
    
    /**
     * 测试步骤
     */
    private String testStep;
    
    /**
     * 预期结果
     */
    private String expectedResult;
    
    /**
     * 用例状态：草稿/待审核/已审核/已废弃
     */
    @Size(max = 50, message = "用例状态长度不能超过50个字符")
    private String caseStatus;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    @Size(max = 100, message = "创建人姓名长度不能超过100个字符")
    private String creatorName;
}

