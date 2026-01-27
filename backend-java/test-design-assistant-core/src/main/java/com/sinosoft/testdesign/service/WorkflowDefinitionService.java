package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.WorkflowDefinition;
import com.sinosoft.testdesign.entity.WorkflowVersion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 工作流定义服务接口
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
public interface WorkflowDefinitionService {
    
    /**
     * 创建工作流定义
     */
    WorkflowDefinition createWorkflow(WorkflowDefinition workflow);
    
    /**
     * 更新工作流定义
     */
    WorkflowDefinition updateWorkflow(WorkflowDefinition workflow);
    
    /**
     * 根据ID查询工作流定义
     */
    Optional<WorkflowDefinition> findById(Long id);
    
    /**
     * 根据工作流代码查询
     */
    Optional<WorkflowDefinition> findByCode(String workflowCode);
    
    /**
     * 查询所有工作流定义
     */
    List<WorkflowDefinition> findAll();
    
    /**
     * 根据工作流类型查询
     */
    List<WorkflowDefinition> findByType(String workflowType);
    
    /**
     * 查询所有启用的工作流
     */
    List<WorkflowDefinition> findAllActive();
    
    /**
     * 删除工作流定义
     */
    void deleteById(Long id);
    
    /**
     * 启用/禁用工作流
     */
    WorkflowDefinition toggleActive(Long id, Boolean isActive);
    
    /**
     * 设置默认工作流
     */
    WorkflowDefinition setDefault(Long id, Boolean isDefault);
    
    /**
     * 验证工作流配置
     */
    Map<String, Object> validateWorkflowConfig(String workflowConfig);
    
    /**
     * 创建工作流版本
     */
    WorkflowVersion createVersion(Long workflowId, String versionDescription);
    
    /**
     * 查询工作流的所有版本
     */
    List<WorkflowVersion> findVersionsByWorkflowId(Long workflowId);
    
    /**
     * 根据版本号查询工作流版本
     */
    Optional<WorkflowVersion> findVersionByWorkflowIdAndVersion(Long workflowId, Integer version);
    
    /**
     * 回滚到指定版本
     */
    WorkflowDefinition rollbackToVersion(Long workflowId, Integer version);
}
