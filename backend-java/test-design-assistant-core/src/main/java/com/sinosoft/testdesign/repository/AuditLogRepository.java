package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Repository
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    
    /**
     * 根据用户ID查询审计日志
     */
    List<AuditLog> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    /**
     * 根据操作类型查询审计日志
     */
    List<AuditLog> findByOperationTypeOrderByCreateTimeDesc(String operationType);
    
    /**
     * 根据操作模块查询审计日志
     */
    List<AuditLog> findByOperationModuleOrderByCreateTimeDesc(String operationModule);
    
    /**
     * 根据时间范围查询审计日志
     */
    List<AuditLog> findByCreateTimeBetweenOrderByCreateTimeDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 分页查询审计日志
     */
    Page<AuditLog> findAll(Pageable pageable);
}

