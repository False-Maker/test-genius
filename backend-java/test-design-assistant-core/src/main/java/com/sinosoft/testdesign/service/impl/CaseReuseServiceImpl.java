package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.service.CaseReuseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用例复用服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseReuseServiceImpl implements CaseReuseService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    @Override
    public boolean initCaseVectorTable() {
        try {
            String url = aiServiceUrl + "/api/v1/case-reuse/init";
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("success"));
        } catch (Exception e) {
            log.error("初始化用例向量表失败", e);
            throw new BusinessException("初始化用例向量表失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean updateCaseEmbedding(Long caseId) {
        try {
            String url = aiServiceUrl + "/api/v1/case-reuse/cases/" + caseId + "/embedding";
            Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);
            return response != null && Boolean.TRUE.equals(response.get("success"));
        } catch (Exception e) {
            log.error("更新用例向量失败: caseId={}", caseId, e);
            return false;
        }
    }
    
    @Override
    public List<Map<String, Object>> searchSimilarCases(String caseText, Long layerId, Long methodId,
                                                       int topK, double similarityThreshold) {
        try {
            String url = aiServiceUrl + "/api/v1/case-reuse/cases/search/similar";
            
            Map<String, Object> request = new HashMap<>();
            request.put("case_text", caseText);
            request.put("layer_id", layerId);
            request.put("method_id", methodId);
            request.put("top_k", topK);
            request.put("similarity_threshold", similarityThreshold);
            
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return objectMapper.convertValue(response.get("cases"), 
                        new TypeReference<List<Map<String, Object>>>() {});
            } else {
                log.warn("搜索相似用例失败: {}", response);
                return List.of();
            }
        } catch (Exception e) {
            log.error("搜索相似用例失败", e);
            throw new BusinessException("搜索相似用例失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> searchCasesByKeyword(String keyword, Long layerId, Long methodId, int topK) {
        try {
            StringBuilder url = new StringBuilder(aiServiceUrl + "/api/v1/case-reuse/cases/search/keyword/" + keyword);
            boolean hasParam = false;
            if (layerId != null) {
                url.append(hasParam ? "&" : "?").append("layer_id=").append(layerId);
                hasParam = true;
            }
            if (methodId != null) {
                url.append(hasParam ? "&" : "?").append("method_id=").append(methodId);
                hasParam = true;
            }
            url.append(hasParam ? "&" : "?").append("top_k=").append(topK);
            
            Map<String, Object> response = restTemplate.getForObject(url.toString(), Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return objectMapper.convertValue(response.get("cases"), 
                        new TypeReference<List<Map<String, Object>>>() {});
            } else {
                log.warn("关键词检索用例失败: {}", response);
                return List.of();
            }
        } catch (Exception e) {
            log.error("关键词检索用例失败", e);
            throw new BusinessException("关键词检索用例失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Map<String, Object>> recommendSimilarCases(Long caseId, int topK) {
        try {
            String url = aiServiceUrl + "/api/v1/case-reuse/cases/" + caseId + "/recommend?top_k=" + topK;
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return objectMapper.convertValue(response.get("cases"), 
                        new TypeReference<List<Map<String, Object>>>() {});
            } else {
                log.warn("推荐相似用例失败: {}", response);
                return List.of();
            }
        } catch (Exception e) {
            log.error("推荐相似用例失败", e);
            throw new BusinessException("推荐相似用例失败: " + e.getMessage());
        }
    }
    
    @Override
    public Long createCaseSuite(String suiteName, List<Long> caseIds, Long creatorId) {
        try {
            String url = aiServiceUrl + "/api/v1/case-reuse/suites";
            
            Map<String, Object> request = new HashMap<>();
            request.put("suite_name", suiteName);
            request.put("case_ids", caseIds);
            request.put("creator_id", creatorId);
            
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return objectMapper.convertValue(response.get("suite_id"), Long.class);
            } else {
                throw new BusinessException("创建用例套件失败: " + response.get("message"));
            }
        } catch (Exception e) {
            log.error("创建用例套件失败", e);
            throw new BusinessException("创建用例套件失败: " + e.getMessage());
        }
    }
}

