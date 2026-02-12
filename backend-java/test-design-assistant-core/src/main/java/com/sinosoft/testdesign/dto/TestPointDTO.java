package com.sinosoft.testdesign.dto;

/**
 * 测试要点DTO
 *
 * @author sinosoft
 * @date 2024-01-01
 */
public class TestPointDTO {
    private String name;
    private String point;
    private String description;
    private String priority;

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        // 同步 point 字段，保持前后端一致
        if (this.point == null) {
            this.point = name;
        }
    }

    public String getPoint() { return point; }
    public void setPoint(String point) {
        this.point = point;
        if (this.name == null) {
            this.name = point;
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
