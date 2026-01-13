package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.service.KnowledgeBaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 知识库管理Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("知识库管理Controller测试")
class KnowledgeBaseControllerTest extends BaseControllerTest {
    
    @MockBean
    private KnowledgeBaseService knowledgeBaseService;
    
    @Test
    @DisplayName("初始化知识库-成功")
    void testInitKnowledgeBase_Success() throws Exception {
        // Given
        when(knowledgeBaseService.initKnowledgeBase())
            .thenReturn(true);
        
        // When & Then
        mockMvc.perform(post("/v1/knowledge/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    @DisplayName("添加知识库文档-成功")
    void testAddDocument_Success() throws Exception {
        // Given
        Long docId = 1L;
        String docCode = "DOC-001";
        String docName = "测试文档";
        String docType = "需求文档";
        String docContent = "文档内容";
        
        when(knowledgeBaseService.addDocument(
            eq(docCode), eq(docName), eq(docType), eq(docContent), 
            any(), any(), any()))
            .thenReturn(docId);
        
        // When & Then
        mockMvc.perform(post("/v1/knowledge/documents")
                .param("docCode", docCode)
                .param("docName", docName)
                .param("docType", docType)
                .param("docContent", docContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(docId));
    }
    
    @Test
    @DisplayName("添加知识库文档-带可选参数")
    void testAddDocument_WithOptionalParams() throws Exception {
        // Given
        Long docId = 1L;
        String docCode = "DOC-001";
        String docName = "测试文档";
        String docType = "需求文档";
        String docContent = "文档内容";
        String docCategory = "业务文档";
        String docUrl = "http://example.com/doc.pdf";
        Long creatorId = 100L;
        
        when(knowledgeBaseService.addDocument(
            eq(docCode), eq(docName), eq(docType), eq(docContent), 
            eq(docCategory), eq(docUrl), eq(creatorId)))
            .thenReturn(docId);
        
        // When & Then
        mockMvc.perform(post("/v1/knowledge/documents")
                .param("docCode", docCode)
                .param("docName", docName)
                .param("docType", docType)
                .param("docContent", docContent)
                .param("docCategory", docCategory)
                .param("docUrl", docUrl)
                .param("creatorId", String.valueOf(creatorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(docId));
    }
    
    @Test
    @DisplayName("语义检索知识库文档-成功")
    void testSearchDocuments_Success() throws Exception {
        // Given
        String queryText = "测试查询";
        int topK = 10;
        double similarityThreshold = 0.7;
        
        List<Map<String, Object>> documents = new ArrayList<>();
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("docId", 1L);
        doc1.put("docName", "测试文档1");
        doc1.put("similarity", 0.85);
        documents.add(doc1);
        
        when(knowledgeBaseService.searchDocuments(
            eq(queryText), any(), eq(topK), eq(similarityThreshold)))
            .thenReturn(documents);
        
        // When & Then
        mockMvc.perform(post("/v1/knowledge/documents/search")
                .param("queryText", queryText)
                .param("topK", String.valueOf(topK))
                .param("similarityThreshold", String.valueOf(similarityThreshold)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].docId").value(1L))
                .andExpect(jsonPath("$.data[0].docName").value("测试文档1"));
    }
    
    @Test
    @DisplayName("语义检索知识库文档-带文档类型过滤")
    void testSearchDocuments_WithDocType() throws Exception {
        // Given
        String queryText = "测试查询";
        String docType = "需求文档";
        int topK = 5;
        
        List<Map<String, Object>> documents = new ArrayList<>();
        
        when(knowledgeBaseService.searchDocuments(
            eq(queryText), eq(docType), eq(topK), anyDouble()))
            .thenReturn(documents);
        
        // When & Then
        mockMvc.perform(post("/v1/knowledge/documents/search")
                .param("queryText", queryText)
                .param("docType", docType)
                .param("topK", String.valueOf(topK)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    @DisplayName("关键词检索知识库文档-成功")
    void testSearchDocumentsByKeyword_Success() throws Exception {
        // Given
        String keyword = "投保";
        int topK = 10;
        
        List<Map<String, Object>> documents = new ArrayList<>();
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("docId", 1L);
        doc1.put("docName", "投保流程文档");
        documents.add(doc1);
        
        when(knowledgeBaseService.searchDocumentsByKeyword(
            eq(keyword), any(), eq(topK)))
            .thenReturn(documents);
        
        // When & Then
        mockMvc.perform(get("/v1/knowledge/documents/keyword/{keyword}", keyword)
                .param("topK", String.valueOf(topK)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].docId").value(1L))
                .andExpect(jsonPath("$.data[0].docName").value("投保流程文档"));
    }
    
    @Test
    @DisplayName("关键词检索知识库文档-带文档类型过滤")
    void testSearchDocumentsByKeyword_WithDocType() throws Exception {
        // Given
        String keyword = "测试";
        String docType = "测试用例";
        int topK = 5;
        
        List<Map<String, Object>> documents = new ArrayList<>();
        
        when(knowledgeBaseService.searchDocumentsByKeyword(
            eq(keyword), eq(docType), eq(topK)))
            .thenReturn(documents);
        
        // When & Then
        mockMvc.perform(get("/v1/knowledge/documents/keyword/{keyword}", keyword)
                .param("docType", docType)
                .param("topK", String.valueOf(topK)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}

