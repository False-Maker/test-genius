package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 测试报告生成服务接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
public interface TestReportService {
    
    /**
     * 生成测试报告
     * @param report 报告信息
     * @return 生成的报告
     */
    TestReport generateReport(TestReport report);
    
    /**
     * 根据编码查询报告
     * @param reportCode 报告编码
     * @return 报告详情
     */
    TestReport getReportByCode(String reportCode);
    
    /**
     * 根据ID查询报告
     * @param id 报告ID
     * @return 报告详情
     */
    TestReport getReportById(Long id);
    
    /**
     * 分页查询报告列表
     * @param pageable 分页参数
     * @return 报告列表
     */
    Page<TestReport> getReportList(Pageable pageable);
    
    /**
     * 根据需求ID查询报告列表
     * @param requirementId 需求ID
     * @return 报告列表
     */
    java.util.List<TestReport> getReportsByRequirementId(Long requirementId);
    
    /**
     * 根据执行任务ID查询报告列表
     * @param executionTaskId 执行任务ID
     * @return 报告列表
     */
    java.util.List<TestReport> getReportsByExecutionTaskId(Long executionTaskId);
    
    /**
     * 更新报告
     * @param id 报告ID
     * @param report 报告信息
     * @return 更新后的报告
     */
    TestReport updateReport(Long id, TestReport report);
    
    /**
     * 发布报告
     * @param id 报告ID
     * @return 发布后的报告
     */
    TestReport publishReport(Long id);
    
    /**
     * 删除报告
     * @param id 报告ID
     */
    void deleteReport(Long id);
    
    /**
     * 导出报告文件
     * @param reportCode 报告编码
     * @param format 文件格式（WORD/PDF/EXCEL）
     * @return 文件URL
     */
    String exportReport(String reportCode, String format);
    
    /**
     * 汇总测试执行结果
     * @param requirementId 需求ID（可选）
     * @param executionTaskId 执行任务ID（可选）
     * @return 汇总结果（JSON格式）
     */
    String summarizeExecutionResults(Long requirementId, Long executionTaskId);
}

