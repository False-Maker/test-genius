package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestExecutionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 测试执行记录数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Repository
public interface TestExecutionRecordRepository extends JpaRepository<TestExecutionRecord, Long>, 
        JpaSpecificationExecutor<TestExecutionRecord> {
    
    /**
     * 根据记录编码查询
     */
    Optional<TestExecutionRecord> findByRecordCode(String recordCode);
    
    /**
     * 查询指定前缀的记录编码列表（用于编码生成优化）
     */
    List<TestExecutionRecord> findByRecordCodeStartingWithOrderByIdDesc(String prefix);
    
    /**
     * 根据任务ID查询执行记录列表
     */
    List<TestExecutionRecord> findByTaskId(Long taskId);
    
    /**
     * 根据用例ID查询执行记录列表
     */
    List<TestExecutionRecord> findByCaseId(Long caseId);
    
    /**
     * 根据执行状态查询执行记录列表
     */
    List<TestExecutionRecord> findByExecutionStatus(String executionStatus);
    
    /**
     * 根据任务ID和执行状态查询执行记录列表
     */
    List<TestExecutionRecord> findByTaskIdAndExecutionStatus(Long taskId, String executionStatus);
    
    /**
     * 分页查询执行记录列表（带过滤条件）
     */
    @Query(value = "SELECT ter.id, ter.record_code, ter.task_id, ter.case_id, " +
            "ter.execution_type, ter.execution_status, ter.execution_result, " +
            "ter.execution_log, ter.error_message, ter.execution_duration, " +
            "ter.executed_by, ter.executed_by_name, ter.execution_time, " +
            "ter.finish_time, ter.screenshot_url, ter.video_url " +
            "FROM test_execution_record ter WHERE " +
            "(:taskId IS NULL OR ter.task_id = :taskId) AND " +
            "(:caseId IS NULL OR ter.case_id = :caseId) AND " +
            "(:executionStatus IS NULL OR ter.execution_status = :executionStatus)",
            countQuery = "SELECT COUNT(ter.id) FROM test_execution_record ter WHERE " +
            "(:taskId IS NULL OR ter.task_id = :taskId) AND " +
            "(:caseId IS NULL OR ter.case_id = :caseId) AND " +
            "(:executionStatus IS NULL OR ter.execution_status = :executionStatus)",
            nativeQuery = true)
    Page<TestExecutionRecord> findWithFilters(
            @Param("taskId") Long taskId,
            @Param("caseId") Long caseId,
            @Param("executionStatus") String executionStatus,
            Pageable pageable);
}

