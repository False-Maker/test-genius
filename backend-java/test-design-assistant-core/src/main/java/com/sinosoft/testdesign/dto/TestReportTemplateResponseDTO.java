package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试报告模板响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestReportTemplateResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 模板编码
     */
    private String templateCode;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 模板类型：EXECUTION/COVERAGE/QUALITY/RISK
     */
    private String templateType;
    
    /**
     * 模板内容（JSON格式）
     */
    private String templateContent;
    
    /**
     * 模板变量定义（JSON格式）
     */
    private String templateVariables;
    
    /**
     * 文件格式：WORD/PDF/EXCEL
     */
    private String fileFormat;
    
    /**
     * 模板描述
     */
    private String templateDescription;
    
    /**
     * 是否默认模板：1-是，0-否
     */
    private String isDefault;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    private String isActive;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

