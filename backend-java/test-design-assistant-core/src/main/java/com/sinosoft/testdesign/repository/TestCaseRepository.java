package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestCase;
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
 * 测试用例数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long>, 
        JpaSpecificationExecutor<TestCase> {
    
    /**
     * 根据用例编码查询
     */
    Optional<TestCase> findByCaseCode(String caseCode);
    
    /**
     * 根据需求ID查询用例列表
     */
    List<TestCase> findByRequirementId(Long requirementId);
    
    /**
     * 查询指定前缀的用例编码列表（用于编码生成优化）
     */
    List<TestCase> findByCaseCodeStartingWithOrderByIdDesc(String prefix);
    
    /**
     * 优化的分页查询：使用索引提示和投影查询优化COUNT
     * 只查询必要的字段，减少数据传输量
     * 注意：使用原生SQL和PostgreSQL的ILIKE避免类型推断问题
     */
    @Query(value = "SELECT * FROM test_case tc WHERE " +
            "(:caseName IS NULL OR tc.case_name ILIKE '%' || :caseName || '%') AND " +
            "(:caseStatus IS NULL OR tc.case_status = :caseStatus) AND " +
            "(:requirementId IS NULL OR tc.requirement_id = :requirementId)",
            countQuery = "SELECT COUNT(tc.id) FROM test_case tc WHERE " +
            "(:caseName IS NULL OR tc.case_name ILIKE '%' || :caseName || '%') AND " +
            "(:caseStatus IS NULL OR tc.case_status = :caseStatus) AND " +
            "(:requirementId IS NULL OR tc.requirement_id = :requirementId)",
            nativeQuery = true)
    Page<TestCase> findWithFilters(
            @Param("caseName") String caseName,
            @Param("caseStatus") String caseStatus,
            @Param("requirementId") Long requirementId,
            Pageable pageable);
    
    /**
     * 批量查询用例的需求信息（避免N+1问题）
     * 通过一次查询获取所有关联的需求ID对应的需求信息
     */
    @Query("SELECT DISTINCT tc.requirementId FROM TestCase tc WHERE tc.requirementId IS NOT NULL")
    List<Long> findDistinctRequirementIds();
}

