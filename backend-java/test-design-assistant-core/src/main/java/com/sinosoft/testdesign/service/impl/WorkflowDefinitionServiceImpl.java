package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.WorkflowDefinition;
import com.sinosoft.testdesign.entity.WorkflowVersion;
import com.sinosoft.testdesign.repository.WorkflowDefinitionRepository;
import com.sinosoft.testdesign.repository.WorkflowVersionRepository;
import com.sinosoft.testdesign.service.WorkflowDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 工作流定义服务实现
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDefinitionServiceImpl implements WorkflowDefinitionService {
    
    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowVersionRepository versionRepository;
    private final ObjectMapper objectMapper;
    
    private static final String WORKFLOW_CODE_PREFIX = "WF";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public WorkflowDefinition createWorkflow(WorkflowDefinition workflow) {
        // 验证工作流配置
        validateWorkflowConfig(workflow.getWorkflowConfig());
        
        // 自动生成工作流代码（如果未提供）
        if (!StringUtils.hasText(workflow.getWorkflowCode())) {
            workflow.setWorkflowCode(generateWorkflowCode());
        } else {
            // 检查编码是否已存在
            if (workflowRepository.findByWorkflowCode(workflow.getWorkflowCode()).isPresent()) {
                throw new BusinessException("工作流代码已存在: " + workflow.getWorkflowCode());
            }
        }
        
        // 设置默认值
        if (workflow.getVersion() == null) {
            workflow.setVersion(1);
        }
        if (workflow.getIsActive() == null) {
            workflow.setIsActive(true);
        }
        if (workflow.getIsDefault() == null) {
            workflow.setIsDefault(false);
        }
        if (workflow.getExecutionCount() == null) {
            workflow.setExecutionCount(0);
        }
        
        WorkflowDefinition saved = workflowRepository.save(workflow);
        
        // 创建初始版本
        createVersion(saved.getId(), "初始版本");
        
        log.info("创建工作流定义成功，编码: {}", saved.getWorkflowCode());
        return saved;
    }
    
    @Override
    @Transactional
    public WorkflowDefinition updateWorkflow(WorkflowDefinition workflow) {
        if (workflow.getId() == null) {
            throw new BusinessException("工作流ID不能为空");
        }
        
        WorkflowDefinition existing = workflowRepository.findById(workflow.getId())
                .orElseThrow(() -> new BusinessException("工作流定义不存在"));
        
        // 验证工作流配置
        if (workflow.getWorkflowConfig() != null) {
            validateWorkflowConfig(workflow.getWorkflowConfig());
        }
        
        // 不允许修改工作流代码
        if (StringUtils.hasText(workflow.getWorkflowCode()) 
                && !workflow.getWorkflowCode().equals(existing.getWorkflowCode())) {
            throw new BusinessException("不允许修改工作流代码");
        }
        
        // 如果配置发生变化，创建新版本
        if (workflow.getWorkflowConfig() != null 
                && !workflow.getWorkflowConfig().equals(existing.getWorkflowConfig())) {
            // 版本号自增
            Integer newVersion = existing.getVersion() + 1;
            workflow.setVersion(newVersion);
            
            // 创建新版本记录
            createVersion(existing.getId(), "更新版本");
        }
        
        // 更新字段
        if (StringUtils.hasText(workflow.getWorkflowName())) {
            existing.setWorkflowName(workflow.getWorkflowName());
        }
        if (workflow.getWorkflowDescription() != null) {
            existing.setWorkflowDescription(workflow.getWorkflowDescription());
        }
        if (workflow.getWorkflowType() != null) {
            existing.setWorkflowType(workflow.getWorkflowType());
        }
        if (workflow.getWorkflowConfig() != null) {
            existing.setWorkflowConfig(workflow.getWorkflowConfig());
        }
        if (workflow.getVersion() != null) {
            existing.setVersion(workflow.getVersion());
        }
        if (workflow.getIsActive() != null) {
            existing.setIsActive(workflow.getIsActive());
        }
        if (workflow.getIsDefault() != null) {
            existing.setIsDefault(workflow.getIsDefault());
        }
        
        return workflowRepository.save(existing);
    }
    
    @Override
    public Optional<WorkflowDefinition> findById(Long id) {
        return workflowRepository.findById(id);
    }
    
    @Override
    public Optional<WorkflowDefinition> findByCode(String workflowCode) {
        return workflowRepository.findByWorkflowCode(workflowCode);
    }
    
    @Override
    public List<WorkflowDefinition> findAll() {
        return workflowRepository.findAll();
    }
    
    @Override
    public List<WorkflowDefinition> findByType(String workflowType) {
        return workflowRepository.findByWorkflowTypeOrderByCreateTimeDesc(workflowType);
    }
    
    @Override
    public List<WorkflowDefinition> findAllActive() {
        return workflowRepository.findByIsActiveTrueOrderByCreateTimeDesc();
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        WorkflowDefinition workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工作流定义不存在"));
        
        // 检查是否有执行记录（这里可以添加检查逻辑）
        // 如果有关联的执行记录，可以阻止删除或软删除
        
        workflowRepository.deleteById(id);
        log.info("删除工作流定义成功，ID: {}", id);
    }
    
    @Override
    @Transactional
    public WorkflowDefinition toggleActive(Long id, Boolean isActive) {
        WorkflowDefinition workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工作流定义不存在"));
        
        workflow.setIsActive(isActive);
        return workflowRepository.save(workflow);
    }
    
    @Override
    @Transactional
    public WorkflowDefinition setDefault(Long id, Boolean isDefault) {
        WorkflowDefinition workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工作流定义不存在"));
        
        // 如果设置为默认，需要取消同类型的其他默认工作流
        if (isDefault && StringUtils.hasText(workflow.getWorkflowType())) {
            Optional<WorkflowDefinition> existingDefault = workflowRepository
                    .findByWorkflowTypeAndIsDefaultTrueAndIsActiveTrue(workflow.getWorkflowType());
            if (existingDefault.isPresent() && !existingDefault.get().getId().equals(id)) {
                existingDefault.get().setIsDefault(false);
                workflowRepository.save(existingDefault.get());
            }
        }
        
        workflow.setIsDefault(isDefault);
        return workflowRepository.save(workflow);
    }
    
    @Override
    public Map<String, Object> validateWorkflowConfig(String workflowConfig) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // 解析JSON配置
            Map<String, Object> config = objectMapper.readValue(workflowConfig, 
                    new TypeReference<Map<String, Object>>() {});
            
            // 验证必需字段
            if (!config.containsKey("nodes")) {
                errors.add("缺少必需字段: nodes");
            }
            if (!config.containsKey("edges")) {
                errors.add("缺少必需字段: edges");
            }
            
            // 验证节点
            if (config.containsKey("nodes")) {
                Object nodesObj = config.get("nodes");
                if (nodesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> nodes = (List<Map<String, Object>>) nodesObj;
                    
                    if (nodes.isEmpty()) {
                        errors.add("工作流必须至少包含一个节点");
                    }
                    
                    // 检查节点ID唯一性
                    Set<String> nodeIds = new HashSet<>();
                    for (Map<String, Object> node : nodes) {
                        String nodeId = (String) node.get("id");
                        if (nodeId == null || nodeId.isEmpty()) {
                            errors.add("节点缺少id字段");
                        } else if (nodeIds.contains(nodeId)) {
                            errors.add("节点ID重复: " + nodeId);
                        } else {
                            nodeIds.add(nodeId);
                        }
                        
                        // 验证节点类型
                        String nodeType = (String) node.get("type");
                        if (nodeType == null || nodeType.isEmpty()) {
                            errors.add("节点缺少type字段: " + nodeId);
                        }
                    }
                } else {
                    errors.add("nodes字段必须是数组");
                }
            }
            
            // 验证边
            if (config.containsKey("edges")) {
                Object edgesObj = config.get("edges");
                if (edgesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> edges = (List<Map<String, Object>>) edgesObj;
                    
                    // 检查边的source和target是否存在于nodes中
                    if (config.containsKey("nodes")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> nodes = (List<Map<String, Object>>) config.get("nodes");
                        Set<String> nodeIds = new HashSet<>();
                        for (Map<String, Object> node : nodes) {
                            nodeIds.add((String) node.get("id"));
                        }
                        
                        for (Map<String, Object> edge : edges) {
                            String source = (String) edge.get("source");
                            String target = (String) edge.get("target");
                            
                            if (source == null || target == null) {
                                errors.add("边缺少source或target字段");
                            } else {
                                if (!nodeIds.contains(source)) {
                                    errors.add("边的source节点不存在: " + source);
                                }
                                if (!nodeIds.contains(target)) {
                                    errors.add("边的target节点不存在: " + target);
                                }
                            }
                        }
                    }
                } else {
                    errors.add("edges字段必须是数组");
                }
            }
            
        } catch (Exception e) {
            errors.add("工作流配置JSON格式错误: " + e.getMessage());
        }
        
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("warnings", warnings);
        
        return result;
    }
    
    @Override
    @Transactional
    public WorkflowVersion createVersion(Long workflowId, String versionDescription) {
        WorkflowDefinition workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new BusinessException("工作流定义不存在"));
        
        WorkflowVersion version = WorkflowVersion.builder()
                .workflowId(workflowId)
                .workflowCode(workflow.getWorkflowCode())
                .version(workflow.getVersion())
                .workflowConfig(workflow.getWorkflowConfig())
                .versionDescription(versionDescription)
                .creatorId(workflow.getCreatorId())
                .createTime(LocalDateTime.now())
                .build();
        
        return versionRepository.save(version);
    }
    
    @Override
    public List<WorkflowVersion> findVersionsByWorkflowId(Long workflowId) {
        return versionRepository.findByWorkflowIdOrderByVersionDesc(workflowId);
    }
    
    @Override
    public Optional<WorkflowVersion> findVersionByWorkflowIdAndVersion(Long workflowId, Integer version) {
        return versionRepository.findByWorkflowIdAndVersion(workflowId, version);
    }
    
    @Override
    @Transactional
    public WorkflowDefinition rollbackToVersion(Long workflowId, Integer version) {
        WorkflowDefinition workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new BusinessException("工作流定义不存在"));
        
        WorkflowVersion targetVersion = versionRepository.findByWorkflowIdAndVersion(workflowId, version)
                .orElseThrow(() -> new BusinessException("工作流版本不存在"));
        
        // 创建新版本（回滚前的版本）
        createVersion(workflowId, "回滚前版本");
        
        // 恢复配置
        workflow.setWorkflowConfig(targetVersion.getWorkflowConfig());
        workflow.setVersion(workflow.getVersion() + 1);
        
        return workflowRepository.save(workflow);
    }
    
    /**
     * 生成工作流代码
     */
    private String generateWorkflowCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = WORKFLOW_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天的工作流数量
        long count = workflowRepository.findAll().stream()
                .filter(w -> w.getWorkflowCode() != null && w.getWorkflowCode().startsWith(prefix))
                .count();
        
        // 生成序号（3位数字，从001开始）
        String sequence = String.format("%03d", count + 1);
        
        return prefix + sequence;
    }
}
