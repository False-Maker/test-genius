package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.WorkflowAbTestExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 工作流 A/B 测试执行记录数据访问接口（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Repository
public interface WorkflowAbTestExecutionRepository extends JpaRepository<WorkflowAbTestExecution, Long> {

    List<WorkflowAbTestExecution> findByAbTestIdOrderByExecutionTimeDesc(Long abTestId);

    Optional<WorkflowAbTestExecution> findByRequestId(String requestId);

    List<WorkflowAbTestExecution> findByAbTestIdAndVersionLabel(Long abTestId, String versionLabel);

    @Query("SELECT COUNT(e) FROM WorkflowAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'A'")
    long countVersionA(@Param("abTestId") Long abTestId);

    @Query("SELECT COUNT(e) FROM WorkflowAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'B'")
    long countVersionB(@Param("abTestId") Long abTestId);

    @Query("SELECT COUNT(e) FROM WorkflowAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'A' AND e.status = 'success'")
    long countVersionASuccess(@Param("abTestId") Long abTestId);

    @Query("SELECT COUNT(e) FROM WorkflowAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'B' AND e.status = 'success'")
    long countVersionBSuccess(@Param("abTestId") Long abTestId);

    @Query("SELECT AVG(e.responseTime) FROM WorkflowAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'A' AND e.status = 'success' AND e.responseTime IS NOT NULL")
    Double avgResponseTimeA(@Param("abTestId") Long abTestId);

    @Query("SELECT AVG(e.responseTime) FROM WorkflowAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'B' AND e.status = 'success' AND e.responseTime IS NOT NULL")
    Double avgResponseTimeB(@Param("abTestId") Long abTestId);

    List<WorkflowAbTestExecution> findByAbTestIdAndExecutionTimeBetween(Long abTestId, LocalDateTime start, LocalDateTime end);
}
