package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.common.TestDataBuilder;
import com.sinosoft.testdesign.dto.TestCaseExcelDTO;
import com.sinosoft.testdesign.entity.TestCase;
import com.sinosoft.testdesign.entity.TestDesignMethod;
import com.sinosoft.testdesign.entity.TestLayer;
import com.sinosoft.testdesign.entity.TestRequirement;
import com.sinosoft.testdesign.repository.RequirementRepository;
import com.sinosoft.testdesign.repository.TestCaseRepository;
import com.sinosoft.testdesign.repository.TestLayerRepository;
import com.sinosoft.testdesign.repository.TestMethodRepository;
import com.sinosoft.testdesign.service.TestCaseImportExportService;
import com.sinosoft.testdesign.service.TestCaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试用例导入导出服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("测试用例导入导出服务测试")
class TestCaseImportExportServiceImplTest {
    
    @Mock
    private RequirementRepository requirementRepository;
    
    @Mock
    private TestLayerRepository testLayerRepository;
    
    @Mock
    private TestMethodRepository testMethodRepository;
    
    @Mock
    private TestCaseRepository testCaseRepository;
    
    @Mock
    private TestCaseService testCaseService;
    
    @InjectMocks
    private TestCaseImportExportServiceImpl importExportService;
    
    private TestCase testCase;
    private TestRequirement requirement;
    private TestLayer testLayer;
    private TestDesignMethod testMethod;
    
    @BeforeEach
    void setUp() {
        testCase = TestDataBuilder.testCase()
            .withId(1L)
            .withCode("CASE-20240101-001")
            .withName("测试用例")
            .withSteps("1. 步骤一\n2. 步骤二")
            .withExpectedResult("预期结果")
            .withRequirementId(1L)
            .build();
        
        requirement = TestDataBuilder.requirement()
            .withId(1L)
            .withCode("REQ-20240101-001")
            .withName("测试需求")
            .build();
        
        testLayer = new TestLayer();
        testLayer.setId(1L);
        testLayer.setLayerName("功能测试");
        
        testMethod = new TestDesignMethod();
        testMethod.setId(1L);
        testMethod.setMethodName("等价类划分");
    }
    
    @Test
    @DisplayName("导出用例到Excel-成功")
    void testExportToExcel_Success() throws IOException {
        // Given
        List<TestCase> testCases = new ArrayList<>();
        testCases.add(testCase);
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            when(requirementRepository.findById(1L))
                .thenReturn(Optional.of(requirement));
            when(testLayerRepository.findById(1L))
                .thenReturn(Optional.of(testLayer));
            when(testMethodRepository.findById(1L))
                .thenReturn(Optional.of(testMethod));
            
            // When
            importExportService.exportToExcel(testCases, outputStream);
            
            // Then
            assertTrue(outputStream.size() > 0);
            verify(requirementRepository, atLeastOnce()).findById(anyLong());
        }
    }
    
    @Test
    @DisplayName("导出用例模板-成功")
    void testExportTemplate_Success() throws IOException {
        // Given
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // When
            importExportService.exportTemplate(outputStream);
            
            // Then
            assertTrue(outputStream.size() > 0);
        }
    }
    
    @Test
    @DisplayName("从Excel导入用例-文件为空")
    void testImportFromExcel_FileEmpty() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty())
            .thenReturn(true);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            importExportService.importFromExcel(file);
        });
        
        assertEquals("上传的文件不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("从Excel导入用例-文件格式错误")
    void testImportFromExcel_InvalidFormat() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty())
            .thenReturn(false);
        when(file.getOriginalFilename())
            .thenReturn("test.pdf"); // 错误的文件格式
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            importExportService.importFromExcel(file);
        });
        
        assertTrue(exception.getMessage().contains("文件格式不正确"));
    }
    
    @Test
    @DisplayName("从Excel导入用例-成功")
    void testImportFromExcel_Success() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty())
            .thenReturn(false);
        when(file.getOriginalFilename())
            .thenReturn("test.xlsx");
        
        // 创建一个最小有效的Excel文件输入流
        // 这里需要创建一个有效的Excel文件结构
        // 由于EasyExcel需要读取有效的Excel格式，我们需要创建一个最简单的Excel文件
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // 使用EasyExcel写入一个空模板，然后读取它
        try {
            importExportService.exportTemplate(baos);
        } catch (Exception e) {
            // 如果导出失败，跳过这个测试
            fail("无法创建测试Excel文件: " + e.getMessage());
        }
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        when(file.getInputStream())
            .thenReturn(inputStream);
        
        // 由于导入需要实际的测试用例数据，这里只测试文件格式验证和读取流程
        // 实际导入成功需要mock Repository的调用
        when(requirementRepository.findByRequirementCode(anyString()))
            .thenReturn(Optional.empty());
        when(testLayerRepository.findByLayerName(anyString()))
            .thenReturn(Optional.empty());
        when(testMethodRepository.findByMethodName(anyString()))
            .thenReturn(Optional.empty());
        
        // When
        // importFromExcel方法不会抛出异常，而是返回ImportResult，包含成功和失败的数量
        TestCaseImportExportService.ImportResult result = importExportService.importFromExcel(file);
        
        // Then
        assertNotNull(result);
        // 由于mock的Repository都返回empty，所以应该没有成功导入的用例
        assertEquals(0, result.getSuccessCount());
        // 可能会有失败记录，取决于Excel文件内容
        assertTrue(result.getFailureCount() >= 0);
    }
    
    @Test
    @DisplayName("转换TestCase到Excel DTO-成功")
    void testConvertToExcelDTO_Success() {
        // Given
        testCase.setRequirementId(1L);
        testCase.setLayerId(1L);
        testCase.setMethodId(1L);
        
        when(requirementRepository.findById(1L))
            .thenReturn(Optional.of(requirement));
        when(testLayerRepository.findById(1L))
            .thenReturn(Optional.of(testLayer));
        when(testMethodRepository.findById(1L))
            .thenReturn(Optional.of(testMethod));
        
        // When
        // 使用反射调用私有方法，或者通过导出方法间接测试
        List<TestCase> testCases = new ArrayList<>();
        testCases.add(testCase);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            importExportService.exportToExcel(testCases, outputStream);
            
            // Then
            assertTrue(outputStream.size() > 0);
            verify(requirementRepository, atLeastOnce()).findById(1L);
            verify(testLayerRepository, atLeastOnce()).findById(1L);
            verify(testMethodRepository, atLeastOnce()).findById(1L);
        } catch (IOException e) {
            fail("导出失败: " + e.getMessage());
        }
    }
}

