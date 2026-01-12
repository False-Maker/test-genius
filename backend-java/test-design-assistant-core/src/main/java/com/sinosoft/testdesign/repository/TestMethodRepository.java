package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestDesignMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 测试设计方法数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface TestMethodRepository extends JpaRepository<TestDesignMethod, Long> {
    
    /**
     * 根据方法编码查询
     */
    Optional<TestDesignMethod> findByMethodCode(String methodCode);
    
    /**
     * 查询所有启用的测试方法
     */
    List<TestDesignMethod> findByIsActive(String isActive);
    
    /**
     * 根据方法名称查询
     */
    Optional<TestDesignMethod> findByMethodName(String methodName);
}

