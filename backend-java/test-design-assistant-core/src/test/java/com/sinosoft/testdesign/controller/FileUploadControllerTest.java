package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.FileUploadResponseDTO;
import com.sinosoft.testdesign.service.FileUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文件上传Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@DisplayName("文件上传Controller测试")
class FileUploadControllerTest extends BaseControllerTest {
    
    @MockBean
    private FileUploadService fileUploadService;
    
    @Test
    @DisplayName("上传文件-成功")
    void testUploadFile_Success() throws Exception {
        // Given
        String fileName = "test.docx";
        String filePath = "uploads/2024/01/test.docx";
        String fileUrl = "http://localhost:8080/files/uploads/2024/01/test.docx";
        byte[] fileContent = "test file content".getBytes();
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            fileName,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            fileContent
        );
        
        when(fileUploadService.uploadFile(any()))
            .thenReturn(filePath);
        when(fileUploadService.getFileUrl(filePath))
            .thenReturn(fileUrl);
        
        // When & Then
        mockMvc.perform(multipart("/v1/files/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.filePath").value(filePath))
                .andExpect(jsonPath("$.data.fileUrl").value(fileUrl))
                .andExpect(jsonPath("$.data.fileName").value(fileName))
                .andExpect(jsonPath("$.data.fileSize").value(String.valueOf(fileContent.length)));
    }
    
    @Test
    @DisplayName("上传文件-PDF格式")
    void testUploadFile_PdfFormat() throws Exception {
        // Given
        String fileName = "test.pdf";
        String filePath = "uploads/2024/01/test.pdf";
        String fileUrl = "http://localhost:8080/files/uploads/2024/01/test.pdf";
        byte[] fileContent = "PDF content".getBytes();
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            fileName,
            "application/pdf",
            fileContent
        );
        
        when(fileUploadService.uploadFile(any()))
            .thenReturn(filePath);
        when(fileUploadService.getFileUrl(filePath))
            .thenReturn(fileUrl);
        
        // When & Then
        mockMvc.perform(multipart("/v1/files/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.filePath").value(filePath))
                .andExpect(jsonPath("$.data.fileName").value(fileName));
    }
    
    @Test
    @DisplayName("删除文件-成功")
    void testDeleteFile_Success() throws Exception {
        // Given
        String filePath = "uploads/2024/01/test.docx";
        
        doNothing().when(fileUploadService).deleteFile(eq(filePath));
        
        // When & Then
        // Spring的路径变量模式 {filePath:.*} 在包含斜杠时需要URL编码
        // Controller会自动解码URL编码的路径变量
        String encodedPath = filePath.replace("/", "%2F");
        mockMvc.perform(delete("/v1/files/" + encodedPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // 验证Service方法被正确调用（路径应该被解码）
        verify(fileUploadService, times(1)).deleteFile(eq(filePath));
    }
    
    @Test
    @DisplayName("删除文件-带路径分隔符")
    void testDeleteFile_WithPathSeparator() throws Exception {
        // Given
        String filePath = "uploads/2024/01/test.docx";
        
        doNothing().when(fileUploadService).deleteFile(eq(filePath));
        
        // When & Then - 使用URL编码的路径分隔符，Controller会自动解码
        String encodedPath = filePath.replace("/", "%2F");
        mockMvc.perform(delete("/v1/files/" + encodedPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // 验证Service方法被正确调用（路径应该被解码）
        verify(fileUploadService, times(1)).deleteFile(eq(filePath));
    }
}

