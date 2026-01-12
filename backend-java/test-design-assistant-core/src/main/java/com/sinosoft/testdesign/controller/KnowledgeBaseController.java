package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {
    
    private final KnowledgeBaseService knowledgeBaseService;
    
    /**
     * 初始化知识库表结构
     */
    @PostMapping("/init")
    public Result<Boolean> initKnowledgeBase() {
        boolean success = knowledgeBaseService.initKnowledgeBase();
        return Result.success(success);
    }
    
    /**
     * 添加知识库文档
     */
    @PostMapping("/documents")
    public Result<Long> addDocument(
            @RequestParam String docCode,
            @RequestParam String docName,
            @RequestParam String docType,
            @RequestParam String docContent,
            @RequestParam(required = false) String docCategory,
            @RequestParam(required = false) String docUrl,
            @RequestParam(required = false) Long creatorId) {
        Long docId = knowledgeBaseService.addDocument(
                docCode, docName, docType, docContent, docCategory, docUrl, creatorId);
        return Result.success(docId);
    }
    
    /**
     * 语义检索知识库文档
     */
    @PostMapping("/documents/search")
    public Result<List<Map<String, Object>>> searchDocuments(
            @RequestParam String queryText,
            @RequestParam(required = false) String docType,
            @RequestParam(defaultValue = "10") int topK,
            @RequestParam(defaultValue = "0.7") double similarityThreshold) {
        List<Map<String, Object>> documents = knowledgeBaseService.searchDocuments(
                queryText, docType, topK, similarityThreshold);
        return Result.success(documents);
    }
    
    /**
     * 关键词检索知识库文档
     */
    @GetMapping("/documents/keyword/{keyword}")
    public Result<List<Map<String, Object>>> searchDocumentsByKeyword(
            @PathVariable String keyword,
            @RequestParam(required = false) String docType,
            @RequestParam(defaultValue = "10") int topK) {
        List<Map<String, Object>> documents = knowledgeBaseService.searchDocumentsByKeyword(
                keyword, docType, topK);
        return Result.success(documents);
    }
}

