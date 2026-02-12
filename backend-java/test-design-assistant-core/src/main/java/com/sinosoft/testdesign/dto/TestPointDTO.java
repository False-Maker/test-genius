package com.sinosoft.testdesign.dto;

/**
 * 测试要点DTO
 *
 * @author sinosoft
 * @date 2024-01-01
 */
public class TestPointDTO {
    private String name;
    private String description;
    private String priority;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
