package com.sinosoft.testdesign.repository;

import com.sinosoft.testdesign.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 应用管理数据访问接口（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByAppCode(String appCode);

    List<Application> findByAppType(String appType);

    List<Application> findByIsActiveTrue();

    List<Application> findByWorkflowId(Long workflowId);

    List<Application> findByPromptTemplateId(Long promptTemplateId);
}
