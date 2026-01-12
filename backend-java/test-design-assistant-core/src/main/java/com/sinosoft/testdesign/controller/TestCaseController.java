package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

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
    
    @Operation(summary = "创建用例", description = "创建新的测试用例")
    @PostMapping
    public Result<TestCase> createTestCase(@RequestBody TestCase testCase) {
        return Result.success(testCaseService.createTestCase(testCase));
    }
    
    @Operation(summary = "查询用例列表", description = "分页查询用例列表，支持按用例名称和状态搜索")
    @GetMapping
    public Result<Page<TestCase>> getTestCaseList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String caseName,
            @RequestParam(required = false) String caseStatus,
            @RequestParam(required = false) Long requirementId) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(testCaseService.getTestCaseList(pageable, caseName, caseStatus, requirementId));
    }
    
    @Operation(summary = "获取用例详情", description = "根据ID获取用例详情")
    @GetMapping("/{id}")
    public Result<TestCase> getTestCaseById(@PathVariable Long id) {
        return Result.success(testCaseService.getTestCaseById(id));
    }
    
    @Operation(summary = "更新用例", description = "更新用例信息")
    @PutMapping("/{id}")
    public Result<TestCase> updateTestCase(
            @PathVariable Long id,
            @RequestBody TestCase testCase) {
        return Result.success(testCaseService.updateTestCase(id, testCase));
    }
    
    @Operation(summary = "删除用例", description = "删除指定用例")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTestCase(@PathVariable Long id) {
        testCaseService.deleteTestCase(id);
        return Result.success();
    }
    
    @Operation(summary = "更新用例状态", description = "更新用例状态（状态流转）")
    @PutMapping("/{id}/status")
    public Result<TestCase> updateCaseStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return Result.success(testCaseService.updateCaseStatus(id, status));
    }
    
    @Operation(summary = "审核用例", description = "审核测试用例")
    @PostMapping("/{id}/review")
    public Result<TestCase> reviewTestCase(
            @PathVariable Long id,
            @RequestParam String reviewResult,
            @RequestParam(required = false) String reviewComment) {
        return Result.success(testCaseService.reviewTestCase(id, reviewResult, reviewComment));
    }
}

