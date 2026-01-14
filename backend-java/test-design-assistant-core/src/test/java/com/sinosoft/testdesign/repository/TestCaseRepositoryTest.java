package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.enums.CaseStatus;
import com.sinosoft.testdesign.enums.RequirementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试用例Repository测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("测试用例Repository测试")
class TestCaseRepositoryTest {
    
    @Autowired
    private TestCaseRepository testCaseRepository;
    
    @Autowired
    private RequirementRepository requirementRepository;
    
    private TestCase testCase;
    private TestRequirement requirement;
    
    @BeforeEach
    void setUp() {
        testCaseRepository.deleteAll();
        requirementRepository.deleteAll();
        
        // 创建需求
        requirement = new TestRequirement();
        requirement.setRequirementCode("REQ-20240101-001");
        requirement.setRequirementName("测试需求");
        requirement.setRequirementType("新功能");
        requirement.setRequirementStatus(RequirementStatus.DRAFT.name());
        requirement.setCreateTime(LocalDateTime.now());
        requirement = requirementRepository.save(requirement);
        
        // 创建用例
        testCase = new TestCase();
        testCase.setCaseCode("CASE-20240101-001");
        testCase.setCaseName("测试用例");
        testCase.setRequirementId(requirement.getId());
        testCase.setTestStep("1. 步骤一\n2. 步骤二");
        testCase.setExpectedResult("预期结果");
        testCase.setCaseStatus(CaseStatus.DRAFT.name());
        testCase.setCreateTime(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("保存用例-成功")
    void testSave_Success() {
        // When
        TestCase saved = testCaseRepository.save(testCase);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("CASE-20240101-001", saved.getCaseCode());
        assertEquals("测试用例", saved.getCaseName());
    }
    
    @Test
    @DisplayName("根据ID查询用例-成功")
    void testFindById_Success() {
        // Given
        TestCase saved = testCaseRepository.save(testCase);
        
        // When
        Optional<TestCase> found = testCaseRepository.findById(saved.getId());
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("CASE-20240101-001", found.get().getCaseCode());
    }
    
    @Test
    @DisplayName("根据ID查询用例-不存在")
    void testFindById_NotFound() {
        // When
        Optional<TestCase> found = testCaseRepository.findById(999L);
        
        // Then
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("根据用例编码查询-成功")
    void testFindByCaseCode_Success() {
        // Given
        testCaseRepository.save(testCase);
        
        // When
        Optional<TestCase> found = testCaseRepository.findByCaseCode("CASE-20240101-001");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("CASE-20240101-001", found.get().getCaseCode());
    }
    
    @Test
    @DisplayName("根据用例编码查询-不存在")
    void testFindByCaseCode_NotFound() {
        // When
        Optional<TestCase> found = testCaseRepository.findByCaseCode("CASE-NOT-EXIST");
        
        // Then
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("根据需求ID查询用例列表-成功")
    void testFindByRequirementId_Success() {
        // Given
        testCaseRepository.save(testCase);
        
        TestCase case2 = new TestCase();
        case2.setCaseCode("CASE-20240101-002");
        case2.setCaseName("用例2");
        case2.setRequirementId(requirement.getId());
        case2.setCaseStatus(CaseStatus.DRAFT.name());
        case2.setCreateTime(LocalDateTime.now());
        testCaseRepository.save(case2);
        
        // When
        List<TestCase> found = testCaseRepository.findByRequirementId(requirement.getId());
        
        // Then
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(c -> c.getRequirementId().equals(requirement.getId())));
    }
    
    @Test
    @DisplayName("根据需求ID查询用例列表-无结果")
    void testFindByRequirementId_Empty() {
        // When
        List<TestCase> found = testCaseRepository.findByRequirementId(999L);
        
        // Then
        assertTrue(found.isEmpty());
    }
    
    @Test
    @DisplayName("查询指定前缀的用例编码列表-成功")
    void testFindByCaseCodeStartingWithOrderByIdDesc_Success() {
        // Given
        TestCase case1 = testCaseRepository.save(testCase);
        
        TestCase case2 = new TestCase();
        case2.setCaseCode("CASE-20240101-002");
        case2.setCaseName("用例2");
        case2.setRequirementId(requirement.getId());
        case2.setCaseStatus(CaseStatus.DRAFT.name());
        case2.setCreateTime(LocalDateTime.now());
        TestCase savedCase2 = testCaseRepository.save(case2);
        
        TestCase case3 = new TestCase();
        case3.setCaseCode("CASE-20240102-001");
        case3.setCaseName("用例3");
        case3.setRequirementId(requirement.getId());
        case3.setCaseStatus(CaseStatus.DRAFT.name());
        case3.setCreateTime(LocalDateTime.now());
        testCaseRepository.save(case3);
        
        // When
        List<TestCase> found = testCaseRepository.findByCaseCodeStartingWithOrderByIdDesc("CASE-20240101");
        
        // Then
        assertEquals(2, found.size());
        assertEquals("CASE-20240101-002", found.get(0).getCaseCode()); // 按ID降序
        assertEquals("CASE-20240101-001", found.get(1).getCaseCode());
    }
    
    @Test
    @DisplayName("分页查询用例列表-无过滤条件")
    void testFindWithFilters_NoFilters() {
        // Given
        testCaseRepository.save(testCase);
        
        TestCase case2 = new TestCase();
        case2.setCaseCode("CASE-20240101-002");
        case2.setCaseName("用例2");
        case2.setRequirementId(requirement.getId());
        case2.setCaseStatus(CaseStatus.DRAFT.name());
        case2.setCreateTime(LocalDateTime.now());
        testCaseRepository.save(case2);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<TestCase> page = testCaseRepository.findWithFilters(null, null, null, pageable);
        
        // Then
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }
    
    @Test
    @DisplayName("分页查询用例列表-按名称过滤")
    void testFindWithFilters_ByName() {
        // Given
        testCaseRepository.save(testCase);
        
        TestCase case2 = new TestCase();
        case2.setCaseCode("CASE-20240101-002");
        case2.setCaseName("其他用例");
        case2.setRequirementId(requirement.getId());
        case2.setCaseStatus(CaseStatus.DRAFT.name());
        case2.setCreateTime(LocalDateTime.now());
        testCaseRepository.save(case2);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<TestCase> page = testCaseRepository.findWithFilters("测试", null, null, pageable);
        
        // Then
        assertEquals(1, page.getTotalElements());
        assertEquals("测试用例", page.getContent().get(0).getCaseName());
    }
    
    @Test
    @DisplayName("更新用例-成功")
    void testUpdate_Success() {
        // Given
        TestCase saved = testCaseRepository.save(testCase);
        
        // When
        saved.setCaseName("更新后的用例名称");
        TestCase updated = testCaseRepository.save(saved);
        
        // Then
        assertEquals("更新后的用例名称", updated.getCaseName());
    }
    
    @Test
    @DisplayName("删除用例-成功")
    void testDelete_Success() {
        // Given
        TestCase saved = testCaseRepository.save(testCase);
        Long id = saved.getId();
        
        // When
        testCaseRepository.delete(saved);
        
        // Then
        Optional<TestCase> found = testCaseRepository.findById(id);
        assertFalse(found.isPresent());
    }
}

