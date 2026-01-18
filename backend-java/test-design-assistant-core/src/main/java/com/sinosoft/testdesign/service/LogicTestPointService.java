package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.LogicTestPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 逻辑测试要点管理服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface LogicTestPointService {
    
    /**
     * 创建逻辑测试要点
     */
    LogicTestPoint createLogicTestPoint(LogicTestPoint logicTestPoint);
    
    /**
     * 更新逻辑测试要点
     */
    LogicTestPoint updateLogicTestPoint(Long id, LogicTestPoint logicTestPoint);
    
    /**
     * 根据ID查询逻辑测试要点
     */
    LogicTestPoint getLogicTestPointById(Long id);
    
    /**
     * 根据编码查询逻辑测试要点
     */
    LogicTestPoint getLogicTestPointByCode(String pointCode);
    
    /**
     * 分页查询逻辑测试要点列表
     * @param pageable 分页参数
     * @param logicName 逻辑名称（模糊搜索，可选）
     * @param specId 规约ID（精确匹配，可选）
     * @param isActive 是否启用（精确匹配，可选）
     */
    Page<LogicTestPoint> getLogicTestPointList(Pageable pageable, String logicName, Long specId, String isActive);
    
    /**
     * 根据规约ID查询逻辑测试要点列表
     */
    List<LogicTestPoint> getLogicTestPointsBySpecId(Long specId);
    
    /**
     * 根据逻辑类型查询逻辑测试要点列表
     */
    List<LogicTestPoint> getLogicTestPointsByType(String logicType);
    
    /**
     * 删除逻辑测试要点
     */
    void deleteLogicTestPoint(Long id);
    
    /**
     * 启用/禁用逻辑测试要点
     */
    LogicTestPoint updateLogicTestPointStatus(Long id, String isActive);
}

