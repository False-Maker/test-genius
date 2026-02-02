package com.sinosoft.testdesign.dto;

import com.sinosoft.testdesign.entity.TestCase;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用例生成任务详情DTO
 *
 * @author sinosoft
 * @date 2025-01-30
 */
@Data
public class TaskDetailDTO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 需求ID
     */
    private Long requirementId;

    /**
     * 需求名称
     */
    private String requirementName;

    /**
     * 需求编码
     */
    private String requirementCode;

    /**
     * 需求描述
     */
    private String requirementDescription;

    /**
     * 测试分层ID
     */
    private Long layerId;

    /**
     * 测试分层名称
     */
    private String layerName;

    /**
     * 测试分层编码
     */
    private String layerCode;

    /**
     * 测试方法ID
     */
    private Long methodId;

    /**
     * 测试方法名称
     */
    private String methodName;

    /**
     * 测试方法编码
     */
    private String methodCode;

    /**
     * 提示词模板ID
     */
    private Long templateId;

    /**
     * 模型编码
     */
    private String modelCode;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 进度（0-100）
     */
    private Integer progress;

    /**
     * 总用例数
     */
    private Integer totalCases;

    /**
     * 成功用例数
     */
    private Integer successCases;

    /**
     * 失败用例数
     */
    private Integer failCases;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 生成的用例列表
     */
    private List<TestCase> cases;
}
