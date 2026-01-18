package com.sinosoft.testdesign.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 测试报告响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestReportResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 报告编码
     */
    private String reportCode;
    
    /**
     * 报告名称
     */
    private String reportName;
    
    /**
     * 报告类型：EXECUTION/COVERAGE/QUALITY/RISK
     */
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
     * 报告内容（JSON格式）
     */
    private String reportContent;
    
    /**
     * 报告摘要
     */
    private String reportSummary;
    
    /**
     * 报告状态：DRAFT/PUBLISHED
     */
    private String reportStatus;
    
    /**
     * 生成配置（JSON格式）
     */
    private String generateConfig;
    
    /**
     * 报告文件URL（Word/PDF）
     */
    private String fileUrl;
    
    /**
     * 文件格式：WORD/PDF/EXCEL
     */
    private String fileFormat;
    
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
     * 发布时间
     */
    private LocalDateTime publishTime;
}

