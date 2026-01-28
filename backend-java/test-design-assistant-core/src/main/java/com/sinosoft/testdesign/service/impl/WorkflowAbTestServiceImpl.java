package com.sinosoft.testdesign.service.impl;

import com.sinosoft.testdesign.common.BusinessException;
import com.sinosoft.testdesign.dto.WorkflowAbTestRequestDTO;
import com.sinosoft.testdesign.dto.WorkflowAbTestResponseDTO;
import com.sinosoft.testdesign.entity.WorkflowAbTest;
import com.sinosoft.testdesign.entity.WorkflowAbTestExecution;
import com.sinosoft.testdesign.entity.WorkflowVersion;
import com.sinosoft.testdesign.repository.WorkflowAbTestExecutionRepository;
import com.sinosoft.testdesign.repository.WorkflowAbTestRepository;
import com.sinosoft.testdesign.repository.WorkflowDefinitionRepository;
import com.sinosoft.testdesign.repository.WorkflowVersionRepository;
import com.sinosoft.testdesign.service.WorkflowAbTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 工作流 A/B 测试服务实现（第四阶段 4.3）
 *
 * @author test-design-assistant
 * @since 2026-01-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowAbTestServiceImpl implements WorkflowAbTestService {

    private final WorkflowAbTestRepository abTestRepository;
    private final WorkflowAbTestExecutionRepository executionRepository;
    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowVersionRepository versionRepository;
    private final Random random = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowAbTestResponseDTO createAbTest(Long workflowId, WorkflowAbTestRequestDTO dto) {
        if (!workflowRepository.existsById(workflowId)) {
            throw new BusinessException("工作流不存在: " + workflowId);
        }
        WorkflowVersion va = versionRepository.findById(dto.getVersionAId())
                .orElseThrow(() -> new BusinessException("版本A不存在: " + dto.getVersionAId()));
        WorkflowVersion vb = versionRepository.findById(dto.getVersionBId())
                .orElseThrow(() -> new BusinessException("版本B不存在: " + dto.getVersionBId()));
        if (!va.getWorkflowId().equals(workflowId) || !vb.getWorkflowId().equals(workflowId)) {
            throw new BusinessException("版本不属于该工作流");
        }
        int sa = dto.getTrafficSplitA() != null ? dto.getTrafficSplitA() : 50;
        int sb = dto.getTrafficSplitB() != null ? dto.getTrafficSplitB() : 50;
        if (sa + sb != 100) {
            throw new BusinessException("流量分配比例之和必须等于100");
        }
        Optional<WorkflowAbTest> running = abTestRepository.findRunningTestByWorkflowId(workflowId);
        if (running.isPresent()) {
            throw new BusinessException("该工作流已有正在运行的 A/B 测试，请先停止");
        }
        WorkflowAbTest ab = WorkflowAbTest.builder()
                .workflowId(workflowId)
                .testName(dto.getTestName())
                .testDescription(dto.getTestDescription())
                .versionAId(dto.getVersionAId())
                .versionBId(dto.getVersionBId())
                .trafficSplitA(sa)
                .trafficSplitB(sb)
                .status("draft")
                .createdBy(dto.getCreatedBy())
                .build();
        WorkflowAbTest saved = abTestRepository.save(ab);
        log.info("创建工作流 A/B 测试成功: id={}, workflowId={}", saved.getId(), workflowId);
        return toResponse(saved);
    }

    @Override
    public WorkflowAbTestResponseDTO getAbTestById(Long id) {
        WorkflowAbTest ab = abTestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("A/B 测试不存在: " + id));
        return toResponse(ab);
    }

    @Override
    public List<WorkflowAbTestResponseDTO> getAbTestsByWorkflowId(Long workflowId) {
        return abTestRepository.findByWorkflowIdOrderByCreateTimeDesc(workflowId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowAbTestResponseDTO startAbTest(Long id) {
        WorkflowAbTest ab = abTestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("A/B 测试不存在: " + id));
        if ("running".equals(ab.getStatus())) {
            throw new BusinessException("A/B 测试已在运行中");
        }
        Optional<WorkflowAbTest> running = abTestRepository.findRunningTestByWorkflowId(ab.getWorkflowId());
        if (running.isPresent() && !running.get().getId().equals(id)) {
            throw new BusinessException("该工作流已有正在运行的 A/B 测试，请先停止");
        }
        ab.setStatus("running");
        ab.setStartTime(LocalDateTime.now());
        ab.setUpdateTime(LocalDateTime.now());
        WorkflowAbTest saved = abTestRepository.save(ab);
        log.info("启动工作流 A/B 测试成功: id={}", id);
        return toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowAbTestResponseDTO pauseAbTest(Long id) {
        WorkflowAbTest ab = abTestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("A/B 测试不存在: " + id));
        ab.setStatus("paused");
        ab.setUpdateTime(LocalDateTime.now());
        return toResponse(abTestRepository.save(ab));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowAbTestResponseDTO stopAbTest(Long id) {
        WorkflowAbTest ab = abTestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("A/B 测试不存在: " + id));
        ab.setStatus("completed");
        ab.setEndTime(LocalDateTime.now());
        ab.setUpdateTime(LocalDateTime.now());
        WorkflowAbTest saved = abTestRepository.save(ab);
        log.info("停止工作流 A/B 测试成功: id={}", id);
        return toResponse(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAbTest(Long id) {
        WorkflowAbTest ab = abTestRepository.findById(id)
                .orElseThrow(() -> new BusinessException("A/B 测试不存在: " + id));
        if ("running".equals(ab.getStatus())) {
            throw new BusinessException("不能删除正在运行的 A/B 测试，请先停止");
        }
        abTestRepository.deleteById(id);
        log.info("删除工作流 A/B 测试成功: id={}", id);
    }

    @Override
    public Map<String, Object> getAbTestStatistics(Long abTestId) {
        WorkflowAbTest ab = abTestRepository.findById(abTestId)
                .orElseThrow(() -> new BusinessException("A/B 测试不存在: " + abTestId));
        long countA = executionRepository.countVersionA(abTestId);
        long countB = executionRepository.countVersionB(abTestId);
        long successA = executionRepository.countVersionASuccess(abTestId);
        long successB = executionRepository.countVersionBSuccess(abTestId);
        Double avgRespA = executionRepository.avgResponseTimeA(abTestId);
        Double avgRespB = executionRepository.avgResponseTimeB(abTestId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("abTestId", abTestId);
        stats.put("workflowId", ab.getWorkflowId());
        stats.put("status", ab.getStatus());
        stats.put("versionACount", countA);
        stats.put("versionBCount", countB);
        stats.put("versionASuccessCount", successA);
        stats.put("versionBSuccessCount", successB);
        stats.put("versionASuccessRate", countA > 0 ? (double) successA / countA : 0.0);
        stats.put("versionBSuccessRate", countB > 0 ? (double) successB / countB : 0.0);
        stats.put("versionAAvgResponseTimeMs", avgRespA != null ? avgRespA : 0.0);
        stats.put("versionBAvgResponseTimeMs", avgRespB != null ? avgRespB : 0.0);
        return stats;
    }

    @Override
    public String selectVersion(Long abTestId, String requestId) {
        WorkflowAbTest ab = abTestRepository.findById(abTestId)
                .orElseThrow(() -> new BusinessException("A/B 测试不存在: " + abTestId));
        if (!"running".equals(ab.getStatus())) {
            return "A";
        }
        int r = random.nextInt(100);
        return r < ab.getTrafficSplitA() ? "A" : "B";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowAbTestExecution recordExecution(WorkflowAbTestExecution execution) {
        if (!abTestRepository.existsById(execution.getAbTestId())) {
            throw new BusinessException("A/B 测试不存在: " + execution.getAbTestId());
        }
        return executionRepository.save(execution);
    }

    private WorkflowAbTestResponseDTO toResponse(WorkflowAbTest ab) {
        WorkflowAbTestResponseDTO dto = new WorkflowAbTestResponseDTO();
        dto.setId(ab.getId());
        dto.setWorkflowId(ab.getWorkflowId());
        dto.setTestName(ab.getTestName());
        dto.setTestDescription(ab.getTestDescription());
        dto.setVersionAId(ab.getVersionAId());
        dto.setVersionBId(ab.getVersionBId());
        dto.setTrafficSplitA(ab.getTrafficSplitA());
        dto.setTrafficSplitB(ab.getTrafficSplitB());
        dto.setStartTime(ab.getStartTime());
        dto.setEndTime(ab.getEndTime());
        dto.setStatus(ab.getStatus());
        dto.setCreatedBy(ab.getCreatedBy());
        dto.setCreateTime(ab.getCreateTime());
        dto.setUpdateTime(ab.getUpdateTime());
        return dto;
    }
}
