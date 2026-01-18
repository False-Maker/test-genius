package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.enums.CaseStatus;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.service.CacheService;
import com.sinosoft.testdesign.service.SpecificationCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试用例管理服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("测试用例管理服务测试")
class TestCaseServiceImplTest {
    
    @Mock
    private TestCaseRepository testCaseRepository;
    
    @Mock
    private RequirementRepository requirementRepository;
    
    @Mock
    private CacheService cacheService;
    
    @Mock
    private SpecificationCheckService specificationCheckService;
    
    @InjectMocks
    private TestCaseServiceImpl testCaseService;
    
    private TestCase testCase;
    private TestRequirement testRequirement;
    
    @BeforeEach
    void setUp() {
        testRequirement = new TestRequirement();
        testRequirement.setId(1L);
        testRequirement.setRequirementCode("REQ-20240101-001");
        testRequirement.setRequirementName("测试需求");
        
        testCase = new TestCase();
        testCase.setId(1L);
        testCase.setCaseCode("CASE-20240101-001");
        testCase.setCaseName("测试用例");
        testCase.setRequirementId(1L);
        testCase.setCaseStatus(CaseStatus.DRAFT.name());
        testCase.setTestStep("1. 登录系统\n2. 执行操作\n3. 验证结果");
        testCase.setExpectedResult("操作成功");
        testCase.setVersion(1);
    }
    
    @Test
    @DisplayName("创建用例-成功")
    void testCreateTestCase_Success() {
        // Given
        TestCase newTestCase = new TestCase();
        newTestCase.setCaseName("新用例");
        newTestCase.setTestStep("测试步骤");
        newTestCase.setExpectedResult("预期结果");
        newTestCase.setRequirementId(1L);
        
        when(requirementRepository.findById(1L))
            .thenReturn(Optional.of(testRequirement));
        when(testCaseRepository.findByCaseCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(testCaseRepository.save(any(TestCase.class)))
            .thenAnswer(invocation -> {
                TestCase tc = invocation.getArgument(0);
                tc.setId(1L);
                return tc;
            });
        
        // When
        TestCase result = testCaseService.createTestCase(newTestCase);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getCaseCode());
        assertTrue(result.getCaseCode().startsWith("CASE-"));
        assertEquals(CaseStatus.DRAFT.name(), result.getCaseStatus());
        assertEquals(1, result.getVersion());
        verify(testCaseRepository, times(1)).save(any(TestCase.class));
    }
    
