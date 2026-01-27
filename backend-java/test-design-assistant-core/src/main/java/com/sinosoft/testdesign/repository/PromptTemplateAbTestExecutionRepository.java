package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.PromptTemplateAbTestExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 提示词模板A/B测试执行记录数据访问接口
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Repository
public interface PromptTemplateAbTestExecutionRepository extends JpaRepository<PromptTemplateAbTestExecution, Long> {
    
    /**
     * 根据A/B测试ID查询所有执行记录
     */
    List<PromptTemplateAbTestExecution> findByAbTestIdOrderByExecutionTimeDesc(Long abTestId);
    
    /**
     * 根据请求ID查询执行记录
     */
    Optional<PromptTemplateAbTestExecution> findByRequestId(String requestId);
    
    /**
     * 根据A/B测试ID和版本标签查询执行记录
     */
    List<PromptTemplateAbTestExecution> findByAbTestIdAndVersionLabel(Long abTestId, String versionLabel);
    
    /**
     * 统计版本A的执行记录数
     */
    @Query("SELECT COUNT(e) FROM PromptTemplateAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'A'")
    Long countVersionAExecutions(@Param("abTestId") Long abTestId);
    
    /**
     * 统计版本B的执行记录数
     */
    @Query("SELECT COUNT(e) FROM PromptTemplateAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'B'")
    Long countVersionBExecutions(@Param("abTestId") Long abTestId);
    
    /**
     * 统计版本A的成功数
     */
    @Query("SELECT COUNT(e) FROM PromptTemplateAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'A' AND e.status = 'success'")
    Long countVersionASuccess(@Param("abTestId") Long abTestId);
    
    /**
     * 统计版本B的成功数
     */
    @Query("SELECT COUNT(e) FROM PromptTemplateAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'B' AND e.status = 'success'")
    Long countVersionBSuccess(@Param("abTestId") Long abTestId);
    
    /**
     * 计算版本A的平均响应时间
     */
    @Query("SELECT AVG(e.responseTime) FROM PromptTemplateAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'A' AND e.status = 'success'")
    Double getVersionAAvgResponseTime(@Param("abTestId") Long abTestId);
    
    /**
     * 计算版本B的平均响应时间
     */
    @Query("SELECT AVG(e.responseTime) FROM PromptTemplateAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'B' AND e.status = 'success'")
    Double getVersionBAvgResponseTime(@Param("abTestId") Long abTestId);
    
    /**
     * 计算版本A的平均用户评分
     */
    @Query("SELECT AVG(e.userRating) FROM PromptTemplateAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'A' AND e.userRating IS NOT NULL")
    Double getVersionAAvgRating(@Param("abTestId") Long abTestId);
    
    /**
     * 计算版本B的平均用户评分
     */
    @Query("SELECT AVG(e.userRating) FROM PromptTemplateAbTestExecution e WHERE e.abTestId = :abTestId AND e.versionLabel = 'B' AND e.userRating IS NOT NULL")
    Double getVersionBAvgRating(@Param("abTestId") Long abTestId);
    
    /**
     * 查询指定时间范围内的执行记录
     */
    List<PromptTemplateAbTestExecution> findByAbTestIdAndExecutionTimeBetween(
            Long abTestId, LocalDateTime startTime, LocalDateTime endTime);
}
