package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 规约检查请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class SpecificationCheckRequestDTO {
    
    /**
     * 用例ID
     */
    @NotNull(message = "用例ID不能为空")
    private Long caseId;
    
    /**
     * 指定要检查的规约ID列表（可选，如果不指定则自动匹配）
     */
    private List<Long> specificationIds;
}

