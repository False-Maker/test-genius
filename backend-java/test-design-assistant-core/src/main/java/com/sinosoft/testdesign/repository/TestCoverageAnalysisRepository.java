package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestCoverageAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 测试覆盖分析数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Repository
public interface TestCoverageAnalysisRepository extends JpaRepository<TestCoverageAnalysis, Long>,
        JpaSpecificationExecutor<TestCoverageAnalysis> {
    
    /**
     * 根据分析编码查询
     */
    Optional<TestCoverageAnalysis> findByAnalysisCode(String analysisCode);
    
    /**
     * 根据需求ID查询覆盖分析列表
     */
    List<TestCoverageAnalysis> findByRequirementId(Long requirementId);
    
    /**
     * 根据覆盖类型查询覆盖分析列表
     */
    List<TestCoverageAnalysis> findByCoverageType(String coverageType);
    
    /**
     * 根据需求ID和覆盖类型查询覆盖分析列表
     */
    List<TestCoverageAnalysis> findByRequirementIdAndCoverageType(Long requirementId, String coverageType);
    
    /**
     * 查询指定前缀的分析编码列表（用于编码生成优化）
     */
    List<TestCoverageAnalysis> findByAnalysisCodeStartingWithOrderByIdDesc(String prefix);
}

