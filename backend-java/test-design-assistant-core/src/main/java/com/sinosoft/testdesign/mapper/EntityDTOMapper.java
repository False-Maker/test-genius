package com.sinosoft.testdesign.mapper;

import com.sinosoft.testdesign.dto.*;
import com.sinosoft.testdesign.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Entity和DTO转换Mapper
 * 使用MapStruct进行自动转换
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Mapper(componentModel = "spring")
public interface EntityDTOMapper {
    
    EntityDTOMapper INSTANCE = Mappers.getMapper(EntityDTOMapper.class);
    
    // ========== TestRequirement 转换 ==========
    
    /**
     * TestRequirement Entity -> TestRequirementResponseDTO
     */
    TestRequirementResponseDTO toRequirementResponseDTO(TestRequirement requirement);
    
    /**
     * TestRequirementRequestDTO -> TestRequirement Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requirementCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    TestRequirement toRequirementEntity(TestRequirementRequestDTO dto);
    
    /**
     * 更新TestRequirement Entity（从RequestDTO）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requirementCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateRequirementFromDTO(TestRequirementRequestDTO dto, @MappingTarget TestRequirement requirement);
    
    /**
     * TestRequirement List -> TestRequirementResponseDTO List
     */
    List<TestRequirementResponseDTO> toRequirementResponseDTOList(List<TestRequirement> requirements);
    
    // ========== TestCase 转换 ==========
    
    /**
     * TestCase Entity -> TestCaseResponseDTO
     */
    TestCaseResponseDTO toTestCaseResponseDTO(TestCase testCase);
    
    /**
     * TestCaseRequestDTO -> TestCase Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    TestCase toTestCaseEntity(TestCaseRequestDTO dto);
    
    /**
     * 更新TestCase Entity（从RequestDTO）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "caseCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateTestCaseFromDTO(TestCaseRequestDTO dto, @MappingTarget TestCase testCase);
    
    /**
     * TestCase List -> TestCaseResponseDTO List
     */
    List<TestCaseResponseDTO> toTestCaseResponseDTOList(List<TestCase> testCases);
    
    // ========== PromptTemplate 转换 ==========
    
    /**
     * PromptTemplate Entity -> PromptTemplateResponseDTO
     */
    PromptTemplateResponseDTO toPromptTemplateResponseDTO(PromptTemplate template);
    
    /**
     * PromptTemplateRequestDTO -> PromptTemplate Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    PromptTemplate toPromptTemplateEntity(PromptTemplateRequestDTO dto);
    
    /**
     * 更新PromptTemplate Entity（从RequestDTO）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updatePromptTemplateFromDTO(PromptTemplateRequestDTO dto, @MappingTarget PromptTemplate template);
    
    /**
     * PromptTemplate List -> PromptTemplateResponseDTO List
     */
    List<PromptTemplateResponseDTO> toPromptTemplateResponseDTOList(List<PromptTemplate> templates);
    
    // ========== ModelConfig 转换 ==========
    
    /**
     * ModelConfig Entity -> ModelConfigResponseDTO
     * 注意：不包含apiKey敏感信息
     */
    @Mapping(target = "apiKey", ignore = true)
    ModelConfigResponseDTO toModelConfigResponseDTO(ModelConfig modelConfig);
    
    /**
     * ModelConfigRequestDTO -> ModelConfig Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "modelCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    ModelConfig toModelConfigEntity(ModelConfigRequestDTO dto);
    
    /**
     * 更新ModelConfig Entity（从RequestDTO）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "modelCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateModelConfigFromDTO(ModelConfigRequestDTO dto, @MappingTarget ModelConfig modelConfig);
    
    /**
     * ModelConfig List -> ModelConfigResponseDTO List
     */
    List<ModelConfigResponseDTO> toModelConfigResponseDTOList(List<ModelConfig> modelConfigs);
    
    // ========== Common 转换 ==========
    
    /**
     * TestLayer Entity -> TestLayerResponseDTO
     */
    TestLayerResponseDTO toTestLayerResponseDTO(TestLayer testLayer);
    
    /**
     * TestLayer List -> TestLayerResponseDTO List
     */
    List<TestLayerResponseDTO> toTestLayerResponseDTOList(List<TestLayer> testLayers);
    
    /**
     * TestDesignMethod Entity -> TestDesignMethodResponseDTO
     */
    TestDesignMethodResponseDTO toTestDesignMethodResponseDTO(TestDesignMethod method);
    
    /**
     * TestDesignMethod List -> TestDesignMethodResponseDTO List
     */
    List<TestDesignMethodResponseDTO> toTestDesignMethodResponseDTOList(List<TestDesignMethod> methods);
}

