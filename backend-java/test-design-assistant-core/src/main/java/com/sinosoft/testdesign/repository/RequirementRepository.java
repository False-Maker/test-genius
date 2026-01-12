package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

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
}

