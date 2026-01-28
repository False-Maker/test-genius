package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.dto.KnowledgeBaseRequestDTO;
import com.sinosoft.testdesign.dto.KnowledgeBaseResponseDTO;
import com.sinosoft.testdesign.dto.KnowledgePermissionRequestDTO;
import com.sinosoft.testdesign.dto.KnowledgePermissionResponseDTO;
import com.sinosoft.testdesign.dto.KnowledgeBaseSyncLogResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理服务接口（第四阶段增强）
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
public interface KnowledgeBaseManageService {
    
    /**
     * 创建知识库
     * 
     * @param request 知识库请求DTO
     * @return 知识库ID
     */
    Long createKnowledgeBase(KnowledgeBaseRequestDTO request);
    
    /**
     * 更新知识库
     * 
     * @param id 知识库ID
     * @param request 知识库请求DTO
     */
    void updateKnowledgeBase(Long id, KnowledgeBaseRequestDTO request);
    
    /**
     * 删除知识库
     * 
     * @param id 知识库ID
     */
    void deleteKnowledgeBase(Long id);
    
    /**
     * 获取知识库详情
     * 
     * @param id 知识库ID
     * @return 知识库响应DTO
     */
    KnowledgeBaseResponseDTO getKnowledgeBaseById(Long id);
    
    /**
     * 获取知识库列表
     * 
     * @return 知识库响应DTO列表
     */
    List<KnowledgeBaseResponseDTO> getKnowledgeBaseList();
    
    /**
     * 获取用户有权限的知识库列表
     * 
     * @param userId 用户ID
     * @param permissionType 权限类型（可选）
     * @return 知识库响应DTO列表
     */
    List<KnowledgeBaseResponseDTO> getUserKnowledgeBaseList(Long userId, String permissionType);
    
    /**
     * 授予权限
     * 
     * @param request 权限请求DTO
     * @return 是否成功
     */
    boolean grantPermission(KnowledgePermissionRequestDTO request);
    
    /**
     * 撤销权限
     * 
     * @param kbId 知识库ID
     * @param userId 用户ID
     * @param permissionType 权限类型
     * @return 是否成功
     */
    boolean revokePermission(Long kbId, Long userId, String permissionType);
    
    /**
     * 检查权限
     * 
     * @param kbId 知识库ID
     * @param userId 用户ID
     * @param permissionType 权限类型
     * @return 是否有权限
     */
    boolean checkPermission(Long kbId, Long userId, String permissionType);
    
    /**
     * 获取知识库的权限列表
     * 
     * @param kbId 知识库ID
     * @return 权限响应DTO列表
     */
    List<KnowledgePermissionResponseDTO> getKnowledgeBasePermissions(Long kbId);
    
    /**
     * 获取用户的权限列表
     * 
     * @param userId 用户ID
     * @return 权限响应DTO列表
     */
    List<KnowledgePermissionResponseDTO> getUserPermissions(Long userId);
    
    /**
     * 同步知识库
     * 
     * @param kbId 知识库ID
     * @param syncType 同步类型（incremental/full）
     * @param sourcePath 源文件路径
     * @return 同步结果
     */
    Map<String, Object> syncKnowledgeBase(Long kbId, String syncType, String sourcePath);
    
    /**
     * 获取同步日志列表
     * 
     * @param kbId 知识库ID
     * @return 同步日志响应DTO列表
     */
    List<KnowledgeBaseSyncLogResponseDTO> getSyncLogs(Long kbId);
    
    /**
     * 上传文档到知识库
     * 
     * @param kbId 知识库ID
     * @param fileName 文件名
     * @param fileContent 文件内容
     * @param creatorId 创建人ID
     * @return 文档编码
     */
    String uploadDocument(Long kbId, String fileName, byte[] fileContent, Long creatorId);
    
    /**
     * 生成知识库编码
     * 
     * @return 知识库编码
     */
    String generateKbCode();
}

