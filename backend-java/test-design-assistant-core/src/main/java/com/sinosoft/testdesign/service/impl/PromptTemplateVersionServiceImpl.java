package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.entity.PromptTemplate;
import com.sinosoft.testdesign.entity.PromptTemplateVersion;
import com.sinosoft.testdesign.repository.PromptTemplateRepository;
import com.sinosoft.testdesign.repository.PromptTemplateVersionRepository;
import com.sinosoft.testdesign.service.PromptTemplateVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 提示词模板版本管理服务实现
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateVersionServiceImpl implements PromptTemplateVersionService {
    
    private final PromptTemplateVersionRepository versionRepository;
    private final PromptTemplateRepository templateRepository;
    
    @Override
    @Transactional
    public PromptTemplateVersion createVersion(Long templateId, PromptTemplateVersion version) {
        log.info("创建模板版本: templateId={}", templateId);
        
        // 验证模板是否存在
        PromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
        
        // 如果没有指定版本号，自动生成
        if (version.getVersionNumber() == null) {
            Integer maxVersion = versionRepository.findMaxVersionNumberByTemplateId(templateId);
            version.setVersionNumber(maxVersion + 1);
        }
        
        // 检查版本号是否已存在
        versionRepository.findByTemplateIdAndVersionNumber(templateId, version.getVersionNumber())
                .ifPresent(v -> {
                    throw new RuntimeException("版本号已存在: " + version.getVersionNumber());
                });
        
        // 设置模板ID
        version.setTemplateId(templateId);
        version.setCreateTime(LocalDateTime.now());
        
        // 如果设置为当前版本，需要先将其他版本设为非当前版本
        if ("1".equals(version.getIsCurrent())) {
            versionRepository.updateAllVersionsToNonCurrent(templateId);
            
            // 同时更新模板表的版本号
            template.setVersion(version.getVersionNumber());
            templateRepository.save(template);
        }
        
        PromptTemplateVersion saved = versionRepository.save(version);
        log.info("创建版本成功: id={}, versionNumber={}", saved.getId(), saved.getVersionNumber());
        
        return saved;
    }
    
    @Override
    public PromptTemplateVersion getVersionById(Long id) {
        return versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("版本不存在: " + id));
    }
    
    @Override
    public List<PromptTemplateVersion> getVersionsByTemplateId(Long templateId) {
        return versionRepository.findByTemplateIdOrderByVersionNumberDesc(templateId);
    }
    
    @Override
    public PromptTemplateVersion getVersionByTemplateIdAndVersionNumber(Long templateId, Integer versionNumber) {
        return versionRepository.findByTemplateIdAndVersionNumber(templateId, versionNumber)
                .orElseThrow(() -> new RuntimeException(
                        String.format("版本不存在: templateId=%d, versionNumber=%d", templateId, versionNumber)
                ));
    }
    
    @Override
    public PromptTemplateVersion getCurrentVersion(Long templateId) {
        Optional<PromptTemplateVersion> current = versionRepository.findByTemplateIdAndIsCurrent(templateId, "1");
        if (current.isPresent()) {
            return current.get();
        }
        // 如果没有当前版本，返回最新版本
        List<PromptTemplateVersion> versions = versionRepository.findByTemplateIdOrderByVersionNumberDesc(templateId);
        if (versions.isEmpty()) {
            throw new RuntimeException("当前版本不存在: " + templateId);
        }
        return versions.get(0);
    }
    
    @Override
    @Transactional
    public PromptTemplateVersion rollbackToVersion(Long templateId, Integer versionNumber) {
        log.info("回滚模板版本: templateId={}, versionNumber={}", templateId, versionNumber);
        
        // 获取要回滚的版本
        PromptTemplateVersion targetVersion = getVersionByTemplateIdAndVersionNumber(templateId, versionNumber);
        
        // 获取模板
        PromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
        
        // 创建新版本（回滚版本）
        PromptTemplateVersion newVersion = new PromptTemplateVersion();
        newVersion.setTemplateId(templateId);
        newVersion.setVersionNumber(versionRepository.findMaxVersionNumberByTemplateId(templateId) + 1);
        newVersion.setVersionName("回滚到版本 " + versionNumber);
        newVersion.setVersionDescription("从版本 " + template.getVersion() + " 回滚到版本 " + versionNumber);
        newVersion.setTemplateContent(targetVersion.getTemplateContent());
        newVersion.setTemplateVariables(targetVersion.getTemplateVariables());
        newVersion.setChangeLog("回滚操作：恢复到版本 " + versionNumber);
        newVersion.setIsCurrent("1");
        newVersion.setCreatedBy(targetVersion.getCreatedBy());
        newVersion.setCreatedByName(targetVersion.getCreatedByName());
        
        // 将所有版本设为非当前版本
        versionRepository.updateAllVersionsToNonCurrent(templateId);
        
        // 保存新版本
        PromptTemplateVersion saved = versionRepository.save(newVersion);
        
        // 更新模板内容
        template.setTemplateContent(targetVersion.getTemplateContent());
        template.setTemplateVariables(targetVersion.getTemplateVariables());
        template.setVersion(saved.getVersionNumber());
        templateRepository.save(template);
        
        log.info("回滚成功: 新版本号={}", saved.getVersionNumber());
        return saved;
    }
    
    @Override
    public Map<String, Object> compareVersions(Long templateId, Integer versionNumber1, Integer versionNumber2) {
        PromptTemplateVersion version1 = getVersionByTemplateIdAndVersionNumber(templateId, versionNumber1);
        PromptTemplateVersion version2 = getVersionByTemplateIdAndVersionNumber(templateId, versionNumber2);
        
        Map<String, Object> result = new HashMap<>();
        result.put("version1", version1);
        result.put("version2", version2);
        
        // 对比内容差异
        Map<String, Object> diff = new HashMap<>();
        diff.put("contentChanged", !version1.getTemplateContent().equals(version2.getTemplateContent()));
        diff.put("variablesChanged", !java.util.Objects.equals(version1.getTemplateVariables(), version2.getTemplateVariables()));
        result.put("diff", diff);
        
        return result;
    }
    
    @Override
    @Transactional
    public void deleteVersion(Long id) {
        log.info("删除版本: id={}", id);
        
        PromptTemplateVersion version = versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("版本不存在: " + id));
        
        // 不能删除当前版本
        if ("1".equals(version.getIsCurrent())) {
            throw new RuntimeException("不能删除当前版本");
        }
        
        versionRepository.deleteById(id);
        log.info("删除版本成功: id={}", id);
    }
}
