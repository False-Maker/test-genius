package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.ModelConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 模型配置管理服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface ModelConfigService {
    
    /**
     * 创建模型配置
     */
    ModelConfig createModelConfig(ModelConfig modelConfig);
    
    /**
     * 更新模型配置
     */
    ModelConfig updateModelConfig(Long id, ModelConfig modelConfig);
    
    /**
     * 根据ID查询模型配置
     */
    ModelConfig getModelConfigById(Long id);
    
    /**
     * 根据模型编码查询模型配置
     */
    ModelConfig getModelConfigByCode(String modelCode);
    
    /**
     * 分页查询模型配置列表
     * @param pageable 分页参数
     * @param modelName 模型名称（模糊搜索，可选）
     * @param modelType 模型类型（精确匹配，可选）
     * @param isActive 是否启用（精确匹配，可选）
     */
    Page<ModelConfig> getModelConfigList(Pageable pageable, String modelName, String modelType, String isActive);
    
    /**
     * 查询所有启用的模型配置，按优先级排序
     */
    List<ModelConfig> getActiveModelConfigs();
    
    /**
     * 根据模型类型查询启用的模型配置
     */
    List<ModelConfig> getActiveModelConfigsByType(String modelType);
    
    /**
     * 删除模型配置
     */
    void deleteModelConfig(Long id);
    
    /**
     * 启用/禁用模型配置
     */
    ModelConfig toggleModelConfigStatus(Long id, String isActive);
}

