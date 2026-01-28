package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.dto.ApplicationRequestDTO;
import com.sinosoft.testdesign.dto.ApplicationResponseDTO;
import com.sinosoft.testdesign.entity.Application;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 应用管理服务接口（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
public interface ApplicationService {

    Long create(ApplicationRequestDTO dto);

    void update(Long id, ApplicationRequestDTO dto);

    void delete(Long id);

    Optional<ApplicationResponseDTO> getById(Long id);

    Optional<ApplicationResponseDTO> getByAppCode(String appCode);

    List<ApplicationResponseDTO> list();

    List<ApplicationResponseDTO> listByType(String appType);

    List<ApplicationResponseDTO> listActive();

    /**
     * 获取应用的版本来源信息（工作流版本 / 提示词版本）
     */
    Map<String, Object> getVersionInfo(Long id);
}
