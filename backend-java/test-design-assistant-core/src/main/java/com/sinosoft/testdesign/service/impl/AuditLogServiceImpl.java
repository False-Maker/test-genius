package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.AuditLog;
import com.sinosoft.testdesign.repository.AuditLogRepository;
import com.sinosoft.testdesign.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Override
    @Transactional
    public void log(AuditLog auditLog) {
        try {
            if (auditLog.getCreateTime() == null) {
                auditLog.setCreateTime(LocalDateTime.now());
            }
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
            // 审计日志记录失败不应该影响主业务流程，只记录错误日志
        }
    }
    
    @Override
    @Async
    @Transactional
    public void logAsync(AuditLog auditLog) {
        log(auditLog);
    }
    
    @Override
    public AuditLog findById(Long id) {
        return auditLogRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<AuditLog> findByUserId(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
    
    @Override
    public List<AuditLog> findByOperationType(String operationType) {
        return auditLogRepository.findByOperationTypeOrderByCreateTimeDesc(operationType);
    }
    
    @Override
    public List<AuditLog> findByOperationModule(String operationModule) {
        return auditLogRepository.findByOperationModuleOrderByCreateTimeDesc(operationModule);
    }
    
    @Override
    public List<AuditLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByCreateTimeBetweenOrderByCreateTimeDesc(startTime, endTime);
    }
    
    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}

