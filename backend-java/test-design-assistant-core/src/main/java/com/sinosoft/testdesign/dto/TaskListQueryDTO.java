package com.sinosoft.testdesign.dto;

import lombok.Data;

/**
 * 用例生成任务列表查询DTO
 *
 * @author sinosoft
 * @date 2025-01-30
 */
@Data
public class TaskListQueryDTO {

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 20;

    /**
     * 任务状态筛选（可选）
     */
    private String taskStatus;

    /**
     * 需求ID筛选（可选）
     */
    private Integer requirementId;

    /**
     * 任务编码模糊搜索（可选）
     */
    private String taskCode;
}
