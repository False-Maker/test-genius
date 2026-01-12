package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    /**
     * 允许的文件类型
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "doc", "docx", "pdf", "txt"
    );
    
    /**
     * 允许的MIME类型
     */
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/pdf",
        "text/plain"
    );
    
    /**
     * 文件上传根目录
     */
    @Value("${app.upload.base-path:./uploads}")
    private String basePath;
    
    /**
     * 最大文件大小（字节），默认100MB
     */
    @Value("${app.upload.max-file-size:104857600}")
    private long maxFileSize;
    
    /**
     * 文件访问URL前缀
     */
    @Value("${app.upload.url-prefix:/api/v1/files}")
    private String urlPrefix;
    
    @Override
    public String uploadFile(MultipartFile file) {
        // 1. 验证文件
        validateFile(file);
        
        // 2. 生成文件存储路径
        String fileName = generateFileName(file.getOriginalFilename());
        String relativePath = generateRelativePath(fileName);
        Path targetPath = Paths.get(basePath, relativePath);
        
        try {
            // 3. 创建目录
            Files.createDirectories(targetPath.getParent());
            
            // 4. 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("文件上传成功: 原始文件名={}, 存储路径={}, 文件大小={}字节", 
                file.getOriginalFilename(), targetPath, file.getSize());
            
            // 5. 返回相对路径（用于存储到数据库）
            return relativePath.replace(File.separator, "/");
            
        } catch (IOException e) {
            log.error("文件上传失败: 原始文件名={}, 错误={}", file.getOriginalFilename(), e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
        
        try {
            Path path = Paths.get(basePath, filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件删除成功: 文件路径={}", filePath);
            } else {
                log.warn("文件不存在，无法删除: 文件路径={}", filePath);
            }
        } catch (IOException e) {
            log.error("文件删除失败: 文件路径={}, 错误={}", filePath, e.getMessage(), e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getFileUrl(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        return urlPrefix + "/" + filePath.replace(File.separator, "/");
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        // 验证文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(
                String.format("文件大小超过限制，最大允许%dMB", maxFileSize / 1024 / 1024)
            );
        }
        
        // 验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException("文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(
                String.format("不支持的文件类型，仅支持: %s", String.join(", ", ALLOWED_EXTENSIONS))
            );
        }
        
        // 验证MIME类型
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("文件MIME类型可能不匹配: 文件名={}, MIME类型={}", originalFilename, contentType);
            // 不强制要求MIME类型匹配，因为某些客户端可能不准确
        }
    }
    
    /**
     * 生成文件名（添加UUID前缀避免重名）
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String baseName = UUID.randomUUID().toString().replace("-", "");
        return baseName + "." + extension;
    }
    
    /**
     * 生成相对路径（按日期组织）
     */
    private String generateRelativePath(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return datePath + File.separator + fileName;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            throw new BusinessException("文件必须包含扩展名");
        }
        return filename.substring(lastDotIndex + 1);
    }
}

