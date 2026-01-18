package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.LogicTestPoint;
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
 * 逻辑测试要点数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface LogicTestPointRepository extends JpaRepository<LogicTestPoint, Long>, 
        JpaSpecificationExecutor<LogicTestPoint> {
    
    /**
     * 根据要点编码查询
     */
    Optional<LogicTestPoint> findByPointCode(String pointCode);
    
    /**
     * 根据规约ID查询所有逻辑测试要点
     */
    List<LogicTestPoint> findBySpecIdOrderByDisplayOrderAsc(Long specId);
    
    /**
     * 根据规约ID和启用状态查询
     */
    List<LogicTestPoint> findBySpecIdAndIsActiveOrderByDisplayOrderAsc(Long specId, String isActive);
    
    /**
     * 根据逻辑名称查询
     */
    List<LogicTestPoint> findByLogicNameContainingIgnoreCase(String logicName);
    
    /**
     * 根据逻辑类型查询
     */
    List<LogicTestPoint> findByLogicType(String logicType);
    
    /**
     * 分页查询：支持按逻辑名称、规约ID、启用状态搜索
     */
    @Query(value = "SELECT * FROM logic_test_point ltp WHERE " +
            "(:logicName IS NULL OR ltp.logic_name ILIKE '%' || :logicName || '%') AND " +
            "(:specId IS NULL OR ltp.spec_id = :specId) AND " +
            "(:isActive IS NULL OR ltp.is_active = :isActive)",
            countQuery = "SELECT COUNT(ltp.id) FROM logic_test_point ltp WHERE " +
            "(:logicName IS NULL OR ltp.logic_name ILIKE '%' || :logicName || '%') AND " +
            "(:specId IS NULL OR ltp.spec_id = :specId) AND " +
            "(:isActive IS NULL OR ltp.is_active = :isActive)",
            nativeQuery = true)
    Page<LogicTestPoint> findWithFilters(
            @Param("logicName") String logicName,
            @Param("specId") Long specId,
            @Param("isActive") String isActive,
            Pageable pageable);
    
    /**
     * 查询指定前缀的要点编码列表（用于编码生成优化）
     */
    List<LogicTestPoint> findByPointCodeStartingWithOrderByIdDesc(String prefix);
}

