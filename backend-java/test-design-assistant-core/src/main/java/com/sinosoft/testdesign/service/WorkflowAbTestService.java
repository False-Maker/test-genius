package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.dto.WorkflowAbTestRequestDTO;
import com.sinosoft.testdesign.dto.WorkflowAbTestResponseDTO;
import com.sinosoft.testdesign.entity.WorkflowAbTest;
import com.sinosoft.testdesign.entity.WorkflowAbTestExecution;

import java.util.List;
import java.util.Map;

/**
 * 工作流 A/B 测试服务接口（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
public interface WorkflowAbTestService {

    WorkflowAbTestResponseDTO createAbTest(Long workflowId, WorkflowAbTestRequestDTO dto);

    WorkflowAbTestResponseDTO getAbTestById(Long id);

    List<WorkflowAbTestResponseDTO> getAbTestsByWorkflowId(Long workflowId);

    WorkflowAbTestResponseDTO startAbTest(Long id);

    WorkflowAbTestResponseDTO pauseAbTest(Long id);

    WorkflowAbTestResponseDTO stopAbTest(Long id);

    void deleteAbTest(Long id);

    Map<String, Object> getAbTestStatistics(Long abTestId);

    /**
     * 选择版本（根据流量分配），返回 "A" 或 "B"
     */
    String selectVersion(Long abTestId, String requestId);

    WorkflowAbTestExecution recordExecution(WorkflowAbTestExecution execution);
}
