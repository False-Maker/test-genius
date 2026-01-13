package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.PromptTemplate;
import com.sinosoft.testdesign.repository.PromptTemplateRepository;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 提示词模板管理服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("提示词模板管理服务测试")
class PromptTemplateServiceImplTest {
    
    @Mock
    private PromptTemplateRepository templateRepository;
    
    @InjectMocks
    private PromptTemplateServiceImpl templateService;
    
    private PromptTemplate template;
    
    @BeforeEach
    void setUp() {
        template = new PromptTemplate();
        template.setId(1L);
        template.setTemplateCode("TMP-20240101-001");
        template.setTemplateName("功能测试模板");
        template.setTemplateContent("这是一个测试模板，包含变量{var1}和{var2}");
        template.setTemplateVariables("{\"var1\":\"变量1\",\"var2\":\"变量2\"}");
        template.setIsActive("1");
        template.setVersion(1);
    }
    
    @Test
    @DisplayName("创建模板-成功")
    void testCreateTemplate_Success() {
        // Given
        PromptTemplate newTemplate = new PromptTemplate();
        newTemplate.setTemplateName("新模板");
        newTemplate.setTemplateContent("模板内容{var}");
        
        when(templateRepository.findByTemplateCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        when(templateRepository.save(any(PromptTemplate.class)))
            .thenAnswer(invocation -> {
                PromptTemplate t = invocation.getArgument(0);
                t.setId(1L);
                return t;
            });
        
        // When
        PromptTemplate result = templateService.createTemplate(newTemplate);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getTemplateCode());
        assertTrue(result.getTemplateCode().startsWith("TMP-"));
        assertEquals("1", result.getIsActive());
        assertEquals(1, result.getVersion());
        verify(templateRepository, times(1)).save(any(PromptTemplate.class));
    }
    
