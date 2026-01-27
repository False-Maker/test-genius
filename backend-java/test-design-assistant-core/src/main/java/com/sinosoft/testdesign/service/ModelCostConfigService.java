package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.ModelCostConfig;

import java.util.List;
import java.util.Optional;

/**
 * 模型成本配置服务接口
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
public interface ModelCostConfigService {
    
    /**
     * 创建成本配置
     */
    ModelCostConfig createCostConfig(ModelCostConfig config);
    
    /**
     * 更新成本配置
     */
    ModelCostConfig updateCostConfig(ModelCostConfig config);
    
    /**
     * 根据ID查询成本配置
     */
    Optional<ModelCostConfig> findById(Long id);
    
    /**
     * 根据模型代码查询成本配置
     */
    Optional<ModelCostConfig> findByModelCode(String modelCode);
    
    /**
     * 查询所有启用的成本配置
     */
    List<ModelCostConfig> findAllActive();
    
    /**
     * 查询所有成本配置
     */
    List<ModelCostConfig> findAll();
    
    /**
     * 删除成本配置
     */
    void deleteById(Long id);
    
    /**
     * 启用/禁用成本配置
     */
    ModelCostConfig toggleActive(Long id, Boolean isActive);
}
