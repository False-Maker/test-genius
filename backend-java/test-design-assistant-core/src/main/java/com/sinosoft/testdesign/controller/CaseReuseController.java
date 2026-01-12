package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.service.CaseReuseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用例复用管理控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/v1/case-reuse")
@RequiredArgsConstructor
public class CaseReuseController {
    
    private final CaseReuseService caseReuseService;
    
    /**
     * 初始化用例向量表
     */
    @PostMapping("/init")
    public Result<Boolean> initCaseVectorTable() {
        boolean success = caseReuseService.initCaseVectorTable();
        return Result.success(success);
    }
    
    /**
     * 更新用例的向量表示
     */
    @PostMapping("/cases/{caseId}/embedding")
    public Result<Boolean> updateCaseEmbedding(@PathVariable Long caseId) {
        boolean success = caseReuseService.updateCaseEmbedding(caseId);
        return Result.success(success);
    }
    
    /**
     * 搜索相似用例（基于语义相似度）
     */
    @PostMapping("/cases/search/similar")
    public Result<List<Map<String, Object>>> searchSimilarCases(
            @RequestParam String caseText,
            @RequestParam(required = false) Long layerId,
            @RequestParam(required = false) Long methodId,
            @RequestParam(defaultValue = "10") int topK,
            @RequestParam(defaultValue = "0.7") double similarityThreshold) {
        List<Map<String, Object>> cases = caseReuseService.searchSimilarCases(
                caseText, layerId, methodId, topK, similarityThreshold);
        return Result.success(cases);
    }
    
    /**
     * 关键词检索用例
     */
    @GetMapping("/cases/search/keyword/{keyword}")
    public Result<List<Map<String, Object>>> searchCasesByKeyword(
            @PathVariable String keyword,
            @RequestParam(required = false) Long layerId,
            @RequestParam(required = false) Long methodId,
            @RequestParam(defaultValue = "10") int topK) {
        List<Map<String, Object>> cases = caseReuseService.searchCasesByKeyword(
                keyword, layerId, methodId, topK);
        return Result.success(cases);
    }
    
    /**
     * 推荐相似用例（基于现有用例）
     */
    @GetMapping("/cases/{caseId}/recommend")
    public Result<List<Map<String, Object>>> recommendSimilarCases(
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "5") int topK) {
        List<Map<String, Object>> cases = caseReuseService.recommendSimilarCases(caseId, topK);
        return Result.success(cases);
    }
    
    /**
     * 创建用例组合（测试套件）
     */
    @PostMapping("/suites")
    public Result<Long> createCaseSuite(
            @RequestParam String suiteName,
            @RequestParam List<Long> caseIds,
            @RequestParam(required = false) Long creatorId) {
        Long suiteId = caseReuseService.createCaseSuite(suiteName, caseIds, creatorId);
        return Result.success(suiteId);
    }
}

