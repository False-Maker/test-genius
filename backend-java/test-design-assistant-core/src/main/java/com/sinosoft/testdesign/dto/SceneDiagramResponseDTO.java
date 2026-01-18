package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 场景图生成响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class SceneDiagramResponseDTO {
    
    /**
     * Mermaid代码（图表定义文本）
     */
    private String mermaidCode;
    
    /**
     * 图表标题
     */
    private String title;
    
    /**
     * 图表格式
     */
    private String format;
    
    /**
     * 文件URL（如果是导出文件格式）
     */
    private String fileUrl;
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 场景节点数量
     */
    private Integer nodeCount;
    
    /**
     * 场景边数量
     */
    private Integer edgeCount;
}

