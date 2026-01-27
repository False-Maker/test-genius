package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.ModelCostConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 模型成本配置Repository
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Repository
public interface ModelCostConfigRepository extends JpaRepository<ModelCostConfig, Long>, JpaSpecificationExecutor<ModelCostConfig> {
    
    /**
     * 根据模型代码查询配置
     */
    Optional<ModelCostConfig> findByModelCode(String modelCode);
    
    /**
     * 查询所有启用的配置
     */
    List<ModelCostConfig> findByIsActiveTrue();
}
