package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestRequirement;
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
 * 需求Repository测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("需求Repository测试")
class RequirementRepositoryTest {
    
    @Autowired
    private RequirementRepository requirementRepository;
    
    private TestRequirement testRequirement;
    
    @BeforeEach
    void setUp() {
        requirementRepository.deleteAll();
        
        testRequirement = new TestRequirement();
        testRequirement.setRequirementCode("REQ-20240101-001");
        testRequirement.setRequirementName("测试需求");
        testRequirement.setRequirementType("新功能");
        testRequirement.setRequirementDescription("需求描述");
        testRequirement.setRequirementStatus(RequirementStatus.DRAFT.name());
        testRequirement.setCreateTime(LocalDateTime.now());
        testRequirement.setUpdateTime(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("保存需求-成功")
    void testSave_Success() {
        // When
        TestRequirement saved = requirementRepository.save(testRequirement);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("REQ-20240101-001", saved.getRequirementCode());
        assertEquals("测试需求", saved.getRequirementName());
    }
    
    @Test
    @DisplayName("根据ID查询需求-成功")
    void testFindById_Success() {
        // Given
        TestRequirement saved = requirementRepository.save(testRequirement);
        
        // When
        Optional<TestRequirement> found = requirementRepository.findById(saved.getId());
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("REQ-20240101-001", found.get().getRequirementCode());
    }
    
    @Test
    @DisplayName("根据ID查询需求-不存在")
    void testFindById_NotFound() {
        // When
        Optional<TestRequirement> found = requirementRepository.findById(999L);
        
        // Then
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("根据需求编码查询-成功")
    void testFindByRequirementCode_Success() {
        // Given
        requirementRepository.save(testRequirement);
        
        // When
        Optional<TestRequirement> found = requirementRepository.findByRequirementCode("REQ-20240101-001");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("REQ-20240101-001", found.get().getRequirementCode());
    }
    
    @Test
    @DisplayName("根据需求编码查询-不存在")
    void testFindByRequirementCode_NotFound() {
        // When
        Optional<TestRequirement> found = requirementRepository.findByRequirementCode("REQ-NOT-EXIST");
        
        // Then
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("查询指定前缀的需求编码列表-成功")
    void testFindByRequirementCodeStartingWithOrderByIdDesc_Success() {
        // Given
        TestRequirement req1 = new TestRequirement();
        req1.setRequirementCode("REQ-20240101-001");
        req1.setRequirementName("需求1");
        req1.setRequirementStatus(RequirementStatus.DRAFT.name());
        req1.setCreateTime(LocalDateTime.now());
        requirementRepository.save(req1);
        
        TestRequirement req2 = new TestRequirement();
        req2.setRequirementCode("REQ-20240101-002");
        req2.setRequirementName("需求2");
        req2.setRequirementStatus(RequirementStatus.DRAFT.name());
        req2.setCreateTime(LocalDateTime.now());
        requirementRepository.save(req2);
        
        TestRequirement req3 = new TestRequirement();
        req3.setRequirementCode("REQ-20240102-001");
        req3.setRequirementName("需求3");
        req3.setRequirementStatus(RequirementStatus.DRAFT.name());
        req3.setCreateTime(LocalDateTime.now());
        requirementRepository.save(req3);
        
        // When
        List<TestRequirement> found = requirementRepository.findByRequirementCodeStartingWithOrderByIdDesc("REQ-20240101");
        
        // Then
        assertEquals(2, found.size());
        assertEquals("REQ-20240101-002", found.get(0).getRequirementCode()); // 按ID降序
        assertEquals("REQ-20240101-001", found.get(1).getRequirementCode());
    }
    
    @Test
    @DisplayName("分页查询需求列表-无过滤条件")
    void testFindWithFilters_NoFilters() {
        // Given
        requirementRepository.save(testRequirement);
        
        TestRequirement req2 = new TestRequirement();
        req2.setRequirementCode("REQ-20240101-002");
        req2.setRequirementName("需求2");
        req2.setRequirementStatus(RequirementStatus.DRAFT.name());
        req2.setCreateTime(LocalDateTime.now());
        requirementRepository.save(req2);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<TestRequirement> page = requirementRepository.findWithFilters(null, null, pageable);
        
        // Then
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }
    
    @Test
    @DisplayName("分页查询需求列表-按名称过滤")
    void testFindWithFilters_ByName() {
        // Given
        requirementRepository.save(testRequirement);
        
        TestRequirement req2 = new TestRequirement();
        req2.setRequirementCode("REQ-20240101-002");
        req2.setRequirementName("其他需求");
        req2.setRequirementStatus(RequirementStatus.DRAFT.name());
        req2.setCreateTime(LocalDateTime.now());
        requirementRepository.save(req2);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<TestRequirement> page = requirementRepository.findWithFilters("测试", null, pageable);
        
        // Then
        assertEquals(1, page.getTotalElements());
        assertEquals("测试需求", page.getContent().get(0).getRequirementName());
    }
    
    @Test
    @DisplayName("分页查询需求列表-按状态过滤")
    void testFindWithFilters_ByStatus() {
        // Given
        requirementRepository.save(testRequirement);
        
        TestRequirement req2 = new TestRequirement();
        req2.setRequirementCode("REQ-20240101-002");
        req2.setRequirementName("需求2");
        req2.setRequirementStatus(RequirementStatus.APPROVED.name());
        req2.setCreateTime(LocalDateTime.now());
        requirementRepository.save(req2);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<TestRequirement> page = requirementRepository.findWithFilters(
            null, RequirementStatus.DRAFT.name(), pageable);
        
        // Then
        assertEquals(1, page.getTotalElements());
        assertEquals(RequirementStatus.DRAFT.name(), page.getContent().get(0).getRequirementStatus());
    }
    
    @Test
    @DisplayName("批量查询需求-根据ID列表")
    void testFindByIds_Success() {
        // Given
        TestRequirement req1 = requirementRepository.save(testRequirement);
        
        TestRequirement req2 = new TestRequirement();
        req2.setRequirementCode("REQ-20240101-002");
        req2.setRequirementName("需求2");
        req2.setRequirementStatus(RequirementStatus.DRAFT.name());
        req2.setCreateTime(LocalDateTime.now());
        TestRequirement savedReq2 = requirementRepository.save(req2);
        
        // When
        List<TestRequirement> found = requirementRepository.findByIds(
            List.of(req1.getId(), savedReq2.getId()));
        
        // Then
        assertEquals(2, found.size());
    }
    
    @Test
    @DisplayName("更新需求-成功")
    void testUpdate_Success() {
        // Given
        TestRequirement saved = requirementRepository.save(testRequirement);
        
        // When
        saved.setRequirementName("更新后的需求名称");
        TestRequirement updated = requirementRepository.save(saved);
        
        // Then
        assertEquals("更新后的需求名称", updated.getRequirementName());
    }
    
    @Test
    @DisplayName("删除需求-成功")
    void testDelete_Success() {
        // Given
        TestRequirement saved = requirementRepository.save(testRequirement);
        Long id = saved.getId();
        
        // When
        requirementRepository.delete(saved);
        
        // Then
        Optional<TestRequirement> found = requirementRepository.findById(id);
        assertFalse(found.isPresent());
    }
}

