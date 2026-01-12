package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface AuditLogService {
    
    /**
     * 记录审计日志
     */
    void log(AuditLog auditLog);
    
    /**
     * 异步记录审计日志
     */
    void logAsync(AuditLog auditLog);
    
    /**
     * 根据ID查询审计日志
     */
    AuditLog findById(Long id);
    
    /**
     * 根据用户ID查询审计日志
     */
    List<AuditLog> findByUserId(Long userId);
    
    /**
     * 根据操作类型查询审计日志
     */
    List<AuditLog> findByOperationType(String operationType);
    
    /**
     * 根据操作模块查询审计日志
     */
    List<AuditLog> findByOperationModule(String operationModule);
    
    /**
     * 根据时间范围查询审计日志
     */
    List<AuditLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 分页查询审计日志
     */
    Page<AuditLog> findAll(Pageable pageable);
}

