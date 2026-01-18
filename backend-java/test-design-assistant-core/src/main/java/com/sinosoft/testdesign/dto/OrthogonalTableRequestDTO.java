package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 正交表生成请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@Data
public class OrthogonalTableRequestDTO {
    
    /**
     * 因素列表（测试参数及其取值）
     * 格式：{"因素名": ["取值1", "取值2", "取值3"]}
     * 例如：{"浏览器": ["Chrome", "Firefox", "Safari"], "操作系统": ["Windows", "Mac", "Linux"]}
     */
    @NotEmpty(message = "因素列表不能为空")
    private Map<String, List<String>> factors;
    
    /**
     * 表格标题
     */
    private String title;
    
    /**
     * 正交表类型：L4/L8/L9/L12/L16/L25（默认自动选择）
     * L4: 4行，适合2因素2水平
     * L8: 8行，适合2-7因素2水平或1因素2水平+1因素3水平
     * L9: 9行，适合2因素3水平
     * L12: 12行，适合最多11因素2水平
     * L16: 16行，适合最多15因素2水平
     * L25: 25行，适合最多2因素5水平
     */
    private String tableType;
    
    /**
     * 导出格式：EXCEL/PDF/JSON
     */
    @NotNull(message = "导出格式不能为空")
    private String format = "EXCEL";
    
    /**
     * 是否包含交互作用分析
     */
    private Boolean includeInteractionAnalysis = false;
}

