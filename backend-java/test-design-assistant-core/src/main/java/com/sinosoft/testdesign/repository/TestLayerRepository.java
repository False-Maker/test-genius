package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.TestLayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 测试分层数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface TestLayerRepository extends JpaRepository<TestLayer, Long> {
    
    /**
     * 根据分层编码查询
     */
    Optional<TestLayer> findByLayerCode(String layerCode);
    
    /**
     * 查询所有启用的测试分层
     */
    List<TestLayer> findByIsActiveOrderByLayerOrder(String isActive);
}

