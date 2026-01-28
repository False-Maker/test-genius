package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.ApplicationRequestDTO;
import com.sinosoft.testdesign.dto.ApplicationResponseDTO;
import com.sinosoft.testdesign.entity.Application;
import com.sinosoft.testdesign.repository.ApplicationRepository;
import com.sinosoft.testdesign.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 应用管理服务实现（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final String APP_CODE_PREFIX = "APP";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ApplicationRequestDTO dto) {
        String code = StringUtils.hasText(dto.getAppCode()) ? dto.getAppCode() : generateAppCode();
        if (applicationRepository.findByAppCode(code).isPresent()) {
            throw new BusinessException("应用编码已存在: " + code);
        }
        Application app = new Application();
        app.setAppCode(code);
        app.setAppName(dto.getAppName());
        app.setAppType(dto.getAppType());
        app.setWorkflowId(dto.getWorkflowId());
        app.setPromptTemplateId(dto.getPromptTemplateId());
        app.setDescription(dto.getDescription());
        app.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        app.setCreatorId(dto.getCreatorId());
        Application saved = applicationRepository.save(app);
        log.info("创建应用成功: id={}, appCode={}", saved.getId(), saved.getAppCode());
        return saved.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ApplicationRequestDTO dto) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("应用不存在: " + id));
        if (StringUtils.hasText(dto.getAppCode()) && !dto.getAppCode().equals(app.getAppCode())) {
            throw new BusinessException("不允许修改应用编码");
        }
        if (StringUtils.hasText(dto.getAppName())) app.setAppName(dto.getAppName());
        if (StringUtils.hasText(dto.getAppType())) app.setAppType(dto.getAppType());
        if (dto.getWorkflowId() != null) app.setWorkflowId(dto.getWorkflowId());
        if (dto.getPromptTemplateId() != null) app.setPromptTemplateId(dto.getPromptTemplateId());
        if (dto.getDescription() != null) app.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) app.setIsActive(dto.getIsActive());
        applicationRepository.save(app);
        log.info("更新应用成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new BusinessException("应用不存在: " + id);
        }
        applicationRepository.deleteById(id);
        log.info("删除应用成功: id={}", id);
    }

    @Override
    public Optional<ApplicationResponseDTO> getById(Long id) {
        return applicationRepository.findById(id).map(this::toResponse);
    }

    @Override
    public Optional<ApplicationResponseDTO> getByAppCode(String appCode) {
        return applicationRepository.findByAppCode(appCode).map(this::toResponse);
    }

    @Override
    public List<ApplicationResponseDTO> list() {
        return applicationRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponseDTO> listByType(String appType) {
        return applicationRepository.findByAppType(appType).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponseDTO> listActive() {
        return applicationRepository.findByIsActiveTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getVersionInfo(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("应用不存在: " + id));
        Map<String, Object> info = new HashMap<>();
        info.put("applicationId", id);
        info.put("appCode", app.getAppCode());
        info.put("appName", app.getAppName());
        if (app.getWorkflowId() != null) {
            info.put("versionSource", "workflow");
            info.put("workflowId", app.getWorkflowId());
            info.put("versionsUrl", "/v1/workflows/" + app.getWorkflowId() + "/versions");
        } else if (app.getPromptTemplateId() != null) {
            info.put("versionSource", "prompt_template");
            info.put("promptTemplateId", app.getPromptTemplateId());
            info.put("versionsUrl", "/v1/prompt-templates/" + app.getPromptTemplateId() + "/versions");
        } else {
            info.put("versionSource", null);
        }
        return info;
    }

    private ApplicationResponseDTO toResponse(Application a) {
        ApplicationResponseDTO dto = new ApplicationResponseDTO();
        dto.setId(a.getId());
        dto.setAppCode(a.getAppCode());
        dto.setAppName(a.getAppName());
        dto.setAppType(a.getAppType());
        dto.setWorkflowId(a.getWorkflowId());
        dto.setPromptTemplateId(a.getPromptTemplateId());
        dto.setDescription(a.getDescription());
        dto.setIsActive(a.getIsActive());
        dto.setCreatorId(a.getCreatorId());
        dto.setCreateTime(a.getCreateTime());
        dto.setUpdateTime(a.getUpdateTime());
        return dto;
    }

    private String generateAppCode() {
        String dateStr = LocalDateTime.now().format(DATE_FMT);
        String prefix = APP_CODE_PREFIX + "-" + dateStr + "-";
        long count = applicationRepository.findAll().stream()
                .filter(a -> a.getAppCode() != null && a.getAppCode().startsWith(prefix))
                .count();
        return prefix + String.format("%04d", count + 1);
    }
}
