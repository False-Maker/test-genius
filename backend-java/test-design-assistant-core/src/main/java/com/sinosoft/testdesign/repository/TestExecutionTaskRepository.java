package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestExecutionTask;
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
 * 测试执行任务数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Repository
public interface TestExecutionTaskRepository extends JpaRepository<TestExecutionTask, Long>, 
        JpaSpecificationExecutor<TestExecutionTask> {
    
    /**
     * 根据任务编码查询
     */
    Optional<TestExecutionTask> findByTaskCode(String taskCode);
    
    /**
     * 查询指定前缀的任务编码列表（用于编码生成优化）
     */
    List<TestExecutionTask> findByTaskCodeStartingWithOrderByIdDesc(String prefix);
    
    /**
     * 根据任务状态查询任务列表
     */
    List<TestExecutionTask> findByTaskStatus(String taskStatus);
    
    /**
     * 根据需求ID查询任务列表
     */
    List<TestExecutionTask> findByRequirementId(Long requirementId);
    
    /**
     * 根据用例ID查询任务列表
     */
    List<TestExecutionTask> findByCaseId(Long caseId);
    
    /**
     * 分页查询任务列表（带过滤条件）
     */
    @Query(value = "SELECT tet.id, tet.task_code, tet.task_name, tet.task_type, " +
            "tet.requirement_id, tet.case_id, tet.case_suite_id, tet.script_type, " +
            "tet.script_content, tet.script_language, tet.page_code_url, " +
            "tet.natural_language_desc, tet.error_log, tet.execution_config, " +
            "tet.task_status, tet.progress, tet.success_count, tet.fail_count, " +
            "tet.result_data, tet.error_message, tet.creator_id, tet.creator_name, " +
            "tet.create_time, tet.update_time, tet.finish_time " +
            "FROM test_execution_task tet WHERE " +
            "(:taskName IS NULL OR tet.task_name ILIKE '%' || :taskName || '%') AND " +
            "(:taskStatus IS NULL OR tet.task_status = :taskStatus) AND " +
            "(:taskType IS NULL OR tet.task_type = :taskType)",
            countQuery = "SELECT COUNT(tet.id) FROM test_execution_task tet WHERE " +
            "(:taskName IS NULL OR tet.task_name ILIKE '%' || :taskName || '%') AND " +
            "(:taskStatus IS NULL OR tet.task_status = :taskStatus) AND " +
            "(:taskType IS NULL OR tet.task_type = :taskType)",
            nativeQuery = true)
    Page<TestExecutionTask> findWithFilters(
            @Param("taskName") String taskName,
            @Param("taskStatus") String taskStatus,
            @Param("taskType") String taskType,
            Pageable pageable);
}

