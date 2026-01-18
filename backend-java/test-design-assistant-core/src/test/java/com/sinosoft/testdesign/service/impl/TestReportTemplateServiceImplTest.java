package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestReportTemplate;
import com.sinosoft.testdesign.repository.TestReportTemplateRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试报告模板服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("测试报告模板服务测试")
class TestReportTemplateServiceImplTest {
    
    @Mock
    private TestReportTemplateRepository templateRepository;
    
    @InjectMocks
    private TestReportTemplateServiceImpl templateService;
    
    private TestReportTemplate testTemplate;
    
    @BeforeEach
    void setUp() {
        testTemplate = new TestReportTemplate();
        testTemplate.setId(1L);
        testTemplate.setTemplateCode("TMP-20240117-001");
        testTemplate.setTemplateName("测试报告模板");
        testTemplate.setTemplateType("EXECUTION");
        testTemplate.setTemplateContent("{\"sections\": []}");
        testTemplate.setFileFormat("WORD");
        testTemplate.setIsActive("1");
        testTemplate.setIsDefault("0");
    }
    
    @Test
    @DisplayName("创建报告模板-成功")
    void testCreateTemplate_Success() {
        // Given
        TestReportTemplate newTemplate = new TestReportTemplate();
        newTemplate.setTemplateName("新报告模板");
        newTemplate.setTemplateType("EXECUTION");
        newTemplate.setTemplateContent("{\"sections\": []}");
        newTemplate.setFileFormat("WORD");
        
        when(templateRepository.findByTemplateCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(templateRepository.save(any(TestReportTemplate.class)))
            .thenAnswer(invocation -> {
                TestReportTemplate template = invocation.getArgument(0);
                template.setId(1L);
                return template;
            });
        
        // When
        TestReportTemplate result = templateService.createTemplate(newTemplate);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getTemplateCode());
        assertTrue(result.getTemplateCode().startsWith("TMP-"));
        verify(templateRepository, times(1)).save(any(TestReportTemplate.class));
    }
    
    @Test
    @DisplayName("创建报告模板-模板名称为空")
    void testCreateTemplate_TemplateNameEmpty() {
        // Given
        TestReportTemplate newTemplate = new TestReportTemplate();
        newTemplate.setTemplateName("");
        newTemplate.setTemplateType("EXECUTION");
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            templateService.createTemplate(newTemplate);
        });
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新报告模板-成功")
    void testUpdateTemplate_Success() {
        // Given
        TestReportTemplate updateTemplate = new TestReportTemplate();
        updateTemplate.setTemplateName("更新后的模板名称");
        updateTemplate.setTemplateType("EXECUTION");
        
        when(templateRepository.findById(1L))
            .thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(TestReportTemplate.class)))
            .thenReturn(testTemplate);
        
        // When
        TestReportTemplate result = templateService.updateTemplate(1L, updateTemplate);
        
        // Then
        assertNotNull(result);
        verify(templateRepository, times(1)).save(any(TestReportTemplate.class));
    }
    
    @Test
    @DisplayName("更新报告模板-模板不存在")
    void testUpdateTemplate_NotFound() {
        // Given
        TestReportTemplate updateTemplate = new TestReportTemplate();
        updateTemplate.setTemplateName("更新后的模板名称");
        
        when(templateRepository.findById(1L))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            templateService.updateTemplate(1L, updateTemplate);
        });
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("查询模板-根据ID")
    void testGetTemplateById_Success() {
        // Given
        when(templateRepository.findById(1L))
            .thenReturn(Optional.of(testTemplate));
        
        // When
        TestReportTemplate result = templateService.getTemplateById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TMP-20240117-001", result.getTemplateCode());
    }
    
    @Test
    @DisplayName("查询模板-根据编码")
    void testGetTemplateByCode_Success() {
        // Given
        when(templateRepository.findByTemplateCode("TMP-20240117-001"))
            .thenReturn(Optional.of(testTemplate));
        
        // When
        TestReportTemplate result = templateService.getTemplateByCode("TMP-20240117-001");
        
        // Then
        assertNotNull(result);
        assertEquals("TMP-20240117-001", result.getTemplateCode());
    }
    
    @Test
    @DisplayName("分页查询模板列表")
    void testGetTemplateList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<TestReportTemplate> templates = new ArrayList<>();
        templates.add(testTemplate);
        Page<TestReportTemplate> page = new PageImpl<>(templates, pageable, 1);
        
        when(templateRepository.findAll(pageable))
            .thenReturn(page);
        
        // When
        Page<TestReportTemplate> result = templateService.getTemplateList(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(templateRepository, times(1)).findAll(pageable);
    }
    
    @Test
    @DisplayName("删除模板-成功")
    void testDeleteTemplate_Success() {
        // Given
        when(templateRepository.findById(1L))
            .thenReturn(Optional.of(testTemplate));
        doNothing().when(templateRepository).delete(any(TestReportTemplate.class));
        
        // When
        templateService.deleteTemplate(1L);
        
        // Then
        verify(templateRepository, times(1)).delete(any(TestReportTemplate.class));
    }
    
    @Test
    @DisplayName("启用/禁用模板-成功")
    void testToggleTemplateStatus_Success() {
        // Given
        when(templateRepository.findById(1L))
            .thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(TestReportTemplate.class)))
            .thenReturn(testTemplate);
        
        // When
        TestReportTemplate result = templateService.toggleTemplateStatus(1L, "0");
        
        // Then
        assertNotNull(result);
        verify(templateRepository, times(1)).save(any(TestReportTemplate.class));
    }
    
    @Test
    @DisplayName("设置默认模板-成功")
    void testSetDefaultTemplate_Success() {
        // Given
        when(templateRepository.findById(1L))
            .thenReturn(Optional.of(testTemplate));
        when(templateRepository.findByTemplateTypeAndIsDefaultAndIsActive("EXECUTION", "1", "1"))
            .thenReturn(Optional.empty());
        when(templateRepository.save(any(TestReportTemplate.class)))
            .thenReturn(testTemplate);
        
        // When
        TestReportTemplate result = templateService.setDefaultTemplate(1L, "EXECUTION");
        
        // Then
        assertNotNull(result);
        verify(templateRepository, times(1)).save(any(TestReportTemplate.class));
    }
    
    @Test
    @DisplayName("根据类型查询启用的模板列表")
    void testGetActiveTemplatesByType_Success() {
        // Given
        List<TestReportTemplate> templates = new ArrayList<>();
        templates.add(testTemplate);
        
        when(templateRepository.findByTemplateTypeAndIsActive("EXECUTION", "1"))
            .thenReturn(templates);
        
        // When
        List<TestReportTemplate> result = templateService.getActiveTemplatesByType("EXECUTION");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EXECUTION", result.get(0).getTemplateType());
    }
    
    @Test
    @DisplayName("根据类型查询默认模板")
    void testGetDefaultTemplateByType_Success() {
        // Given
        testTemplate.setIsDefault("1");
        List<TestReportTemplate> templates = new ArrayList<>();
        templates.add(testTemplate);
        
        when(templateRepository.findByTemplateTypeAndIsDefaultAndIsActive("EXECUTION", "1", "1"))
            .thenReturn(Optional.of(testTemplate));
        
        // When
        TestReportTemplate result = templateService.getDefaultTemplateByType("EXECUTION");
        
        // Then
        assertNotNull(result);
        assertEquals("1", result.getIsDefault());
    }
}

