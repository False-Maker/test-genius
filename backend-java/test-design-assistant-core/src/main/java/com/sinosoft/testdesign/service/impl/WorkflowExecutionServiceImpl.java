package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.entity.WorkflowDefinition;
import com.sinosoft.testdesign.entity.WorkflowExecution;
import com.sinosoft.testdesign.repository.WorkflowDefinitionRepository;
import com.sinosoft.testdesign.repository.WorkflowExecutionRepository;
import com.sinosoft.testdesign.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 工作流执行服务实现
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {
    
    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    @Override
    @Transactional
    public WorkflowExecution executeWorkflow(
            Long workflowId,
            Map<String, Object> inputData,
            Long creatorId,
            String creatorName) {
        
        // 查询工作流定义
        WorkflowDefinition workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("工作流定义不存在: " + workflowId));
        
        if (!workflow.getIsActive()) {
            throw new IllegalArgumentException("工作流未启用: " + workflow.getWorkflowCode());
        }
        
        // 生成执行ID
        String executionId = "EXEC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        // 创建执行记录
        String inputDataJson;
        try {
            inputDataJson = objectMapper.writeValueAsString(inputData);
        } catch (Exception e) {
            throw new RuntimeException("序列化输入数据失败", e);
        }
        
        WorkflowExecution execution = WorkflowExecution.builder()
                .executionId(executionId)
                .workflowId(workflowId)
                .workflowCode(workflow.getWorkflowCode())
                .workflowVersion(workflow.getVersion())
                .executionType("API")
                .inputData(inputDataJson)
                .status("PENDING")
                .progress(0)
                .creatorId(creatorId)
                .creatorName(creatorName)
                .createTime(LocalDateTime.now())
                .build();
        
        execution = executionRepository.save(execution);
        
        // 异步执行工作流
        executeWorkflowAsync(execution, workflow, inputData);
        
        return execution;
    }
    
    @Async
    @Transactional
    public void executeWorkflowAsync(
            WorkflowExecution execution,
            WorkflowDefinition workflow,
            Map<String, Object> inputData) {
        
        long startTime = System.currentTimeMillis();
        execution.setStatus("RUNNING");
        execution.setStartTime(LocalDateTime.now());
        executionRepository.save(execution);
        
        try {
            // 调用Python服务执行工作流
            String url = aiServiceUrl + "/api/v1/workflow/execute";
            Map<String, Object> request = new HashMap<>();
            request.put("workflow_config", workflow.getWorkflowConfig());
            request.put("input_data", inputData);
            request.put("workflow_id", workflow.getId());
            request.put("workflow_code", workflow.getWorkflowCode());
            request.put("workflow_version", workflow.getVersion());
            
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            if (response == null) {
                throw new RuntimeException("Python服务返回空响应");
            }
            
            String status = (String) response.get("status");
            long duration = System.currentTimeMillis() - startTime;
            
            execution.setStatus("success".equals(status) ? "SUCCESS" : "FAILED");
            execution.setProgress(100);
            execution.setEndTime(LocalDateTime.now());
            execution.setDuration((int) duration);
            
            if (response.containsKey("output")) {
                try {
                    execution.setOutputData(objectMapper.writeValueAsString(response.get("output")));
                } catch (Exception e) {
                    log.warn("序列化输出数据失败", e);
                }
            }
            
            if (response.containsKey("error")) {
                execution.setErrorMessage((String) response.get("error"));
                execution.setErrorNodeId((String) response.get("error_node"));
            }
            
            executionRepository.save(execution);
            
            // 更新工作流定义的执行统计
            workflow.setLastExecutionTime(LocalDateTime.now());
            workflow.setExecutionCount(workflow.getExecutionCount() + 1);
            workflowRepository.save(workflow);
            
            log.info("工作流执行完成: {}, 状态: {}, 耗时: {}ms", 
                    execution.getExecutionId(), execution.getStatus(), duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            execution.setStatus("FAILED");
            execution.setErrorMessage(e.getMessage());
            execution.setEndTime(LocalDateTime.now());
            execution.setDuration((int) duration);
            executionRepository.save(execution);
            
            log.error("工作流执行失败: {}", execution.getExecutionId(), e);
        }
    }
    
    @Override
    public Optional<WorkflowExecution> findByExecutionId(String executionId) {
        return executionRepository.findByExecutionId(executionId);
    }
    
    @Override
    public List<WorkflowExecution> findByWorkflowId(Long workflowId) {
        return executionRepository.findByWorkflowIdOrderByCreateTimeDesc(workflowId);
    }
    
    @Override
    public List<WorkflowExecution> findByWorkflowCode(String workflowCode) {
        return executionRepository.findByWorkflowCodeOrderByCreateTimeDesc(workflowCode);
    }
    
    @Override
    public List<WorkflowExecution> findByStatus(String status) {
        return executionRepository.findByStatusOrderByCreateTimeDesc(status);
    }
    
    @Override
    public Page<WorkflowExecution> findAll(Pageable pageable) {
        return executionRepository.findAll(pageable);
    }
    
    @Override
    @Transactional
    public WorkflowExecution cancelExecution(String executionId) {
        WorkflowExecution execution = executionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new IllegalArgumentException("执行记录不存在: " + executionId));
        
        if (!"PENDING".equals(execution.getStatus()) && !"RUNNING".equals(execution.getStatus())) {
            throw new IllegalArgumentException("只能取消PENDING或RUNNING状态的执行");
        }
        
        execution.setStatus("CANCELLED");
        execution.setEndTime(LocalDateTime.now());
        
        return executionRepository.save(execution);
    }
    
    @Override
    public Map<String, Object> getExecutionProgress(String executionId) {
        WorkflowExecution execution = executionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new IllegalArgumentException("执行记录不存在: " + executionId));
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("executionId", execution.getExecutionId());
        progress.put("status", execution.getStatus());
        progress.put("progress", execution.getProgress());
        progress.put("currentNodeId", execution.getCurrentNodeId());
        progress.put("startTime", execution.getStartTime());
        progress.put("duration", execution.getDuration());
        
        return progress;
    }
}
