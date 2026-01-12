package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.ModelConfig;
import com.sinosoft.testdesign.repository.ModelConfigRepository;
import com.sinosoft.testdesign.service.ModelConfigService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型配置管理服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl implements ModelConfigService {
    
    private final ModelConfigRepository modelConfigRepository;
    
    @Override
    @Transactional
    public ModelConfig createModelConfig(ModelConfig modelConfig) {
        log.info("创建模型配置: {}", modelConfig.getModelName());
        
        // 数据验证
        validateModelConfig(modelConfig, true);
        
        // 检查模型编码是否已存在
        if (StringUtils.hasText(modelConfig.getModelCode())) {
            if (modelConfigRepository.findByModelCode(modelConfig.getModelCode()).isPresent()) {
                throw new BusinessException("模型编码已存在: " + modelConfig.getModelCode());
            }
        } else {
            throw new BusinessException("模型编码不能为空");
        }
        
        // 设置默认值
        if (!StringUtils.hasText(modelConfig.getIsActive())) {
            modelConfig.setIsActive("1");
        }
        
        if (modelConfig.getPriority() == null) {
            // 如果没有设置优先级，设置为当前最大优先级+1
            List<ModelConfig> allConfigs = modelConfigRepository.findAll();
            int maxPriority = allConfigs.stream()
                    .mapToInt(c -> c.getPriority() != null ? c.getPriority() : 0)
                    .max()
                    .orElse(0);
            modelConfig.setPriority(maxPriority + 1);
        }
        
        log.info("创建模型配置成功，编码: {}", modelConfig.getModelCode());
        return modelConfigRepository.save(modelConfig);
    }
    
    @Override
    @Transactional
    public ModelConfig updateModelConfig(Long id, ModelConfig modelConfig) {
        log.info("更新模型配置: {}", id);
        
        ModelConfig existing = modelConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模型配置不存在"));
        
        // 数据验证
        validateModelConfig(modelConfig, false);
        
        // 不允许修改模型编码
        if (StringUtils.hasText(modelConfig.getModelCode()) 
                && !modelConfig.getModelCode().equals(existing.getModelCode())) {
            throw new BusinessException("不允许修改模型编码");
        }
        
        // 更新字段
        if (StringUtils.hasText(modelConfig.getModelName())) {
            existing.setModelName(modelConfig.getModelName());
        }
        if (StringUtils.hasText(modelConfig.getModelType())) {
            existing.setModelType(modelConfig.getModelType());
        }
        if (StringUtils.hasText(modelConfig.getApiEndpoint())) {
            existing.setApiEndpoint(modelConfig.getApiEndpoint());
        }
        if (StringUtils.hasText(modelConfig.getApiKey())) {
            existing.setApiKey(modelConfig.getApiKey());
        }
        if (StringUtils.hasText(modelConfig.getModelVersion())) {
            existing.setModelVersion(modelConfig.getModelVersion());
        }
        if (modelConfig.getMaxTokens() != null) {
            existing.setMaxTokens(modelConfig.getMaxTokens());
        }
        if (modelConfig.getTemperature() != null) {
            existing.setTemperature(modelConfig.getTemperature());
        }
        if (StringUtils.hasText(modelConfig.getIsActive())) {
            existing.setIsActive(modelConfig.getIsActive());
        }
        if (modelConfig.getPriority() != null) {
            existing.setPriority(modelConfig.getPriority());
        }
        if (modelConfig.getDailyLimit() != null) {
            existing.setDailyLimit(modelConfig.getDailyLimit());
        }
        
        log.info("更新模型配置成功，编码: {}", existing.getModelCode());
        return modelConfigRepository.save(existing);
    }
    
    @Override
    public ModelConfig getModelConfigById(Long id) {
        return modelConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模型配置不存在"));
    }
    
    @Override
    public ModelConfig getModelConfigByCode(String modelCode) {
        return modelConfigRepository.findByModelCode(modelCode)
                .orElseThrow(() -> new BusinessException("模型配置不存在: " + modelCode));
    }
    
    @Override
    public Page<ModelConfig> getModelConfigList(Pageable pageable, String modelName, String modelType, String isActive) {
        Specification<ModelConfig> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(modelName)) {
                predicates.add(cb.like(cb.lower(root.get("modelName")), 
                        "%" + modelName.toLowerCase() + "%"));
            }
            
            if (StringUtils.hasText(modelType)) {
                predicates.add(cb.equal(root.get("modelType"), modelType));
            }
            
            if (StringUtils.hasText(isActive)) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return modelConfigRepository.findAll(spec, pageable);
    }
    
    @Override
    public List<ModelConfig> getActiveModelConfigs() {
        return modelConfigRepository.findByIsActiveOrderByPriorityAsc("1");
    }
    
    @Override
    public List<ModelConfig> getActiveModelConfigsByType(String modelType) {
        return modelConfigRepository.findByModelTypeAndIsActive(modelType, "1");
    }
    
    @Override
    @Transactional
    public void deleteModelConfig(Long id) {
        log.info("删除模型配置: {}", id);
        
        ModelConfig modelConfig = modelConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模型配置不存在"));
        
        modelConfigRepository.delete(modelConfig);
        log.info("删除模型配置成功，编码: {}", modelConfig.getModelCode());
    }
    
    @Override
    @Transactional
    public ModelConfig toggleModelConfigStatus(Long id, String isActive) {
        log.info("更新模型配置状态: {}, isActive: {}", id, isActive);
        
        ModelConfig modelConfig = modelConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模型配置不存在"));
        
        if (!"0".equals(isActive) && !"1".equals(isActive)) {
            throw new BusinessException("状态值无效，必须为0或1");
        }
        
        modelConfig.setIsActive(isActive);
        log.info("更新模型配置状态成功，编码: {}, 状态: {}", modelConfig.getModelCode(), isActive);
        return modelConfigRepository.save(modelConfig);
    }
    
    /**
     * 验证模型配置数据
     */
    private void validateModelConfig(ModelConfig modelConfig, boolean isCreate) {
        if (isCreate && !StringUtils.hasText(modelConfig.getModelCode())) {
            throw new BusinessException("模型编码不能为空");
        }
        
        if (!StringUtils.hasText(modelConfig.getModelName())) {
            throw new BusinessException("模型名称不能为空");
        }
        
        if (StringUtils.hasText(modelConfig.getApiEndpoint())) {
            // 简单的URL格式验证
            if (!modelConfig.getApiEndpoint().startsWith("http://") 
                    && !modelConfig.getApiEndpoint().startsWith("https://")) {
                throw new BusinessException("API端点格式不正确，必须以http://或https://开头");
            }
        }
        
        if (modelConfig.getMaxTokens() != null && modelConfig.getMaxTokens() <= 0) {
            throw new BusinessException("最大Token数必须大于0");
        }
        
        if (modelConfig.getTemperature() != null) {
            if (modelConfig.getTemperature().compareTo(java.math.BigDecimal.ZERO) < 0 
                    || modelConfig.getTemperature().compareTo(new java.math.BigDecimal("2")) > 0) {
                throw new BusinessException("温度参数必须在0-2之间");
            }
        }
        
        if (modelConfig.getPriority() != null && modelConfig.getPriority() < 0) {
            throw new BusinessException("优先级必须大于等于0");
        }
        
        if (modelConfig.getDailyLimit() != null && modelConfig.getDailyLimit() < 0) {
            throw new BusinessException("每日调用限制必须大于等于0");
        }
    }
}

