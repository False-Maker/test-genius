package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.TestCoverageAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 测试覆盖分析服务接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
public interface TestCoverageService {
    
    /**
     * 分析测试覆盖
     * @param analysis 覆盖分析信息
     * @return 生成的覆盖分析
     */
    TestCoverageAnalysis analyzeCoverage(TestCoverageAnalysis analysis);
    
    /**
     * 分析需求覆盖
     * @param requirementId 需求ID
     * @return 生成的覆盖分析
     */
    TestCoverageAnalysis analyzeRequirementCoverage(Long requirementId);
    
    /**
     * 分析功能覆盖
     * @param requirementId 需求ID
     * @return 生成的覆盖分析
     */
    TestCoverageAnalysis analyzeFunctionCoverage(Long requirementId);
    
    /**
     * 分析场景覆盖
     * @param requirementId 需求ID
     * @return 生成的覆盖分析
     */
    TestCoverageAnalysis analyzeScenarioCoverage(Long requirementId);
    
    /**
     * 分析代码覆盖
     * @param requirementId 需求ID（可选）
     * @param coverageData 代码覆盖数据（JSON格式）
     * @return 生成的覆盖分析
     */
    TestCoverageAnalysis analyzeCodeCoverage(Long requirementId, String coverageData);
    
    /**
     * 根据ID查询覆盖分析
     * @param id 分析ID
     * @return 覆盖分析详情
     */
    TestCoverageAnalysis getAnalysisById(Long id);
    
    /**
     * 根据编码查询覆盖分析
     * @param analysisCode 分析编码
     * @return 覆盖分析详情
     */
    TestCoverageAnalysis getAnalysisByCode(String analysisCode);
    
    /**
     * 分页查询覆盖分析列表
     * @param pageable 分页参数
     * @return 覆盖分析列表
     */
    Page<TestCoverageAnalysis> getAnalysisList(Pageable pageable);
    
    /**
     * 根据需求ID查询覆盖分析列表
     * @param requirementId 需求ID
     * @return 覆盖分析列表
     */
    List<TestCoverageAnalysis> getAnalysisByRequirementId(Long requirementId);
    
    /**
     * 根据覆盖类型查询覆盖分析列表
     * @param coverageType 覆盖类型
     * @return 覆盖分析列表
     */
    List<TestCoverageAnalysis> getAnalysisByCoverageType(String coverageType);
    
    /**
     * 获取覆盖趋势分析
     * @param requirementId 需求ID（可选）
     * @param coverageType 覆盖类型（可选）
     * @param days 天数（默认7天）
     * @return 趋势分析数据（JSON格式）
     */
    String getCoverageTrend(Long requirementId, String coverageType, Integer days);
    
    /**
     * 检查覆盖不足
     * @param requirementId 需求ID（可选）
     * @param threshold 覆盖率阈值（默认80）
     * @return 覆盖不足项列表（JSON格式）
     */
    String checkCoverageInsufficiency(Long requirementId, Double threshold);
    
    /**
     * 生成覆盖报告
     * @param requirementId 需求ID
     * @param coverageType 覆盖类型（可选）
     * @return 报告内容（JSON格式）
     */
    String generateCoverageReport(Long requirementId, String coverageType);
}

