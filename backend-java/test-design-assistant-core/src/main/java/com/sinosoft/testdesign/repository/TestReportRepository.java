package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 测试报告数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Repository
public interface TestReportRepository extends JpaRepository<TestReport, Long>, 
        JpaSpecificationExecutor<TestReport> {
    
    /**
     * 根据报告编码查询
     */
    Optional<TestReport> findByReportCode(String reportCode);
    
    /**
     * 根据报告类型查询
     */
    List<TestReport> findByReportType(String reportType);
    
    /**
     * 根据报告状态查询
     */
    List<TestReport> findByReportStatus(String reportStatus);
    
    /**
     * 根据需求ID查询
     */
    List<TestReport> findByRequirementId(Long requirementId);
    
    /**
     * 根据模板ID查询
     */
    List<TestReport> findByTemplateId(Long templateId);
    
    /**
     * 根据执行任务ID查询
     */
    List<TestReport> findByExecutionTaskId(Long executionTaskId);
    
    /**
     * 查询指定前缀的报告编码列表（用于编码生成优化）
     */
    List<TestReport> findByReportCodeStartingWithOrderByIdDesc(String prefix);
}

