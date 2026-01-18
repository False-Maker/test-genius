package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.PromptTemplate;
import com.sinosoft.testdesign.repository.PromptTemplateRepository;
import com.sinosoft.testdesign.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词模板管理服务实现
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl implements PromptTemplateService {
    
    private final PromptTemplateRepository templateRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String TEMPLATE_CODE_PREFIX = "TMP";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    @Override
    @Transactional
    public PromptTemplate createTemplate(PromptTemplate template) {
        log.info("创建提示词模板: {}", template.getTemplateName());
        
        // 数据验证
        validateTemplate(template, true);
        
        // 自动生成模板编码（如果未提供）
        if (!StringUtils.hasText(template.getTemplateCode())) {
            template.setTemplateCode(generateTemplateCode());
        } else {
            // 检查编码是否已存在
            if (templateRepository.findByTemplateCode(template.getTemplateCode()).isPresent()) {
                throw new BusinessException("模板编码已存在: " + template.getTemplateCode());
            }
        }
        
        // 设置默认版本号
        if (template.getVersion() == null) {
            template.setVersion(1);
        }
        
        // 设置默认启用状态
        if (!StringUtils.hasText(template.getIsActive())) {
            template.setIsActive("1");
        }
        
        // 验证模板变量定义
        if (StringUtils.hasText(template.getTemplateVariables())) {
            validateTemplateVariables(template.getTemplateVariables());
        }
        
        log.info("创建模板成功，编码: {}", template.getTemplateCode());
        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
    public PromptTemplate updateTemplate(Long id, PromptTemplate template) {
        log.info("更新提示词模板: {}", id);
        
        PromptTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        
        // 数据验证
        validateTemplate(template, false);
        
        // 不允许修改模板编码
        if (StringUtils.hasText(template.getTemplateCode()) 
                && !template.getTemplateCode().equals(existing.getTemplateCode())) {
            throw new BusinessException("不允许修改模板编码");
        }
        
        // 验证模板变量定义
        if (StringUtils.hasText(template.getTemplateVariables())) {
            validateTemplateVariables(template.getTemplateVariables());
        }
        
        // 更新字段
        if (StringUtils.hasText(template.getTemplateName())) {
            existing.setTemplateName(template.getTemplateName());
        }
        if (StringUtils.hasText(template.getTemplateCategory())) {
            existing.setTemplateCategory(template.getTemplateCategory());
        }
        if (StringUtils.hasText(template.getTemplateType())) {
            existing.setTemplateType(template.getTemplateType());
        }
        if (StringUtils.hasText(template.getTemplateContent())) {
            existing.setTemplateContent(template.getTemplateContent());
        }
        if (template.getTemplateVariables() != null) {
            existing.setTemplateVariables(template.getTemplateVariables());
        }
        if (StringUtils.hasText(template.getApplicableLayers())) {
            existing.setApplicableLayers(template.getApplicableLayers());
        }
        if (StringUtils.hasText(template.getApplicableMethods())) {
            existing.setApplicableMethods(template.getApplicableMethods());
        }
        if (StringUtils.hasText(template.getApplicableModules())) {
            existing.setApplicableModules(template.getApplicableModules());
        }
        if (template.getTemplateDescription() != null) {
            existing.setTemplateDescription(template.getTemplateDescription());
        }
        if (StringUtils.hasText(template.getIsActive())) {
            existing.setIsActive(template.getIsActive());
        }
        
        // 版本号自增
        existing.setVersion(existing.getVersion() + 1);
        
        log.info("更新模板成功，编码: {}", existing.getTemplateCode());
        return templateRepository.save(existing);
    }
    
    @Override
    public PromptTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
    }
    
    @Override
    public Page<PromptTemplate> getTemplateList(Pageable pageable) {
        return templateRepository.findAll(pageable);
    }
    
    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("删除提示词模板: {}", id);
        
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        
        templateRepository.deleteById(id);
        log.info("删除模板成功，编码: {}", template.getTemplateCode());
    }
    
    @Override
    @Transactional
    public PromptTemplate toggleTemplateStatus(Long id, String isActive) {
        log.info("切换模板状态: {} -> {}", id, isActive);
        
        PromptTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        
        if (!"0".equals(isActive) && !"1".equals(isActive)) {
            throw new BusinessException("状态值必须是 0 或 1");
        }
        
        template.setIsActive(isActive);
        template.setVersion(template.getVersion() + 1);
        
        log.info("切换模板状态成功，编码: {}, 新状态: {}", template.getTemplateCode(), isActive);
        return templateRepository.save(template);
    }
    
    @Override
    public String generatePrompt(Long templateId, Map<String, Object> variables) {
        PromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        
        if (!"1".equals(template.getIsActive())) {
            throw new BusinessException("模板未启用，无法生成提示词");
        }
        
        return generatePrompt(template.getTemplateContent(), variables);
    }
    
    @Override
    public String generatePrompt(String templateContent, Map<String, Object> variables) {
        if (!StringUtils.hasText(templateContent)) {
            throw new BusinessException("模板内容不能为空");
        }
        
        if (variables == null) {
            variables = new HashMap<>();
        }
        
        String result = templateContent;
        Matcher matcher = VARIABLE_PATTERN.matcher(templateContent);
        
        // 查找所有变量占位符
        Set<String> requiredVariables = new HashSet<>();
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            requiredVariables.add(variableName);
        }
        
        // 检查必需变量是否都提供了值
        for (String varName : requiredVariables) {
            if (!variables.containsKey(varName) || variables.get(varName) == null) {
                log.warn("模板变量 {} 未提供值，将使用空字符串替换", varName);
            }
        }
        
        // 替换所有变量
        matcher = VARIABLE_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * 生成模板编码
     * 格式：TMP-YYYYMMDD-序号（如 TMP-20240101-001）
     */
    private String generateTemplateCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = TEMPLATE_CODE_PREFIX + "-" + dateStr + "-";
        
        // 优化：只查询当天前缀的模板，避免全表扫描
        List<PromptTemplate> todayTemplates = templateRepository
                .findByTemplateCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (PromptTemplate t : todayTemplates) {
            String code = t.getTemplateCode();
            if (code.length() > prefix.length()) {
                try {
                    int sequence = Integer.parseInt(code.substring(prefix.length()));
                    maxSequence = Math.max(maxSequence, sequence);
                } catch (NumberFormatException e) {
                    // 忽略格式不正确的编码
                }
            }
        }
        
        // 生成新序号
        int newSequence = maxSequence + 1;
        return prefix + String.format("%03d", newSequence);
    }
    
    /**
     * 验证模板数据
     */
    private void validateTemplate(PromptTemplate template, boolean isCreate) {
        if (template == null) {
            throw new BusinessException("模板信息不能为空");
        }
        
        // 验证模板名称
        if (!StringUtils.hasText(template.getTemplateName())) {
            throw new BusinessException("模板名称不能为空");
        }
        if (template.getTemplateName().length() > 500) {
            throw new BusinessException("模板名称长度不能超过500个字符");
        }
        
        // 验证模板编码（创建时）
        if (isCreate && StringUtils.hasText(template.getTemplateCode())) {
            if (template.getTemplateCode().length() > 100) {
                throw new BusinessException("模板编码长度不能超过100个字符");
            }
        }
        
        // 验证模板内容（创建时必须，更新时可选）
        if (isCreate && !StringUtils.hasText(template.getTemplateContent())) {
            throw new BusinessException("模板内容不能为空");
        }
    }
    
    /**
     * 验证模板变量定义JSON格式
     */
    private void validateTemplateVariables(String templateVariables) {
        try {
            // 尝试解析JSON
            Map<String, Object> variables = objectMapper.readValue(
                    templateVariables, 
                    new TypeReference<Map<String, Object>>() {}
            );
            
            // 验证变量定义格式（可选，根据实际需求）
            // 这里可以添加更详细的验证逻辑
            
        } catch (Exception e) {
            throw new BusinessException("模板变量定义格式不正确，必须是有效的JSON格式: " + e.getMessage());
        }
    }
}

