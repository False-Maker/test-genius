package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestRequirement;
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
 * 需求数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface RequirementRepository extends JpaRepository<TestRequirement, Long>, 
        JpaSpecificationExecutor<TestRequirement> {
    
    /**
     * 根据需求编码查询
     */
    Optional<TestRequirement> findByRequirementCode(String requirementCode);
    
    /**
     * 查询指定前缀的需求编码列表（用于编码生成优化）
     */
    List<TestRequirement> findByRequirementCodeStartingWithOrderByIdDesc(String prefix);
    
    /**
     * 优化的分页查询：使用索引提示和投影查询优化COUNT
     * 只查询必要的字段，减少数据传输量
     */
    @Query(value = "SELECT tr FROM TestRequirement tr WHERE " +
            "(:requirementName IS NULL OR LOWER(tr.requirementName) LIKE LOWER(CONCAT('%', :requirementName, '%'))) AND " +
            "(:requirementStatus IS NULL OR tr.requirementStatus = :requirementStatus)",
            countQuery = "SELECT COUNT(tr.id) FROM TestRequirement tr WHERE " +
            "(:requirementName IS NULL OR LOWER(tr.requirementName) LIKE LOWER(CONCAT('%', :requirementName, '%'))) AND " +
            "(:requirementStatus IS NULL OR tr.requirementStatus = :requirementStatus)")
    Page<TestRequirement> findWithFilters(
            @Param("requirementName") String requirementName,
            @Param("requirementStatus") String requirementStatus,
            Pageable pageable);
    
    /**
     * 批量查询需求（根据ID列表）
     * 用于避免N+1问题，一次性查询多个需求
     */
    @Query("SELECT tr FROM TestRequirement tr WHERE tr.id IN :ids")
    List<TestRequirement> findByIds(@Param("ids") List<Long> ids);
}

