package com.sinosoft.testdesign.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface FileUploadService {
    
    /**
     * 上传文件
     * @param file 文件
     * @return 文件存储路径
     */
    String uploadFile(MultipartFile file);
    
    /**
     * 删除文件
     * @param filePath 文件路径
     */
    void deleteFile(String filePath);
    
    /**
     * 获取文件URL
     * @param filePath 文件路径
     * @return 文件访问URL
     */
    String getFileUrl(String filePath);
}

