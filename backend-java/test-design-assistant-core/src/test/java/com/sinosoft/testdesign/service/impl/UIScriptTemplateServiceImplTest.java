package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.UIScriptTemplate;
import com.sinosoft.testdesign.repository.UIScriptTemplateRepository;
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
 * UI脚本模板服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UI脚本模板服务测试")
class UIScriptTemplateServiceImplTest {
    
    @Mock
    private UIScriptTemplateRepository templateRepository;
    
    @InjectMocks
    private UIScriptTemplateServiceImpl templateService;
    
    private UIScriptTemplate testTemplate;
    
    @BeforeEach
    void setUp() {
        testTemplate = new UIScriptTemplate();
        testTemplate.setId(1L);
        testTemplate.setTemplateCode("TMP-20240117-001");
        testTemplate.setTemplateName("Selenium Python脚本模板");
        testTemplate.setTemplateType("SELENIUM");
        testTemplate.setScriptLanguage("PYTHON");
        testTemplate.setTemplateContent("from selenium import webdriver\n# {element_locator}");
        testTemplate.setIsActive("1");
    }
    
    @Test
    @DisplayName("创建脚本模板-成功")
    void testCreateTemplate_Success() {
        // Given
        UIScriptTemplate newTemplate = new UIScriptTemplate();
        newTemplate.setTemplateName("新脚本模板");
        newTemplate.setTemplateType("SELENIUM");
        newTemplate.setScriptLanguage("PYTHON");
        newTemplate.setTemplateContent("from selenium import webdriver");
        
        when(templateRepository.findByTemplateCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(templateRepository.save(any(UIScriptTemplate.class)))
            .thenAnswer(invocation -> {
                UIScriptTemplate template = invocation.getArgument(0);
                template.setId(1L);
                return template;
            });
        
        // When
        UIScriptTemplate result = templateService.createTemplate(newTemplate);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getTemplateCode());
        assertTrue(result.getTemplateCode().startsWith("TMP-"));
        verify(templateRepository, times(1)).save(any(UIScriptTemplate.class));
    }
    
    @Test
    @DisplayName("创建脚本模板-模板名称为空")
    void testCreateTemplate_TemplateNameEmpty() {
        // Given
        UIScriptTemplate newTemplate = new UIScriptTemplate();
        newTemplate.setTemplateName("");
        newTemplate.setTemplateType("SELENIUM");
        
        // When & Then
        assertThrows(BusinessException.class, () -> {
            templateService.createTemplate(newTemplate);
        });
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新脚本模板-成功")
    void testUpdateTemplate_Success() {
        // Given
        UIScriptTemplate updateTemplate = new UIScriptTemplate();
        updateTemplate.setTemplateName("更新后的模板名称");
        
        when(templateRepository.findById(1L))
            .thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(UIScriptTemplate.class)))
            .thenReturn(testTemplate);
        
        // When
        UIScriptTemplate result = templateService.updateTemplate(1L, updateTemplate);
        
        // Then
        assertNotNull(result);
        verify(templateRepository, times(1)).save(any(UIScriptTemplate.class));
    }
    
    @Test
    @DisplayName("更新脚本模板-模板不存在")
    void testUpdateTemplate_NotFound() {
        // Given
        UIScriptTemplate updateTemplate = new UIScriptTemplate();
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
    @DisplayName("查询脚本模板-根据ID")
    void testGetTemplateById_Success() {
        // Given
        when(templateRepository.findById(1L))
            .thenReturn(Optional.of(testTemplate));
        
        // When
        UIScriptTemplate result = templateService.getTemplateById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TMP-20240117-001", result.getTemplateCode());
    }
    
    @Test
    @DisplayName("查询脚本模板-根据编码")
    void testGetTemplateByCode_Success() {
        // Given
        when(templateRepository.findByTemplateCode("TMP-20240117-001"))
            .thenReturn(Optional.of(testTemplate));
        
        // When
        UIScriptTemplate result = templateService.getTemplateByCode("TMP-20240117-001");
        
        // Then
        assertNotNull(result);
        assertEquals("TMP-20240117-001", result.getTemplateCode());
    }
    
    @Test
    @DisplayName("分页查询脚本模板列表")
    void testGetTemplateList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<UIScriptTemplate> templates = new ArrayList<>();
        templates.add(testTemplate);
        Page<UIScriptTemplate> page = new PageImpl<>(templates, pageable, 1);
        
        when(templateRepository.findWithFilters(eq(null), eq(null), eq(null), eq(null), eq(pageable)))
            .thenReturn(page);
        
        // When
        Page<UIScriptTemplate> result = templateService.getTemplateList(pageable, null, null, null, null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(templateRepository, times(1)).findWithFilters(eq(null), eq(null), eq(null), eq(null), eq(pageable));
    }
    
    @Test
    @DisplayName("根据类型和语言查询启用的模板列表")
    void testGetActiveTemplatesByTypeAndLanguage_Success() {
        // Given
        List<UIScriptTemplate> templates = new ArrayList<>();
        templates.add(testTemplate);
        
        when(templateRepository.findByTemplateTypeAndScriptLanguageAndIsActive("SELENIUM", "PYTHON", "1"))
            .thenReturn(templates);
        
        // When
        List<UIScriptTemplate> result = templateService.getActiveTemplatesByTypeAndLanguage("SELENIUM", "PYTHON");
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SELENIUM", result.get(0).getTemplateType());
        assertEquals("PYTHON", result.get(0).getScriptLanguage());
    }
    
    @Test
    @DisplayName("更新模板状态-成功")
    void testUpdateTemplateStatus_Success() {
        // Given
        when(templateRepository.findById(1L))
            .thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(UIScriptTemplate.class)))
            .thenReturn(testTemplate);
        
        // When
        UIScriptTemplate result = templateService.updateTemplateStatus(1L, "0");
        
        // Then
        assertNotNull(result);
        verify(templateRepository, times(1)).save(any(UIScriptTemplate.class));
    }
    
    @Test
    @DisplayName("删除脚本模板-成功")
    void testDeleteTemplate_Success() {
        // Given
        when(templateRepository.findById(1L))
            .thenReturn(Optional.of(testTemplate));
        doNothing().when(templateRepository).delete(any(UIScriptTemplate.class));
        
        // When
        templateService.deleteTemplate(1L);
        
        // Then
        verify(templateRepository, times(1)).findById(1L);
        verify(templateRepository, times(1)).delete(any(UIScriptTemplate.class));
    }
}

