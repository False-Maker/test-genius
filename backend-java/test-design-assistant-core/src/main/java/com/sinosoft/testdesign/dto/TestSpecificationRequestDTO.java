package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 测试规约请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class TestSpecificationRequestDTO {
    
    /**
     * 规约名称
     */
    @NotBlank(message = "规约名称不能为空")
    @Size(max = 500, message = "规约名称长度不能超过500个字符")
    private String specName;
    
    /**
     * 规约类型：APPLICATION/PUBLIC（应用级/公共）
     */
    @NotBlank(message = "规约类型不能为空")
    @Size(max = 50, message = "规约类型长度不能超过50个字符")
    private String specType;
    
    /**
     * 规约分类
     */
    @Size(max = 100, message = "规约分类长度不能超过100个字符")
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
    @Size(max = 500, message = "适用模块长度不能超过500个字符")
    private String applicableModules;
    
    /**
     * 适用测试分层（多个分层用逗号分隔）
     */
    @Size(max = 500, message = "适用测试分层长度不能超过500个字符")
    private String applicableLayers;
    
    /**
     * 适用测试方法（多个方法用逗号分隔）
     */
    @Size(max = 500, message = "适用测试方法长度不能超过500个字符")
    private String applicableMethods;
    
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
    @Size(max = 100, message = "创建人姓名长度不能超过100个字符")
    private String creatorName;
}

