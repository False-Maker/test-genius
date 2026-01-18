package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.FieldTestPoint;
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
 * 字段测试要点数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface FieldTestPointRepository extends JpaRepository<FieldTestPoint, Long>, 
        JpaSpecificationExecutor<FieldTestPoint> {
    
    /**
     * 根据要点编码查询
     */
    Optional<FieldTestPoint> findByPointCode(String pointCode);
    
    /**
     * 根据规约ID查询所有字段测试要点
     */
    List<FieldTestPoint> findBySpecIdOrderByDisplayOrderAsc(Long specId);
    
    /**
     * 根据规约ID和启用状态查询
     */
    List<FieldTestPoint> findBySpecIdAndIsActiveOrderByDisplayOrderAsc(Long specId, String isActive);
    
    /**
     * 根据字段名称查询
     */
    List<FieldTestPoint> findByFieldNameContainingIgnoreCase(String fieldName);
    
    /**
     * 分页查询：支持按字段名称、规约ID、启用状态搜索
     */
    @Query(value = "SELECT * FROM field_test_point ftp WHERE " +
            "(:fieldName IS NULL OR ftp.field_name ILIKE '%' || :fieldName || '%') AND " +
            "(:specId IS NULL OR ftp.spec_id = :specId) AND " +
            "(:isActive IS NULL OR ftp.is_active = :isActive)",
            countQuery = "SELECT COUNT(ftp.id) FROM field_test_point ftp WHERE " +
            "(:fieldName IS NULL OR ftp.field_name ILIKE '%' || :fieldName || '%') AND " +
            "(:specId IS NULL OR ftp.spec_id = :specId) AND " +
            "(:isActive IS NULL OR ftp.is_active = :isActive)",
            nativeQuery = true)
    Page<FieldTestPoint> findWithFilters(
            @Param("fieldName") String fieldName,
            @Param("specId") Long specId,
            @Param("isActive") String isActive,
            Pageable pageable);
    
    /**
     * 查询指定前缀的要点编码列表（用于编码生成优化）
     */
    List<FieldTestPoint> findByPointCodeStartingWithOrderByIdDesc(String prefix);
}

