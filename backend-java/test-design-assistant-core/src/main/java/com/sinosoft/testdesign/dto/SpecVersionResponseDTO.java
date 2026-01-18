package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规约版本响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class SpecVersionResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 规约ID
     */
    private Long specId;
    
    /**
     * 版本号
     */
    private String versionNumber;
    
    /**
     * 版本名称
     */
    private String versionName;
    
    /**
     * 版本描述
     */
    private String versionDescription;
    
    /**
     * 规约内容（JSON格式）
     */
    private String specContent;
    
    /**
     * 变更日志
     */
    private String changeLog;
    
    /**
     * 是否当前版本：1-是，0-否
     */
    private String isCurrent;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 创建人姓名
     */
    private String createdByName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

