package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.*;
import com.sinosoft.testdesign.entity.KnowledgeBase;
import com.sinosoft.testdesign.entity.KnowledgeBasePermission;
import com.sinosoft.testdesign.entity.KnowledgeBaseSyncLog;
import com.sinosoft.testdesign.repository.KnowledgeBaseRepository;
import com.sinosoft.testdesign.repository.KnowledgeBasePermissionRepository;
import com.sinosoft.testdesign.repository.KnowledgeBaseSyncLogRepository;
import com.sinosoft.testdesign.service.KnowledgeBaseManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 知识库管理服务实现（第四阶段增强）
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseManageServiceImpl implements KnowledgeBaseManageService {
    
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final KnowledgeBasePermissionRepository permissionRepository;
    private final KnowledgeBaseSyncLogRepository syncLogRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createKnowledgeBase(KnowledgeBaseRequestDTO request) {
        log.info("创建知识库: {}", request.getKbName());
        
        // 检查知识库名称是否已存在
        Optional<KnowledgeBase> existingKb = knowledgeBaseRepository.findByKbCode(request.getKbCode());
        if (existingKb.isPresent()) {
            throw new BusinessException("知识库编码已存在: " + request.getKbCode());
        }
        
        // 创建知识库实体
        KnowledgeBase kb = new KnowledgeBase();
        kb.setKbCode(request.getKbCode() != null ? request.getKbCode() : generateKbCode());
        kb.setKbName(request.getKbName());
        kb.setKbDescription(request.getKbDescription());
        kb.setKbType(request.getKbType());
        kb.setEmbeddingModel(request.getEmbeddingModel());
        kb.setChunkingStrategy(request.getChunkingStrategy());
        kb.setChunkSize(request.getChunkSize());
        kb.setChunkOverlap(request.getChunkOverlap());
        kb.setIsActive(request.getIsActive());
        kb.setCreatorId(request.getCreatorId());
        
        // 保存知识库
        KnowledgeBase savedKb = knowledgeBaseRepository.save(kb);
        
        // 授予创建者管理员权限
        if (request.getCreatorId() != null) {
            KnowledgeBasePermission permission = new KnowledgeBasePermission();
            permission.setKbId(savedKb.getId());
            permission.setUserId(request.getCreatorId());
            permission.setPermissionType("admin");
            permissionRepository.save(permission);
        }
        
        log.info("知识库创建成功, ID: {}", savedKb.getId());
        return savedKb.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKnowledgeBase(Long id, KnowledgeBaseRequestDTO request) {
        log.info("更新知识库: ID={}", id);
        
        KnowledgeBase kb = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识库不存在: " + id));
        
        // 不允许修改编码
        if (request.getKbCode() != null && !request.getKbCode().equals(kb.getKbCode())) {
            throw new BusinessException("不允许修改知识库编码");
        }
        
        // 更新字段
        if (request.getKbName() != null) {
            kb.setKbName(request.getKbName());
        }
        if (request.getKbDescription() != null) {
            kb.setKbDescription(request.getKbDescription());
        }
        if (request.getKbType() != null) {
            kb.setKbType(request.getKbType());
        }
        if (request.getEmbeddingModel() != null) {
            kb.setEmbeddingModel(request.getEmbeddingModel());
        }
        if (request.getChunkingStrategy() != null) {
            kb.setChunkingStrategy(request.getChunkingStrategy());
        }
        if (request.getChunkSize() != null) {
            kb.setChunkSize(request.getChunkSize());
        }
        if (request.getChunkOverlap() != null) {
            kb.setChunkOverlap(request.getChunkOverlap());
        }
        if (request.getIsActive() != null) {
            kb.setIsActive(request.getIsActive());
        }
        
        knowledgeBaseRepository.save(kb);
        log.info("知识库更新成功: ID={}", id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeBase(Long id) {
        log.info("删除知识库: ID={}", id);
        
        KnowledgeBase kb = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识库不存在: " + id));
        
        // 检查是否有文档
        // 添加文档数量检查逻辑
        long documentCount = countDocumentsByKbId(id);
        if (documentCount > 0) {
            throw new BusinessException("知识库中存在 " + documentCount + " 个文档，请先删除所有文档后再删除知识库");
        }
        
        // 删除权限
        permissionRepository.deleteByKbIdAndUserId(id, null);
        
        // 删除知识库
        knowledgeBaseRepository.deleteById(id);
        
        log.info("知识库删除成功: ID={}", id);
    }
    
    @Override
    public KnowledgeBaseResponseDTO getKnowledgeBaseById(Long id) {
        KnowledgeBase kb = knowledgeBaseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识库不存在: " + id));
        
        return convertToResponseDTO(kb);
    }
    
    @Override
    public List<KnowledgeBaseResponseDTO> getKnowledgeBaseList() {
        List<KnowledgeBase> kbList = knowledgeBaseRepository.findByIsActive("1");
        return kbList.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }
    
    @Override
    public List<KnowledgeBaseResponseDTO> getUserKnowledgeBaseList(Long userId, String permissionType) {
        // 获取用户有权限的知识库ID列表
        List<Long> kbIds;
        if (permissionType != null) {
            kbIds = permissionRepository.findKbIdByUserIdAndPermissionType(userId, permissionType);
        } else {
            List<KnowledgeBasePermission> permissions = permissionRepository.findByUserId(userId);
            kbIds = permissions.stream()
                    .map(KnowledgeBasePermission::getKbId)
                    .distinct()
                    .toList();
        }
        
        // 获取知识库列表
        return kbIds.stream()
                .map(knowledgeBaseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::convertToResponseDTO)
                .toList();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grantPermission(KnowledgePermissionRequestDTO request) {
        log.info("授予权限: kbId={}, userId={}, permissionType={}", 
                request.getKbId(), request.getUserId(), request.getPermissionType());
        
        // 检查知识库是否存在
        knowledgeBaseRepository.findById(request.getKbId())
                .orElseThrow(() -> new BusinessException("知识库不存在: " + request.getKbId()));
        
        // 检查权限是否已存在
        Optional<KnowledgeBasePermission> existingPermission = 
                permissionRepository.findByKbIdAndUserIdAndPermissionType(
                        request.getKbId(), request.getUserId(), request.getPermissionType());
        
        if (existingPermission.isPresent()) {
            log.warn("权限已存在，无需重复授予");
            return true;
        }
        
        // 创建权限
        KnowledgeBasePermission permission = new KnowledgeBasePermission();
        permission.setKbId(request.getKbId());
        permission.setUserId(request.getUserId());
        permission.setPermissionType(request.getPermissionType());
        
        permissionRepository.save(permission);
        log.info("权限授予成功");
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokePermission(Long kbId, Long userId, String permissionType) {
        log.info("撤销权限: kbId={}, userId={}, permissionType={}", kbId, userId, permissionType);
        
        Optional<KnowledgeBasePermission> permission = 
                permissionRepository.findByKbIdAndUserIdAndPermissionType(kbId, userId, permissionType);
        
        if (permission.isPresent()) {
            permissionRepository.delete(permission.get());
            log.info("权限撤销成功");
            return true;
        } else {
            log.warn("权限不存在，无需撤销");
            return false;
        }
    }
    
    @Override
    public boolean checkPermission(Long kbId, Long userId, String permissionType) {
        return permissionRepository.existsByKbIdAndUserIdAndPermissionType(
                kbId, userId, permissionType);
    }
    
    @Override
    public List<KnowledgePermissionResponseDTO> getKnowledgeBasePermissions(Long kbId) {
        List<KnowledgeBasePermission> permissions = permissionRepository.findByKbId(kbId);
        
        return permissions.stream()
                .map(this::convertToPermissionResponseDTO)
                .toList();
    }
    
    @Override
    public List<KnowledgePermissionResponseDTO> getUserPermissions(Long userId) {
        List<KnowledgeBasePermission> permissions = permissionRepository.findByUserId(userId);
        
        return permissions.stream()
                .map(this::convertToPermissionResponseDTO)
                .toList();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncKnowledgeBase(Long kbId, String syncType, String sourcePath) {
        log.info("同步知识库: kbId={}, syncType={}, sourcePath={}", kbId, syncType, sourcePath);
        
        // 检查知识库是否存在
        KnowledgeBase kb = knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new BusinessException("知识库不存在: " + kbId));
        
        // 创建同步日志
        KnowledgeBaseSyncLog syncLog = new KnowledgeBaseSyncLog();
        syncLog.setKbId(kbId);
        syncLog.setSyncType(syncType);
        syncLog.setSourcePath(sourcePath);
        syncLog.setStatus("running");
        syncLog.setStartTime(LocalDateTime.now());
        syncLogRepository.save(syncLog);
        
        try {
            // 调用Python AI服务进行同步
            String url = aiServiceUrl + "/api/v1/knowledge/sync";
            
            Map<String, Object> request = new HashMap<>();
            request.put("kb_id", kbId);
            request.put("sync_type", syncType);
            request.put("source_path", sourcePath);
            request.put("chunking_strategy", kb.getChunkingStrategy());
            request.put("chunk_size", kb.getChunkSize());
            request.put("chunk_overlap", kb.getChunkOverlap());
            
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                // 更新同步日志
                syncLog.setAddedCount((Integer) response.getOrDefault("added_count", 0));
                syncLog.setUpdatedCount((Integer) response.getOrDefault("updated_count", 0));
                syncLog.setDeletedCount((Integer) response.getOrDefault("deleted_count", 0));
                syncLog.setFailedCount((Integer) response.getOrDefault("failed_count", 0));
                syncLog.setStatus("success");
                syncLog.setEndTime(LocalDateTime.now());
                syncLogRepository.save(syncLog);
                
                log.info("知识库同步成功: kbId={}", kbId);
                return response;
            } else {
                throw new BusinessException("同步失败: " + response.get("message"));
            }
        } catch (Exception e) {
            // 更新同步日志为失败状态
            syncLog.setStatus("failed");
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setEndTime(LocalDateTime.now());
            syncLogRepository.save(syncLog);
            
            log.error("知识库同步失败: kbId={}", kbId, e);
            throw new BusinessException("知识库同步失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<KnowledgeBaseSyncLogResponseDTO> getSyncLogs(Long kbId) {
        List<KnowledgeBaseSyncLog> syncLogs = syncLogRepository.findByKbIdOrderByCreateTimeDesc(kbId);
        
        return syncLogs.stream()
                .map(this::convertToSyncLogResponseDTO)
                .toList();
    }
    
    @Override
    public String uploadDocument(Long kbId, String fileName, byte[] fileContent, Long creatorId) {
        log.info("上传文档到知识库: kbId={}, fileName={}", kbId, fileName);
        
        // 检查知识库是否存在
        knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new BusinessException("知识库不存在: " + kbId));
        
        try {
            String url = aiServiceUrl + "/api/v1/knowledge/upload";
            
            Map<String, Object> request = new HashMap<>();
            request.put("kb_id", kbId);
            request.put("file_name", fileName);
            request.put("file_content", java.util.Base64.getEncoder().encodeToString(fileContent));
            request.put("creator_id", creatorId);
            
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                log.info("文档上传成功: fileName={}", fileName);
                return (String) response.get("doc_code");
            } else {
                throw new BusinessException("上传文档失败: " + response.get("message"));
            }
        } catch (Exception e) {
            log.error("上传文档失败: fileName={}", fileName, e);
            throw new BusinessException("上传文档失败: " + e.getMessage());
        }
    }
    
    @Override
    public String generateKbCode() {
        // 生成知识库编码：KB-YYYYMMDD-序号
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "KB-" + dateStr + "-";
        
        List<KnowledgeBase> existingKbs = knowledgeBaseRepository.findByKbCodeStartingWithOrderByIdDesc(prefix);
        
        int nextSeq = 1;
        if (!existingKbs.isEmpty()) {
            String lastCode = existingKbs.get(0).getKbCode();
            try {
                lastSeq = Integer.parseInt(lastCode.substring(lastCode.lastIndexOf("-") + 1));
                nextSeq = lastSeq + 1;
            } catch (Exception e) {
                log.warn("解析最后一个编码失败，使用默认序号: {}", lastCode, e);
            }
        }
        
        return String.format("%s%04d", prefix, nextSeq);
    }
    
    /**
     * 转换为响应DTO
     */
    private KnowledgeBaseResponseDTO convertToResponseDTO(KnowledgeBase kb) {
        KnowledgeBaseResponseDTO dto = new KnowledgeBaseResponseDTO();
        dto.setId(kb.getId());
        dto.setKbCode(kb.getKbCode());
        dto.setKbName(kb.getKbName());
        dto.setKbDescription(kb.getKbDescription());
        dto.setKbType(kb.getKbType());
        dto.setEmbeddingModel(kb.getEmbeddingModel());
        dto.setChunkingStrategy(kb.getChunkingStrategy());
        dto.setChunkSize(kb.getChunkSize());
        dto.setChunkOverlap(kb.getChunkOverlap());
        dto.setIsActive(kb.getIsActive());
        dto.setCreatorId(kb.getCreatorId());
        dto.setCreateTime(kb.getCreateTime());
        dto.setUpdateTime(kb.getUpdateTime());
        
        // 查询文档数量、分块数量、最后同步时间
        try {
            Map<String, Object> statistics = getKnowledgeBaseStatistics(kb.getId());
            if (statistics != null) {
                dto.setDocumentCount((Integer) statistics.getOrDefault("document_count", 0));
                dto.setChunkCount((Integer) statistics.getOrDefault("chunk_count", 0));
                
                String lastSyncTimeStr = (String) statistics.get("last_sync_time");
                if (lastSyncTimeStr != null && !lastSyncTimeStr.isEmpty()) {
                    try {
                        dto.setLastSyncTime(LocalDateTime.parse(lastSyncTimeStr.replace("Z", "")));
                    } catch (Exception e) {
                        log.warn("解析最后同步时间失败: {}", lastSyncTimeStr);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查询知识库统计信息失败: kbId={}, 错误={}", kb.getId(), e.getMessage());
            // 设置默认值
            dto.setDocumentCount(0);
            dto.setChunkCount(0);
        }
        
        return dto;
    }
    
    /**
     * 转换为权限响应DTO
     */
    private KnowledgePermissionResponseDTO convertToPermissionResponseDTO(KnowledgeBasePermission permission) {
        KnowledgePermissionResponseDTO dto = new KnowledgePermissionResponseDTO();
        dto.setId(permission.getId());
        dto.setKbId(permission.getKbId());
        dto.setUserId(permission.getUserId());
        dto.setPermissionType(permission.getPermissionType());
        dto.setCreateTime(permission.getCreateTime());
        
        // 查询知识库名称、用户名
        try {
            // 查询知识库信息
            Optional<KnowledgeBase> kb = knowledgeBaseRepository.findById(permission.getKbId());
            if (kb.isPresent()) {
                dto.setKbCode(kb.get().getKbCode());
                dto.setKbName(kb.get().getKbName());
            }
            
            // 查询用户名（这里需要根据实际的用户服务来获取）
            // 暂时使用用户ID作为用户名，实际应该调用用户服务
            dto.setUserName("用户" + permission.getUserId());
        } catch (Exception e) {
            log.warn("查询权限相关信息失败: permissionId={}, 错误={}", permission.getId(), e.getMessage());
        }
        
        return dto;
    }
    
    /**
     * 转换为同步日志响应DTO
     */
    private KnowledgeBaseSyncLogResponseDTO convertToSyncLogResponseDTO(KnowledgeBaseSyncLog syncLog) {
        KnowledgeBaseSyncLogResponseDTO dto = new KnowledgeBaseSyncLogResponseDTO();
        dto.setId(syncLog.getId());
        dto.setKbId(syncLog.getKbId());
        dto.setSyncType(syncLog.getSyncType());
        dto.setSourcePath(syncLog.getSourcePath());
        dto.setAddedCount(syncLog.getAddedCount());
        dto.setUpdatedCount(syncLog.getUpdatedCount());
        dto.setDeletedCount(syncLog.getDeletedCount());
        dto.setFailedCount(syncLog.getFailedCount());
        dto.setStatus(syncLog.getStatus());
        dto.setErrorMessage(syncLog.getErrorMessage());
        dto.setStartTime(syncLog.getStartTime());
        dto.setEndTime(syncLog.getEndTime());
        dto.setCreateTime(syncLog.getCreateTime());
        
        // 计算耗时
        if (syncLog.getStartTime() != null && syncLog.getEndTime() != null) {
            long duration = java.time.Duration.between(syncLog.getStartTime(), syncLog.getEndTime()).getSeconds();
            dto.setDuration(duration);
        }
        
        return dto;
    }
    
    /**
     * 统计知识库中的文档数量
     */
    private long countDocumentsByKbId(Long kbId) {
        try {
            // 通过AI服务查询文档数量
            String url = aiServiceUrl + "/api/v1/knowledge/documents/count";
            
            Map<String, Object> request = new HashMap<>();
            request.put("kb_id", kbId);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> entity = 
                new org.springframework.http.HttpEntity<>(request, headers);
            
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success")) && response.containsKey("count")) {
                return ((Number) response.get("count")).longValue();
            }
            
            return 0L;
        } catch (Exception e) {
            log.error("查询知识库文档数量失败: kbId={}, 错误={}", kbId, e.getMessage());
            // 如果查询失败，返回一个较大的值来阻止删除，保证数据安全
            return Long.MAX_VALUE;
        }
    }
    
    /**
     * 获取知识库统计信息
     */
    private Map<String, Object> getKnowledgeBaseStatistics(Long kbId) {
        try {
            // 通过AI服务查询统计信息
            String url = aiServiceUrl + "/api/v1/knowledge/statistics";
            
            Map<String, Object> request = new HashMap<>();
            request.put("kb_id", kbId);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> entity = 
                new org.springframework.http.HttpEntity<>(request, headers);
            
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return response;
            }
            
            return null;
        } catch (Exception e) {
            log.error("查询知识库统计信息失败: kbId={}, 错误={}", kbId, e.getMessage());
            return null;
        }
    }
}

