package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.WorkflowAbTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工作流 A/B 测试配置数据访问接口（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Repository
public interface WorkflowAbTestRepository extends JpaRepository<WorkflowAbTest, Long> {

    List<WorkflowAbTest> findByWorkflowIdOrderByCreateTimeDesc(Long workflowId);

    List<WorkflowAbTest> findByWorkflowIdAndStatus(Long workflowId, String status);

    @Query("SELECT w FROM WorkflowAbTest w WHERE w.workflowId = :workflowId AND w.status = 'running'")
    Optional<WorkflowAbTest> findRunningTestByWorkflowId(@Param("workflowId") Long workflowId);

    List<WorkflowAbTest> findByStatus(String status);
}
