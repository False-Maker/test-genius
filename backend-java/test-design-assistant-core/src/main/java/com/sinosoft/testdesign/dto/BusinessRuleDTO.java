package com.sinosoft.testdesign.dto;

/**
 * 业务规则DTO
 *
 * @author sinosoft
 * @date 2024-01-01
 */
public class BusinessRuleDTO {
    private String name;
    private String description;
    private String type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
