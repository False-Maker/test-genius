package com.sinosoft.testdesign.enums;

import lombok.Getter;

/**
 * 用例状态枚举
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Getter
public enum CaseStatus {
    
    /**
     * 草稿
     */
    DRAFT("草稿"),
    
    /**
     * 待审核
     */
    PENDING_REVIEW("待审核"),
    
    /**
     * 已审核
     */
    REVIEWED("已审核"),
    
    /**
     * 已废弃
     */
    OBSOLETE("已废弃");
    
    private final String description;
    
    CaseStatus(String description) {
        this.description = description;
    }
    
    /**
     * 判断状态是否可以流转到目标状态
     */
    public boolean canTransitionTo(CaseStatus targetStatus) {
        if (this == targetStatus) {
            return true; // 相同状态允许
        }
        
        switch (this) {
            case DRAFT:
                return targetStatus == PENDING_REVIEW || targetStatus == OBSOLETE;
            case PENDING_REVIEW:
                return targetStatus == REVIEWED || targetStatus == DRAFT || targetStatus == OBSOLETE;
            case REVIEWED:
                return targetStatus == OBSOLETE || targetStatus == DRAFT; // 已审核可以废弃或重新编辑
            case OBSOLETE:
                return targetStatus == DRAFT; // 已废弃可以重新编辑
            default:
                return false;
        }
    }
    
    /**
     * 根据字符串获取枚举
     */
    public static CaseStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return DRAFT;
        }
        for (CaseStatus s : values()) {
            if (s.name().equalsIgnoreCase(status) || s.description.equals(status)) {
                return s;
            }
        }
        return DRAFT;
    }
}

