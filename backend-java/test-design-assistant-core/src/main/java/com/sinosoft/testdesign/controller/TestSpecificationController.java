package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.*;
import com.sinosoft.testdesign.entity.FieldTestPoint;
import com.sinosoft.testdesign.entity.LogicTestPoint;
import com.sinosoft.testdesign.entity.SpecVersion;
import com.sinosoft.testdesign.entity.TestSpecification;
import com.sinosoft.testdesign.mapper.EntityDTOMapper;
import com.sinosoft.testdesign.service.FieldTestPointService;
import com.sinosoft.testdesign.service.LogicTestPointService;
import com.sinosoft.testdesign.service.TestSpecificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 测试规约管理控制器
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Tag(name = "测试规约管理", description = "测试规约管理相关接口")
@RestController
@RequestMapping("/v1/specifications")
@RequiredArgsConstructor
public class TestSpecificationController {
    
    private final TestSpecificationService specificationService;
    private final FieldTestPointService fieldTestPointService;
    private final LogicTestPointService logicTestPointService;
    private final EntityDTOMapper entityDTOMapper;
    
    // ========== 测试规约管理 ==========
    
    @Operation(summary = "创建测试规约", description = "创建新的测试规约（应用级或公共规约）")
    @PostMapping
    public Result<TestSpecificationResponseDTO> createSpecification(
            @Valid @RequestBody TestSpecificationRequestDTO dto) {
        TestSpecification specification = entityDTOMapper.toTestSpecificationEntity(dto);
        TestSpecification saved = specificationService.createSpecification(specification);
        return Result.success(entityDTOMapper.toTestSpecificationResponseDTO(saved));
    }
    
