package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 测试报告模板请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestReportTemplateRequestDTO {
    
    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 500, message = "模板名称长度不能超过500个字符")
    private String templateName;
    
    /**
     * 模板类型：EXECUTION/COVERAGE/QUALITY/RISK
     */
    @NotBlank(message = "模板类型不能为空")
    @Pattern(regexp = "EXECUTION|COVERAGE|QUALITY|RISK", message = "模板类型必须是 EXECUTION/COVERAGE/QUALITY/RISK 之一")
    private String templateType;
    
    /**
     * 模板内容（JSON格式）
     */
    @NotBlank(message = "模板内容不能为空")
    private String templateContent;
    
    /**
     * 模板变量定义（JSON格式）
     */
    private String templateVariables;
    
    /**
     * 文件格式：WORD/PDF/EXCEL
     */
    @Pattern(regexp = "WORD|PDF|EXCEL", message = "文件格式必须是 WORD/PDF/EXCEL 之一")
    private String fileFormat;
    
    /**
     * 模板描述
     */
    private String templateDescription;
    
    /**
     * 是否默认模板：1-是，0-否
     */
    @Pattern(regexp = "0|1", message = "是否默认模板必须是 0 或 1")
    private String isDefault;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    @Pattern(regexp = "0|1", message = "是否启用必须是 0 或 1")
    private String isActive;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
}

