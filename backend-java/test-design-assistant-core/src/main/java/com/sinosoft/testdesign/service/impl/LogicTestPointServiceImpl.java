package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.LogicTestPoint;
import com.sinosoft.testdesign.repository.LogicTestPointRepository;
import com.sinosoft.testdesign.service.CacheService;
import com.sinosoft.testdesign.service.LogicTestPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 逻辑测试要点管理服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogicTestPointServiceImpl implements LogicTestPointService {
    
    private final LogicTestPointRepository logicTestPointRepository;
    private final CacheService cacheService;
    
    private static final String POINT_CODE_PREFIX = "LTP";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public LogicTestPoint createLogicTestPoint(LogicTestPoint logicTestPoint) {
        log.info("创建逻辑测试要点: {}", logicTestPoint.getLogicName());
        
        // 数据验证
        validateLogicTestPoint(logicTestPoint, true);
        
        // 自动生成要点编码
        if (!StringUtils.hasText(logicTestPoint.getPointCode())) {
            logicTestPoint.setPointCode(generatePointCode());
        } else {
            // 检查编码是否已存在
            if (logicTestPointRepository.findByPointCode(logicTestPoint.getPointCode()).isPresent()) {
                throw new BusinessException("要点编码已存在: " + logicTestPoint.getPointCode());
            }
        }
        
        // 设置默认值
        if (!StringUtils.hasText(logicTestPoint.getIsActive())) {
            logicTestPoint.setIsActive("1");
        }
        
        log.info("创建逻辑测试要点成功，编码: {}", logicTestPoint.getPointCode());
        return logicTestPointRepository.save(logicTestPoint);
    }
    
    @Override
    @Transactional
    public LogicTestPoint updateLogicTestPoint(Long id, LogicTestPoint logicTestPoint) {
        log.info("更新逻辑测试要点: {}", id);
        
        LogicTestPoint existing = logicTestPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("逻辑测试要点不存在"));
        
        // 数据验证
        validateLogicTestPoint(logicTestPoint, false);
        
        // 不允许修改要点编码
        if (StringUtils.hasText(logicTestPoint.getPointCode()) 
                && !logicTestPoint.getPointCode().equals(existing.getPointCode())) {
            throw new BusinessException("不允许修改要点编码");
        }
        
        // 更新字段
        if (StringUtils.hasText(logicTestPoint.getLogicName())) {
            existing.setLogicName(logicTestPoint.getLogicName());
        }
        if (logicTestPoint.getSpecId() != null) {
            existing.setSpecId(logicTestPoint.getSpecId());
        }
        if (logicTestPoint.getLogicType() != null) {
            existing.setLogicType(logicTestPoint.getLogicType());
        }
        if (logicTestPoint.getLogicDescription() != null) {
            existing.setLogicDescription(logicTestPoint.getLogicDescription());
        }
        if (logicTestPoint.getTestRequirement() != null) {
            existing.setTestRequirement(logicTestPoint.getTestRequirement());
        }
        if (logicTestPoint.getTestMethod() != null) {
            existing.setTestMethod(logicTestPoint.getTestMethod());
        }
        if (logicTestPoint.getTestCases() != null) {
            existing.setTestCases(logicTestPoint.getTestCases());
        }
        if (logicTestPoint.getValidationRules() != null) {
            existing.setValidationRules(logicTestPoint.getValidationRules());
        }
        if (logicTestPoint.getApplicableScenarios() != null) {
            existing.setApplicableScenarios(logicTestPoint.getApplicableScenarios());
        }
        if (logicTestPoint.getDisplayOrder() != null) {
            existing.setDisplayOrder(logicTestPoint.getDisplayOrder());
        }
        
        log.info("更新逻辑测试要点成功，编码: {}", existing.getPointCode());
        return logicTestPointRepository.save(existing);
    }
    
    @Override
    public LogicTestPoint getLogicTestPointById(Long id) {
        return logicTestPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("逻辑测试要点不存在"));
    }
    
    @Override
    public LogicTestPoint getLogicTestPointByCode(String pointCode) {
        return logicTestPointRepository.findByPointCode(pointCode)
                .orElseThrow(() -> new BusinessException("逻辑测试要点不存在: " + pointCode));
    }
    
    @Override
    public Page<LogicTestPoint> getLogicTestPointList(Pageable pageable, String logicName, 
                                                      Long specId, String isActive) {
        return logicTestPointRepository.findWithFilters(
                StringUtils.hasText(logicName) ? logicName : null,
                specId,
                StringUtils.hasText(isActive) ? isActive : null,
                pageable);
    }
    
    @Override
    public List<LogicTestPoint> getLogicTestPointsBySpecId(Long specId) {
        return logicTestPointRepository.findBySpecIdAndIsActiveOrderByDisplayOrderAsc(specId, "1");
    }
    
    @Override
    public List<LogicTestPoint> getLogicTestPointsByType(String logicType) {
        return logicTestPointRepository.findByLogicType(logicType);
    }
    
    @Override
    @Transactional
    public void deleteLogicTestPoint(Long id) {
        log.info("删除逻辑测试要点: {}", id);
        
        LogicTestPoint logicTestPoint = logicTestPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("逻辑测试要点不存在"));
        
        logicTestPointRepository.deleteById(id);
        
        log.info("删除逻辑测试要点成功，编码: {}", logicTestPoint.getPointCode());
    }
    
    @Override
    @Transactional
    public LogicTestPoint updateLogicTestPointStatus(Long id, String isActive) {
        log.info("更新逻辑测试要点状态: {} -> {}", id, isActive);
        
        LogicTestPoint logicTestPoint = logicTestPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("逻辑测试要点不存在"));
        
        if (!"0".equals(isActive) && !"1".equals(isActive)) {
            throw new BusinessException("启用状态必须是0或1");
        }
        
        logicTestPoint.setIsActive(isActive);
        
        log.info("更新逻辑测试要点状态成功，编码: {}, 状态: {}", logicTestPoint.getPointCode(), isActive);
        return logicTestPointRepository.save(logicTestPoint);
    }
    
    /**
     * 生成要点编码
     * 格式：LTP-YYYYMMDD-序号（如 LTP-20240101-001）
     */
    private String generatePointCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = POINT_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天已生成的要点编码（使用数据库查询，避免全量加载）
        List<LogicTestPoint> todayPoints = logicTestPointRepository
                .findByPointCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (LogicTestPoint point : todayPoints) {
            String code = point.getPointCode();
            if (code != null && code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的编码
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        return prefix + String.format("%03d", newSequence);
    }
    
    /**
     * 验证逻辑测试要点数据
     */
    private void validateLogicTestPoint(LogicTestPoint logicTestPoint, boolean isCreate) {
        if (logicTestPoint == null) {
            throw new BusinessException("逻辑测试要点信息不能为空");
        }
        
        // 验证逻辑名称
        if (!StringUtils.hasText(logicTestPoint.getLogicName())) {
            throw new BusinessException("逻辑名称不能为空");
        }
        if (logicTestPoint.getLogicName().length() > 200) {
            throw new BusinessException("逻辑名称长度不能超过200个字符");
        }
        
        // 验证要点编码（创建时）
        if (isCreate && StringUtils.hasText(logicTestPoint.getPointCode())) {
            if (logicTestPoint.getPointCode().length() > 100) {
                throw new BusinessException("要点编码长度不能超过100个字符");
            }
        }
    }
}

