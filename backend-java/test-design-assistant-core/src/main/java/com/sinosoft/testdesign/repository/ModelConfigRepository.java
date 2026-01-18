package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.ModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 模型配置数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface ModelConfigRepository extends JpaRepository<ModelConfig, Long>, 
        JpaSpecificationExecutor<ModelConfig> {
    
    /**
     * 根据模型编码查询
     */
    Optional<ModelConfig> findByModelCode(String modelCode);
    
    /**
     * 查询所有启用的模型配置，按优先级排序
     */
    List<ModelConfig> findByIsActiveOrderByPriorityAsc(String isActive);
    
    /**
     * 根据模型类型查询启用的模型配置
     */
    List<ModelConfig> findByModelTypeAndIsActive(String modelType, String isActive);
    
    /**
     * 根据编码前缀查询模型配置，按ID倒序排列（用于生成编码）
     */
    List<ModelConfig> findByModelCodeStartingWithOrderByIdDesc(String prefix);
}

