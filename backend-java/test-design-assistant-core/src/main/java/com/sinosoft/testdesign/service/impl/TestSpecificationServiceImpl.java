package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.SpecVersion;
import com.sinosoft.testdesign.entity.TestSpecification;
import com.sinosoft.testdesign.repository.SpecVersionRepository;
import com.sinosoft.testdesign.repository.TestSpecificationRepository;
import com.sinosoft.testdesign.service.CacheService;
import com.sinosoft.testdesign.service.TestSpecificationService;
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
 * 测试规约管理服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestSpecificationServiceImpl implements TestSpecificationService {
    
    private final TestSpecificationRepository specificationRepository;
    private final SpecVersionRepository specVersionRepository;
    private final CacheService cacheService;
    
    private static final String SPEC_CODE_PREFIX = "SPEC";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // 缓存键前缀
    private static final String CACHE_KEY_SPEC_BY_ID = "cache:specification:id:";
    private static final long CACHE_TIMEOUT_DETAIL = 3600; // 详情缓存1小时
    
    @Override
    @Transactional
    public TestSpecification createSpecification(TestSpecification specification) {
        log.info("创建测试规约: {}", specification.getSpecName());
        
        // 数据验证
        validateSpecification(specification, true);
        
        // 自动生成规约编码
        if (!StringUtils.hasText(specification.getSpecCode())) {
            specification.setSpecCode(generateSpecCode());
        } else {
            // 检查编码是否已存在
            if (specificationRepository.findBySpecCode(specification.getSpecCode()).isPresent()) {
                throw new BusinessException("规约编码已存在: " + specification.getSpecCode());
            }
        }
        
        // 设置默认值
        if (!StringUtils.hasText(specification.getIsActive())) {
            specification.setIsActive("1");
        }
        if (specification.getVersion() == null) {
            specification.setVersion(1);
        }
        
        // 设置初始版本号
        if (!StringUtils.hasText(specification.getCurrentVersion())) {
            specification.setCurrentVersion("1.0.0");
        }
        
        log.info("创建测试规约成功，编码: {}", specification.getSpecCode());
        TestSpecification saved = specificationRepository.save(specification);
        
        // 创建初始版本记录
        createVersionRecord(saved.getId(), specification.getCurrentVersion(), 
                          "初始版本", "创建规约时的初始版本", null, specification.getCreatorId(), 
                          specification.getCreatorName());
        
        return saved;
    }
    
    @Override
    @Transactional
    public TestSpecification updateSpecification(Long id, TestSpecification specification) {
        log.info("更新测试规约: {}", id);
        
        TestSpecification existing = specificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("测试规约不存在"));
        
        // 数据验证
        validateSpecification(specification, false);
        
        // 不允许修改规约编码
        if (StringUtils.hasText(specification.getSpecCode()) 
                && !specification.getSpecCode().equals(existing.getSpecCode())) {
            throw new BusinessException("不允许修改规约编码");
        }
        
        // 更新字段
        if (StringUtils.hasText(specification.getSpecName())) {
            existing.setSpecName(specification.getSpecName());
        }
        if (StringUtils.hasText(specification.getSpecType())) {
            existing.setSpecType(specification.getSpecType());
        }
        if (specification.getSpecCategory() != null) {
            existing.setSpecCategory(specification.getSpecCategory());
        }
        if (specification.getSpecDescription() != null) {
            existing.setSpecDescription(specification.getSpecDescription());
        }
        if (specification.getSpecContent() != null) {
            existing.setSpecContent(specification.getSpecContent());
        }
        if (specification.getApplicableModules() != null) {
            existing.setApplicableModules(specification.getApplicableModules());
        }
        if (specification.getApplicableLayers() != null) {
            existing.setApplicableLayers(specification.getApplicableLayers());
        }
        if (specification.getApplicableMethods() != null) {
            existing.setApplicableMethods(specification.getApplicableMethods());
        }
        if (specification.getEffectiveDate() != null) {
            existing.setEffectiveDate(specification.getEffectiveDate());
        }
        if (specification.getExpireDate() != null) {
            existing.setExpireDate(specification.getExpireDate());
        }
        
        // 版本号自增
        existing.setVersion(existing.getVersion() + 1);
        
        log.info("更新测试规约成功，编码: {}", existing.getSpecCode());
        TestSpecification saved = specificationRepository.save(existing);
        
        // 清除相关缓存
        cacheService.delete(CACHE_KEY_SPEC_BY_ID + saved.getId());
        
        return saved;
    }
    
    @Override
    public TestSpecification getSpecificationById(Long id) {
        // 尝试从缓存获取
        String cacheKey = CACHE_KEY_SPEC_BY_ID + id;
        TestSpecification cached = cacheService.get(cacheKey, TestSpecification.class);
        if (cached != null) {
            log.debug("从缓存获取规约详情: id={}", id);
            return cached;
        }
        
        // 从数据库查询
        TestSpecification specification = specificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("测试规约不存在"));
        
        // 存入缓存
        cacheService.set(cacheKey, specification, CACHE_TIMEOUT_DETAIL);
        
        return specification;
    }
    
    @Override
    public TestSpecification getSpecificationByCode(String specCode) {
        return specificationRepository.findBySpecCode(specCode)
                .orElseThrow(() -> new BusinessException("测试规约不存在: " + specCode));
    }
    
    @Override
    public Page<TestSpecification> getSpecificationList(Pageable pageable, String specName, 
                                                         String specType, String isActive) {
        return specificationRepository.findWithFilters(
                StringUtils.hasText(specName) ? specName : null,
                StringUtils.hasText(specType) ? specType : null,
                StringUtils.hasText(isActive) ? isActive : null,
                pageable);
    }
    
    @Override
    @Transactional
    public void deleteSpecification(Long id) {
        log.info("删除测试规约: {}", id);
        
        TestSpecification specification = specificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("测试规约不存在"));
        
        // 检查是否有版本记录
        List<SpecVersion> versions = specVersionRepository.findBySpecIdOrderByCreateTimeDesc(id);
        if (!versions.isEmpty()) {
            // 删除所有版本记录
            specVersionRepository.deleteAll(versions);
        }
        
        specificationRepository.deleteById(id);
        
        // 清除相关缓存
        cacheService.delete(CACHE_KEY_SPEC_BY_ID + id);
        
        log.info("删除测试规约成功，编码: {}", specification.getSpecCode());
    }
    
    @Override
    @Transactional
    public TestSpecification updateSpecificationStatus(Long id, String isActive) {
        log.info("更新测试规约状态: {} -> {}", id, isActive);
        
        TestSpecification specification = specificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("测试规约不存在"));
        
        if (!"0".equals(isActive) && !"1".equals(isActive)) {
            throw new BusinessException("启用状态必须是0或1");
        }
        
        specification.setIsActive(isActive);
        specification.setVersion(specification.getVersion() + 1);
        
        log.info("更新测试规约状态成功，编码: {}, 状态: {}", specification.getSpecCode(), isActive);
        TestSpecification saved = specificationRepository.save(specification);
        
        // 清除相关缓存
        cacheService.delete(CACHE_KEY_SPEC_BY_ID + saved.getId());
        
        return saved;
    }
    
    @Override
    public List<TestSpecification> getApplicationSpecifications() {
        return specificationRepository.findBySpecTypeAndIsActive("APPLICATION", "1");
    }
    
    @Override
    public List<TestSpecification> getPublicSpecifications() {
        return specificationRepository.findBySpecTypeAndIsActive("PUBLIC", "1");
    }
    
    @Override
    public List<TestSpecification> getSpecificationsByModule(String module) {
        return specificationRepository.findByApplicableModule("%" + module + "%");
    }
    
    @Override
    public List<TestSpecification> getSpecificationsByLayer(String layer) {
        return specificationRepository.findByApplicableLayer("%" + layer + "%");
    }
    
    @Override
    @Transactional
    public TestSpecification createVersion(Long specId, String versionNumber, String versionName,
                                          String versionDescription, String changeLog) {
        log.info("创建规约版本: specId={}, versionNumber={}", specId, versionNumber);
        
        TestSpecification specification = specificationRepository.findById(specId)
                .orElseThrow(() -> new BusinessException("测试规约不存在"));
        
        // 检查版本号是否已存在
        if (specVersionRepository.findBySpecIdAndVersionNumber(specId, versionNumber).isPresent()) {
            throw new BusinessException("版本号已存在: " + versionNumber);
        }
        
        // 创建版本记录
        SpecVersion version = createVersionRecord(specId, versionNumber, versionName, 
                                                 versionDescription, changeLog, 
                                                 specification.getCreatorId(), 
                                                 specification.getCreatorName());
        
        // 如果设为当前版本，则更新规约的当前版本号
        if ("1".equals(version.getIsCurrent())) {
            specification.setCurrentVersion(versionNumber);
            specification.setVersion(specification.getVersion() + 1);
            specificationRepository.save(specification);
        }
        
        log.info("创建规约版本成功，版本号: {}", versionNumber);
        return specification;
    }
    
    @Override
    @Transactional
    public TestSpecification switchVersion(Long specId, String versionNumber) {
        log.info("切换规约版本: specId={}, versionNumber={}", specId, versionNumber);
        
        TestSpecification specification = specificationRepository.findById(specId)
                .orElseThrow(() -> new BusinessException("测试规约不存在"));
        
        SpecVersion targetVersion = specVersionRepository.findBySpecIdAndVersionNumber(specId, versionNumber)
                .orElseThrow(() -> new BusinessException("规约版本不存在: " + versionNumber));
        
        // 将所有版本设为非当前版本
        List<SpecVersion> allVersions = specVersionRepository.findBySpecIdOrderByCreateTimeDesc(specId);
        for (SpecVersion v : allVersions) {
            v.setIsCurrent("0");
        }
        
        // 设置目标版本为当前版本
        targetVersion.setIsCurrent("1");
        specVersionRepository.saveAll(allVersions);
        
        // 更新规约的当前版本号和内容
        specification.setCurrentVersion(versionNumber);
        specification.setSpecContent(targetVersion.getSpecContent());
        specification.setVersion(specification.getVersion() + 1);
        
        log.info("切换规约版本成功，当前版本: {}", versionNumber);
        TestSpecification saved = specificationRepository.save(specification);
        
        // 清除相关缓存
        cacheService.delete(CACHE_KEY_SPEC_BY_ID + saved.getId());
        
        return saved;
    }
    
    @Override
    public List<SpecVersion> getVersionList(Long specId) {
        return specVersionRepository.findBySpecIdOrderByCreateTimeDesc(specId);
    }
    
    /**
     * 生成规约编码
     * 格式：SPEC-YYYYMMDD-序号（如 SPEC-20240101-001）
     */
    private String generateSpecCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = SPEC_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天已生成的规约编码
        List<TestSpecification> todaySpecs = specificationRepository
                .findBySpecCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestSpecification spec : todaySpecs) {
            String code = spec.getSpecCode();
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
     * 创建版本记录
     */
    private SpecVersion createVersionRecord(Long specId, String versionNumber, String versionName,
                                          String versionDescription, String changeLog,
                                          Long createdBy, String createdByName) {
        TestSpecification spec = specificationRepository.findById(specId)
                .orElseThrow(() -> new BusinessException("测试规约不存在"));
        
        SpecVersion version = new SpecVersion();
        version.setSpecId(specId);
        version.setVersionNumber(versionNumber);
        version.setVersionName(versionName);
        version.setVersionDescription(versionDescription);
        version.setSpecContent(spec.getSpecContent());
        version.setChangeLog(changeLog);
        version.setCreatedBy(createdBy);
        version.setCreatedByName(createdByName);
        
        // 如果是第一个版本，设为当前版本
        List<SpecVersion> existingVersions = specVersionRepository.findBySpecIdOrderByCreateTimeDesc(specId);
        if (existingVersions.isEmpty()) {
            version.setIsCurrent("1");
        } else {
            version.setIsCurrent("0");
        }
        
        return specVersionRepository.save(version);
    }
    
    /**
     * 验证规约数据
     */
    private void validateSpecification(TestSpecification specification, boolean isCreate) {
        if (specification == null) {
            throw new BusinessException("规约信息不能为空");
        }
        
        // 验证规约名称
        if (!StringUtils.hasText(specification.getSpecName())) {
            throw new BusinessException("规约名称不能为空");
        }
        if (specification.getSpecName().length() > 500) {
            throw new BusinessException("规约名称长度不能超过500个字符");
        }
        
        // 验证规约类型
        if (!StringUtils.hasText(specification.getSpecType())) {
            throw new BusinessException("规约类型不能为空");
        }
        if (!"APPLICATION".equals(specification.getSpecType()) 
                && !"PUBLIC".equals(specification.getSpecType())) {
            throw new BusinessException("规约类型必须是：APPLICATION或PUBLIC");
        }
        
        // 验证规约编码（创建时）
        if (isCreate && StringUtils.hasText(specification.getSpecCode())) {
            if (specification.getSpecCode().length() > 100) {
                throw new BusinessException("规约编码长度不能超过100个字符");
            }
        }
    }
}

