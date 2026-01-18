package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 测试覆盖分析请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Data
public class TestCoverageAnalysisRequestDTO {
    
    /**
     * 分析名称
     */
    @NotBlank(message = "分析名称不能为空")
    @Size(max = 500, message = "分析名称长度不能超过500个字符")
    private String analysisName;
    
    /**
     * 关联需求ID
     */
    private Long requirementId;
    
    /**
     * 覆盖类型：REQUIREMENT/FUNCTION/SCENARIO/CODE
     */
    @NotBlank(message = "覆盖类型不能为空")
    @Pattern(regexp = "REQUIREMENT|FUNCTION|SCENARIO|CODE", 
             message = "覆盖类型必须是 REQUIREMENT/FUNCTION/SCENARIO/CODE 之一")
    private String coverageType;
    
    /**
     * 分析人ID
     */
    private Long analyzerId;
}

