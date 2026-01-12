package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.enums.RequirementStatus;
import com.sinosoft.testdesign.repository.RequirementRepository;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 需求管理服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("需求管理服务测试")
class RequirementServiceImplTest {
    
    @Mock
    private RequirementRepository requirementRepository;
    
    @InjectMocks
    private RequirementServiceImpl requirementService;
    
    private TestRequirement testRequirement;
    
    @BeforeEach
    void setUp() {
        testRequirement = new TestRequirement();
        testRequirement.setId(1L);
        testRequirement.setRequirementCode("REQ-20240101-001");
        testRequirement.setRequirementName("测试需求");
        testRequirement.setRequirementType("新功能");
        testRequirement.setRequirementDescription("这是一个测试需求");
        testRequirement.setRequirementStatus(RequirementStatus.DRAFT.name());
        testRequirement.setBusinessModule("投保模块");
        testRequirement.setVersion(1);
    }
    
    @Test
    @DisplayName("创建需求-成功")
    void testCreateRequirement_Success() {
        // Given
        TestRequirement newRequirement = new TestRequirement();
        newRequirement.setRequirementName("新需求");
        newRequirement.setRequirementType("新功能");
        
        when(requirementRepository.findByRequirementCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(requirementRepository.save(any(TestRequirement.class)))
            .thenAnswer(invocation -> {
                TestRequirement req = invocation.getArgument(0);
                req.setId(1L);
                return req;
            });
        
        // When
        TestRequirement result = requirementService.createRequirement(newRequirement);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getRequirementCode());
        assertTrue(result.getRequirementCode().startsWith("REQ-"));
        assertEquals(RequirementStatus.DRAFT.name(), result.getRequirementStatus());
        assertEquals(1, result.getVersion());
        verify(requirementRepository, times(1)).save(any(TestRequirement.class));
    }
    
