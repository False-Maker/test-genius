package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 测试报告请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestReportRequestDTO {
    
    /**
     * 报告名称
     */
    @NotBlank(message = "报告名称不能为空")
    @Size(max = 500, message = "报告名称长度不能超过500个字符")
    private String reportName;
    
    /**
     * 报告类型：EXECUTION/COVERAGE/QUALITY/RISK
     */
    @NotBlank(message = "报告类型不能为空")
    @Pattern(regexp = "EXECUTION|COVERAGE|QUALITY|RISK", message = "报告类型必须是 EXECUTION/COVERAGE/QUALITY/RISK 之一")
    private String reportType;
    
    /**
     * 报告模板ID
     */
    private Long templateId;
    
    /**
     * 关联需求ID
     */
    private Long requirementId;
    
    /**
     * 关联执行任务ID
     */
    private Long executionTaskId;
    
    /**
     * 生成配置（JSON格式）
     */
    private String generateConfig;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    private String creatorName;
}

