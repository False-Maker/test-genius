package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 路径图生成请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class PathDiagramRequestDTO {
    
    /**
     * 用例ID（生成基于单个用例的路径图）
     */
    private Long caseId;
    
    /**
     * 用例ID列表（生成基于多个用例的路径图）
     */
    private java.util.List<Long> caseIds;
    
    /**
     * 需求ID（生成需求下所有用例的路径图）
     */
    private Long requirementId;
    
    /**
     * 图表格式：MERMAID/SVG/PNG/PDF
     */
    @NotNull(message = "图表格式不能为空")
    private String format = "MERMAID";
    
    /**
     * 图表标题（可选，不提供则使用用例名称或默认标题）
     */
    private String title;
    
    /**
     * 图表方向：LR（从左到右）/TB（从上到下）/RL（从右到左）/BT（从下到上）
     */
    private String direction = "LR";
    
    /**
     * 是否合并相同步骤（将相同步骤合并为一个节点）
     */
    private Boolean mergeSameSteps = true;
}

