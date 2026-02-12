package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.service.ModelCallService;
import com.sinosoft.testdesign.service.RequirementAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final ModelCallService modelCallService;
    
    /** 需求分析任务类型常量 */
    private static final String TASK_TYPE_REQUIREMENT_ANALYSIS = "REQUIREMENT_ANALYSIS";
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Value("${app.upload.base-path:./uploads}")
    private String uploadBasePath;

    @Value("${app.upload.url-prefix:/api/v1/files}")
    private String uploadUrlPrefix;
    
    @Override
    public RequirementAnalysisResult analyzeRequirement(Long requirementId) {
        log.info("开始分析需求: requirementId={}", requirementId);
        
        // 1. 查询需求信息
        TestRequirement requirement = requirementRepository.findById(requirementId)
            .orElseThrow(() -> new BusinessException("需求不存在: " + requirementId));
        
        // 2. 准备需求文本
        String requirementText = requirement.getRequirementDescription();
        boolean hasDescription = requirementText != null && !requirementText.trim().isEmpty();
        boolean hasDocUrl = requirement.getRequirementDocUrl() != null && !requirement.getRequirementDocUrl().isEmpty();
        
        // 3. 如果有文档URL，尝试解析文档内容
        String documentContent = null;
        boolean docParseFailed = false;
        String docParseFailReason = null;
        
        if (hasDocUrl) {
            try {
                documentContent = parseDocument(requirement.getRequirementDocUrl());
                if (documentContent == null || documentContent.isEmpty()) {
                    docParseFailed = true;
                    docParseFailReason = "文件可能不存在或已被清理（路径: " + requirement.getRequirementDocUrl() + "）";
                    log.warn("文档解析返回空内容: docUrl={}", requirement.getRequirementDocUrl());
                }
            } catch (Exception e) {
                docParseFailed = true;
                docParseFailReason = e.getMessage();
                log.warn("文档解析异常: docUrl={}, 错误={}", requirement.getRequirementDocUrl(), e.getMessage());
            }
        }
        
        // 4. 确定最终用于分析的文本
        // 优先使用文档内容，文档不可用时回退到需求描述
        String analysisText = null;
        if (documentContent != null && !documentContent.isEmpty()) {
            analysisText = documentContent;
        } else if (hasDescription) {
            if (docParseFailed) {
                log.info("文档解析失败，回退使用需求描述进行分析: requirementId={}", requirementId);
            }
            analysisText = requirementText;
        }
        
        // 5. 如果既没有文档内容也没有需求描述，无法分析
        if (analysisText == null || analysisText.trim().isEmpty()) {
            String errorMsg;
            if (hasDocUrl && docParseFailed) {
                errorMsg = "需求文档解析失败（" + docParseFailReason + "），且需求描述为空，无法进行分析。" +
                    "请重新上传需求文档或填写需求描述后再进行分析。";
            } else if (hasDocUrl) {
                errorMsg = "需求文档内容为空，且需求描述为空，无法进行分析。" +
                    "请重新上传需求文档或填写需求描述后再进行分析。";
            } else {
                errorMsg = "需求描述为空且未上传需求文档，无法进行分析。" +
                    "请至少填写需求描述或上传需求文档后再进行分析。";
            }
            throw new BusinessException(errorMsg);
        }
        
        // 6. 调用AI服务进行需求分析（使用模型配置）
        try {
            Map<String, Object> analysisResult = callAnalysisService(analysisText);
            
            RequirementAnalysisResult result = new RequirementAnalysisResult();
            result.setRequirementId(requirementId);
            result.setRequirementName(requirement.getRequirementName());
            result.setRequirementText(analysisText);
            result.setTestPoints(extractTestPoints(analysisResult));
            result.setBusinessRules(extractBusinessRules(analysisResult));
            result.setKeyInfo(extractKeyInfo(analysisResult));
            
            log.info("需求分析完成: requirementId={}, 测试要点数={}, 业务规则数={}", 
                requirementId, 
                result.getTestPoints().size(),
                result.getBusinessRules().size());
            
            return result;
            
        } catch (BusinessException e) {
            throw e;
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
            String resolvedPath = resolveDocumentPath(docUrl);
            if (resolvedPath == null) {
                log.warn("无法解析文档路径: docUrl={}", docUrl);
                return null;
            }

            File file = new File(resolvedPath);
            if (!file.exists() || !file.isFile()) {
                Path basePath = resolveUploadBasePath();
                boolean baseDirExists = Files.isDirectory(basePath);
                log.warn("文档不存在或不是文件: resolvedPath={}, uploadBasePath={}, 上传根目录存在={}（若为Docker部署请确认 volumes 中 uploads 已挂载；若曾重建过卷请重新上传文档）",
                    resolvedPath, uploadBasePath, baseDirExists);
                return null;
            }

            String url = aiServiceUrl + "/api/v1/document/parse";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                return (String) responseBody.get("content");
            }

            return null;
        } catch (Exception e) {
            log.error("文档解析失败: docUrl={}, 错误={}", docUrl, e.getMessage());
            return null;
        }
    }

    private String resolveDocumentPath(String docUrl) {
        if (docUrl == null) {
            return null;
        }

        String trimmed = docUrl.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String pathPart = trimmed;
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            try {
                URI uri = URI.create(trimmed);
                if (uri.getPath() != null) {
                    pathPart = uri.getPath();
                }
            } catch (Exception ignored) {
                pathPart = trimmed;
            }
        }

        if (pathPart.startsWith(uploadUrlPrefix)) {
            pathPart = pathPart.substring(uploadUrlPrefix.length());
        }

        if (pathPart.startsWith("/")) {
            pathPart = pathPart.substring(1);
        }

        Path basePath = resolveUploadBasePath();
        Path resolved = basePath.resolve(pathPart).normalize();

        if (!resolved.startsWith(basePath)) {
            log.warn("文档路径不安全: {}", docUrl);
            return null;
        }

        return resolved.toString();
    }

    private Path resolveUploadBasePath() {
        Path path = Paths.get(uploadBasePath);
        if (!path.isAbsolute()) {
            String userDir = System.getProperty("user.dir");
            path = Paths.get(userDir, uploadBasePath);
        }
        return path.normalize().toAbsolutePath();
    }
    
    /**
     * 调用AI分析服务（通过模型配置）
     * 
     * 降级策略：
     * 1. 优先使用模型配置中的模型调用 AI 服务
     * 2. 模型配置不可用时，直接调用 Python AI 服务（不带模型配置）
     * 3. AI 服务完全不可用时，使用本地关键词分析
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callAnalysisService(String requirementText) {
        try {
            String prompt = buildAnalysisPrompt(requirementText);
            String url = aiServiceUrl + "/api/v1/requirement/analyze";
            
            Map<String, Object> request = new HashMap<>();
            request.put("requirement_text", requirementText);
            request.put("prompt", prompt);
            
            // 优先从模型配置中获取最佳模型
            Optional<ModelConfig> modelConfigOpt = modelCallService.getBestModelForTask(TASK_TYPE_REQUIREMENT_ANALYSIS);
            
            if (modelConfigOpt.isPresent()) {
                ModelConfig modelConfig = modelConfigOpt.get();
                log.info("需求分析使用模型配置: modelCode={}, modelName={}, modelType={}",
                        modelConfig.getModelCode(), modelConfig.getModelName(), modelConfig.getModelType());
                
                try {
                    Map<String, Object> response = modelCallService.callWithModel(modelConfig, url, request);
                    
                    if (Boolean.TRUE.equals(response.get("success"))) {
                        Map<String, Object> analysisResult = (Map<String, Object>) response.get("result");
                        if (analysisResult != null) {
                            return analysisResult;
                        }
                    }
                    log.warn("模型配置调用返回失败，尝试降级: {}", response.get("message"));
                } catch (Exception e) {
                    log.warn("模型配置调用异常，尝试降级: {}", e.getMessage());
                }
            } else {
                log.warn("未找到可用的模型配置，尝试直接调用 AI 服务");
            }
            
            // 降级策略1：不带模型配置直接调用 Python AI 服务
            Map<String, Object> fallbackResult = callAIServiceDirectly(url, request);
            if (fallbackResult != null) {
                return fallbackResult;
            }
            
            // 降级策略2：使用本地简单分析
            log.warn("AI 服务不可用，使用本地文本分析");
            return localTextAnalysis(requirementText);
            
        } catch (Exception e) {
            log.error("调用AI分析服务失败，使用本地分析降级: {}", e.getMessage());
            return localTextAnalysis(requirementText);
        }
    }
    
    /**
     * 不带模型配置直接调用 AI 服务（降级策略1）
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callAIServiceDirectly(String url, Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    Map<String, Object> analysisResult = (Map<String, Object>) body.get("result");
                    if (analysisResult != null) {
                        return analysisResult;
                    }
                }
                log.warn("直接调用 AI 服务返回失败: {}", body.get("message"));
            }
            return null;
        } catch (Exception e) {
            log.warn("直接调用 AI 服务异常: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 本地文本分析（降级策略2）
     */
    private Map<String, Object> localTextAnalysis(String requirementText) {
        Map<String, Object> result = new HashMap<>();
        result.put("test_points", extractTestPointsFromText(requirementText));
        result.put("business_rules", extractBusinessRulesFromText(requirementText));
        result.put("key_info", extractKeyInfoFromText(requirementText));
        return result;
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
        List<Map<String, Object>> points = new java.util.ArrayList<>();
        
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
        List<Map<String, Object>> rules = new java.util.ArrayList<>();
        
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
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
