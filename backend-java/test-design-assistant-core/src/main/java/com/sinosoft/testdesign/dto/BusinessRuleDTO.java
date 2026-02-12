package com.sinosoft.testdesign.dto;

/**
 * 业务规则DTO
 *
 * @author sinosoft
 * @date 2024-01-01
 */
public class BusinessRuleDTO {
    private String name;
    private String rule;
    private String description;
    private String type;

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        // 同步 rule 字段，保持前后端一致
        if (this.rule == null) {
            this.rule = name;
        }
    }

    public String getRule() { return rule; }
    public void setRule(String rule) {
        this.rule = rule;
        if (this.name == null) {
            this.name = rule;
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
