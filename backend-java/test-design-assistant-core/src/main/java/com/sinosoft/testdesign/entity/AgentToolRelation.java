package com.sinosoft.testdesign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent工具关联实体
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@Data
@Entity
@Table(name = "agent_tool_relation", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"agent_id", "tool_id"})
})
public class AgentToolRelation {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Agent ID
     */
    @Column(name = "agent_id", nullable = false)
    private Long agentId;
    
    /**
     * 工具ID
     */
    @Column(name = "tool_id", nullable = false)
    private Long toolId;
    
    /**
     * 是否启用
     */
    @Column(name = "is_enabled", length = 1)
    private String isEnabled = "1";
    
    /**
     * 工具排序
     */
    @Column(name = "tool_order")
    private Integer toolOrder = 0;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}

