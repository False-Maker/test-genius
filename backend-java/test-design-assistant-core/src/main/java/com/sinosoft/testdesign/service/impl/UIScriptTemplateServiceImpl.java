package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.UIScriptTemplate;
import com.sinosoft.testdesign.repository.UIScriptTemplateRepository;
import com.sinosoft.testdesign.service.UIScriptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * UI脚本模板服务实现
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UIScriptTemplateServiceImpl implements UIScriptTemplateService {
    
    private final UIScriptTemplateRepository templateRepository;
    
    private static final String TEMPLATE_CODE_PREFIX = "TMP";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public UIScriptTemplate createTemplate(UIScriptTemplate template) {
        log.info("创建脚本模板: {}", template.getTemplateName());
        
        // 数据验证
        if (!StringUtils.hasText(template.getTemplateName())) {
            throw new BusinessException("模板名称不能为空");
        }
        if (!StringUtils.hasText(template.getTemplateType())) {
            throw new BusinessException("模板类型不能为空");
        }
        if (!StringUtils.hasText(template.getScriptLanguage())) {
            throw new BusinessException("脚本语言不能为空");
        }
        if (!StringUtils.hasText(template.getTemplateContent())) {
            throw new BusinessException("模板内容不能为空");
        }
        
        // 自动生成模板编码（如果未提供）
        if (!StringUtils.hasText(template.getTemplateCode())) {
            template.setTemplateCode(generateTemplateCode());
        } else {
            // 检查编码是否已存在
            if (templateRepository.findByTemplateCode(template.getTemplateCode()).isPresent()) {
                throw new BusinessException("模板编码已存在: " + template.getTemplateCode());
            }
        }
        
        // 设置默认值
        if (template.getVersion() == null) {
            template.setVersion(1);
        }
        if (!StringUtils.hasText(template.getIsActive())) {
            template.setIsActive("1");
        }
        
        log.info("创建脚本模板成功，编码: {}", template.getTemplateCode());
        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
    public UIScriptTemplate updateTemplate(Long id, UIScriptTemplate template) {
        log.info("更新脚本模板: {}", id);
        
        UIScriptTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("脚本模板不存在"));
        
        // 不允许修改模板编码
        if (StringUtils.hasText(template.getTemplateCode()) && 
            !template.getTemplateCode().equals(existing.getTemplateCode())) {
            throw new BusinessException("模板编码不允许修改");
        }
        
        // 更新字段
        if (StringUtils.hasText(template.getTemplateName())) {
            existing.setTemplateName(template.getTemplateName());
        }
        if (StringUtils.hasText(template.getTemplateType())) {
            existing.setTemplateType(template.getTemplateType());
        }
        if (StringUtils.hasText(template.getScriptLanguage())) {
            existing.setScriptLanguage(template.getScriptLanguage());
        }
        if (StringUtils.hasText(template.getTemplateContent())) {
            existing.setTemplateContent(template.getTemplateContent());
            // 内容更新时版本号自增
            existing.setVersion(existing.getVersion() + 1);
        }
        if (StringUtils.hasText(template.getTemplateVariables())) {
            existing.setTemplateVariables(template.getTemplateVariables());
        }
        if (StringUtils.hasText(template.getApplicableScenarios())) {
            existing.setApplicableScenarios(template.getApplicableScenarios());
        }
        if (StringUtils.hasText(template.getTemplateDescription())) {
            existing.setTemplateDescription(template.getTemplateDescription());
        }
        
        log.info("更新脚本模板成功，编码: {}", existing.getTemplateCode());
        return templateRepository.save(existing);
    }
    
    @Override
    public UIScriptTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("脚本模板不存在"));
    }
    
    @Override
    public UIScriptTemplate getTemplateByCode(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new BusinessException("脚本模板不存在: " + templateCode));
    }
    
    @Override
    public Page<UIScriptTemplate> getTemplateList(Pageable pageable, String templateName, 
            String templateType, String scriptLanguage, String isActive) {
        return templateRepository.findWithFilters(templateName, templateType, scriptLanguage, isActive, pageable);
    }
    
    @Override
    public List<UIScriptTemplate> getActiveTemplatesByTypeAndLanguage(String templateType, String scriptLanguage) {
        return templateRepository.findByTemplateTypeAndScriptLanguageAndIsActive(templateType, scriptLanguage, "1");
    }
    
    @Override
    @Transactional
    public UIScriptTemplate updateTemplateStatus(Long id, String isActive) {
        log.info("更新模板状态: {} -> {}", id, isActive);
        
        UIScriptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("脚本模板不存在"));
        
        template.setIsActive(isActive);
        
        log.info("更新模板状态成功，编码: {}, 新状态: {}", template.getTemplateCode(), isActive);
        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("删除脚本模板: {}", id);
        
        UIScriptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("脚本模板不存在"));
        
        templateRepository.delete(template);
        log.info("删除脚本模板成功，编码: {}", template.getTemplateCode());
    }
    
    /**
     * 生成模板编码
     * 格式：TMP-YYYYMMDD-序号（如 TMP-20240117-001）
     */
    private String generateTemplateCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = TEMPLATE_CODE_PREFIX + "-" + dateStr + "-";
        
        // 查询当天前缀的模板，避免全表扫描
        List<UIScriptTemplate> todayTemplates = templateRepository.findByTemplateCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (UIScriptTemplate template : todayTemplates) {
            String code = template.getTemplateCode();
            if (code != null && code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的编码
                    log.warn("模板编码格式不正确: {}", code);
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        String templateCode = prefix + String.format("%03d", newSequence);
        log.debug("生成模板编码: {}", templateCode);
        return templateCode;
    }
}

