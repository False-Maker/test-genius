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
     * 注意：不包含apiKey敏感信息（DTO中没有该字段，MapStruct会自动忽略）
     */
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
    
    // ========== TestExecutionTask 转换 ==========
    
    /**
     * TestExecutionTask Entity -> TestExecutionTaskResponseDTO
     */
    TestExecutionTaskResponseDTO toTestExecutionTaskResponseDTO(TestExecutionTask task);
    
    /**
     * TestExecutionTaskRequestDTO -> TestExecutionTask Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taskCode", ignore = true)
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "progress", ignore = true)
    @Mapping(target = "successCount", ignore = true)
    @Mapping(target = "failCount", ignore = true)
    @Mapping(target = "resultData", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "finishTime", ignore = true)
    TestExecutionTask toTestExecutionTaskEntity(TestExecutionTaskRequestDTO dto);
    
    /**
     * TestExecutionTask List -> TestExecutionTaskResponseDTO List
     */
    List<TestExecutionTaskResponseDTO> toTestExecutionTaskResponseDTOList(List<TestExecutionTask> tasks);
    
    // ========== TestExecutionRecord 转换 ==========
    
    /**
     * TestExecutionRecord Entity -> TestExecutionRecordResponseDTO
     */
    TestExecutionRecordResponseDTO toTestExecutionRecordResponseDTO(TestExecutionRecord record);
    
    /**
     * TestExecutionRecordRequestDTO -> TestExecutionRecord Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recordCode", ignore = true)
    @Mapping(target = "executionStatus", ignore = true)
    @Mapping(target = "executionTime", ignore = true)
    @Mapping(target = "finishTime", ignore = true)
    TestExecutionRecord toTestExecutionRecordEntity(TestExecutionRecordRequestDTO dto);
    
    /**
     * TestExecutionRecord List -> TestExecutionRecordResponseDTO List
     */
    List<TestExecutionRecordResponseDTO> toTestExecutionRecordResponseDTOList(List<TestExecutionRecord> records);
    
    // ========== UIScriptTemplate 转换 ==========
    
    /**
     * UIScriptTemplate Entity -> UIScriptTemplateResponseDTO
     */
    UIScriptTemplateResponseDTO toUIScriptTemplateResponseDTO(UIScriptTemplate template);
    
    /**
     * UIScriptTemplateRequestDTO -> UIScriptTemplate Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateCode", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    UIScriptTemplate toUIScriptTemplateEntity(UIScriptTemplateRequestDTO dto);
    
    /**
     * UIScriptTemplate List -> UIScriptTemplateResponseDTO List
     */
    List<UIScriptTemplateResponseDTO> toUIScriptTemplateResponseDTOList(List<UIScriptTemplate> templates);
    
    // ========== PageElementInfo 转换 ==========
    
    /**
     * PageElementInfo Entity -> PageElementInfoResponseDTO
     */
    PageElementInfoResponseDTO toPageElementInfoResponseDTO(PageElementInfo element);
    
    /**
     * PageElementInfoRequestDTO -> PageElementInfo Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "elementCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    PageElementInfo toPageElementInfoEntity(PageElementInfoRequestDTO dto);
    
    /**
     * PageElementInfo List -> PageElementInfoResponseDTO List
     */
    List<PageElementInfoResponseDTO> toPageElementInfoResponseDTOList(List<PageElementInfo> elements);
    
    // ========== TestReportTemplate 转换 ==========
    
    /**
     * TestReportTemplate Entity -> TestReportTemplateResponseDTO
     */
    TestReportTemplateResponseDTO toTestReportTemplateResponseDTO(TestReportTemplate template);
    
    /**
     * TestReportTemplateRequestDTO -> TestReportTemplate Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    TestReportTemplate toTestReportTemplateEntity(TestReportTemplateRequestDTO dto);
    
    /**
     * 更新TestReportTemplate Entity（从RequestDTO）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateTestReportTemplateFromDTO(TestReportTemplateRequestDTO dto, @MappingTarget TestReportTemplate template);
    
    /**
     * TestReportTemplate List -> TestReportTemplateResponseDTO List
     */
    List<TestReportTemplateResponseDTO> toTestReportTemplateResponseDTOList(List<TestReportTemplate> templates);
    
    // ========== TestReport 转换 ==========
    
    /**
     * TestReport Entity -> TestReportResponseDTO
     */
    TestReportResponseDTO toTestReportResponseDTO(TestReport report);
    
    /**
     * TestReportRequestDTO -> TestReport Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reportCode", ignore = true)
    @Mapping(target = "reportContent", ignore = true)
    @Mapping(target = "reportSummary", ignore = true)
    @Mapping(target = "reportStatus", ignore = true)
    @Mapping(target = "fileUrl", ignore = true)
    @Mapping(target = "fileFormat", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "publishTime", ignore = true)
    TestReport toTestReportEntity(TestReportRequestDTO dto);
    
    /**
     * 更新TestReport Entity（从RequestDTO）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reportCode", ignore = true)
    @Mapping(target = "reportContent", ignore = true)
    @Mapping(target = "reportSummary", ignore = true)
    @Mapping(target = "reportStatus", ignore = true)
    @Mapping(target = "fileUrl", ignore = true)
    @Mapping(target = "fileFormat", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "publishTime", ignore = true)
    void updateTestReportFromDTO(TestReportRequestDTO dto, @MappingTarget TestReport report);
    
    /**
     * TestReport List -> TestReportResponseDTO List
     */
    List<TestReportResponseDTO> toTestReportResponseDTOList(List<TestReport> reports);
    
    // ========== TestCoverageAnalysis 转换 ==========
    
    /**
     * TestCoverageAnalysis Entity -> TestCoverageAnalysisResponseDTO
     */
    TestCoverageAnalysisResponseDTO toTestCoverageAnalysisResponseDTO(TestCoverageAnalysis analysis);
    
    /**
     * TestCoverageAnalysisRequestDTO -> TestCoverageAnalysis Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "analysisCode", ignore = true)
    @Mapping(target = "totalItems", ignore = true)
    @Mapping(target = "coveredItems", ignore = true)
    @Mapping(target = "coverageRate", ignore = true)
    @Mapping(target = "uncoveredItems", ignore = true)
    @Mapping(target = "coverageDetails", ignore = true)
    @Mapping(target = "analysisTime", ignore = true)
    TestCoverageAnalysis toTestCoverageAnalysisEntity(TestCoverageAnalysisRequestDTO dto);
    
    /**
     * TestCoverageAnalysis List -> TestCoverageAnalysisResponseDTO List
     */
    List<TestCoverageAnalysisResponseDTO> toTestCoverageAnalysisResponseDTOList(List<TestCoverageAnalysis> analyses);
    
    // ========== TestRiskAssessment 转换 ==========
    
    /**
     * TestRiskAssessment Entity -> TestRiskAssessmentResponseDTO
     */
    TestRiskAssessmentResponseDTO toTestRiskAssessmentResponseDTO(TestRiskAssessment assessment);
    
    /**
     * TestRiskAssessmentRequestDTO -> TestRiskAssessment Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assessmentCode", ignore = true)
    @Mapping(target = "riskLevel", ignore = true)
    @Mapping(target = "riskScore", ignore = true)
    @Mapping(target = "riskItems", ignore = true)
    @Mapping(target = "feasibilityScore", ignore = true)
    @Mapping(target = "feasibilityRecommendation", ignore = true)
    @Mapping(target = "assessmentDetails", ignore = true)
    @Mapping(target = "assessmentTime", ignore = true)
    TestRiskAssessment toTestRiskAssessmentEntity(TestRiskAssessmentRequestDTO dto);
    
    /**
     * TestRiskAssessment List -> TestRiskAssessmentResponseDTO List
     */
    List<TestRiskAssessmentResponseDTO> toTestRiskAssessmentResponseDTOList(List<TestRiskAssessment> assessments);
    
    // ========== TestSpecification 转换 ==========
    
    /**
     * TestSpecification Entity -> TestSpecificationResponseDTO
     */
    TestSpecificationResponseDTO toTestSpecificationResponseDTO(TestSpecification specification);
    
    /**
     * TestSpecificationRequestDTO -> TestSpecification Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "currentVersion", ignore = true)
    TestSpecification toTestSpecificationEntity(TestSpecificationRequestDTO dto);
    
    /**
     * 更新TestSpecification Entity（从RequestDTO）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "currentVersion", ignore = true)
    void updateTestSpecificationFromDTO(TestSpecificationRequestDTO dto, @MappingTarget TestSpecification specification);
    
    /**
     * TestSpecification List -> TestSpecificationResponseDTO List
     */
    List<TestSpecificationResponseDTO> toTestSpecificationResponseDTOList(List<TestSpecification> specifications);
    
    // ========== SpecVersion 转换 ==========
    
    /**
     * SpecVersion Entity -> SpecVersionResponseDTO
     */
    SpecVersionResponseDTO toSpecVersionResponseDTO(SpecVersion specVersion);
    
    /**
     * SpecVersion List -> SpecVersionResponseDTO List
     */
    List<SpecVersionResponseDTO> toSpecVersionResponseDTOList(List<SpecVersion> specVersions);
    
    // ========== FieldTestPoint 转换 ==========
    
    /**
     * FieldTestPoint Entity -> FieldTestPointResponseDTO
     */
    FieldTestPointResponseDTO toFieldTestPointResponseDTO(FieldTestPoint fieldTestPoint);
    
    /**
     * FieldTestPointRequestDTO -> FieldTestPoint Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pointCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    FieldTestPoint toFieldTestPointEntity(FieldTestPointRequestDTO dto);
    
    /**
     * 更新FieldTestPoint Entity（从RequestDTO）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pointCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateFieldTestPointFromDTO(FieldTestPointRequestDTO dto, @MappingTarget FieldTestPoint fieldTestPoint);
    
    /**
     * FieldTestPoint List -> FieldTestPointResponseDTO List
     */
    List<FieldTestPointResponseDTO> toFieldTestPointResponseDTOList(List<FieldTestPoint> fieldTestPoints);
    
    // ========== LogicTestPoint 转换 ==========
    
    /**
     * LogicTestPoint Entity -> LogicTestPointResponseDTO
     */
    LogicTestPointResponseDTO toLogicTestPointResponseDTO(LogicTestPoint logicTestPoint);
    
    /**
     * LogicTestPointRequestDTO -> LogicTestPoint Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pointCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    LogicTestPoint toLogicTestPointEntity(LogicTestPointRequestDTO dto);
    
    /**
     * 更新LogicTestPoint Entity（从RequestDTO）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pointCode", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateLogicTestPointFromDTO(LogicTestPointRequestDTO dto, @MappingTarget LogicTestPoint logicTestPoint);
    
    /**
     * LogicTestPoint List -> LogicTestPointResponseDTO List
     */
    List<LogicTestPointResponseDTO> toLogicTestPointResponseDTOList(List<LogicTestPoint> logicTestPoints);
}

