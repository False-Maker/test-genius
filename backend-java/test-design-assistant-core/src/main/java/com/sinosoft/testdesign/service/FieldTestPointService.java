package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.entity.FieldTestPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 字段测试要点管理服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface FieldTestPointService {
    
    /**
     * 创建字段测试要点
     */
    FieldTestPoint createFieldTestPoint(FieldTestPoint fieldTestPoint);
    
    /**
     * 更新字段测试要点
     */
    FieldTestPoint updateFieldTestPoint(Long id, FieldTestPoint fieldTestPoint);
    
    /**
     * 根据ID查询字段测试要点
     */
    FieldTestPoint getFieldTestPointById(Long id);
    
    /**
     * 根据编码查询字段测试要点
     */
    FieldTestPoint getFieldTestPointByCode(String pointCode);
    
    /**
     * 分页查询字段测试要点列表
     * @param pageable 分页参数
     * @param fieldName 字段名称（模糊搜索，可选）
     * @param specId 规约ID（精确匹配，可选）
     * @param isActive 是否启用（精确匹配，可选）
     */
    Page<FieldTestPoint> getFieldTestPointList(Pageable pageable, String fieldName, Long specId, String isActive);
    
    /**
     * 根据规约ID查询字段测试要点列表
     */
    List<FieldTestPoint> getFieldTestPointsBySpecId(Long specId);
    
    /**
     * 删除字段测试要点
     */
    void deleteFieldTestPoint(Long id);
    
    /**
     * 启用/禁用字段测试要点
     */
    FieldTestPoint updateFieldTestPointStatus(Long id, String isActive);
}

