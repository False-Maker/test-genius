package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.BatchCaseGenerationRequest;
import com.sinosoft.testdesign.dto.BatchCaseGenerationResult;
import com.sinosoft.testdesign.dto.CaseGenerationRequest;
import com.sinosoft.testdesign.dto.CaseGenerationResult;
import com.sinosoft.testdesign.dto.GenerationTaskDTO;
import com.sinosoft.testdesign.entity.*;
import com.sinosoft.testdesign.enums.CaseStatus;
import com.sinosoft.testdesign.repository.*;
import com.sinosoft.testdesign.metrics.BusinessMetricsCollector;
import com.sinosoft.testdesign.service.IntelligentCaseGenerationService;
import com.sinosoft.testdesign.service.SpecificationCheckService;
import com.sinosoft.testdesign.service.TestCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 智能用例生成服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentCaseGenerationServiceImpl implements IntelligentCaseGenerationService {
    
    private final CaseGenerationTaskRepository taskRepository;
    private final RequirementRepository requirementRepository;
    private final TestLayerRepository layerRepository;
    private final TestMethodRepository methodRepository;
    private final ModelConfigRepository modelConfigRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCaseService testCaseService;
    private final SpecificationCheckService specificationCheckService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final BusinessMetricsCollector metricsCollector;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    private static final String TASK_CODE_PREFIX = "TASK";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public CaseGenerationResult generateTestCases(CaseGenerationRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("创建用例生成任务，需求ID: {}, 测试分层: {}, 测试方法: {}, 模型: {}", 
                request.getRequirementId(), request.getLayerCode(), request.getMethodCode(), request.getModelCode());
        
        // 验证需求是否存在
        TestRequirement requirement = requirementRepository.findById(request.getRequirementId())
                .orElseThrow(() -> new BusinessException("需求不存在"));
        
        // 验证测试分层（如果提供）
        Long layerId = null;
        if (StringUtils.hasText(request.getLayerCode())) {
            TestLayer layer = layerRepository.findByLayerCode(request.getLayerCode())
                    .orElseThrow(() -> new BusinessException("测试分层不存在: " + request.getLayerCode()));
            layerId = layer.getId();
        }
        
        // 验证测试方法（如果提供）
        Long methodId = null;
        if (StringUtils.hasText(request.getMethodCode())) {
            TestDesignMethod method = methodRepository.findByMethodCode(request.getMethodCode())
                    .orElseThrow(() -> new BusinessException("测试方法不存在: " + request.getMethodCode()));
            methodId = method.getId();
        }
        
        // 验证模型配置（如果提供，否则使用默认模型）
        String modelCode = request.getModelCode();
        if (!StringUtils.hasText(modelCode)) {
            // 使用第一个启用的模型作为默认模型
            List<ModelConfig> activeModels = modelConfigRepository.findByIsActiveOrderByPriorityAsc("1");
            if (activeModels.isEmpty()) {
                throw new BusinessException("没有可用的模型配置");
            }
            modelCode = activeModels.get(0).getModelCode();
        } else {
            final String finalModelCode = modelCode;
            ModelConfig config = modelConfigRepository.findByModelCode(modelCode)
                    .orElseThrow(() -> new BusinessException("模型配置不存在: " + finalModelCode));
        }
        
        // 创建任务（带重试机制处理并发冲突）
        CaseGenerationTask task = createTaskWithRetry(
                request.getRequirementId(),
                layerId,
                methodId,
                request.getTemplateId(),
                modelCode,
                request.getCreatorId()
        );
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("用例生成任务创建成功，任务ID: {}, 任务编码: {}, 耗时: {}ms", 
                task.getId(), task.getTaskCode(), elapsedTime);
        
        // 记录指标：任务创建
        metricsCollector.recordCaseGenerationTaskCreated();
        
        // 异步执行任务
        executeGenerationTask(task.getId());
        
        // 返回结果
        CaseGenerationResult result = new CaseGenerationResult();
        result.setTaskId(task.getId());
        result.setStatus("PROCESSING");
        result.setMessage("用例生成任务已提交，正在处理中...");
        
        return result;
    }
    
    @Override
    public GenerationTaskDTO getGenerationTask(Long taskId) {
        CaseGenerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));
        
        return convertToDTO(task);
    }
    
    @Override
    public List<GenerationTaskDTO> getBatchGenerationTasks(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<CaseGenerationTask> tasks = taskRepository.findAllById(taskIds);
        return tasks.stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    @Override
    @Transactional
    public BatchCaseGenerationResult batchGenerateTestCases(BatchCaseGenerationRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("创建批量用例生成任务，需求数量: {}, 测试分层: {}, 测试方法: {}", 
                request.getRequirementIds() != null ? request.getRequirementIds().size() : 0,
                request.getLayerCode(), request.getMethodCode());
        
        // 验证请求参数
        if (request.getRequirementIds() == null || request.getRequirementIds().isEmpty()) {
            throw new BusinessException("需求ID列表不能为空");
        }
        
        // 验证测试分层（如果提供）
        Long layerId = null;
        if (StringUtils.hasText(request.getLayerCode())) {
            TestLayer layer = layerRepository.findByLayerCode(request.getLayerCode())
                    .orElseThrow(() -> new BusinessException("测试分层不存在: " + request.getLayerCode()));
            layerId = layer.getId();
        }
        
        // 验证测试方法（如果提供）
        Long methodId = null;
        if (StringUtils.hasText(request.getMethodCode())) {
            TestDesignMethod method = methodRepository.findByMethodCode(request.getMethodCode())
                    .orElseThrow(() -> new BusinessException("测试方法不存在: " + request.getMethodCode()));
            methodId = method.getId();
        }
        
        // 验证模型配置（如果提供，否则使用默认模型）
        String modelCode = request.getModelCode();
        if (!StringUtils.hasText(modelCode)) {
            List<ModelConfig> activeModels = modelConfigRepository.findByIsActiveOrderByPriorityAsc("1");
            if (activeModels.isEmpty()) {
                throw new BusinessException("没有可用的模型配置");
            }
            modelCode = activeModels.get(0).getModelCode();
        } else {
            final String finalModelCode = modelCode;
            ModelConfig config = modelConfigRepository.findByModelCode(modelCode)
                    .orElseThrow(() -> new BusinessException("模型配置不存在: " + finalModelCode));
        }
        
        // 创建批量任务并异步处理
        List<Long> taskIds = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        
        for (Long requirementId : request.getRequirementIds()) {
            try {
                // 验证需求是否存在
                TestRequirement requirement = requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new BusinessException("需求不存在: " + requirementId));
                
                // 创建单个任务（使用重试机制处理并发冲突）
                CaseGenerationTask task = createTaskWithRetry(
                        requirementId,
                        layerId,
                        methodId,
                        request.getTemplateId(),
                        modelCode,
                        request.getCreatorId()
                );
                
                taskIds.add(task.getId());
                
                // 异步执行任务
                executeGenerationTask(task.getId());
                successCount++;
                
            } catch (Exception e) {
                log.error("创建批量任务失败，需求ID: {}, 错误: {}", requirementId, e.getMessage(), e);
                failCount++;
            }
        }
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("批量用例生成任务创建完成，总需求数: {}, 成功: {}, 失败: {}, 耗时: {}ms", 
                request.getRequirementIds().size(), successCount, failCount, elapsedTime);
        
        // 返回结果
        BatchCaseGenerationResult result = new BatchCaseGenerationResult();
        result.setTotalTasks(request.getRequirementIds().size());
        result.setSuccessTasks(successCount);
        result.setFailTasks(failCount);
        result.setTaskIds(taskIds);
        result.setStatus("PROCESSING");
        result.setMessage(String.format("批量任务已提交，共%d个需求，成功创建%d个任务，失败%d个", 
                request.getRequirementIds().size(), successCount, failCount));
        
        return result;
    }
    
    @Override
    @Async
    @Transactional
    public void executeGenerationTask(Long taskId) {
        long startTime = System.currentTimeMillis();
        log.info("开始执行用例生成任务，任务ID: {}", taskId);
        
        CaseGenerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));
        
        try {
            // 更新任务状态为处理中
            task.setTaskStatus("PROCESSING");
            task.setProgress(10);
            task = taskRepository.save(task);
            
            // 获取需求信息
            TestRequirement requirement = requirementRepository.findById(task.getRequirementId())
                    .orElseThrow(() -> new BusinessException("需求不存在"));
            
            // 获取分层和方法编码
            String layerCode = null;
            if (task.getLayerId() != null) {
                layerCode = layerRepository.findById(task.getLayerId())
                        .map(TestLayer::getLayerCode)
                        .orElse(null);
            }
            
            String methodCode = null;
            if (task.getMethodId() != null) {
                methodCode = methodRepository.findById(task.getMethodId())
                        .map(TestDesignMethod::getMethodCode)
                        .orElse(null);
            }
            
            if (layerCode == null || methodCode == null) {
                throw new BusinessException("测试分层或测试方法不能为空");
            }
            
            // 获取需求文本，如果需求描述为空，使用需求名称作为备选
            String requirementText = requirement.getRequirementDescription();
            if (requirementText == null || requirementText.trim().isEmpty()) {
                if (requirement.getRequirementName() != null && !requirement.getRequirementName().trim().isEmpty()) {
                    requirementText = requirement.getRequirementName();
                    log.warn("需求ID {} 的需求描述为空，使用需求名称作为备选: {}", requirement.getId(), requirementText);
                } else {
                    throw new BusinessException("需求描述和需求名称不能同时为空，无法生成用例");
                }
            }
            
            // 构建Python服务请求
            Map<String, Object> pythonRequest = new HashMap<>();
            pythonRequest.put("requirement_id", requirement.getId());
            pythonRequest.put("requirement_text", requirementText);
            pythonRequest.put("layer_code", layerCode);
            pythonRequest.put("method_code", methodCode);
            pythonRequest.put("model_code", task.getModelCode());
            if (task.getTemplateId() != null) {
                pythonRequest.put("template_id", task.getTemplateId());
            }
            
            log.info("调用Python服务生成用例，请求: {}", pythonRequest);
            
            // 调用Python服务
            task.setProgress(30);
            task = taskRepository.save(task);
            
            String url = aiServiceUrl + "/api/v1/case/generate";
            Map<String, Object> response = restTemplate.postForObject(url, pythonRequest, Map.class);
            
            if (response == null) {
                throw new BusinessException("Python服务返回空响应");
            }
            
            log.info("Python服务响应: {}", response);
            
            // 解析响应
            task.setProgress(60);
            task = taskRepository.save(task);
            
            String status = (String) response.get("status");
            if (!"success".equals(status)) {
                Object messageObj = response.get("message");
                String errorMessage = messageObj != null ? messageObj.toString() : "未知错误";
                throw new BusinessException("用例生成失败: " + errorMessage);
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cases = (List<Map<String, Object>>) response.get("cases");
            if (cases == null || cases.isEmpty()) {
                throw new BusinessException("未生成任何用例");
            }
            
            // 保存用例到数据库
            task.setProgress(80);
            task = taskRepository.save(task);
            
            int successCount = 0;
            int failCount = 0;
            List<Map<String, Object>> savedCases = new ArrayList<>();
            
            for (Map<String, Object> caseData : cases) {
                try {
                    TestCase testCase = convertToTestCase(caseData, requirement.getId(), 
                            task.getLayerId(), task.getMethodId());
                    
                    // 在保存用例之前应用规约（注入规约内容）
                    try {
                        SpecificationCheckService.SpecificationInjectionResult injectionResult = 
                                specificationCheckService.injectSpecification(testCase, null);
                        if (injectionResult.getEnhancedTestCase() != null) {
                            TestCase enhanced = injectionResult.getEnhancedTestCase();
                            // 使用增强后的用例内容
                            testCase.setPreCondition(enhanced.getPreCondition());
                            testCase.setTestStep(enhanced.getTestStep());
                            testCase.setExpectedResult(enhanced.getExpectedResult());
                            log.debug("已应用规约到用例: {}, 注入内容数: {}", 
                                    testCase.getCaseCode(), injectionResult.getInjectedContents().size());
                        }
                    } catch (Exception e) {
                        // 规约注入失败不影响用例生成，仅记录日志
                        log.warn("规约注入失败，继续保存用例: {}", e.getMessage());
                    }
                    
                    testCase = testCaseService.createTestCase(testCase);
                    savedCases.add(createCaseMap(testCase));
                    successCount++;
                } catch (Exception e) {
                    log.error("保存用例失败: {}", e.getMessage(), e);
                    failCount++;
                }
            }
            
            // 更新任务状态
            task.setTaskStatus("SUCCESS");
            task.setProgress(100);
            task.setTotalCases(cases.size());
            task.setSuccessCases(successCount);
            task.setFailCases(failCount);
            task.setCompleteTime(LocalDateTime.now());
            task.setResultData(objectMapper.writeValueAsString(savedCases));
            task = taskRepository.save(task);
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            double durationSeconds = elapsedTime / 1000.0;
            log.info("用例生成任务完成，任务ID: {}, 成功: {}, 失败: {}, 总耗时: {}ms", 
                    taskId, successCount, failCount, elapsedTime);
            
            // 记录指标：任务成功
            metricsCollector.recordCaseGenerationTaskSuccess(durationSeconds);
            
        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            double durationSeconds = elapsedTime / 1000.0;
            log.error("执行用例生成任务失败，任务ID: {}, 耗时: {}ms", taskId, elapsedTime, e);
            
            task.setTaskStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setCompleteTime(LocalDateTime.now());
            taskRepository.save(task);
            
            // 记录指标：任务失败
            metricsCollector.recordCaseGenerationTaskFailed(durationSeconds, e.getClass().getSimpleName());
        }
    }
    
    /**
     * 将Python服务返回的用例数据转换为TestCase实体
     */
    private TestCase convertToTestCase(Map<String, Object> caseData, Long requirementId, 
                                       Long layerId, Long methodId) {
        TestCase testCase = new TestCase();
        testCase.setRequirementId(requirementId);
        testCase.setLayerId(layerId);
        testCase.setMethodId(methodId);
        testCase.setCaseName(getStringValue(caseData, "case_name", "未命名用例"));
        testCase.setCaseType(getStringValue(caseData, "case_type", "正常"));
        testCase.setCasePriority(getStringValue(caseData, "case_priority", "中"));
        testCase.setPreCondition(getStringValue(caseData, "pre_condition", null));
        testCase.setTestStep(getStringValue(caseData, "test_step", null));
        testCase.setExpectedResult(getStringValue(caseData, "expected_result", null));
        testCase.setCaseStatus(CaseStatus.DRAFT.name());
        return testCase;
    }
    
    /**
     * 创建用例Map（用于结果数据）
     */
    private Map<String, Object> createCaseMap(TestCase testCase) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", testCase.getId());
        map.put("caseCode", testCase.getCaseCode());
        map.put("caseName", testCase.getCaseName());
        map.put("caseType", testCase.getCaseType());
        map.put("casePriority", testCase.getCasePriority());
        return map;
    }
    
    /**
     * 从Map中获取String值
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }
    
    /**
     * 将实体转换为DTO
     */
    private GenerationTaskDTO convertToDTO(CaseGenerationTask task) {
        GenerationTaskDTO dto = new GenerationTaskDTO();
        dto.setId(task.getId());
        dto.setTaskCode(task.getTaskCode());
        dto.setRequirementId(task.getRequirementId());
        dto.setStatus(task.getTaskStatus());
        dto.setProgress(task.getProgress());
        dto.setTotalCases(task.getTotalCases());
        dto.setSuccessCases(task.getSuccessCases());
        dto.setFailCases(task.getFailCases());
        dto.setErrorMessage(task.getErrorMessage());
        dto.setCreateTime(task.getCreateTime());
        dto.setUpdateTime(task.getUpdateTime());
        dto.setCompleteTime(task.getCompleteTime());
        
        // 解析结果数据
        if (StringUtils.hasText(task.getResultData())) {
            try {
                dto.setResult(objectMapper.readValue(task.getResultData(), 
                        new TypeReference<List<Map<String, Object>>>() {}));
            } catch (Exception e) {
                log.warn("解析任务结果数据失败: {}", e.getMessage());
            }
        }
        
        return dto;
    }
    
    /**
     * 生成任务编码
     * 格式：TASK-YYYYMMDD-序号（如 TASK-20240101-001）
     * 优化：使用数据库查询替代全量查询，提高性能
     * 注意：在高并发场景下可能存在竞态条件，通过 createTaskWithRetry 方法的重试机制处理
     */
    private String generateTaskCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = TASK_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天已生成的任务编码（使用数据库查询，避免全量加载）
        // 使用数据库索引优化查询性能
        List<CaseGenerationTask> todayTasks = taskRepository.findByTaskCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (CaseGenerationTask t : todayTasks) {
            String code = t.getTaskCode();
            if (code != null && code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的编码
                    log.warn("任务编码格式不正确: {}", code);
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        String taskCode = prefix + String.format("%03d", newSequence);
        log.debug("生成任务编码: {}", taskCode);
        return taskCode;
    }
    
    /**
     * 创建任务（带重试机制处理并发冲突）
     * 当遇到唯一约束冲突时，重新生成编码并重试
     */
    private CaseGenerationTask createTaskWithRetry(
            Long requirementId,
            Long layerId,
            Long methodId,
            Long templateId,
            String modelCode,
            Long creatorId
    ) {
        int maxRetries = 5;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                CaseGenerationTask task = new CaseGenerationTask();
                task.setTaskCode(generateTaskCode());
                task.setRequirementId(requirementId);
                task.setLayerId(layerId);
                task.setMethodId(methodId);
                task.setTemplateId(templateId);
                task.setModelCode(modelCode);
                task.setTaskStatus("PENDING");
                task.setProgress(0);
                task.setCreatorId(creatorId);
                
                return taskRepository.save(task);
                
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // 捕获唯一约束冲突异常
                retryCount++;
                if (retryCount >= maxRetries) {
                    log.error("创建任务失败，已重试{}次，任务编码冲突", maxRetries, e);
                    throw new BusinessException("创建任务失败，任务编码冲突，请稍后重试");
                }
                // 等待一小段时间后重试（指数退避）
                try {
                    Thread.sleep(50 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException("创建任务被中断");
                }
                log.warn("任务编码冲突，重试第{}次", retryCount);
            } catch (Exception e) {
                // 其他异常直接抛出
                log.error("创建任务失败", e);
                throw new BusinessException("创建任务失败: " + e.getMessage());
            }
        }
        
        throw new BusinessException("创建任务失败");
    }
}

