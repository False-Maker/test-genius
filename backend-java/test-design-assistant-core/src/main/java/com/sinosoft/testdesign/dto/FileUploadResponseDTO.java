package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 文件上传响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
public class FileUploadResponseDTO {
    
    /**
     * 文件存储路径
     */
    private String filePath;
    
    /**
     * 文件访问URL
     */
    private String fileUrl;
    
    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private String fileSize;
}

