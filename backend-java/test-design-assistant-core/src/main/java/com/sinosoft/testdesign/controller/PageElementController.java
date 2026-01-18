package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.PageElementInfoRequestDTO;
import com.sinosoft.testdesign.dto.PageElementInfoResponseDTO;
import com.sinosoft.testdesign.entity.PageElementInfo;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.PageElementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 页面元素信息控制器
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Tag(name = "页面元素信息", description = "页面元素信息管理相关接口")
@RestController
@RequestMapping("/v1/page-elements")
@RequiredArgsConstructor
public class PageElementController {
    
    private final PageElementService pageElementService;
    private final EntityDTOMapper entityDTOMapper;
    
    @Operation(summary = "创建页面元素信息", description = "创建新的页面元素信息")
    @PostMapping
    public Result<PageElementInfoResponseDTO> createPageElement(@Valid @RequestBody PageElementInfoRequestDTO dto) {
        PageElementInfo element = entityDTOMapper.toPageElementInfoEntity(dto);
        PageElementInfo saved = pageElementService.createPageElement(element);
        return Result.success(entityDTOMapper.toPageElementInfoResponseDTO(saved));
    }
    
    @Operation(summary = "查询页面元素信息列表", description = "分页查询页面元素信息列表，支持按页面URL、元素类型搜索")
    @GetMapping
    public Result<Page<PageElementInfoResponseDTO>> getPageElementList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String pageUrl,
            @RequestParam(required = false) String elementType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PageElementInfo> elementPage = pageElementService.getPageElementList(pageable, pageUrl, elementType);
        
        Page<PageElementInfoResponseDTO> dtoPage = elementPage.map(entityDTOMapper::toPageElementInfoResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取页面元素信息详情", description = "根据ID获取页面元素信息详情")
    @GetMapping("/{id}")
    public Result<PageElementInfoResponseDTO> getPageElementById(@PathVariable Long id) {
        PageElementInfo element = pageElementService.getPageElementById(id);
        return Result.success(entityDTOMapper.toPageElementInfoResponseDTO(element));
    }
    
    @Operation(summary = "根据元素编码获取页面元素信息详情", description = "根据元素编码获取页面元素信息详情")
    @GetMapping("/code/{elementCode}")
    public Result<PageElementInfoResponseDTO> getPageElementByCode(@PathVariable String elementCode) {
        PageElementInfo element = pageElementService.getPageElementByCode(elementCode);
        return Result.success(entityDTOMapper.toPageElementInfoResponseDTO(element));
    }
    
    @Operation(summary = "根据页面URL查询元素列表", description = "根据页面URL查询所有元素信息")
    @GetMapping("/by-url")
    public Result<List<PageElementInfoResponseDTO>> getPageElementsByUrl(@RequestParam String pageUrl) {
        List<PageElementInfo> elements = pageElementService.getPageElementsByUrl(pageUrl);
        List<PageElementInfoResponseDTO> dtoList = elements.stream()
                .map(entityDTOMapper::toPageElementInfoResponseDTO)
                .toList();
        return Result.success(dtoList);
    }
    
    @Operation(summary = "更新页面元素信息", description = "更新页面元素信息")
    @PutMapping("/{id}")
    public Result<PageElementInfoResponseDTO> updatePageElement(
            @PathVariable Long id,
            @Valid @RequestBody PageElementInfoRequestDTO dto) {
        PageElementInfo element = entityDTOMapper.toPageElementInfoEntity(dto);
        PageElementInfo updated = pageElementService.updatePageElement(id, element);
        return Result.success(entityDTOMapper.toPageElementInfoResponseDTO(updated));
    }
    
    @Operation(summary = "删除页面元素信息", description = "删除指定页面元素信息")
    @DeleteMapping("/{id}")
    public Result<Void> deletePageElement(@PathVariable Long id) {
        pageElementService.deletePageElement(id);
        return Result.success();
    }
}

