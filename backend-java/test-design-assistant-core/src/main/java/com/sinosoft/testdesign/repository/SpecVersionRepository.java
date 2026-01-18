package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.SpecVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 规约版本管理数据访问接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Repository
public interface SpecVersionRepository extends JpaRepository<SpecVersion, Long> {
    
    /**
     * 根据规约ID查询所有版本
     */
    List<SpecVersion> findBySpecIdOrderByCreateTimeDesc(Long specId);
    
    /**
     * 根据规约ID和版本号查询
     */
    Optional<SpecVersion> findBySpecIdAndVersionNumber(Long specId, String versionNumber);
    
    /**
     * 查询当前版本
     */
    Optional<SpecVersion> findBySpecIdAndIsCurrent(Long specId, String isCurrent);
    
    /**
     * 更新指定规约的所有版本为非当前版本
     */
    @Modifying
    @Query("UPDATE SpecVersion sv SET sv.isCurrent = '0' WHERE sv.specId = :specId")
    void updateAllVersionsToNonCurrent(@Param("specId") Long specId);
}

