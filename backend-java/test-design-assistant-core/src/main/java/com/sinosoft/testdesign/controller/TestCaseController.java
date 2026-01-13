package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.TestCaseRequestDTO;
import com.sinosoft.testdesign.dto.TestCaseResponseDTO;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.TestCaseImportExportService;
import com.sinosoft.testdesign.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 测试用例管理控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Tag(name = "测试用例管理", description = "测试用例管理相关接口")
@RestController
@RequestMapping("/v1/test-cases")
@RequiredArgsConstructor
public class TestCaseController {
    
    private final TestCaseService testCaseService;
    private final TestCaseImportExportService importExportService;
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "创建用例", description = "创建新的测试用例")
    @PostMapping
    public Result<TestCaseResponseDTO> createTestCase(@Valid @RequestBody TestCaseRequestDTO dto) {
        TestCase testCase = entityDTOMapper.toTestCaseEntity(dto);
        TestCase saved = testCaseService.createTestCase(testCase);
        return Result.success(entityDTOMapper.toTestCaseResponseDTO(saved));
    }
    
    @Operation(summary = "查询用例列表", description = "分页查询用例列表，支持按用例名称和状态搜索")
    @GetMapping
    public Result<Page<TestCaseResponseDTO>> getTestCaseList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String caseName,
            @RequestParam(required = false) String caseStatus,
            @RequestParam(required = false) Long requirementId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestCase> testCasePage = testCaseService.getTestCaseList(pageable, caseName, caseStatus, requirementId);
        
        // 转换为DTO分页
        Page<TestCaseResponseDTO> dtoPage = testCasePage.map(entityDTOMapper::toTestCaseResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取用例详情", description = "根据ID获取用例详情")
    @GetMapping("/{id}")
    public Result<TestCaseResponseDTO> getTestCaseById(@PathVariable Long id) {
        TestCase testCase = testCaseService.getTestCaseById(id);
        return Result.success(entityDTOMapper.toTestCaseResponseDTO(testCase));
    }
    
    @Operation(summary = "更新用例", description = "更新用例信息")
    @PutMapping("/{id}")
    public Result<TestCaseResponseDTO> updateTestCase(
            @PathVariable Long id,
            @Valid @RequestBody TestCaseRequestDTO dto) {
        TestCase testCase = testCaseService.getTestCaseById(id);
        entityDTOMapper.updateTestCaseFromDTO(dto, testCase);
        TestCase updated = testCaseService.updateTestCase(id, testCase);
        return Result.success(entityDTOMapper.toTestCaseResponseDTO(updated));
    }
    
    @Operation(summary = "删除用例", description = "删除指定用例")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTestCase(@PathVariable Long id) {
        testCaseService.deleteTestCase(id);
        return Result.success();
    }
    
    @Operation(summary = "更新用例状态", description = "更新用例状态（状态流转）")
    @PutMapping("/{id}/status")
    public Result<TestCaseResponseDTO> updateCaseStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        TestCase updated = testCaseService.updateCaseStatus(id, status);
        return Result.success(entityDTOMapper.toTestCaseResponseDTO(updated));
    }
    
    @Operation(summary = "审核用例", description = "审核测试用例")
    @PostMapping("/{id}/review")
    public Result<TestCaseResponseDTO> reviewTestCase(
            @PathVariable Long id,
            @RequestParam String reviewResult,
            @RequestParam(required = false) String reviewComment) {
        TestCase updated = testCaseService.reviewTestCase(id, reviewResult, reviewComment);
        return Result.success(entityDTOMapper.toTestCaseResponseDTO(updated));
    }
    
    @Operation(summary = "导出用例", description = "导出用例列表到Excel")
    @GetMapping("/export")
    public void exportTestCases(
            @RequestParam(required = false) String caseName,
            @RequestParam(required = false) String caseStatus,
            @RequestParam(required = false) Long requirementId,
            HttpServletResponse response) throws IOException {
        // 查询所有符合条件的用例（不分页）
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<TestCase> page = testCaseService.getTestCaseList(pageable, caseName, caseStatus, requirementId);
        List<TestCase> testCases = page.getContent();
        
        // 设置响应头
        String fileName = "测试用例_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        
        // 导出到Excel
        importExportService.exportToExcel(testCases, response.getOutputStream());
    }
    
    @Operation(summary = "导出用例模板", description = "导出用例导入模板Excel")
    @GetMapping("/export-template")
    public void exportTemplate(HttpServletResponse response) throws IOException {
        // 设置响应头
        String fileName = "测试用例导入模板.xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        
        // 导出模板
        importExportService.exportTemplate(response.getOutputStream());
    }
    
    @Operation(summary = "导入用例", description = "从Excel文件导入用例")
    @PostMapping("/import")
    public Result<TestCaseImportExportService.ImportResult> importTestCases(
            @RequestParam("file") MultipartFile file) throws IOException {
        TestCaseImportExportService.ImportResult result = importExportService.importFromExcel(file);
        return Result.success(result);
    }
}

