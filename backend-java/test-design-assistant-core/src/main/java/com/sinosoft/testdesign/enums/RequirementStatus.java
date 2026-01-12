package com.sinosoft.testdesign.enums;

import lombok.Getter;

/**
 * 需求状态枚举
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Getter
public enum RequirementStatus {
    
    /**
     * 草稿
     */
    DRAFT("草稿"),
    
    /**
     * 审核中
     */
    REVIEWING("审核中"),
    
    /**
     * 已通过
     */
    APPROVED("已通过"),
    
    /**
     * 已关闭
     */
    CLOSED("已关闭");
    
    private final String description;
    
    RequirementStatus(String description) {
        this.description = description;
    }
    
    /**
     * 判断状态是否可以流转到目标状态
     */
    public boolean canTransitionTo(RequirementStatus targetStatus) {
        if (this == targetStatus) {
            return true; // 相同状态允许
        }
        
        switch (this) {
            case DRAFT:
                return targetStatus == REVIEWING || targetStatus == CLOSED;
            case REVIEWING:
                return targetStatus == APPROVED || targetStatus == CLOSED || targetStatus == DRAFT;
            case APPROVED:
                return targetStatus == CLOSED;
            case CLOSED:
                return false; // 已关闭状态不能流转
            default:
                return false;
        }
    }
    
    /**
     * 根据字符串获取枚举
     */
    public static RequirementStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return DRAFT;
        }
        for (RequirementStatus s : values()) {
            if (s.name().equalsIgnoreCase(status) || s.description.equals(status)) {
                return s;
            }
        }
        return DRAFT;
    }
}