    @Test
    @DisplayName("创建需求-需求名称为空")
    void testCreateRequirement_NameEmpty() {
        // Given
        TestRequirement newRequirement = new TestRequirement();
        newRequirement.setRequirementName("");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementService.createRequirement(newRequirement);
        });
        
        assertEquals("需求名称不能为空", exception.getMessage());
        verify(requirementRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建需求-编码已存在")
    void testCreateRequirement_CodeExists() {
        // Given
        TestRequirement newRequirement = new TestRequirement();
        newRequirement.setRequirementName("新需求");
        newRequirement.setRequirementCode("REQ-20240101-001");
        
        when(requirementRepository.findByRequirementCode("REQ-20240101-001"))
            .thenReturn(Optional.of(testRequirement));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementService.createRequirement(newRequirement);
        });
        
        assertEquals("需求编码已存在: REQ-20240101-001", exception.getMessage());
        verify(requirementRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新需求-成功")
    void testUpdateRequirement_Success() {
        // Given
        Long id = 1L;
        TestRequirement updateRequirement = new TestRequirement();
        updateRequirement.setRequirementName("更新后的需求名称");
        updateRequirement.setRequirementType("优化");
        
        when(requirementRepository.findById(id))
            .thenReturn(Optional.of(testRequirement));
        when(requirementRepository.save(any(TestRequirement.class)))
            .thenReturn(testRequirement);
        
        // When
        TestRequirement result = requirementService.updateRequirement(id, updateRequirement);
        
        // Then
        assertNotNull(result);
        assertEquals("更新后的需求名称", result.getRequirementName());
        assertEquals("优化", result.getRequirementType());
        verify(requirementRepository, times(1)).findById(id);
        verify(requirementRepository, times(1)).save(any(TestRequirement.class));
    }
    
    @Test
    @DisplayName("更新需求-需求不存在")
    void testUpdateRequirement_NotFound() {
        // Given
        Long id = 999L;
        TestRequirement updateRequirement = new TestRequirement();
        
        when(requirementRepository.findById(id))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementService.updateRequirement(id, updateRequirement);
        });
        
        assertEquals("需求不存在", exception.getMessage());
        verify(requirementRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新需求-不允许修改编码")
    void testUpdateRequirement_CannotModifyCode() {
        // Given
        Long id = 1L;
        TestRequirement updateRequirement = new TestRequirement();
        updateRequirement.setRequirementName("更新后的需求名称"); // 必须设置名称，否则会先抛出"需求名称不能为空"
        updateRequirement.setRequirementCode("REQ-20240101-999"); // 尝试修改编码
        
        when(requirementRepository.findById(id))
            .thenReturn(Optional.of(testRequirement));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementService.updateRequirement(id, updateRequirement);
        });
        
        assertEquals("不允许修改需求编码", exception.getMessage());
        verify(requirementRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新需求状态-成功")
    void testUpdateRequirementStatus_Success() {
        // Given
        Long id = 1L;
        String newStatus = RequirementStatus.REVIEWING.name();
        
        when(requirementRepository.findById(id))
            .thenReturn(Optional.of(testRequirement));
        when(requirementRepository.save(any(TestRequirement.class)))
            .thenReturn(testRequirement);
        
        // When
        TestRequirement result = requirementService.updateRequirementStatus(id, newStatus);
        
        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getRequirementStatus());
        verify(requirementRepository, times(1)).findById(id);
        verify(requirementRepository, times(1)).save(any(TestRequirement.class));
    }
    
    @Test
    @DisplayName("更新需求状态-状态流转不合法")
    void testUpdateRequirementStatus_InvalidTransition() {
        // Given
        Long id = 1L;
        String newStatus = RequirementStatus.APPROVED.name(); // 从DRAFT直接到APPROVED不合法
        
        when(requirementRepository.findById(id))
            .thenReturn(Optional.of(testRequirement));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementService.updateRequirementStatus(id, newStatus);
        });
        
        assertTrue(exception.getMessage().contains("需求状态不能从"));
        verify(requirementRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("查询需求-根据ID")
    void testGetRequirementById_Success() {
        // Given
        Long id = 1L;
        when(requirementRepository.findById(id))
            .thenReturn(Optional.of(testRequirement));
        
        // When
        TestRequirement result = requirementService.getRequirementById(id);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("REQ-20240101-001", result.getRequirementCode());
        verify(requirementRepository, times(1)).findById(id);
    }
    
    @Test
    @DisplayName("查询需求-不存在")
    void testGetRequirementById_NotFound() {
        // Given
        Long id = 999L;
        when(requirementRepository.findById(id))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementService.getRequirementById(id);
        });
        
        assertEquals("需求不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("分页查询需求列表-成功")
    void testGetRequirementList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TestRequirement> requirements = new ArrayList<>();
        requirements.add(testRequirement);
        Page<TestRequirement> page = new PageImpl<>(requirements, pageable, 1);
        
        when(requirementRepository.findAll(any(Specification.class), eq(pageable)))
            .thenReturn(page);
        
        // When
        Page<TestRequirement> result = requirementService.getRequirementList(pageable, null, null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(requirementRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("分页查询需求列表-按名称搜索")
    void testGetRequirementList_ByName() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String requirementName = "测试";
        List<TestRequirement> requirements = new ArrayList<>();
        requirements.add(testRequirement);
        Page<TestRequirement> page = new PageImpl<>(requirements, pageable, 1);
        
        when(requirementRepository.findAll(any(Specification.class), eq(pageable)))
            .thenReturn(page);
        
        // When
        Page<TestRequirement> result = requirementService.getRequirementList(pageable, requirementName, null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(requirementRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }
    
    @Test
    @DisplayName("删除需求-成功")
    void testDeleteRequirement_Success() {
        // Given
        Long id = 1L;
        when(requirementRepository.findById(id))
            .thenReturn(Optional.of(testRequirement));
        doNothing().when(requirementRepository).deleteById(id);
        
        // When
        requirementService.deleteRequirement(id);
        
        // Then
        verify(requirementRepository, times(1)).findById(id);
        verify(requirementRepository, times(1)).deleteById(id);
    }
    
    @Test
    @DisplayName("删除需求-已通过状态不能删除")
    void testDeleteRequirement_ApprovedCannotDelete() {
        // Given
        Long id = 1L;
        testRequirement.setRequirementStatus(RequirementStatus.APPROVED.name());
        
        when(requirementRepository.findById(id))
            .thenReturn(Optional.of(testRequirement));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementService.deleteRequirement(id);
        });
        
        assertEquals("已通过或已关闭的需求不能删除", exception.getMessage());
        verify(requirementRepository, never()).deleteById(any());
    }
    
    @Test
    @DisplayName("删除需求-已关闭状态不能删除")
    void testDeleteRequirement_ClosedCannotDelete() {
        // Given
        Long id = 1L;
        testRequirement.setRequirementStatus(RequirementStatus.CLOSED.name());
        
        when(requirementRepository.findById(id))
            .thenReturn(Optional.of(testRequirement));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            requirementService.deleteRequirement(id);
        });
        
        assertEquals("已通过或已关闭的需求不能删除", exception.getMessage());
        verify(requirementRepository, never()).deleteById(any());
    }
}

