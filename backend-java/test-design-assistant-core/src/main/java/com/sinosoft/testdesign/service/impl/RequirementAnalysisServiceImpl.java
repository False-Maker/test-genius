package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.service.ModelCallService;
import com.sinosoft.testdesign.service.RequirementAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 需求分析服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementAnalysisServiceImpl implements RequirementAnalysisService {
    
    private final RequirementRepository requirementRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    @Override
    public RequirementAnalysisResult analyzeRequirement(Long requirementId) {
        log.info("开始分析需求: requirementId={}", requirementId);
        
        // 1. 查询需求信息
        TestRequirement requirement = requirementRepository.findById(requirementId)
            .orElseThrow(() -> new BusinessException("需求不存在: " + requirementId));
        
        // 2. 如果需求有文档，先解析文档
        String requirementText = requirement.getRequirementDescription();
        if (requirement.getRequirementDocUrl() != null && !requirement.getRequirementDocUrl().isEmpty()) {
            // 如果有文档，调用文档解析服务
            try {
                String documentContent = parseDocument(requirement.getRequirementDocUrl());
                if (documentContent != null && !documentContent.isEmpty()) {
                    requirementText = documentContent;
                }
            } catch (Exception e) {
                log.warn("文档解析失败，使用需求描述: {}", e.getMessage());
            }
        }
        
        if (requirementText == null || requirementText.trim().isEmpty()) {
            throw new BusinessException("需求描述或文档内容不能为空");
        }
        
        // 3. 调用AI服务进行需求分析
        try {
            Map<String, Object> analysisResult = callAnalysisService(requirementText);
            
            RequirementAnalysisResult result = new RequirementAnalysisResult();
            result.setRequirementId(requirementId);
            result.setRequirementName(requirement.getRequirementName());
            result.setRequirementText(requirementText);
            result.setTestPoints(extractTestPoints(analysisResult));
            result.setBusinessRules(extractBusinessRules(analysisResult));
            result.setKeyInfo(extractKeyInfo(analysisResult));
            
            log.info("需求分析完成: requirementId={}, 测试要点数={}, 业务规则数={}", 
                requirementId, 
                result.getTestPoints().size(),
                result.getBusinessRules().size());
            
            return result;
            
        } catch (Exception e) {
            log.error("需求分析失败: requirementId={}, 错误={}", requirementId, e.getMessage(), e);
            throw new BusinessException("需求分析失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<TestPoint> extractTestPoints(Long requirementId) {
        RequirementAnalysisResult result = analyzeRequirement(requirementId);
        return result.getTestPoints();
    }
    
    @Override
    public List<BusinessRule> extractBusinessRules(Long requirementId) {
        RequirementAnalysisResult result = analyzeRequirement(requirementId);
        return result.getBusinessRules();
    }
    
    /**
     * 解析文档
     */
    private String parseDocument(String docUrl) {
        try {
            // 调用Python服务的文档解析接口
            String url = aiServiceUrl + "/api/v1/document/parse-by-path";
            
            Map<String, String> request = new HashMap<>();
            request.put("file_path", docUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return (String) body.get("content");
            }
            
            return null;
        } catch (Exception e) {
            log.error("文档解析失败: docUrl={}, 错误={}", docUrl, e.getMessage());
            return null;
        }
    }
    
    /**
     * 调用AI分析服务
     */
    private Map<String, Object> callAnalysisService(String requirementText) {
        try {
            // 构建分析提示词
            String prompt = buildAnalysisPrompt(requirementText);
            
            // 调用大模型进行分析（这里简化处理，实际应该调用ModelCallService）
            // 暂时返回模拟数据，后续可以集成大模型调用
            Map<String, Object> result = new HashMap<>();
            result.put("test_points", extractTestPointsFromText(requirementText));
            result.put("business_rules", extractBusinessRulesFromText(requirementText));
            result.put("key_info", extractKeyInfoFromText(requirementText));
            
            return result;
        } catch (Exception e) {
            log.error("调用AI分析服务失败: {}", e.getMessage(), e);
            throw new BusinessException("AI分析服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建分析提示词
     */
    private String buildAnalysisPrompt(String requirementText) {
        return String.format(
            "请分析以下需求文档，提取测试要点和业务规则：\n\n%s\n\n" +
            "请以JSON格式返回分析结果，包含以下字段：\n" +
            "1. test_points: 测试要点列表，每个测试要点包含：名称、描述、优先级\n" +
            "2. business_rules: 业务规则列表，每个业务规则包含：规则名称、规则描述、规则类型\n" +
            "3. key_info: 关键信息，包含：关键词、功能模块、涉及角色等",
            requirementText
        );
    }
    
    /**
     * 从分析结果中提取测试要点
     */
    @SuppressWarnings("unchecked")
    private List<TestPoint> extractTestPoints(Map<String, Object> analysisResult) {
        try {
            List<Map<String, Object>> testPointsData = (List<Map<String, Object>>) analysisResult.get("test_points");
            if (testPointsData == null) {
                return List.of();
            }
            
            return testPointsData.stream()
                .map(data -> {
                    TestPoint point = new TestPoint();
                    point.setName((String) data.get("name"));
                    point.setDescription((String) data.get("description"));
                    point.setPriority((String) data.getOrDefault("priority", "中"));
                    return point;
                })
                .toList();
        } catch (Exception e) {
            log.warn("提取测试要点失败: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 从分析结果中提取业务规则
     */
    @SuppressWarnings("unchecked")
    private List<BusinessRule> extractBusinessRules(Map<String, Object> analysisResult) {
        try {
            List<Map<String, Object>> rulesData = (List<Map<String, Object>>) analysisResult.get("business_rules");
            if (rulesData == null) {
                return List.of();
            }
            
            return rulesData.stream()
                .map(data -> {
                    BusinessRule rule = new BusinessRule();
                    rule.setName((String) data.get("name"));
                    rule.setDescription((String) data.get("description"));
                    rule.setType((String) data.getOrDefault("type", "业务规则"));
                    return rule;
                })
                .toList();
        } catch (Exception e) {
            log.warn("提取业务规则失败: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * 从分析结果中提取关键信息
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractKeyInfo(Map<String, Object> analysisResult) {
        try {
            Map<String, Object> keyInfo = (Map<String, Object>) analysisResult.get("key_info");
            return keyInfo != null ? keyInfo : new HashMap<>();
        } catch (Exception e) {
            log.warn("提取关键信息失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 从文本中提取测试要点（简单实现）
     */
    private List<Map<String, Object>> extractTestPointsFromText(String text) {
        // 简单的关键词匹配，实际应该使用大模型
        List<Map<String, Object>> points = new java.util.ArrayList<>();
        
        // 查找包含"测试"、"验证"、"检查"等关键词的句子
        String[] sentences = text.split("[。！？\n]");
        for (String sentence : sentences) {
            if (sentence.contains("测试") || sentence.contains("验证") || sentence.contains("检查")) {
                Map<String, Object> point = new HashMap<>();
                point.put("name", sentence.substring(0, Math.min(50, sentence.length())));
                point.put("description", sentence);
                point.put("priority", "中");
                points.add(point);
            }
        }
        
        return points.isEmpty() ? List.of(createDefaultTestPoint()) : points;
    }
    
    /**
     * 从文本中提取业务规则（简单实现）
     */
    private List<Map<String, Object>> extractBusinessRulesFromText(String text) {
        // 简单的关键词匹配，实际应该使用大模型
        List<Map<String, Object>> rules = new java.util.ArrayList<>();
        
        // 查找包含"规则"、"要求"、"必须"等关键词的句子
        String[] sentences = text.split("[。！？\n]");
        for (String sentence : sentences) {
            if (sentence.contains("规则") || sentence.contains("要求") || sentence.contains("必须")) {
                Map<String, Object> rule = new HashMap<>();
                rule.put("name", sentence.substring(0, Math.min(50, sentence.length())));
                rule.put("description", sentence);
                rule.put("type", "业务规则");
                rules.add(rule);
            }
        }
        
        return rules.isEmpty() ? List.of(createDefaultBusinessRule()) : rules;
    }
    
    /**
     * 从文本中提取关键信息
     */
    private Map<String, Object> extractKeyInfoFromText(String text) {
        Map<String, Object> keyInfo = new HashMap<>();
        
        // 提取关键词（简单实现）
        java.util.Set<String> keywords = new java.util.HashSet<>();
        String[] words = text.split("[\\s，。！？、\n]");
        for (String word : words) {
            if (word.length() >= 2 && word.length() <= 6) {
                keywords.add(word);
            }
        }
        
        keyInfo.put("keywords", keywords.stream().limit(10).toList());
        keyInfo.put("content_length", text.length());
        keyInfo.put("sentence_count", text.split("[。！？\n]").length);
        
        return keyInfo;
    }
    
    private Map<String, Object> createDefaultTestPoint() {
        Map<String, Object> point = new HashMap<>();
        point.put("name", "功能测试");
        point.put("description", "验证功能是否按需求实现");
        point.put("priority", "高");
        return point;
    }
    
    private Map<String, Object> createDefaultBusinessRule() {
        Map<String, Object> rule = new HashMap<>();
        rule.put("name", "业务规则");
        rule.put("description", "遵循业务规则和规范");
        rule.put("type", "业务规则");
        return rule;
    }
    
    /**
     * 需求分析结果
     */
    public static class RequirementAnalysisResult {
        private Long requirementId;
        private String requirementName;
        private String requirementText;
        private List<TestPoint> testPoints;
        private List<BusinessRule> businessRules;
        private Map<String, Object> keyInfo;
        
        // Getters and Setters
        public Long getRequirementId() { return requirementId; }
        public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }
        
        public String getRequirementName() { return requirementName; }
        public void setRequirementName(String requirementName) { this.requirementName = requirementName; }
        
        public String getRequirementText() { return requirementText; }
        public void setRequirementText(String requirementText) { this.requirementText = requirementText; }
        
        public List<TestPoint> getTestPoints() { return testPoints; }
        public void setTestPoints(List<TestPoint> testPoints) { this.testPoints = testPoints; }
        
        public List<BusinessRule> getBusinessRules() { return businessRules; }
        public void setBusinessRules(List<BusinessRule> businessRules) { this.businessRules = businessRules; }
        
        public Map<String, Object> getKeyInfo() { return keyInfo; }
        public void setKeyInfo(Map<String, Object> keyInfo) { this.keyInfo = keyInfo; }
    }
    
    /**
     * 测试要点
     */
    public static class TestPoint {
        private String name;
        private String description;
        private String priority;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
    
    /**
     * 业务规则
     */
    public static class BusinessRule {
        private String name;
        private String description;
        private String type;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}