    @Operation(summary = "查询测试规约列表", description = "分页查询测试规约列表，支持按名称、类型、状态搜索")
    @GetMapping
    public Result<Page<TestSpecificationResponseDTO>> getSpecificationList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String specName,
            @RequestParam(required = false) String specType,
            @RequestParam(required = false) String isActive) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TestSpecification> specificationPage = specificationService.getSpecificationList(
                pageable, specName, specType, isActive);
        
        // 转换为DTO分页
        Page<TestSpecificationResponseDTO> dtoPage = specificationPage.map(
                entityDTOMapper::toTestSpecificationResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取测试规约详情", description = "根据ID获取测试规约详情")
    @GetMapping("/{id}")
    public Result<TestSpecificationResponseDTO> getSpecificationById(@PathVariable Long id) {
        TestSpecification specification = specificationService.getSpecificationById(id);
        return Result.success(entityDTOMapper.toTestSpecificationResponseDTO(specification));
    }
    
    @Operation(summary = "根据编码获取测试规约", description = "根据规约编码获取测试规约详情")
    @GetMapping("/code/{specCode}")
    public Result<TestSpecificationResponseDTO> getSpecificationByCode(@PathVariable String specCode) {
        TestSpecification specification = specificationService.getSpecificationByCode(specCode);
        return Result.success(entityDTOMapper.toTestSpecificationResponseDTO(specification));
    }
    
    @Operation(summary = "更新测试规约", description = "更新测试规约信息")
    @PutMapping("/{id}")
    public Result<TestSpecificationResponseDTO> updateSpecification(
            @PathVariable Long id,
            @Valid @RequestBody TestSpecificationRequestDTO dto) {
        TestSpecification specification = specificationService.getSpecificationById(id);
        entityDTOMapper.updateTestSpecificationFromDTO(dto, specification);
        TestSpecification updated = specificationService.updateSpecification(id, specification);
        return Result.success(entityDTOMapper.toTestSpecificationResponseDTO(updated));
    }
    
    @Operation(summary = "删除测试规约", description = "删除指定测试规约")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSpecification(@PathVariable Long id) {
        specificationService.deleteSpecification(id);
        return Result.success();
    }
    
    @Operation(summary = "更新测试规约状态", description = "启用/禁用测试规约")
    @PutMapping("/{id}/status")
    public Result<TestSpecificationResponseDTO> updateSpecificationStatus(
            @PathVariable Long id,
            @RequestParam String isActive) {
        TestSpecification updated = specificationService.updateSpecificationStatus(id, isActive);
        return Result.success(entityDTOMapper.toTestSpecificationResponseDTO(updated));
    }
    
    @Operation(summary = "查询应用级规约列表", description = "查询所有启用的应用级规约")
    @GetMapping("/application")
    public Result<List<TestSpecificationResponseDTO>> getApplicationSpecifications() {
        List<TestSpecification> specifications = specificationService.getApplicationSpecifications();
        List<TestSpecificationResponseDTO> dtoList = specifications.stream()
                .map(entityDTOMapper::toTestSpecificationResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "查询公共规约列表", description = "查询所有启用的公共规约")
    @GetMapping("/public")
    public Result<List<TestSpecificationResponseDTO>> getPublicSpecifications() {
        List<TestSpecification> specifications = specificationService.getPublicSpecifications();
        List<TestSpecificationResponseDTO> dtoList = specifications.stream()
                .map(entityDTOMapper::toTestSpecificationResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "根据模块查询规约", description = "根据适用模块查询规约列表")
    @GetMapping("/module/{module}")
    public Result<List<TestSpecificationResponseDTO>> getSpecificationsByModule(@PathVariable String module) {
        List<TestSpecification> specifications = specificationService.getSpecificationsByModule(module);
        List<TestSpecificationResponseDTO> dtoList = specifications.stream()
                .map(entityDTOMapper::toTestSpecificationResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "根据测试分层查询规约", description = "根据适用测试分层查询规约列表")
    @GetMapping("/layer/{layer}")
    public Result<List<TestSpecificationResponseDTO>> getSpecificationsByLayer(@PathVariable String layer) {
        List<TestSpecification> specifications = specificationService.getSpecificationsByLayer(layer);
        List<TestSpecificationResponseDTO> dtoList = specifications.stream()
                .map(entityDTOMapper::toTestSpecificationResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    // ========== 规约版本管理 ==========
    
    @Operation(summary = "创建规约版本", description = "为测试规约创建新版本")
    @PostMapping("/{specId}/versions")
    public Result<TestSpecificationResponseDTO> createVersion(
            @PathVariable Long specId,
            @RequestParam String versionNumber,
            @RequestParam(required = false) String versionName,
            @RequestParam(required = false) String versionDescription,
            @RequestParam(required = false) String changeLog) {
        TestSpecification specification = specificationService.createVersion(
                specId, versionNumber, versionName, versionDescription, changeLog);
        return Result.success(entityDTOMapper.toTestSpecificationResponseDTO(specification));
    }
    
    @Operation(summary = "切换规约版本", description = "切换测试规约的当前版本")
    @PutMapping("/{specId}/versions/{versionNumber}/switch")
    public Result<TestSpecificationResponseDTO> switchVersion(
            @PathVariable Long specId,
            @PathVariable String versionNumber) {
        TestSpecification specification = specificationService.switchVersion(specId, versionNumber);
        return Result.success(entityDTOMapper.toTestSpecificationResponseDTO(specification));
    }
    
    @Operation(summary = "查询规约版本列表", description = "查询指定测试规约的所有版本")
    @GetMapping("/{specId}/versions")
    public Result<List<SpecVersionResponseDTO>> getVersionList(@PathVariable Long specId) {
        List<SpecVersion> versions = specificationService.getVersionList(specId);
        List<SpecVersionResponseDTO> dtoList = versions.stream()
                .map(entityDTOMapper::toSpecVersionResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    // ========== 字段测试要点管理 ==========
    
    @Operation(summary = "创建字段测试要点", description = "创建新的字段测试要点")
    @PostMapping("/field-points")
    public Result<FieldTestPointResponseDTO> createFieldTestPoint(
            @Valid @RequestBody FieldTestPointRequestDTO dto) {
        FieldTestPoint fieldTestPoint = entityDTOMapper.toFieldTestPointEntity(dto);
        FieldTestPoint saved = fieldTestPointService.createFieldTestPoint(fieldTestPoint);
        return Result.success(entityDTOMapper.toFieldTestPointResponseDTO(saved));
    }
    
    @Operation(summary = "查询字段测试要点列表", description = "分页查询字段测试要点列表，支持按字段名称、规约ID、状态搜索")
    @GetMapping("/field-points")
    public Result<Page<FieldTestPointResponseDTO>> getFieldTestPointList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String fieldName,
            @RequestParam(required = false) Long specId,
            @RequestParam(required = false) String isActive) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FieldTestPoint> pointPage = fieldTestPointService.getFieldTestPointList(
                pageable, fieldName, specId, isActive);
        
        // 转换为DTO分页
        Page<FieldTestPointResponseDTO> dtoPage = pointPage.map(
                entityDTOMapper::toFieldTestPointResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取字段测试要点详情", description = "根据ID获取字段测试要点详情")
    @GetMapping("/field-points/{id}")
    public Result<FieldTestPointResponseDTO> getFieldTestPointById(@PathVariable Long id) {
        FieldTestPoint fieldTestPoint = fieldTestPointService.getFieldTestPointById(id);
        return Result.success(entityDTOMapper.toFieldTestPointResponseDTO(fieldTestPoint));
    }
    
    @Operation(summary = "根据规约ID查询字段测试要点", description = "查询指定规约的所有字段测试要点")
    @GetMapping("/{specId}/field-points")
    public Result<List<FieldTestPointResponseDTO>> getFieldTestPointsBySpecId(@PathVariable Long specId) {
        List<FieldTestPoint> fieldTestPoints = fieldTestPointService.getFieldTestPointsBySpecId(specId);
        List<FieldTestPointResponseDTO> dtoList = fieldTestPoints.stream()
                .map(entityDTOMapper::toFieldTestPointResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "更新字段测试要点", description = "更新字段测试要点信息")
    @PutMapping("/field-points/{id}")
    public Result<FieldTestPointResponseDTO> updateFieldTestPoint(
            @PathVariable Long id,
            @Valid @RequestBody FieldTestPointRequestDTO dto) {
        FieldTestPoint fieldTestPoint = fieldTestPointService.getFieldTestPointById(id);
        entityDTOMapper.updateFieldTestPointFromDTO(dto, fieldTestPoint);
        FieldTestPoint updated = fieldTestPointService.updateFieldTestPoint(id, fieldTestPoint);
        return Result.success(entityDTOMapper.toFieldTestPointResponseDTO(updated));
    }
    
    @Operation(summary = "删除字段测试要点", description = "删除指定字段测试要点")
    @DeleteMapping("/field-points/{id}")
    public Result<Void> deleteFieldTestPoint(@PathVariable Long id) {
        fieldTestPointService.deleteFieldTestPoint(id);
        return Result.success();
    }
    
    @Operation(summary = "更新字段测试要点状态", description = "启用/禁用字段测试要点")
    @PutMapping("/field-points/{id}/status")
    public Result<FieldTestPointResponseDTO> updateFieldTestPointStatus(
            @PathVariable Long id,
            @RequestParam String isActive) {
        FieldTestPoint updated = fieldTestPointService.updateFieldTestPointStatus(id, isActive);
        return Result.success(entityDTOMapper.toFieldTestPointResponseDTO(updated));
    }
    
    // ========== 逻辑测试要点管理 ==========
    
    @Operation(summary = "创建逻辑测试要点", description = "创建新的逻辑测试要点")
    @PostMapping("/logic-points")
    public Result<LogicTestPointResponseDTO> createLogicTestPoint(
            @Valid @RequestBody LogicTestPointRequestDTO dto) {
        LogicTestPoint logicTestPoint = entityDTOMapper.toLogicTestPointEntity(dto);
        LogicTestPoint saved = logicTestPointService.createLogicTestPoint(logicTestPoint);
        return Result.success(entityDTOMapper.toLogicTestPointResponseDTO(saved));
    }
    
    @Operation(summary = "查询逻辑测试要点列表", description = "分页查询逻辑测试要点列表，支持按逻辑名称、规约ID、状态搜索")
    @GetMapping("/logic-points")
    public Result<Page<LogicTestPointResponseDTO>> getLogicTestPointList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String logicName,
            @RequestParam(required = false) Long specId,
            @RequestParam(required = false) String isActive) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LogicTestPoint> pointPage = logicTestPointService.getLogicTestPointList(
                pageable, logicName, specId, isActive);
        
        // 转换为DTO分页
        Page<LogicTestPointResponseDTO> dtoPage = pointPage.map(
                entityDTOMapper::toLogicTestPointResponseDTO);
        return Result.success(dtoPage);
    }
    
    @Operation(summary = "获取逻辑测试要点详情", description = "根据ID获取逻辑测试要点详情")
    @GetMapping("/logic-points/{id}")
    public Result<LogicTestPointResponseDTO> getLogicTestPointById(@PathVariable Long id) {
        LogicTestPoint logicTestPoint = logicTestPointService.getLogicTestPointById(id);
        return Result.success(entityDTOMapper.toLogicTestPointResponseDTO(logicTestPoint));
    }
    
    @Operation(summary = "根据规约ID查询逻辑测试要点", description = "查询指定规约的所有逻辑测试要点")
    @GetMapping("/{specId}/logic-points")
    public Result<List<LogicTestPointResponseDTO>> getLogicTestPointsBySpecId(@PathVariable Long specId) {
        List<LogicTestPoint> logicTestPoints = logicTestPointService.getLogicTestPointsBySpecId(specId);
        List<LogicTestPointResponseDTO> dtoList = logicTestPoints.stream()
                .map(entityDTOMapper::toLogicTestPointResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "根据逻辑类型查询逻辑测试要点", description = "根据逻辑类型查询逻辑测试要点列表")
    @GetMapping("/logic-points/type/{logicType}")
    public Result<List<LogicTestPointResponseDTO>> getLogicTestPointsByType(@PathVariable String logicType) {
        List<LogicTestPoint> logicTestPoints = logicTestPointService.getLogicTestPointsByType(logicType);
        List<LogicTestPointResponseDTO> dtoList = logicTestPoints.stream()
                .map(entityDTOMapper::toLogicTestPointResponseDTO)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @Operation(summary = "更新逻辑测试要点", description = "更新逻辑测试要点信息")
    @PutMapping("/logic-points/{id}")
    public Result<LogicTestPointResponseDTO> updateLogicTestPoint(
            @PathVariable Long id,
            @Valid @RequestBody LogicTestPointRequestDTO dto) {
        LogicTestPoint logicTestPoint = logicTestPointService.getLogicTestPointById(id);
        entityDTOMapper.updateLogicTestPointFromDTO(dto, logicTestPoint);
        LogicTestPoint updated = logicTestPointService.updateLogicTestPoint(id, logicTestPoint);
        return Result.success(entityDTOMapper.toLogicTestPointResponseDTO(updated));
    }
    
    @Operation(summary = "删除逻辑测试要点", description = "删除指定逻辑测试要点")
    @DeleteMapping("/logic-points/{id}")
    public Result<Void> deleteLogicTestPoint(@PathVariable Long id) {
        logicTestPointService.deleteLogicTestPoint(id);
        return Result.success();
    }
    
    @Operation(summary = "更新逻辑测试要点状态", description = "启用/禁用逻辑测试要点")
    @PutMapping("/logic-points/{id}/status")
    public Result<LogicTestPointResponseDTO> updateLogicTestPointStatus(
            @PathVariable Long id,
            @RequestParam String isActive) {
        LogicTestPoint updated = logicTestPointService.updateLogicTestPointStatus(id, isActive);
        return Result.success(entityDTOMapper.toLogicTestPointResponseDTO(updated));
    }
}

