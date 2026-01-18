package com.sinosoft.testdesign.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 场景图生成请求DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class SceneDiagramRequestDTO {
    
    /**
     * 需求ID（生成基于需求的场景图）
     */
    private Long requirementId;
    
    /**
     * 用例ID列表（生成基于用例的场景图）
     */
    private java.util.List<Long> caseIds;
    
    /**
     * 图表格式：MERMAID/SVG/PNG/PDF
     */
    @NotNull(message = "图表格式不能为空")
    private String format = "MERMAID";
    
    /**
     * 图表标题（可选，不提供则使用需求名称或默认标题）
     */
    private String title;
    
    /**
     * 图表方向：LR（从左到右）/TB（从上到下）/RL（从右到左）/BT（从下到上）
     */
    private String direction = "LR";
    
    /**
     * 是否包含用例详情（场景节点是否显示用例信息）
     */
    private Boolean includeCaseDetails = false;
}

