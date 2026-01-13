package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.enums.RequirementStatus;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.service.CacheService;
import com.sinosoft.testdesign.service.RequirementService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 需求管理服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementServiceImpl implements RequirementService {
    
    private final RequirementRepository requirementRepository;
    private final CacheService cacheService;
    
    private static final String REQUIREMENT_CODE_PREFIX = "REQ";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // 缓存键前缀
    private static final String CACHE_KEY_REQ_LIST = "cache:requirement:list:";
    private static final String CACHE_KEY_REQ_BY_ID = "cache:requirement:id:";
    private static final long CACHE_TIMEOUT_LIST = 300; // 列表缓存5分钟
    private static final long CACHE_TIMEOUT_DETAIL = 3600; // 详情缓存1小时
    
    @Override
    @Transactional
    public TestRequirement createRequirement(TestRequirement requirement) {
        log.info("创建需求: {}", requirement.getRequirementName());
        
        // 数据验证
        validateRequirement(requirement, true);
        
        // 自动生成需求编码（如果未提供）
        if (!StringUtils.hasText(requirement.getRequirementCode())) {
            requirement.setRequirementCode(generateRequirementCode());
        } else {
            // 检查编码是否已存在
            if (requirementRepository.findByRequirementCode(requirement.getRequirementCode()).isPresent()) {
                throw new BusinessException("需求编码已存在: " + requirement.getRequirementCode());
            }
        }
        
        // 设置默认状态
        if (!StringUtils.hasText(requirement.getRequirementStatus())) {
            requirement.setRequirementStatus(RequirementStatus.DRAFT.name());
        }
        
        // 设置默认版本号
        if (requirement.getVersion() == null) {
            requirement.setVersion(1);
        }
        
        log.info("创建需求成功，编码: {}", requirement.getRequirementCode());
        TestRequirement saved = requirementRepository.save(requirement);
        
        // 清除相关缓存
        clearRequirementCache();
        
        return saved;
    }
    
    @Override
    @Transactional
    public TestRequirement updateRequirement(Long id, TestRequirement requirement) {
        log.info("更新需求: {}", id);
        
        TestRequirement existing = requirementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("需求不存在"));
        
        // 数据验证
        validateRequirement(requirement, false);
        
        // 不允许修改需求编码
        if (StringUtils.hasText(requirement.getRequirementCode()) 
                && !requirement.getRequirementCode().equals(existing.getRequirementCode())) {
            throw new BusinessException("不允许修改需求编码");
        }
        
        // 状态流转验证
        if (StringUtils.hasText(requirement.getRequirementStatus())) {
            RequirementStatus currentStatus = RequirementStatus.fromString(existing.getRequirementStatus());
            RequirementStatus newStatus = RequirementStatus.fromString(requirement.getRequirementStatus());
            
            if (!currentStatus.canTransitionTo(newStatus)) {
                throw new BusinessException(
                    String.format("需求状态不能从 %s 流转到 %s", 
                        currentStatus.getDescription(), newStatus.getDescription()));
            }
        }
        
        // 更新字段
        if (StringUtils.hasText(requirement.getRequirementName())) {
            existing.setRequirementName(requirement.getRequirementName());
        }
        if (StringUtils.hasText(requirement.getRequirementType())) {
            existing.setRequirementType(requirement.getRequirementType());
        }
        if (requirement.getRequirementDescription() != null) {
            existing.setRequirementDescription(requirement.getRequirementDescription());
        }
        if (StringUtils.hasText(requirement.getRequirementDocUrl())) {
            existing.setRequirementDocUrl(requirement.getRequirementDocUrl());
        }
        if (StringUtils.hasText(requirement.getRequirementStatus())) {
            existing.setRequirementStatus(requirement.getRequirementStatus());
        }
        if (StringUtils.hasText(requirement.getBusinessModule())) {
            existing.setBusinessModule(requirement.getBusinessModule());
        }
        
        // 版本号自增
        existing.setVersion(existing.getVersion() + 1);
        
        log.info("更新需求成功，编码: {}", existing.getRequirementCode());
        TestRequirement saved = requirementRepository.save(existing);
        
        // 清除相关缓存
        clearRequirementCache();
        cacheService.delete(CACHE_KEY_REQ_BY_ID + saved.getId());
        
        return saved;
    }
    
    @Override
    public TestRequirement getRequirementById(Long id) {
        // 尝试从缓存获取
        String cacheKey = CACHE_KEY_REQ_BY_ID + id;
        TestRequirement cached = cacheService.get(cacheKey, TestRequirement.class);
        if (cached != null) {
            log.debug("从缓存获取需求详情: id={}", id);
            return cached;
        }
        
        // 从数据库查询
        TestRequirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("需求不存在"));
        
        // 存入缓存
        cacheService.set(cacheKey, requirement, CACHE_TIMEOUT_DETAIL);
        
        return requirement;
    }
    
    @Override
    public Page<TestRequirement> getRequirementList(Pageable pageable, String requirementName, String requirementStatus) {
        // 使用优化的查询方法（使用@Query注解，优化COUNT查询）
        // 注意：分页查询结果不缓存，因为Page接口序列化复杂且缓存失效频繁
        // 详情查询已单独缓存，可以有效减少数据库查询
        return requirementRepository.findWithFilters(
                StringUtils.hasText(requirementName) ? requirementName : null,
                StringUtils.hasText(requirementStatus) ? requirementStatus : null,
                pageable);
    }
    
    @Override
    @Transactional
    public void deleteRequirement(Long id) {
        log.info("删除需求: {}", id);
        
        TestRequirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("需求不存在"));
        
        // 检查是否可以删除（已通过或已关闭的需求不能删除）
        RequirementStatus status = RequirementStatus.fromString(requirement.getRequirementStatus());
        if (status == RequirementStatus.APPROVED || status == RequirementStatus.CLOSED) {
            throw new BusinessException("已通过或已关闭的需求不能删除");
        }
        
        requirementRepository.deleteById(id);
        
        // 清除相关缓存
        clearRequirementCache();
        cacheService.delete(CACHE_KEY_REQ_BY_ID + id);
        
        log.info("删除需求成功，编码: {}", requirement.getRequirementCode());
    }
    
    /**
     * 更新需求状态
     */
    @Transactional
    public TestRequirement updateRequirementStatus(Long id, String status) {
        log.info("更新需求状态: {} -> {}", id, status);
        
        TestRequirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("需求不存在"));
        
        RequirementStatus currentStatus = RequirementStatus.fromString(requirement.getRequirementStatus());
        RequirementStatus newStatus = RequirementStatus.fromString(status);
        
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessException(
                String.format("需求状态不能从 %s 流转到 %s", 
                    currentStatus.getDescription(), newStatus.getDescription()));
        }
        
        requirement.setRequirementStatus(newStatus.name());
        requirement.setVersion(requirement.getVersion() + 1);
        
        log.info("更新需求状态成功，编码: {}, 新状态: {}", requirement.getRequirementCode(), newStatus.getDescription());
        return requirementRepository.save(requirement);
    }
    
    /**
     * 生成需求编码
     * 格式：REQ-YYYYMMDD-序号（如 REQ-20240101-001）
     */
    private String generateRequirementCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = REQUIREMENT_CODE_PREFIX + "-" + dateStr + "-";
        
        // 优化：只查询当天前缀的需求，避免全表扫描
        List<TestRequirement> todayRequirements = requirementRepository
                .findByRequirementCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestRequirement req : todayRequirements) {
            String code = req.getRequirementCode();
            if (code.length() > prefix.length()) {
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
     * 验证需求数据
     */
    private void validateRequirement(TestRequirement requirement, boolean isCreate) {
        if (requirement == null) {
            throw new BusinessException("需求信息不能为空");
        }
        
        // 验证需求名称
        if (!StringUtils.hasText(requirement.getRequirementName())) {
            throw new BusinessException("需求名称不能为空");
        }
        if (requirement.getRequirementName().length() > 500) {
            throw new BusinessException("需求名称长度不能超过500个字符");
        }
        
        // 验证需求编码（创建时）
        if (isCreate && StringUtils.hasText(requirement.getRequirementCode())) {
            if (requirement.getRequirementCode().length() > 100) {
                throw new BusinessException("需求编码长度不能超过100个字符");
            }
        }
        
        // 验证需求类型
        if (StringUtils.hasText(requirement.getRequirementType())) {
            String type = requirement.getRequirementType();
            if (!type.equals("新功能") && !type.equals("优化") && !type.equals("缺陷修复")) {
                throw new BusinessException("需求类型必须是：新功能、优化或缺陷修复");
            }
        }
        
        // 验证业务模块
        if (StringUtils.hasText(requirement.getBusinessModule())) {
            if (requirement.getBusinessModule().length() > 100) {
                throw new BusinessException("业务模块长度不能超过100个字符");
            }
        }
    }
    
    /**
     * 清除需求列表缓存（预留，当前分页查询不缓存）
     */
    private void clearRequirementCache() {
        // 分页查询不缓存，此方法预留用于未来扩展
        // cacheService.deleteByPattern(CACHE_KEY_REQ_LIST + "*");
    }
}

