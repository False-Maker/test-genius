package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.ModelCostConfig;
import com.sinosoft.testdesign.repository.ModelCostConfigRepository;
import com.sinosoft.testdesign.service.ModelCostConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 模型成本配置服务实现
 * 
 * @author sinosoft
 * @date 2026-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelCostConfigServiceImpl implements ModelCostConfigService {
    
    private final ModelCostConfigRepository repository;
    
    @Override
    @Transactional
    public ModelCostConfig createCostConfig(ModelCostConfig config) {
        // 检查模型代码是否已存在
        Optional<ModelCostConfig> existing = repository.findByModelCode(config.getModelCode());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("模型代码已存在: " + config.getModelCode());
        }
        
        return repository.save(config);
    }
    
    @Override
    @Transactional
    public ModelCostConfig updateCostConfig(ModelCostConfig config) {
        if (config.getId() == null) {
            throw new IllegalArgumentException("成本配置ID不能为空");
        }
        
        Optional<ModelCostConfig> existing = repository.findById(config.getId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("成本配置不存在: " + config.getId());
        }
        
        return repository.save(config);
    }
    
    @Override
    public Optional<ModelCostConfig> findById(Long id) {
        return repository.findById(id);
    }
    
    @Override
    public Optional<ModelCostConfig> findByModelCode(String modelCode) {
        return repository.findByModelCode(modelCode);
    }
    
    @Override
    public List<ModelCostConfig> findAllActive() {
        return repository.findByIsActiveTrue();
    }
    
    @Override
    public List<ModelCostConfig> findAll() {
        return repository.findAll();
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    @Override
    @Transactional
    public ModelCostConfig toggleActive(Long id, Boolean isActive) {
        Optional<ModelCostConfig> config = repository.findById(id);
        if (config.isEmpty()) {
            throw new IllegalArgumentException("成本配置不存在: " + id);
        }
        
        ModelCostConfig costConfig = config.get();
        costConfig.setIsActive(isActive);
        return repository.save(costConfig);
    }
}
