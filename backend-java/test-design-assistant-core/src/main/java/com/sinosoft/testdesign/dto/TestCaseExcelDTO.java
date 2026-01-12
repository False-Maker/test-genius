package com.sinosoft.testdesign.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 测试用例Excel导入导出DTO
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
@Data
@ColumnWidth(20)
public class TestCaseExcelDTO {
    
    @ExcelProperty(value = "用例编码", index = 0)
    @ColumnWidth(25)
    private String caseCode;
    
    @ExcelProperty(value = "用例名称", index = 1)
    @ColumnWidth(40)
    private String caseName;
    
    @ExcelProperty(value = "需求编码", index = 2)
    @ColumnWidth(25)
    private String requirementCode;
    
    @ExcelProperty(value = "测试分层", index = 3)
    @ColumnWidth(20)
    private String layerName;
    
    @ExcelProperty(value = "测试方法", index = 4)
    @ColumnWidth(20)
    private String methodName;
    
    @ExcelProperty(value = "用例类型", index = 5)
    @ColumnWidth(15)
    private String caseType;
    
    @ExcelProperty(value = "用例优先级", index = 6)
    @ColumnWidth(15)
    private String casePriority;
    
    @ExcelProperty(value = "前置条件", index = 7)
    @ColumnWidth(50)
    private String preCondition;
    
    @ExcelProperty(value = "测试步骤", index = 8)
    @ColumnWidth(50)
    private String testStep;
    
    @ExcelProperty(value = "预期结果", index = 9)
    @ColumnWidth(50)
    private String expectedResult;
    
    @ExcelProperty(value = "用例状态", index = 10)
    @ColumnWidth(15)
    private String caseStatus;
    
    @ExcelProperty(value = "版本号", index = 11)
    @ColumnWidth(10)
    private Integer version;
}