    @Test
    @DisplayName("创建模板-模板名称为空")
    void testCreateTemplate_NameEmpty() {
        // Given
        PromptTemplate newTemplate = new PromptTemplate();
        newTemplate.setTemplateName("");
        newTemplate.setTemplateContent("模板内容");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            templateService.createTemplate(newTemplate);
        });
        
        assertEquals("模板名称不能为空", exception.getMessage());
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建模板-模板内容为空")
    void testCreateTemplate_ContentEmpty() {
        // Given
        PromptTemplate newTemplate = new PromptTemplate();
        newTemplate.setTemplateName("新模板");
        newTemplate.setTemplateContent("");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            templateService.createTemplate(newTemplate);
        });
        
        assertEquals("模板内容不能为空", exception.getMessage());
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建模板-编码已存在")
    void testCreateTemplate_CodeExists() {
        // Given
        PromptTemplate newTemplate = new PromptTemplate();
        newTemplate.setTemplateName("新模板");
        newTemplate.setTemplateContent("模板内容");
        newTemplate.setTemplateCode("TMP-20240101-001");
        
        when(templateRepository.findByTemplateCode("TMP-20240101-001"))
            .thenReturn(Optional.of(template));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            templateService.createTemplate(newTemplate);
        });
        
        assertEquals("模板编码已存在: TMP-20240101-001", exception.getMessage());
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("创建模板-变量定义格式错误")
    void testCreateTemplate_InvalidVariables() {
        // Given
        PromptTemplate newTemplate = new PromptTemplate();
        newTemplate.setTemplateName("新模板");
        newTemplate.setTemplateContent("模板内容");
        newTemplate.setTemplateVariables("invalid json");
        
        when(templateRepository.findByTemplateCodeStartingWithOrderByIdDesc(anyString()))
            .thenReturn(new ArrayList<>());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            templateService.createTemplate(newTemplate);
        });
        
        assertTrue(exception.getMessage().contains("模板变量定义格式不正确"));
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新模板-成功")
    void testUpdateTemplate_Success() {
        // Given
        Long id = 1L;
        PromptTemplate updateTemplate = new PromptTemplate();
        updateTemplate.setTemplateName("更新后的模板名称");
        updateTemplate.setTemplateContent("更新后的内容");
        
        when(templateRepository.findById(id))
            .thenReturn(Optional.of(template));
        when(templateRepository.save(any(PromptTemplate.class)))
            .thenReturn(template);
        
        // When
        PromptTemplate result = templateService.updateTemplate(id, updateTemplate);
        
        // Then
        assertNotNull(result);
        assertEquals("更新后的模板名称", result.getTemplateName());
        assertEquals(2, result.getVersion()); // 版本号自增
        verify(templateRepository, times(1)).findById(id);
        verify(templateRepository, times(1)).save(any(PromptTemplate.class));
    }
    
    @Test
    @DisplayName("更新模板-模板不存在")
    void testUpdateTemplate_NotFound() {
        // Given
        Long id = 999L;
        PromptTemplate updateTemplate = new PromptTemplate();
        
        when(templateRepository.findById(id))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            templateService.updateTemplate(id, updateTemplate);
        });
        
        assertEquals("模板不存在", exception.getMessage());
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("更新模板-不允许修改编码")
    void testUpdateTemplate_CannotModifyCode() {
        // Given
        Long id = 1L;
        PromptTemplate updateTemplate = new PromptTemplate();
        updateTemplate.setTemplateName("更新后的模板名称");
        updateTemplate.setTemplateCode("TMP-20240101-999"); // 尝试修改编码
        
        when(templateRepository.findById(id))
            .thenReturn(Optional.of(template));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            templateService.updateTemplate(id, updateTemplate);
        });
        
        assertEquals("不允许修改模板编码", exception.getMessage());
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("查询模板-根据ID")
    void testGetTemplateById_Success() {
        // Given
        Long id = 1L;
        when(templateRepository.findById(id))
            .thenReturn(Optional.of(template));
        
        // When
        PromptTemplate result = templateService.getTemplateById(id);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("TMP-20240101-001", result.getTemplateCode());
        verify(templateRepository, times(1)).findById(id);
    }
    
    @Test
    @DisplayName("查询模板-不存在")
    void testGetTemplateById_NotFound() {
        // Given
        Long id = 999L;
        when(templateRepository.findById(id))
            .thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            templateService.getTemplateById(id);
        });
        
        assertEquals("模板不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("分页查询模板列表-成功")
    void testGetTemplateList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<PromptTemplate> templates = new ArrayList<>();
        templates.add(template);
        Page<PromptTemplate> page = new PageImpl<>(templates, pageable, 1);
        
        when(templateRepository.findAll(pageable))
            .thenReturn(page);
        
        // When
        Page<PromptTemplate> result = templateService.getTemplateList(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(templateRepository, times(1)).findAll(pageable);
    }
    
    @Test
    @DisplayName("删除模板-成功")
    void testDeleteTemplate_Success() {
        // Given
        Long id = 1L;
        when(templateRepository.findById(id))
            .thenReturn(Optional.of(template));
        doNothing().when(templateRepository).deleteById(id);
        
        // When
        templateService.deleteTemplate(id);
        
        // Then
        verify(templateRepository, times(1)).findById(id);
        verify(templateRepository, times(1)).deleteById(id);
    }
    
    @Test
    @DisplayName("切换模板状态-成功")
    void testToggleTemplateStatus_Success() {
        // Given
        Long id = 1L;
        String newStatus = "0";
        
        when(templateRepository.findById(id))
            .thenReturn(Optional.of(template));
        when(templateRepository.save(any(PromptTemplate.class)))
            .thenReturn(template);
        
        // When
        PromptTemplate result = templateService.toggleTemplateStatus(id, newStatus);
        
        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getIsActive());
        assertEquals(2, result.getVersion()); // 版本号自增
        verify(templateRepository, times(1)).findById(id);
        verify(templateRepository, times(1)).save(any(PromptTemplate.class));
    }
    
    @Test
    @DisplayName("切换模板状态-状态值无效")
    void testToggleTemplateStatus_InvalidStatus() {
        // Given
        Long id = 1L;
        String invalidStatus = "2";
        
        when(templateRepository.findById(id))
            .thenReturn(Optional.of(template));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            templateService.toggleTemplateStatus(id, invalidStatus);
        });
        
        assertEquals("状态值必须是 0 或 1", exception.getMessage());
        verify(templateRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("生成提示词-成功")
    void testGeneratePrompt_Success() {
        // Given
        Long templateId = 1L;
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1", "值1");
        variables.put("var2", "值2");
        
        when(templateRepository.findById(templateId))
            .thenReturn(Optional.of(template));
        
        // When
        String result = templateService.generatePrompt(templateId, variables);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("值1"));
        assertTrue(result.contains("值2"));
        assertFalse(result.contains("{var1}"));
        assertFalse(result.contains("{var2}"));
        verify(templateRepository, times(1)).findById(templateId);
    }
    
    @Test
    @DisplayName("生成提示词-模板未启用")
    void testGeneratePrompt_TemplateNotActive() {
        // Given
        Long templateId = 1L;
        template.setIsActive("0"); // 未启用
        Map<String, Object> variables = new HashMap<>();
        
        when(templateRepository.findById(templateId))
            .thenReturn(Optional.of(template));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            templateService.generatePrompt(templateId, variables);
        });
        
        assertEquals("模板未启用，无法生成提示词", exception.getMessage());
    }
    
    @Test
    @DisplayName("生成提示词-变量未提供")
    void testGeneratePrompt_MissingVariables() {
        // Given
        String templateContent = "这是一个测试模板，包含变量{var1}和{var2}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1", "值1");
        // var2未提供
        
        // When
        String result = templateService.generatePrompt(templateContent, variables);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("值1"));
        assertTrue(result.contains("{var2}") || result.contains("")); // 未提供的变量会被替换为空字符串
    }
}

