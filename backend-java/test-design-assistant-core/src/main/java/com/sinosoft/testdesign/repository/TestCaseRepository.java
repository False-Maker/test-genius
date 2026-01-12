package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}