    @Test
    @DisplayName("创建用例-用例名称为空")
    void testCreateTestCase_NameEmpty() {
        // Given
        TestCase newTestCase = new TestCase();
        newTestCase.setCaseName("");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testCaseService.createTestCase(newTestCase);
        });
        
        assertEquals("用例名称不能为空", exception.getMessage());
        verify(testCaseRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建用例-关联需求不存在")
    void testCreateTestCase_RequirementNotFound() {
        // Given
        TestCase newTestCase = new TestCase();
        newTestCase.setCaseName("新用例");
        newTestCase.setRequirementId(999L);
        
        when(requirementRepository.findById(999L))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testCaseService.createTestCase(newTestCase);
        });
        
        assertEquals("关联的需求不存在", exception.getMessage());
        verify(testCaseRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新用例-成功")
    void testUpdateTestCase_Success() {
        // Given
        Long id = 1L;
        TestCase updateTestCase = new TestCase();
        updateTestCase.setCaseName("更新后的用例名称");
        updateTestCase.setTestStep("更新后的测试步骤");
        
        when(testCaseRepository.findById(id))
            .thenReturn(Optional.of(testCase));
        when(testCaseRepository.save(any(TestCase.class)))
            .thenReturn(testCase);
        
        // When
        TestCase result = testCaseService.updateTestCase(id, updateTestCase);
        
        // Then
        assertNotNull(result);
        assertEquals("更新后的用例名称", result.getCaseName());
        verify(testCaseRepository, times(1)).findById(id);
        verify(testCaseRepository, times(1)).save(any(TestCase.class));
        verify(cacheService, atLeastOnce()).delete(anyString());
    }
    
    @Test
    @DisplayName("更新用例-用例不存在")
    void testUpdateTestCase_NotFound() {
        // Given
        Long id = 999L;
        TestCase updateTestCase = new TestCase();
        
        when(testCaseRepository.findById(id))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testCaseService.updateTestCase(id, updateTestCase);
        });
        
        assertEquals("测试用例不存在", exception.getMessage());
        verify(testCaseRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新用例状态-成功")
    void testUpdateCaseStatus_Success() {
        // Given
        Long id = 1L;
        String newStatus = CaseStatus.PENDING_REVIEW.name();
        
        when(testCaseRepository.findById(id))
            .thenReturn(Optional.of(testCase));
        when(testCaseRepository.save(any(TestCase.class)))
            .thenReturn(testCase);
        
        // When
        TestCase result = testCaseService.updateCaseStatus(id, newStatus);
        
        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getCaseStatus());
        verify(testCaseRepository, times(1)).findById(id);
        verify(testCaseRepository, times(1)).save(any(TestCase.class));
    }
    
    @Test
    @DisplayName("审核用例-通过")
    void testReviewTestCase_Approved() {
        // Given
        Long id = 1L;
        String reviewResult = "PASS";
        String reviewComment = "审核通过";
        testCase.setCaseStatus(CaseStatus.PENDING_REVIEW.name());
        
        when(testCaseRepository.findById(id))
            .thenReturn(Optional.of(testCase));
        when(testCaseRepository.save(any(TestCase.class)))
            .thenReturn(testCase);
        
        // When
        TestCase result = testCaseService.reviewTestCase(id, reviewResult, reviewComment);
        
        // Then
        assertNotNull(result);
        assertEquals(CaseStatus.REVIEWED.name(), result.getCaseStatus());
        verify(testCaseRepository, times(1)).findById(id);
        verify(testCaseRepository, times(1)).save(any(TestCase.class));
    }
    
    @Test
    @DisplayName("审核用例-不通过")
    void testReviewTestCase_Rejected() {
        // Given
        Long id = 1L;
        String reviewResult = "REJECT";
        String reviewComment = "用例不完整";
        testCase.setCaseStatus(CaseStatus.PENDING_REVIEW.name());
        
        when(testCaseRepository.findById(id))
            .thenReturn(Optional.of(testCase));
        when(testCaseRepository.save(any(TestCase.class)))
            .thenReturn(testCase);
        
        // When
        TestCase result = testCaseService.reviewTestCase(id, reviewResult, reviewComment);
        
        // Then
        assertNotNull(result);
        assertEquals(CaseStatus.DRAFT.name(), result.getCaseStatus());
        verify(testCaseRepository, times(1)).findById(id);
        verify(testCaseRepository, times(1)).save(any(TestCase.class));
    }
    
    @Test
    @DisplayName("删除用例-成功")
    void testDeleteTestCase_Success() {
        // Given
        Long id = 1L;
        when(testCaseRepository.findById(id))
            .thenReturn(Optional.of(testCase));
        doNothing().when(testCaseRepository).deleteById(id);
        
        // When
        testCaseService.deleteTestCase(id);
        
        // Then
        verify(testCaseRepository, times(1)).findById(id);
        verify(testCaseRepository, times(1)).deleteById(id);
    }
    
    @Test
    @DisplayName("删除用例-已审核状态不能删除")
    void testDeleteTestCase_ReviewedCannotDelete() {
        // Given
        Long id = 1L;
        testCase.setCaseStatus(CaseStatus.REVIEWED.name());
        
        when(testCaseRepository.findById(id))
            .thenReturn(Optional.of(testCase));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            testCaseService.deleteTestCase(id);
        });
        
        assertEquals("已审核的用例不能删除，请先将其废弃", exception.getMessage());
        verify(testCaseRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("分页查询用例列表-成功")
    void testGetTestCaseList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TestCase> testCases = new ArrayList<>();
        testCases.add(testCase);
        Page<TestCase> page = new PageImpl<>(testCases, pageable, 1);
        
        when(testCaseRepository.findWithFilters(eq((String) null), eq((String) null), eq((Long) null), eq(pageable)))
            .thenReturn(page);
        
        // When
        Page<TestCase> result = testCaseService.getTestCaseList(pageable, null, null, null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(testCaseRepository, times(1)).findWithFilters(eq((String) null), eq((String) null), eq((Long) null), eq(pageable));
    }
}

