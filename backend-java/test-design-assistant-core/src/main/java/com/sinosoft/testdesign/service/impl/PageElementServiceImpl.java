package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.entity.PageElementInfo;
import com.sinosoft.testdesign.repository.PageElementInfoRepository;
import com.sinosoft.testdesign.service.PageElementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * 页面元素信息服务实现
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PageElementServiceImpl implements PageElementService {
    
    private final PageElementInfoRepository elementRepository;
    
    @Override
    @Transactional
    public PageElementInfo createPageElement(PageElementInfo element) {
        log.info("创建页面元素信息，页面URL: {}", element.getPageUrl());
        
        // 数据验证
        if (!StringUtils.hasText(element.getPageUrl())) {
            throw new BusinessException("页面URL不能为空");
        }
        
        // 自动生成元素编码（如果未提供）
        if (!StringUtils.hasText(element.getElementCode())) {
            element.setElementCode(generateElementCode());
        } else {
            // 检查编码是否已存在
            if (elementRepository.findByElementCode(element.getElementCode()).isPresent()) {
                throw new BusinessException("元素编码已存在: " + element.getElementCode());
            }
        }
        
        log.info("创建页面元素信息成功，编码: {}", element.getElementCode());
        return elementRepository.save(element);
    }
    
    @Override
    @Transactional
    public PageElementInfo updatePageElement(Long id, PageElementInfo element) {
        log.info("更新页面元素信息: {}", id);
        
        PageElementInfo existing = elementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("页面元素信息不存在"));
        
        // 不允许修改元素编码
        if (StringUtils.hasText(element.getElementCode()) && 
            !element.getElementCode().equals(existing.getElementCode())) {
            throw new BusinessException("元素编码不允许修改");
        }
        
        // 更新字段
        if (StringUtils.hasText(element.getPageUrl())) {
            existing.setPageUrl(element.getPageUrl());
        }
        if (StringUtils.hasText(element.getElementType())) {
            existing.setElementType(element.getElementType());
        }
        if (StringUtils.hasText(element.getElementLocatorType())) {
            existing.setElementLocatorType(element.getElementLocatorType());
        }
        if (StringUtils.hasText(element.getElementLocatorValue())) {
            existing.setElementLocatorValue(element.getElementLocatorValue());
        }
        if (StringUtils.hasText(element.getElementText())) {
            existing.setElementText(element.getElementText());
        }
        if (StringUtils.hasText(element.getElementAttributes())) {
            existing.setElementAttributes(element.getElementAttributes());
        }
        if (StringUtils.hasText(element.getPageStructure())) {
            existing.setPageStructure(element.getPageStructure());
        }
        if (StringUtils.hasText(element.getScreenshotUrl())) {
            existing.setScreenshotUrl(element.getScreenshotUrl());
        }
        
        log.info("更新页面元素信息成功，编码: {}", existing.getElementCode());
        return elementRepository.save(existing);
    }
    
    @Override
    public PageElementInfo getPageElementById(Long id) {
        return elementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("页面元素信息不存在"));
    }
    
    @Override
    public PageElementInfo getPageElementByCode(String elementCode) {
        return elementRepository.findByElementCode(elementCode)
                .orElseThrow(() -> new BusinessException("页面元素信息不存在: " + elementCode));
    }
    
    @Override
    public List<PageElementInfo> getPageElementsByUrl(String pageUrl) {
        return elementRepository.findByPageUrl(pageUrl);
    }
    
    @Override
    public Page<PageElementInfo> getPageElementList(Pageable pageable, String pageUrl, String elementType) {
        return elementRepository.findWithFilters(pageUrl, elementType, pageable);
    }
    
    @Override
    @Transactional
    public void deletePageElement(Long id) {
        log.info("删除页面元素信息: {}", id);
        
        PageElementInfo element = elementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("页面元素信息不存在"));
        
        elementRepository.delete(element);
        log.info("删除页面元素信息成功，编码: {}", element.getElementCode());
    }
    
    /**
     * 生成元素编码
     * 使用UUID生成唯一编码
     */
    private String generateElementCode() {
        // 使用UUID生成唯一编码，格式：ELE-{UUID前8位}
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String elementCode = "ELE-" + uuid.substring(0, 8).toUpperCase();
        log.debug("生成元素编码: {}", elementCode);
        return elementCode;
    }
}

