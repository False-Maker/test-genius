package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.enums.CaseStatus;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.service.CacheService;
import com.sinosoft.testdesign.service.SpecificationCheckService;
import com.sinosoft.testdesign.service.TestCaseService;
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
import java.util.stream.Collectors;

/**
 * 测试用例管理服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {
    
    private final TestCaseRepository testCaseRepository;
    private final RequirementRepository requirementRepository;
    private final CacheService cacheService;
    private final SpecificationCheckService specificationCheckService;
    
    private static final String CASE_CODE_PREFIX = "CASE";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // 缓存键前缀
    private static final String CACHE_KEY_CASE_LIST = "cache:testcase:list:";
    private static final String CACHE_KEY_CASE_BY_ID = "cache:testcase:id:";
    private static final long CACHE_TIMEOUT_LIST = 300; // 列表缓存5分钟
    private static final long CACHE_TIMEOUT_DETAIL = 3600; // 详情缓存1小时
    
    /**
     * 创建测试用例
     * 
     * @param testCase 测试用例实体（包含用例名称、需求ID、测试分层、测试方法等信息）
     * @return 创建成功的测试用例（包含自动生成的用例编码）
     * @throws BusinessException 如果用例编码已存在、关联的需求不存在或数据验证失败
     */
    @Override
    @Transactional
    public TestCase createTestCase(TestCase testCase) {
        log.info("创建测试用例: {}", testCase.getCaseName());
        
        // 数据验证
        validateTestCase(testCase, true);
        
        // 验证需求是否存在
        if (testCase.getRequirementId() != null) {
            requirementRepository.findById(testCase.getRequirementId())
                    .orElseThrow(() -> new BusinessException("关联的需求不存在"));
        }
        
        // 自动生成用例编码（如果未提供）
        if (!StringUtils.hasText(testCase.getCaseCode())) {
            testCase.setCaseCode(generateCaseCode());
        } else {
            // 检查编码是否已存在
            if (testCaseRepository.findByCaseCode(testCase.getCaseCode()).isPresent()) {
                throw new BusinessException("用例编码已存在: " + testCase.getCaseCode());
            }
        }
        
        // 设置默认状态
        if (!StringUtils.hasText(testCase.getCaseStatus())) {
            testCase.setCaseStatus(CaseStatus.DRAFT.name());
        }
        
        // 设置默认版本号
        if (testCase.getVersion() == null) {
            testCase.setVersion(1);
        }
        
        log.info("创建用例成功，编码: {}", testCase.getCaseCode());
        TestCase saved = testCaseRepository.save(testCase);
        
        // 清除相关缓存
        clearTestCaseCache();
        
        return saved;
    }
    
    /**
     * 更新测试用例
     * 
     * @param id 用例ID
     * @param testCase 更新的用例信息
     * @return 更新后的测试用例
     * @throws BusinessException 如果用例不存在、用例编码被修改、状态流转不合法或数据验证失败
     */
    @Override
    @Transactional
    public TestCase updateTestCase(Long id, TestCase testCase) {
        log.info("更新测试用例: {}", id);
        
        TestCase existing = testCaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("测试用例不存在"));
        
        // 数据验证
        validateTestCase(testCase, false);
        
        // 不允许修改用例编码
        if (StringUtils.hasText(testCase.getCaseCode()) 
                && !testCase.getCaseCode().equals(existing.getCaseCode())) {
            throw new BusinessException("不允许修改用例编码");
        }
        
        // 验证需求是否存在
        if (testCase.getRequirementId() != null 
                && !testCase.getRequirementId().equals(existing.getRequirementId())) {
            requirementRepository.findById(testCase.getRequirementId())
                    .orElseThrow(() -> new BusinessException("关联的需求不存在"));
        }
        
        // 状态流转验证
        if (StringUtils.hasText(testCase.getCaseStatus())) {
            CaseStatus currentStatus = CaseStatus.fromString(existing.getCaseStatus());
            CaseStatus newStatus = CaseStatus.fromString(testCase.getCaseStatus());
            
            if (!currentStatus.canTransitionTo(newStatus)) {
                throw new BusinessException(
                    String.format("用例状态不能从 %s 流转到 %s", 
                        currentStatus.getDescription(), newStatus.getDescription()));
            }
        }
        
        // 版本管理：如果关键字段发生变化，版本号自增
        boolean needVersionIncrement = isKeyFieldChanged(existing, testCase);
        
        // 更新字段
        if (StringUtils.hasText(testCase.getCaseName())) {
            existing.setCaseName(testCase.getCaseName());
        }
        if (testCase.getRequirementId() != null) {
            existing.setRequirementId(testCase.getRequirementId());
        }
        if (testCase.getLayerId() != null) {
            existing.setLayerId(testCase.getLayerId());
        }
        if (testCase.getMethodId() != null) {
            existing.setMethodId(testCase.getMethodId());
        }
        if (StringUtils.hasText(testCase.getCaseType())) {
            existing.setCaseType(testCase.getCaseType());
        }
        if (StringUtils.hasText(testCase.getCasePriority())) {
            existing.setCasePriority(testCase.getCasePriority());
        }
        if (testCase.getPreCondition() != null) {
            existing.setPreCondition(testCase.getPreCondition());
        }
        if (testCase.getTestStep() != null) {
            existing.setTestStep(testCase.getTestStep());
        }
        if (testCase.getExpectedResult() != null) {
            existing.setExpectedResult(testCase.getExpectedResult());
        }
        if (StringUtils.hasText(testCase.getCaseStatus())) {
            existing.setCaseStatus(testCase.getCaseStatus());
        }
        
        // 版本号自增
        if (needVersionIncrement) {
            existing.setVersion(existing.getVersion() + 1);
        }
        
        log.info("更新用例成功，编码: {}", existing.getCaseCode());
        TestCase saved = testCaseRepository.save(existing);
        
        // 清除相关缓存
        clearTestCaseCache();
        cacheService.delete(CACHE_KEY_CASE_BY_ID + saved.getId());
        
        return saved;
    }
    
    /**
     * 根据ID获取测试用例详情
     * 
     * @param id 用例ID
     * @return 测试用例实体
     * @throws BusinessException 如果用例不存在
     */
    @Override
    public TestCase getTestCaseById(Long id) {
        // 尝试从缓存获取
        String cacheKey = CACHE_KEY_CASE_BY_ID + id;
        TestCase cached = cacheService.get(cacheKey, TestCase.class);
        if (cached != null) {
            log.debug("从缓存获取用例详情: id={}", id);
            return cached;
        }
        
        // 从数据库查询
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("测试用例不存在"));
        
        // 存入缓存
        cacheService.set(cacheKey, testCase, CACHE_TIMEOUT_DETAIL);
        
        return testCase;
    }
    
    /**
     * 分页查询测试用例列表
     * 
     * @param pageable 分页参数（页码、每页大小）
     * @param caseName 用例名称（模糊匹配，可选）
     * @param caseStatus 用例状态（精确匹配，可选）
     * @param requirementId 需求ID（精确匹配，可选）
     * @return 分页的测试用例列表
     */
    @Override
    public Page<TestCase> getTestCaseList(Pageable pageable, String caseName, String caseStatus, Long requirementId) {
        // 使用优化的查询方法（使用@Query注解，优化COUNT查询）
        // 注意：分页查询结果不缓存，因为Page接口序列化复杂且缓存失效频繁
        // 详情查询已单独缓存，可以有效减少数据库查询
        return testCaseRepository.findWithFilters(
                StringUtils.hasText(caseName) ? caseName : null,
                StringUtils.hasText(caseStatus) ? caseStatus : null,
                requirementId,
                pageable);
    }
    
    /**
     * 删除测试用例
     * 
     * @param id 用例ID
     * @throws BusinessException 如果用例不存在或用例已审核（已审核的用例不能直接删除，需要先废弃）
     */
    @Override
    @Transactional
    public void deleteTestCase(Long id) {
        log.info("删除测试用例: {}", id);
        
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("测试用例不存在"));
        
        // 检查是否可以删除（已审核的用例不能删除，需要先废弃）
        CaseStatus status = CaseStatus.fromString(testCase.getCaseStatus());
        if (status == CaseStatus.REVIEWED) {
            throw new BusinessException("已审核的用例不能删除，请先将其废弃");
        }
        
        testCaseRepository.deleteById(id);
        
        // 清除相关缓存
        clearTestCaseCache();
        cacheService.delete(CACHE_KEY_CASE_BY_ID + id);
        
        log.info("删除用例成功，编码: {}", testCase.getCaseCode());
    }
    
    /**
     * 更新用例状态（状态流转）
     * 
     * @param id 用例ID
     * @param status 新状态（DRAFT/PENDING_REVIEW/REVIEWED/DEPRECATED）
     * @return 更新后的测试用例
     * @throws BusinessException 如果用例不存在或状态流转不合法
     */
    @Transactional
    public TestCase updateCaseStatus(Long id, String status) {
        log.info("更新用例状态: {} -> {}", id, status);
        
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("测试用例不存在"));
        
        CaseStatus currentStatus = CaseStatus.fromString(testCase.getCaseStatus());
        CaseStatus newStatus = CaseStatus.fromString(status);
        
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BusinessException(
                String.format("用例状态不能从 %s 流转到 %s", 
                    currentStatus.getDescription(), newStatus.getDescription()));
        }
        
        testCase.setCaseStatus(newStatus.name());
        // 状态变更时版本号自增
        testCase.setVersion(testCase.getVersion() + 1);
        
        log.info("更新用例状态成功，编码: {}, 新状态: {}", testCase.getCaseCode(), newStatus.getDescription());
        return testCaseRepository.save(testCase);
    }
    
    /**
     * 审核测试用例
     * 
     * @param id 用例ID
     * @param reviewResult 审核结果（PASS：通过，REJECT：不通过）
     * @param reviewComment 审核意见（可选）
     * @return 审核后的测试用例
     * @throws BusinessException 如果用例不存在、用例状态不是待审核或审核结果格式不正确
     */
    @Transactional
    public TestCase reviewTestCase(Long id, String reviewResult, String reviewComment) {
        log.info("审核用例: {}, 结果: {}", id, reviewResult);
        
        TestCase testCase = testCaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("测试用例不存在"));
        
        CaseStatus currentStatus = CaseStatus.fromString(testCase.getCaseStatus());
        
        // 只有待审核状态的用例才能审核
        if (currentStatus != CaseStatus.PENDING_REVIEW) {
            throw new BusinessException("只有待审核状态的用例才能进行审核");
        }
        
        // 在审核之前检查规约符合性（仅记录日志，不阻塞审核流程）
        try {
            SpecificationCheckService.SpecificationComplianceResult complianceResult = 
                    specificationCheckService.checkCompliance(testCase, null);
            
            log.info("用例规约符合性检查结果，用例ID: {}, 符合度评分: {}, 是否符合: {}, 问题数: {}", 
                    id, complianceResult.getComplianceScore(), 
                    complianceResult.isCompliant(), complianceResult.getFailedChecks());
            
            // 如果不符合规约，在日志中记录警告
            if (!complianceResult.isCompliant()) {
                        log.warn("用例不符合规约要求，用例ID: {}, 符合度评分: {}, 问题列表: {}", 
                        id, complianceResult.getComplianceScore(), 
                        complianceResult.getIssues().stream()
                                .map(SpecificationCheckService.ComplianceIssue::getIssueDescription)
                                .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            // 规约检查失败不影响审核流程，仅记录日志
            log.warn("规约符合性检查失败，继续审核流程: {}", e.getMessage());
        }
        
        // 审核通过
        if ("PASS".equalsIgnoreCase(reviewResult)) {
            testCase.setCaseStatus(CaseStatus.REVIEWED.name());
            testCase.setVersion(testCase.getVersion() + 1);
            log.info("用例审核通过，编码: {}", testCase.getCaseCode());
        } 
        // 审核不通过，退回草稿
        else if ("REJECT".equalsIgnoreCase(reviewResult)) {
            testCase.setCaseStatus(CaseStatus.DRAFT.name());
            testCase.setVersion(testCase.getVersion() + 1);
            log.info("用例审核不通过，退回草稿，编码: {}", testCase.getCaseCode());
        } else {
            throw new BusinessException("审核结果必须是 PASS 或 REJECT");
        }
        
        return testCaseRepository.save(testCase);
    }
    
    /**
     * 生成用例编码
     * 格式：CASE-YYYYMMDD-序号（如 CASE-20240101-001）
     * 
     * @return 生成的用例编码
     */
    private String generateCaseCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = CASE_CODE_PREFIX + "-" + dateStr + "-";
        
        // 优化：只查询当天前缀的用例，避免全表扫描
        List<TestCase> todayCases = testCaseRepository
                .findByCaseCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestCase c : todayCases) {
            String code = c.getCaseCode();
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
     * 验证用例数据
     */
    private void validateTestCase(TestCase testCase, boolean isCreate) {
        if (testCase == null) {
            throw new BusinessException("用例信息不能为空");
        }
        
        // 验证用例名称
        if (!StringUtils.hasText(testCase.getCaseName())) {
            throw new BusinessException("用例名称不能为空");
        }
        if (testCase.getCaseName().length() > 500) {
            throw new BusinessException("用例名称长度不能超过500个字符");
        }
        
        // 验证用例编码（创建时）
        if (isCreate && StringUtils.hasText(testCase.getCaseCode())) {
            if (testCase.getCaseCode().length() > 100) {
                throw new BusinessException("用例编码长度不能超过100个字符");
            }
        }
        
        // 验证用例类型
        if (StringUtils.hasText(testCase.getCaseType())) {
            String type = testCase.getCaseType();
            if (!type.equals("正常") && !type.equals("异常") && !type.equals("边界")) {
                throw new BusinessException("用例类型必须是：正常、异常或边界");
            }
        }
        
        // 验证用例优先级
        if (StringUtils.hasText(testCase.getCasePriority())) {
            String priority = testCase.getCasePriority();
            if (!priority.equals("高") && !priority.equals("中") && !priority.equals("低")) {
                throw new BusinessException("用例优先级必须是：高、中或低");
            }
        }
        
        // 验证测试步骤和预期结果（已审核的用例必须完整）
        if (isCreate || StringUtils.hasText(testCase.getCaseStatus())) {
            CaseStatus status = CaseStatus.fromString(testCase.getCaseStatus());
            if (status == CaseStatus.PENDING_REVIEW || status == CaseStatus.REVIEWED) {
                if (!StringUtils.hasText(testCase.getTestStep())) {
                    throw new BusinessException("提交审核的用例必须填写测试步骤");
                }
                if (!StringUtils.hasText(testCase.getExpectedResult())) {
                    throw new BusinessException("提交审核的用例必须填写预期结果");
                }
            }
        }
    }
    
    /**
     * 判断关键字段是否发生变化（用于版本管理）
     */
    private boolean isKeyFieldChanged(TestCase existing, TestCase updated) {
        return !existing.getCaseName().equals(updated.getCaseName())
                || !equalsNullable(existing.getPreCondition(), updated.getPreCondition())
                || !equalsNullable(existing.getTestStep(), updated.getTestStep())
                || !equalsNullable(existing.getExpectedResult(), updated.getExpectedResult())
                || !equalsNullable(existing.getCaseType(), updated.getCaseType())
                || !equalsNullable(existing.getCasePriority(), updated.getCasePriority());
    }
    
    private boolean equalsNullable(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
    
    /**
     * 清除用例列表缓存（预留，当前分页查询不缓存）
     */
    private void clearTestCaseCache() {
        // 分页查询不缓存，此方法预留用于未来扩展
        // cacheService.deleteByPattern(CACHE_KEY_CASE_LIST + "*");
    }
}

