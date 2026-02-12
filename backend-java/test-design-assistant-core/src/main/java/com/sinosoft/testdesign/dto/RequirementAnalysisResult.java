package com.sinosoft.testdesign.dto;

import java.util.List;
import java.util.Map;

/**
 * 需求分析结果DTO
 *
 * @author sinosoft
 * @date 2024-01-01
 */
public class RequirementAnalysisResult {
    private Long requirementId;
    private String requirementName;
    private String requirementText;
    private List<String> keywords;
    private Integer contentLength;
    private Integer sentenceCount;
    private String analysisTime;
    private List<TestPointDTO> testPoints;
    private List<BusinessRuleDTO> businessRules;
    private Map<String, Object> keyInfo;

    public Long getRequirementId() { return requirementId; }
    public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }

    public String getRequirementName() { return requirementName; }
    public void setRequirementName(String requirementName) { this.requirementName = requirementName; }

    public String getRequirementText() { return requirementText; }
    public void setRequirementText(String requirementText) { this.requirementText = requirementText; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public Integer getContentLength() { return contentLength; }
    public void setContentLength(Integer contentLength) { this.contentLength = contentLength; }

    public Integer getSentenceCount() { return sentenceCount; }
    public void setSentenceCount(Integer sentenceCount) { this.sentenceCount = sentenceCount; }

    public String getAnalysisTime() { return analysisTime; }
    public void setAnalysisTime(String analysisTime) { this.analysisTime = analysisTime; }

    public List<TestPointDTO> getTestPoints() { return testPoints; }
    public void setTestPoints(List<TestPointDTO> testPoints) { this.testPoints = testPoints; }

    public List<BusinessRuleDTO> getBusinessRules() { return businessRules; }
    public void setBusinessRules(List<BusinessRuleDTO> businessRules) { this.businessRules = businessRules; }

    public Map<String, Object> getKeyInfo() { return keyInfo; }
    public void setKeyInfo(Map<String, Object> keyInfo) { this.keyInfo = keyInfo; }
}
