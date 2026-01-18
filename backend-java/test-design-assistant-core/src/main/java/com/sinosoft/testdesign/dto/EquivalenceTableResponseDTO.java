package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 等价类表生成响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@Data
public class EquivalenceTableResponseDTO {
    
    /**
     * 表格标题
     */
    private String title;
    
    /**
     * 参数列表
     */
    private List<String> parameters;
    
    /**
     * 等价类数据
     * 格式：{"参数名": {"有效等价类": ["值1", "值2"], "无效等价类": ["值3"]}}
     */
    private Map<String, Map<String, List<String>>> equivalenceClasses;
    
    /**
     * 等价类表数据（表格格式）
     * 每行代表一个等价类组合，列包括：参数1、参数2、...、用例编号、用例名称、是否有效
     */
    private List<Map<String, Object>> tableData;
    
    /**
     * 生成的用例数量
     */
    private Integer testCaseCount;
    
    /**
     * 有效等价类用例数量
     */
    private Integer validCaseCount;
    
    /**
     * 无效等价类用例数量
     */
    private Integer invalidCaseCount;
    
    /**
     * Excel文件URL（如果格式为EXCEL）
     */
    private String excelFileUrl;
    
    /**
     * PDF文件URL（如果格式为PDF）
     */
    private String pdfFileUrl;
    
    /**
     * JSON数据（原始数据）
     */
    private String jsonData;
    
    /**
     * 导出格式
     */
    private String format;
}

