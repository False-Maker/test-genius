package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.service.KnowledgeBaseManageService;
import com.sinosoft.testdesign.service.KnowledgeBaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("知识库管理Controller测试")
@WebMvcTest(KnowledgeBaseController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class KnowledgeBaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KnowledgeBaseManageService knowledgeBaseManageService;

    @MockBean
    private KnowledgeBaseService knowledgeBaseService;

    @Test
    @DisplayName("初始化知识库-成功")
    void testInitKnowledgeBase_Success() throws Exception {
        when(knowledgeBaseService.initKnowledgeBase()).thenReturn(true);

        mockMvc.perform(post("/v1/knowledge-base/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("添加知识库文档-成功")
    void testAddDocument_Success() throws Exception {
        Long docId = 1L;
        Long kbId = 2L;
        String docCode = "DOC-001";
        String docName = "测试文档";
        String docType = "需求文档";
        String docContent = "文档内容";

        when(knowledgeBaseService.addDocument(
                eq(kbId), eq(docCode), eq(docName), eq(docType), eq(docContent), any(), any(), any()))
                .thenReturn(docId);

        mockMvc.perform(post("/v1/knowledge-base/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kbId": 2,
                                  "docCode": "DOC-001",
                                  "docName": "测试文档",
                                  "docType": "需求文档",
                                  "docContent": "文档内容"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(docId));
    }

    @Test
    @DisplayName("添加知识库文档-带可选参数")
    void testAddDocument_WithOptionalParams() throws Exception {
        Long docId = 1L;
        Long kbId = 2L;
        String docCode = "DOC-001";
        String docName = "测试文档";
        String docType = "需求文档";
        String docContent = "文档内容";
        String docCategory = "业务文档";
        String docUrl = "http://example.com/doc.pdf";
        Long creatorId = 100L;

        when(knowledgeBaseService.addDocument(
                eq(kbId), eq(docCode), eq(docName), eq(docType), eq(docContent),
                eq(docCategory), eq(docUrl), eq(creatorId)))
                .thenReturn(docId);

        mockMvc.perform(post("/v1/knowledge-base/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kbId": 2,
                                  "docCode": "DOC-001",
                                  "docName": "测试文档",
                                  "docType": "需求文档",
                                  "docContent": "文档内容",
                                  "docCategory": "业务文档",
                                  "docUrl": "http://example.com/doc.pdf",
                                  "creatorId": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(docId));
    }

    @Test
    @DisplayName("语义检索知识库文档-成功")
    void testSearchDocuments_Success() throws Exception {
        String queryText = "测试查询";
        int topK = 10;
        double similarityThreshold = 0.7;

        List<Map<String, Object>> documents = new ArrayList<>();
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("docCode", "DOC-001");
        doc1.put("docName", "测试文档1");
        doc1.put("similarity", 0.85);
        documents.add(doc1);

        when(knowledgeBaseService.searchDocuments(
                eq(queryText), any(), eq(topK), eq(similarityThreshold)))
                .thenReturn(documents);

        mockMvc.perform(post("/v1/knowledge-base/documents/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "queryText": "测试查询",
                                  "topK": 10,
                                  "similarityThreshold": 0.7
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].docCode").value("DOC-001"))
                .andExpect(jsonPath("$.data[0].docName").value("测试文档1"));
    }

    @Test
    @DisplayName("语义检索知识库文档-带文档类型过滤")
    void testSearchDocuments_WithDocType() throws Exception {
        String queryText = "测试查询";
        String docType = "需求文档";
        int topK = 5;

        when(knowledgeBaseService.searchDocuments(
                eq(queryText), eq(docType), eq(topK), anyDouble()))
                .thenReturn(List.of());

        mockMvc.perform(post("/v1/knowledge-base/documents/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "queryText": "测试查询",
                                  "docType": "需求文档",
                                  "topK": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("关键词检索知识库文档-成功")
    void testSearchDocumentsByKeyword_Success() throws Exception {
        String keyword = "投保";
        int topK = 10;

        List<Map<String, Object>> documents = new ArrayList<>();
        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("docCode", "DOC-002");
        doc1.put("docName", "投保流程文档");
        documents.add(doc1);

        when(knowledgeBaseService.searchDocumentsByKeyword(
                eq(keyword), any(), eq(topK)))
                .thenReturn(documents);

        mockMvc.perform(get("/v1/knowledge-base/documents/keyword/{keyword}", keyword)
                        .param("topK", String.valueOf(topK)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].docCode").value("DOC-002"))
                .andExpect(jsonPath("$.data[0].docName").value("投保流程文档"));
    }

    @Test
    @DisplayName("关键词检索知识库文档-带文档类型过滤")
    void testSearchDocumentsByKeyword_WithDocType() throws Exception {
        String keyword = "测试";
        String docType = "测试用例";
        int topK = 5;

        when(knowledgeBaseService.searchDocumentsByKeyword(
                eq(keyword), eq(docType), eq(topK)))
                .thenReturn(List.of());

        mockMvc.perform(get("/v1/knowledge-base/documents/keyword/{keyword}", keyword)
                        .param("docType", docType)
                        .param("topK", String.valueOf(topK)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
