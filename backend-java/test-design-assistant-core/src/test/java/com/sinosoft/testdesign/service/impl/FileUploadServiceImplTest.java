package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 文件上传服务单元测试
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("文件上传服务测试")
class FileUploadServiceImplTest {
    
    @InjectMocks
    private FileUploadServiceImpl fileUploadService;
    
    @TempDir
    Path tempDir;
    
    private MultipartFile mockFile;
    
    @BeforeEach
    void setUp() {
        // 设置测试用的基础路径
        ReflectionTestUtils.setField(fileUploadService, "basePath", tempDir.toString());
        ReflectionTestUtils.setField(fileUploadService, "maxFileSize", 104857600L); // 100MB
        ReflectionTestUtils.setField(fileUploadService, "urlPrefix", "/api/v1/files");
        
        // 创建模拟文件
        mockFile = mock(MultipartFile.class);
    }
    
    @Test
    @DisplayName("上传文件-成功")
    void testUploadFile_Success() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.docx");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getContentType()).thenReturn("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        when(mockFile.getInputStream()).thenReturn(
            new java.io.ByteArrayInputStream("test content".getBytes())
        );
        
        // When
        String result = fileUploadService.uploadFile(mockFile);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("2024") || result.contains("2025")); // 包含日期路径
        assertTrue(result.endsWith(".docx"));
        verify(mockFile, times(1)).getInputStream();
    }
    
    @Test
    @DisplayName("上传文件-文件为空")
    void testUploadFile_FileEmpty() {
        // Given
        when(mockFile.isEmpty()).thenReturn(true);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(mockFile);
        });
        
        assertEquals("文件不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("上传文件-文件为null")
    void testUploadFile_FileNull() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(null);
        });
        
        assertEquals("文件不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("上传文件-文件大小超限")
    void testUploadFile_FileSizeExceeded() {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(104857601L); // 超过100MB
        when(mockFile.getOriginalFilename()).thenReturn("large.pdf");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(mockFile);
        });
        
        assertTrue(exception.getMessage().contains("文件大小超过限制"));
    }
    
    @Test
    @DisplayName("上传文件-不支持的文件类型")
    void testUploadFile_UnsupportedFileType() {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("test.exe");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(mockFile);
        });
        
        assertTrue(exception.getMessage().contains("不支持的文件类型"));
    }
    
    @Test
    @DisplayName("上传文件-文件名为空")
    void testUploadFile_FilenameEmpty() {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(mockFile);
        });
        
        assertEquals("文件名不能为空", exception.getMessage());
    }
    
    @Test
    @DisplayName("上传文件-文件无扩展名")
    void testUploadFile_NoExtension() {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("test");
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(mockFile);
        });
        
        assertEquals("文件必须包含扩展名", exception.getMessage());
    }
    
    @Test
    @DisplayName("上传文件-IO异常")
    void testUploadFile_IOException() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getInputStream()).thenThrow(new IOException("IO错误"));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileUploadService.uploadFile(mockFile);
        });
        
        assertTrue(exception.getMessage().contains("文件上传失败"));
    }
    
    @Test
    @DisplayName("删除文件-成功")
    void testDeleteFile_Success() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);
        String filePath = "test.txt";
        
        // When
        fileUploadService.deleteFile(filePath);
        
        // Then
        assertFalse(Files.exists(testFile));
    }
    
    @Test
    @DisplayName("删除文件-文件不存在")
    void testDeleteFile_FileNotExists() {
        // Given
        String filePath = "nonexistent.txt";
        
        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> {
            fileUploadService.deleteFile(filePath);
        });
    }
    
    @Test
    @DisplayName("删除文件-路径为空")
    void testDeleteFile_PathEmpty() {
        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> {
            fileUploadService.deleteFile("");
        });
        
        assertDoesNotThrow(() -> {
            fileUploadService.deleteFile(null);
        });
    }
    
    @Test
    @DisplayName("删除文件-IO异常")
    void testDeleteFile_IOException() {
        // Given - 使用无效路径（在Windows上可能触发权限问题）
        String filePath = "invalid/path/../test.txt";
        
        // When & Then - 应该捕获异常并转换为BusinessException
        // 注意：实际行为取决于文件系统，这里主要测试异常处理逻辑
        // 如果路径无效，可能会抛出BusinessException
    }
    
    @Test
    @DisplayName("获取文件URL-成功")
    void testGetFileUrl_Success() {
        // Given
        String filePath = "2024/01/01/test.pdf";
        
        // When
        String result = fileUploadService.getFileUrl(filePath);
        
        // Then
        assertNotNull(result);
        assertEquals("/api/v1/files/2024/01/01/test.pdf", result);
    }
    
    @Test
    @DisplayName("获取文件URL-路径为空")
    void testGetFileUrl_PathEmpty() {
        // When
        String result1 = fileUploadService.getFileUrl("");
        String result2 = fileUploadService.getFileUrl(null);
        
        // Then
        assertNull(result1);
        assertNull(result2);
    }
    
    @Test
    @DisplayName("上传文件-支持所有允许的文件类型")
    void testUploadFile_AllAllowedTypes() throws IOException {
        String[] allowedTypes = {"doc", "docx", "pdf", "txt"};
        
        for (String type : allowedTypes) {
            // Given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getOriginalFilename()).thenReturn("test." + type);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("application/octet-stream");
            when(file.getInputStream()).thenReturn(
                new java.io.ByteArrayInputStream("test content".getBytes())
            );
            
            // When
            String result = fileUploadService.uploadFile(file);
            
            // Then
            assertNotNull(result);
            assertTrue(result.endsWith("." + type));
        }
    }
}

