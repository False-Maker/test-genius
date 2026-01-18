package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 测试规约响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class TestSpecificationResponseDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 规约编码
     */
    private String specCode;
    
    /**
     * 规约名称
     */
    private String specName;
    
    /**
     * 规约类型：APPLICATION/PUBLIC（应用级/公共）
     */
    private String specType;
    
    /**
     * 规约分类
     */
    private String specCategory;
    
    /**
     * 规约描述
     */
    private String specDescription;
    
    /**
     * 规约内容（JSON格式）
     */
    private String specContent;
    
    /**
     * 适用模块（多个模块用逗号分隔）
     */
    private String applicableModules;
    
    /**
     * 适用测试分层（多个分层用逗号分隔）
     */
    private String applicableLayers;
    
    /**
     * 适用测试方法（多个方法用逗号分隔）
     */
    private String applicableMethods;
    
    /**
     * 当前版本号
     */
    private String currentVersion;
    
    /**
     * 是否启用：1-启用，0-禁用
     */
    private String isActive;
    
    /**
     * 生效日期
     */
    private LocalDate effectiveDate;
    
    /**
     * 失效日期
     */
    private LocalDate expireDate;
    
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

