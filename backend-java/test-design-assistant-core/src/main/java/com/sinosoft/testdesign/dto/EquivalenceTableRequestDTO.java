package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 等价类表生成请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@Data
public class EquivalenceTableRequestDTO {
    
    /**
     * 需求ID（可选，基于需求生成等价类表）
     */
    private Long requirementId;
    
    /**
     * 用例ID列表（可选，基于用例生成等价类表）
     */
    private List<Long> caseIds;
    
    /**
     * 输入参数定义（直接指定参数和等价类）
     * 格式：{"参数名": {"有效等价类": ["值1", "值2"], "无效等价类": ["值3", "值4"]}}
     */
    private Map<String, Map<String, List<String>>> inputParameters;
    
    /**
     * 参数名称列表（用于从用例中提取参数）
     */
    private List<String> parameterNames;
    
    /**
     * 表格标题
     */
    private String title;
    
    /**
     * 导出格式：EXCEL/PDF/JSON
     */
    @NotNull(message = "导出格式不能为空")
    private String format = "EXCEL";
    
    /**
     * 是否包含边界值分析
     */
    private Boolean includeBoundaryValues = false;
    
    /**
     * 是否自动识别参数（从用例中提取参数名）
     */
    private Boolean autoIdentifyParameters = true;
}

