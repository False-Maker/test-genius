package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.dto.PathDiagramRequestDTO;
import com.sinosoft.testdesign.dto.PathDiagramResponseDTO;
import com.sinosoft.testdesign.dto.SceneDiagramRequestDTO;
import com.sinosoft.testdesign.dto.SceneDiagramResponseDTO;

/**
 * 流程文档生成服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface FlowDocumentGenerationService {
    
    /**
     * 生成场景图
     * @param request 场景图生成请求
     * @return 场景图生成响应
     */
    SceneDiagramResponseDTO generateSceneDiagram(SceneDiagramRequestDTO request);
    
    /**
     * 生成路径图
     * @param request 路径图生成请求
     * @return 路径图生成响应
     */
    PathDiagramResponseDTO generatePathDiagram(PathDiagramRequestDTO request);
    
    /**
     * 导出场景图文件
     * @param mermaidCode Mermaid代码
     * @param format 文件格式（PNG/SVG/PDF）
     * @param fileName 文件名称
     * @return 文件URL
     */
    String exportSceneDiagramFile(String mermaidCode, String format, String fileName);
    
    /**
     * 导出路径图文件
     * @param mermaidCode Mermaid代码
     * @param format 文件格式（PNG/SVG/PDF）
     * @param fileName 文件名称
     * @return 文件URL
     */
    String exportPathDiagramFile(String mermaidCode, String format, String fileName);
}

