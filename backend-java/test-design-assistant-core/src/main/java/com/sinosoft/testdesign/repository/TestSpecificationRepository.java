package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestSpecification;
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
 * 测试规约数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface TestSpecificationRepository extends JpaRepository<TestSpecification, Long>, 
        JpaSpecificationExecutor<TestSpecification> {
    
    /**
     * 根据规约编码查询
     */
    Optional<TestSpecification> findBySpecCode(String specCode);
    
    /**
     * 根据规约类型查询
     */
    List<TestSpecification> findBySpecType(String specType);
    
    /**
     * 根据规约类型和启用状态查询
     */
    List<TestSpecification> findBySpecTypeAndIsActive(String specType, String isActive);
    
    /**
     * 查询指定前缀的规约编码列表（用于编码生成优化）
     */
    List<TestSpecification> findBySpecCodeStartingWithOrderByIdDesc(String prefix);
    
    /**
     * 根据ID列表查询规约
     */
    List<TestSpecification> findByIdIn(List<Long> ids);
    
    /**
     * 根据适用模块查询
     */
    @Query("SELECT ts FROM TestSpecification ts WHERE ts.applicableModules LIKE :pattern AND ts.isActive = '1'")
    List<TestSpecification> findByApplicableModule(@Param("pattern") String pattern);
    
    /**
     * 根据适用测试分层查询
     */
    @Query("SELECT ts FROM TestSpecification ts WHERE ts.applicableLayers LIKE :pattern AND ts.isActive = '1'")
    List<TestSpecification> findByApplicableLayer(@Param("pattern") String pattern);
    
    /**
     * 分页查询：支持按名称、类型、状态搜索
     * 注意：使用原生SQL和PostgreSQL的ILIKE避免类型推断问题
     */
    @Query(value = "SELECT * FROM test_specification ts WHERE " +
            "(:specName IS NULL OR ts.spec_name ILIKE '%' || :specName || '%') AND " +
            "(:specType IS NULL OR ts.spec_type = :specType) AND " +
            "(:isActive IS NULL OR ts.is_active = :isActive)",
            countQuery = "SELECT COUNT(*) FROM test_specification ts WHERE " +
            "(:specName IS NULL OR ts.spec_name ILIKE '%' || :specName || '%') AND " +
            "(:specType IS NULL OR ts.spec_type = :specType) AND " +
            "(:isActive IS NULL OR ts.is_active = :isActive)",
            nativeQuery = true)
    Page<TestSpecification> findWithFilters(
            @Param("specName") String specName,
            @Param("specType") String specType,
            @Param("isActive") String isActive,
            Pageable pageable);
}

