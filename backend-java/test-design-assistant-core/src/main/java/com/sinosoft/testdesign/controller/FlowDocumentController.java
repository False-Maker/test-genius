package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.PathDiagramRequestDTO;
import com.sinosoft.testdesign.dto.PathDiagramResponseDTO;
import com.sinosoft.testdesign.dto.SceneDiagramRequestDTO;
import com.sinosoft.testdesign.dto.SceneDiagramResponseDTO;
import com.sinosoft.testdesign.service.FlowDocumentGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 流程文档生成控制器
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@Slf4j
@RestController
@RequestMapping("/v1/flow-documents")
@RequiredArgsConstructor
@Tag(name = "流程文档生成", description = "测试流程文档生成接口，包括场景图和路径图生成")
public class FlowDocumentController {
    
    private final FlowDocumentGenerationService flowDocumentGenerationService;
    
    /**
     * 生成场景图
     */
    @PostMapping("/scene-diagrams")
    @Operation(summary = "生成场景图", description = "根据需求或用例生成测试场景图，支持Mermaid格式")
    public Result<SceneDiagramResponseDTO> generateSceneDiagram(
            @Valid @RequestBody SceneDiagramRequestDTO request) {
        log.info("生成场景图请求：{}", request);
        SceneDiagramResponseDTO response = flowDocumentGenerationService.generateSceneDiagram(request);
        return Result.success(response);
    }
    
    /**
     * 生成路径图
     */
    @PostMapping("/path-diagrams")
    @Operation(summary = "生成路径图", description = "根据用例生成测试路径图，支持Mermaid格式")
    public Result<PathDiagramResponseDTO> generatePathDiagram(
            @Valid @RequestBody PathDiagramRequestDTO request) {
        log.info("生成路径图请求：{}", request);
        PathDiagramResponseDTO response = flowDocumentGenerationService.generatePathDiagram(request);
        return Result.success(response);
    }
    
    /**
     * 导出场景图文件
     */
    @PostMapping("/scene-diagrams/export")
    @Operation(summary = "导出场景图文件", description = "将Mermaid代码导出为PNG/SVG/PDF文件")
    public Result<String> exportSceneDiagramFile(
            @RequestParam String mermaidCode,
            @RequestParam String format,
            @RequestParam(required = false) String fileName) {
        log.info("导出场景图文件，格式：{}，文件名：{}", format, fileName);
        String fileUrl = flowDocumentGenerationService.exportSceneDiagramFile(
                mermaidCode, format, fileName != null ? fileName : "scene_diagram");
        return Result.success(fileUrl);
    }
    
    /**
     * 导出路径图文件
     */
    @PostMapping("/path-diagrams/export")
    @Operation(summary = "导出路径图文件", description = "将Mermaid代码导出为PNG/SVG/PDF文件")
    public Result<String> exportPathDiagramFile(
            @RequestParam String mermaidCode,
            @RequestParam String format,
            @RequestParam(required = false) String fileName) {
        log.info("导出路径图文件，格式：{}，文件名：{}", format, fileName);
        String fileUrl = flowDocumentGenerationService.exportPathDiagramFile(
                mermaidCode, format, fileName != null ? fileName : "path_diagram");
        return Result.success(fileUrl);
    }
}

