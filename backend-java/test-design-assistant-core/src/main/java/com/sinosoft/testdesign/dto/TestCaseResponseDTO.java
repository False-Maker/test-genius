package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试用例响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class TestCaseResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用例编码
     */
    private String caseCode;
    
    /**
     * 用例名称
     */
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
    private String caseType;
    
    /**
     * 用例优先级：高/中/低
     */
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
    private String caseStatus;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    private String creatorName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 版本号
     */
    private Integer version;
}

