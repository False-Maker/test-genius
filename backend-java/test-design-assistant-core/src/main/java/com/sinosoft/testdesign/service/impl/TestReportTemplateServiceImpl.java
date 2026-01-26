package com.sinosoft.testdesign.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.TestReport;
import com.sinosoft.testdesign.entity.TestReportTemplate;
import com.sinosoft.testdesign.repository.TestReportRepository;
import com.sinosoft.testdesign.repository.TestReportTemplateRepository;
import com.sinosoft.testdesign.service.TestReportTemplateService;
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
import java.util.Optional;

/**
 * 测试报告模板管理服务实现
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestReportTemplateServiceImpl implements TestReportTemplateService {
    
    private final TestReportTemplateRepository templateRepository;
    private final TestReportRepository testReportRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String TEMPLATE_CODE_PREFIX = "TMP";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    @Override
    @Transactional
    public TestReportTemplate createTemplate(TestReportTemplate template) {
        log.info("创建测试报告模板: {}", template.getTemplateName());
        
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
        
        // 设置默认模板标志
        if (!StringUtils.hasText(template.getIsDefault())) {
            template.setIsDefault("0");
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
    public TestReportTemplate updateTemplate(Long id, TestReportTemplate template) {
        log.info("更新测试报告模板: {}", id);
        
        TestReportTemplate existing = templateRepository.findById(id)
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
        if (StringUtils.hasText(template.getTemplateType())) {
            existing.setTemplateType(template.getTemplateType());
        }
        if (StringUtils.hasText(template.getTemplateContent())) {
            existing.setTemplateContent(template.getTemplateContent());
        }
        if (template.getTemplateVariables() != null) {
            existing.setTemplateVariables(template.getTemplateVariables());
        }
        if (StringUtils.hasText(template.getFileFormat())) {
            existing.setFileFormat(template.getFileFormat());
        }
        if (template.getTemplateDescription() != null) {
            existing.setTemplateDescription(template.getTemplateDescription());
        }
        if (StringUtils.hasText(template.getIsActive())) {
            existing.setIsActive(template.getIsActive());
        }
        if (StringUtils.hasText(template.getIsDefault())) {
            existing.setIsDefault(template.getIsDefault());
        }
        
        // 版本号自增
        existing.setVersion(existing.getVersion() + 1);
        
        log.info("更新模板成功，编码: {}", existing.getTemplateCode());
        return templateRepository.save(existing);
    }
    
    @Override
    public TestReportTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
    }
    
    @Override
    public TestReportTemplate getTemplateByCode(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new BusinessException("模板不存在: " + templateCode));
    }
    
    @Override
    public Page<TestReportTemplate> getTemplateList(Pageable pageable) {
        return templateRepository.findAll(pageable);
    }
    
    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("删除测试报告模板: {}", id);
        
        TestReportTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        
        // 检查是否被使用
        List<TestReport> reports = testReportRepository.findByTemplateId(id);
        if (!reports.isEmpty()) {
            throw new BusinessException("该模板正在被 " + reports.size() + " 个报告使用，无法删除");
        }
        
        templateRepository.delete(template);
        log.info("删除模板成功，编码: {}", template.getTemplateCode());
    }
    
    @Override
    @Transactional
    public TestReportTemplate toggleTemplateStatus(Long id, String isActive) {
        log.info("切换模板状态: {} -> {}", id, isActive);
        
        TestReportTemplate template = templateRepository.findById(id)
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
    @Transactional
    public TestReportTemplate setDefaultTemplate(Long id, String templateType) {
        log.info("设置默认模板: {} (类型: {})", id, templateType);
        
        TestReportTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        
        // 检查模板类型是否匹配
        if (!templateType.equals(template.getTemplateType())) {
            throw new BusinessException("模板类型不匹配");
        }
        
        // 取消同类型其他模板的默认标志
        List<TestReportTemplate> defaultTemplates = templateRepository
                .findByTemplateTypeAndIsDefaultAndIsActive(templateType, "1", "1")
                .stream().toList();
        
        for (TestReportTemplate t : defaultTemplates) {
            if (!t.getId().equals(id)) {
                t.setIsDefault("0");
                templateRepository.save(t);
            }
        }
        
        // 设置当前模板为默认
        template.setIsDefault("1");
        template.setVersion(template.getVersion() + 1);
        
        log.info("设置默认模板成功，编码: {}", template.getTemplateCode());
        return templateRepository.save(template);
    }
    
    @Override
    public List<TestReportTemplate> getActiveTemplatesByType(String templateType) {
        return templateRepository.findByTemplateTypeAndIsActive(templateType, "1");
    }
    
    @Override
    public TestReportTemplate getDefaultTemplateByType(String templateType) {
        return templateRepository.findByTemplateTypeAndIsDefaultAndIsActive(templateType, "1", "1")
                .orElseThrow(() -> new BusinessException("未找到类型为 " + templateType + " 的默认模板"));
    }
    
    /**
     * 生成模板编码
     * 格式：TMP-YYYYMMDD-序号（如 TMP-20240117-001）
     */
    private String generateTemplateCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = TEMPLATE_CODE_PREFIX + "-" + dateStr + "-";
        
        // 优化：只查询当天前缀的模板，避免全表扫描
        List<TestReportTemplate> todayTemplates = templateRepository
                .findByTemplateCodeStartingWithOrderByIdDesc(prefix);
        
        int maxSequence = 0;
        for (TestReportTemplate t : todayTemplates) {
            String code = t.getTemplateCode();
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
        return prefix + String.format("%03d", newSequence);
    }
    
    /**
     * 验证模板数据
     */
    private void validateTemplate(TestReportTemplate template, boolean isCreate) {
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
        
        // 验证模板类型
        if (!StringUtils.hasText(template.getTemplateType())) {
            throw new BusinessException("模板类型不能为空");
        }
        // 验证模板类型值
        String templateType = template.getTemplateType();
        if (!templateType.matches("EXECUTION|COVERAGE|QUALITY|RISK")) {
            throw new BusinessException("模板类型必须是 EXECUTION/COVERAGE/QUALITY/RISK 之一");
        }
        
        // 验证模板编码（创建时）
        if (isCreate && StringUtils.hasText(template.getTemplateCode())) {
            if (template.getTemplateCode().length() > 100) {
                throw new BusinessException("模板编码长度不能超过100个字符");
            }
        }
        
        // 验证模板内容（创建时必须提供，更新时如果提供则验证）
        if (isCreate) {
            if (!StringUtils.hasText(template.getTemplateContent())) {
                throw new BusinessException("模板内容不能为空");
            }
        } else if (StringUtils.hasText(template.getTemplateContent())) {
            // 更新时，如果提供了模板内容，则验证其不为空（已经在StringUtils.hasText中验证）
        }
        
        // 验证文件格式（可选）
        if (StringUtils.hasText(template.getFileFormat())) {
            String fileFormat = template.getFileFormat();
            if (!fileFormat.matches("WORD|PDF|EXCEL")) {
                throw new BusinessException("文件格式必须是 WORD/PDF/EXCEL 之一");
            }
        }
    }
    
    /**
     * 验证模板变量定义JSON格式
     */
    private void validateTemplateVariables(String templateVariables) {
        try {
            // 尝试解析JSON
            objectMapper.readValue(
                    templateVariables, 
                    new TypeReference<Object>() {}
            );
        } catch (Exception e) {
            throw new BusinessException("模板变量定义格式不正确，必须是有效的JSON格式: " + e.getMessage());
        }
    }
}

