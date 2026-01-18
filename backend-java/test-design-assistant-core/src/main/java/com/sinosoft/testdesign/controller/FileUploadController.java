package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Tag(name = "文件上传", description = "文件上传相关接口")
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    @Operation(summary = "上传文件", description = "上传Word、PDF等文档文件")
    @PostMapping("/upload")
    public Result<com.sinosoft.testdesign.dto.FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("收到文件上传请求: 文件名={}, 文件大小={}字节, 文件类型={}", 
            file.getOriginalFilename(), file.getSize(), file.getContentType());
        
        String filePath = fileUploadService.uploadFile(file);
        String fileUrl = fileUploadService.getFileUrl(filePath);
        
        com.sinosoft.testdesign.dto.FileUploadResponseDTO result = new com.sinosoft.testdesign.dto.FileUploadResponseDTO();
        result.setFilePath(filePath);
        result.setFileUrl(fileUrl);
        result.setFileName(file.getOriginalFilename());
        result.setFileSize(String.valueOf(file.getSize()));
        
        return Result.success(result);
    }
    
    @Operation(summary = "删除文件", description = "删除指定文件")
    @DeleteMapping("/{filePath:.*}")
    public Result<Void> deleteFile(@PathVariable String filePath) {
        log.info("收到文件删除请求: 文件路径={}", filePath);
        
        // URL解码路径变量，因为路径可能包含编码的斜杠（%2F）
        try {
            String decodedPath = java.net.URLDecoder.decode(filePath, java.nio.charset.StandardCharsets.UTF_8);
            fileUploadService.deleteFile(decodedPath);
        } catch (Exception e) {
            // 如果解码失败，使用原始路径
            log.warn("路径解码失败，使用原始路径: {}", filePath, e);
            fileUploadService.deleteFile(filePath);
        }
        
        return Result.success();
    }
}

