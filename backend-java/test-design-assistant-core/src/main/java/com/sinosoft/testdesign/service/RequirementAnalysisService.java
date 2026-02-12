package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.dto.BusinessRuleDTO;
import com.sinosoft.testdesign.dto.RequirementAnalysisResult;
import com.sinosoft.testdesign.dto.TestPointDTO;

import java.util.List;

/**
 * 需求分析服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface RequirementAnalysisService {
    
    /**
     * 分析需求文档
     * @param requirementId 需求ID
     * @return 需求分析结果
     */
    RequirementAnalysisResult analyzeRequirement(Long requirementId);
    
    /**
     * 提取测试要点
     * @param requirementId 需求ID
     * @return 测试要点列表
     */
    List<TestPointDTO> extractTestPoints(Long requirementId);
    
    /**
     * 提取业务规则
     * @param requirementId 需求ID
     * @return 业务规则列表
     */
    List<BusinessRuleDTO> extractBusinessRules(Long requirementId);
}
