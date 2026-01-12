package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    @Override
    public boolean initKnowledgeBase() {
        try {
            String url = aiServiceUrl + "/api/v1/knowledge/init";
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("success"));
        } catch (Exception e) {
            log.error("初始化知识库失败", e);
            throw new BusinessException("初始化知识库失败: " + e.getMessage());
        }
    }
    
    @Override
    public Long addDocument(String docCode, String docName, String docType, String docContent,
                           String docCategory, String docUrl, Long creatorId) {
        try {
            String url = aiServiceUrl + "/api/v1/knowledge/documents";
            
            Map<String, Object> request = new HashMap<>();
            request.put("doc_code", docCode);
            request.put("doc_name", docName);
            request.put("doc_type", docType);
            request.put("doc_content", docContent);
            request.put("doc_category", docCategory);
            request.put("doc_url", docUrl);
            request.put("creator_id", creatorId);
            
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return objectMapper.convertValue(response.get("doc_id"), Long.class);
            } else {
                throw new BusinessException("添加文档失败: " + response.get("message"));
            }
        } catch (Exception e) {
            log.error("添加知识库文档失败", e);
            throw new BusinessException("添加知识库文档失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> searchDocuments(String queryText, String docType,
                                                      int topK, double similarityThreshold) {
        try {
            String url = aiServiceUrl + "/api/v1/knowledge/documents/search";
            
            Map<String, Object> request = new HashMap<>();
            request.put("query_text", queryText);
            request.put("doc_type", docType);
            request.put("top_k", topK);
            request.put("similarity_threshold", similarityThreshold);
            
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return objectMapper.convertValue(response.get("documents"), 
                        new TypeReference<List<Map<String, Object>>>() {});
            } else {
                log.warn("检索文档失败: {}", response);
                return List.of();
            }
        } catch (Exception e) {
            log.error("语义检索知识库文档失败", e);
            throw new BusinessException("语义检索知识库文档失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> searchDocumentsByKeyword(String keyword, String docType, int topK) {
        try {
            String url = aiServiceUrl + "/api/v1/knowledge/documents/keyword/" + keyword;
            if (docType != null) {
                url += "?doc_type=" + docType + "&top_k=" + topK;
            } else {
                url += "?top_k=" + topK;
            }
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return objectMapper.convertValue(response.get("documents"), 
                        new TypeReference<List<Map<String, Object>>>() {});
            } else {
                log.warn("关键词检索文档失败: {}", response);
                return List.of();
            }
        } catch (Exception e) {
            log.error("关键词检索知识库文档失败", e);
            throw new BusinessException("关键词检索知识库文档失败: " + e.getMessage());
        }
    }
}

