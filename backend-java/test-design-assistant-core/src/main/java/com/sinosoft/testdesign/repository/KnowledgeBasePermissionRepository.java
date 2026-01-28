package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.KnowledgeBasePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知识库权限数据访问接口
 * 
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Repository
public interface KnowledgeBasePermissionRepository extends JpaRepository<KnowledgeBasePermission, Long>, 
        JpaSpecificationExecutor<KnowledgeBasePermission> {
    
    /**
     * 查询指定知识库和用户的权限
     * 
     * @param kbId 知识库ID
     * @param userId 用户ID
     * @return 权限列表
     */
    List<KnowledgeBasePermission> findByKbIdAndUserId(Long kbId, Long userId);
    
    /**
     * 查询指定知识库、用户和权限类型的记录
     * 
     * @param kbId 知识库ID
     * @param userId 用户ID
     * @param permissionType 权限类型（read/write/admin）
     * @return 权限实体
     */
    Optional<KnowledgeBasePermission> findByKbIdAndUserIdAndPermissionType(
            Long kbId, Long userId, String permissionType);
    
    /**
     * 查询用户的所有权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    List<KnowledgeBasePermission> findByUserId(Long userId);
    
    /**
     * 查询知识库的所有权限
     * 
     * @param kbId 知识库ID
     * @return 权限列表
     */
    List<KnowledgeBasePermission> findByKbId(Long kbId);
    
    /**
     * 检查用户是否有指定知识库的指定权限
     * 
     * @param kbId 知识库ID
     * @param userId 用户ID
     * @param permissionType 权限类型
     * @return 是否存在该权限
     */
    boolean existsByKbIdAndUserIdAndPermissionType(Long kbId, Long userId, String permissionType);
    
    /**
     * 查询有管理员权限的知识库ID列表
     * 
     * @param userId 用户ID
     * @param permissionType 权限类型
     * @return 知识库ID列表
     */
    List<Long> findKbIdByUserIdAndPermissionType(Long userId, String permissionType);
    
    /**
     * 删除指定知识库和用户的所有权限
     * 
     * @param kbId 知识库ID
     * @param userId 用户ID
     */
    void deleteByKbIdAndUserId(Long kbId, Long userId);
}

