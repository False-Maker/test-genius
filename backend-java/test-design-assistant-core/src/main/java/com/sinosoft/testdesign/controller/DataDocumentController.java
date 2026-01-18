package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.EquivalenceTableRequestDTO;
import com.sinosoft.testdesign.dto.EquivalenceTableResponseDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableRequestDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableResponseDTO;
import com.sinosoft.testdesign.service.DataDocumentGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 数据文档生成控制器
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/data-documents")
@RequiredArgsConstructor
@Tag(name = "数据文档生成", description = "测试数据文档生成接口，包括等价类表和正交表生成")
public class DataDocumentController {
    
    private final DataDocumentGenerationService dataDocumentGenerationService;
    
    /**
     * 生成等价类表
     */
    @PostMapping("/equivalence-tables")
    @Operation(summary = "生成等价类表", description = "根据需求或用例生成等价类表，支持Excel和Word格式导出")
    public Result<EquivalenceTableResponseDTO> generateEquivalenceTable(
            @Valid @RequestBody EquivalenceTableRequestDTO request) {
        log.info("生成等价类表请求：{}", request);
        EquivalenceTableResponseDTO response = dataDocumentGenerationService.generateEquivalenceTable(request);
        return Result.success(response);
    }
    
    /**
     * 生成正交表
     */
    @PostMapping("/orthogonal-tables")
    @Operation(summary = "生成正交表", description = "根据因素和水平生成正交表，支持Excel和Word格式导出")
    public Result<OrthogonalTableResponseDTO> generateOrthogonalTable(
            @Valid @RequestBody OrthogonalTableRequestDTO request) {
        log.info("生成正交表请求：{}", request);
        OrthogonalTableResponseDTO response = dataDocumentGenerationService.generateOrthogonalTable(request);
        return Result.success(response);
    }
    
    /**
     * 导出等价类表到Excel
     */
    @PostMapping("/equivalence-tables/export/excel")
    @Operation(summary = "导出等价类表到Excel", description = "将等价类表导出为Excel文件")
    public void exportEquivalenceTableToExcel(
            @Valid @RequestBody EquivalenceTableResponseDTO response,
            HttpServletResponse httpResponse) throws IOException {
        log.info("导出等价类表到Excel");
        
        String fileName = response.getTitle() != null ? response.getTitle() : "等价类表";
        fileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
        
        httpResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        httpResponse.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        
        try (OutputStream outputStream = httpResponse.getOutputStream()) {
            dataDocumentGenerationService.exportEquivalenceTableToExcel(response, outputStream);
        }
    }
    
    /**
     * 导出等价类表到Word
     */
    @PostMapping("/equivalence-tables/export/word")
    @Operation(summary = "导出等价类表到Word", description = "将等价类表导出为Word文件")
    public void exportEquivalenceTableToWord(
            @Valid @RequestBody EquivalenceTableResponseDTO response,
            HttpServletResponse httpResponse) throws IOException {
        log.info("导出等价类表到Word");
        
        String fileName = response.getTitle() != null ? response.getTitle() : "等价类表";
        fileName = URLEncoder.encode(fileName + ".docx", StandardCharsets.UTF_8);
        
        httpResponse.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        httpResponse.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        
        try (OutputStream outputStream = httpResponse.getOutputStream()) {
            dataDocumentGenerationService.exportEquivalenceTableToWord(response, outputStream);
        }
    }
    
    /**
     * 导出正交表到Excel
     */
    @PostMapping("/orthogonal-tables/export/excel")
    @Operation(summary = "导出正交表到Excel", description = "将正交表导出为Excel文件")
    public void exportOrthogonalTableToExcel(
            @Valid @RequestBody OrthogonalTableResponseDTO response,
            HttpServletResponse httpResponse) throws IOException {
        log.info("导出正交表到Excel");
        
        String fileName = response.getTitle() != null ? response.getTitle() : "正交表";
        fileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8);
        
        httpResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        httpResponse.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        
        try (OutputStream outputStream = httpResponse.getOutputStream()) {
            dataDocumentGenerationService.exportOrthogonalTableToExcel(response, outputStream);
        }
    }
    
    /**
     * 导出正交表到Word
     */
    @PostMapping("/orthogonal-tables/export/word")
    @Operation(summary = "导出正交表到Word", description = "将正交表导出为Word文件")
    public void exportOrthogonalTableToWord(
            @Valid @RequestBody OrthogonalTableResponseDTO response,
            HttpServletResponse httpResponse) throws IOException {
        log.info("导出正交表到Word");
        
        String fileName = response.getTitle() != null ? response.getTitle() : "正交表";
        fileName = URLEncoder.encode(fileName + ".docx", StandardCharsets.UTF_8);
        
        httpResponse.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        httpResponse.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        
        try (OutputStream outputStream = httpResponse.getOutputStream()) {
            dataDocumentGenerationService.exportOrthogonalTableToWord(response, outputStream);
        }
    }
}

