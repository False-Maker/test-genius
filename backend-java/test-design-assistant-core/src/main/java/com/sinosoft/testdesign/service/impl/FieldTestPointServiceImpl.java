package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.FieldTestPoint;
import com.sinosoft.testdesign.repository.FieldTestPointRepository;
import com.sinosoft.testdesign.service.CacheService;
import com.sinosoft.testdesign.service.FieldTestPointService;
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
 * 字段测试要点管理服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldTestPointServiceImpl implements FieldTestPointService {
    
    private final FieldTestPointRepository fieldTestPointRepository;
    private final CacheService cacheService;
    
    private static final String POINT_CODE_PREFIX = "FTP";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public FieldTestPoint createFieldTestPoint(FieldTestPoint fieldTestPoint) {
        log.info("创建字段测试要点: {}", fieldTestPoint.getFieldName());
        
        // 数据验证
        validateFieldTestPoint(fieldTestPoint, true);
        
        // 自动生成要点编码
        if (!StringUtils.hasText(fieldTestPoint.getPointCode())) {
            fieldTestPoint.setPointCode(generatePointCode());
        } else {
            // 检查编码是否已存在
            if (fieldTestPointRepository.findByPointCode(fieldTestPoint.getPointCode()).isPresent()) {
                throw new BusinessException("要点编码已存在: " + fieldTestPoint.getPointCode());
            }
        }
        
        // 设置默认值
        if (!StringUtils.hasText(fieldTestPoint.getIsActive())) {
            fieldTestPoint.setIsActive("1");
        }
        if (!StringUtils.hasText(fieldTestPoint.getIsRequired())) {
            fieldTestPoint.setIsRequired("0");
        }
        
        log.info("创建字段测试要点成功，编码: {}", fieldTestPoint.getPointCode());
        return fieldTestPointRepository.save(fieldTestPoint);
    }
    
    @Override
    @Transactional
    public FieldTestPoint updateFieldTestPoint(Long id, FieldTestPoint fieldTestPoint) {
        log.info("更新字段测试要点: {}", id);
        
        FieldTestPoint existing = fieldTestPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字段测试要点不存在"));
        
        // 数据验证
        validateFieldTestPoint(fieldTestPoint, false);
        
        // 不允许修改要点编码
        if (StringUtils.hasText(fieldTestPoint.getPointCode()) 
                && !fieldTestPoint.getPointCode().equals(existing.getPointCode())) {
            throw new BusinessException("不允许修改要点编码");
        }
        
        // 更新字段
        if (StringUtils.hasText(fieldTestPoint.getFieldName())) {
            existing.setFieldName(fieldTestPoint.getFieldName());
        }
        if (fieldTestPoint.getSpecId() != null) {
            existing.setSpecId(fieldTestPoint.getSpecId());
        }
        if (fieldTestPoint.getFieldType() != null) {
            existing.setFieldType(fieldTestPoint.getFieldType());
        }
        if (fieldTestPoint.getTestRequirement() != null) {
            existing.setTestRequirement(fieldTestPoint.getTestRequirement());
        }
        if (fieldTestPoint.getTestMethod() != null) {
            existing.setTestMethod(fieldTestPoint.getTestMethod());
        }
        if (fieldTestPoint.getTestCases() != null) {
            existing.setTestCases(fieldTestPoint.getTestCases());
        }
        if (fieldTestPoint.getValidationRules() != null) {
            existing.setValidationRules(fieldTestPoint.getValidationRules());
        }
        if (StringUtils.hasText(fieldTestPoint.getIsRequired())) {
            existing.setIsRequired(fieldTestPoint.getIsRequired());
        }
        if (fieldTestPoint.getDisplayOrder() != null) {
            existing.setDisplayOrder(fieldTestPoint.getDisplayOrder());
        }
        
        log.info("更新字段测试要点成功，编码: {}", existing.getPointCode());
        return fieldTestPointRepository.save(existing);
    }
    
    @Override
    public FieldTestPoint getFieldTestPointById(Long id) {
        return fieldTestPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字段测试要点不存在"));
    }
    
    @Override
    public FieldTestPoint getFieldTestPointByCode(String pointCode) {
        return fieldTestPointRepository.findByPointCode(pointCode)
                .orElseThrow(() -> new BusinessException("字段测试要点不存在: " + pointCode));
    }
    
    @Override
    public Page<FieldTestPoint> getFieldTestPointList(Pageable pageable, String fieldName, 
                                                      Long specId, String isActive) {
        return fieldTestPointRepository.findWithFilters(
                StringUtils.hasText(fieldName) ? fieldName : null,
                specId,
                StringUtils.hasText(isActive) ? isActive : null,
                pageable);
    }
    
    @Override
    public List<FieldTestPoint> getFieldTestPointsBySpecId(Long specId) {
        return fieldTestPointRepository.findBySpecIdAndIsActiveOrderByDisplayOrderAsc(specId, "1");
    }
    
    @Override
    @Transactional
    public void deleteFieldTestPoint(Long id) {
        log.info("删除字段测试要点: {}", id);
        
        FieldTestPoint fieldTestPoint = fieldTestPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字段测试要点不存在"));
        
        fieldTestPointRepository.deleteById(id);
        
        log.info("删除字段测试要点成功，编码: {}", fieldTestPoint.getPointCode());
    }
    
    @Override
    @Transactional
    public FieldTestPoint updateFieldTestPointStatus(Long id, String isActive) {
        log.info("更新字段测试要点状态: {} -> {}", id, isActive);
        
        FieldTestPoint fieldTestPoint = fieldTestPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("字段测试要点不存在"));
        
        if (!"0".equals(isActive) && !"1".equals(isActive)) {
            throw new BusinessException("启用状态必须是0或1");
        }
        
        fieldTestPoint.setIsActive(isActive);
        
        log.info("更新字段测试要点状态成功，编码: {}, 状态: {}", fieldTestPoint.getPointCode(), isActive);
        return fieldTestPointRepository.save(fieldTestPoint);
    }
    
    /**
     * 生成要点编码
     * 格式：FTP-YYYYMMDD-序号（如 FTP-20240101-001）
     */
    private String generatePointCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = POINT_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天已生成的要点编码（使用数据库查询，避免全量加载）
        List<FieldTestPoint> todayPoints = fieldTestPointRepository
                .findByPointCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (FieldTestPoint point : todayPoints) {
            String code = point.getPointCode();
            if (code != null && code.startsWith(prefix) && code.length() > prefix.length()) {
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
     * 验证字段测试要点数据
     */
    private void validateFieldTestPoint(FieldTestPoint fieldTestPoint, boolean isCreate) {
        if (fieldTestPoint == null) {
            throw new BusinessException("字段测试要点信息不能为空");
        }
        
        // 验证字段名称
        if (!StringUtils.hasText(fieldTestPoint.getFieldName())) {
            throw new BusinessException("字段名称不能为空");
        }
        if (fieldTestPoint.getFieldName().length() > 200) {
            throw new BusinessException("字段名称长度不能超过200个字符");
        }
        
        // 验证要点编码（创建时）
        if (isCreate && StringUtils.hasText(fieldTestPoint.getPointCode())) {
            if (fieldTestPoint.getPointCode().length() > 100) {
                throw new BusinessException("要点编码长度不能超过100个字符");
            }
        }
    }
}

