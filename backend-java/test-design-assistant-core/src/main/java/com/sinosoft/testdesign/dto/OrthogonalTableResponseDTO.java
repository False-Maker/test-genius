package com.sinosoft.testdesign.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 正交表生成响应DTO
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
@Data
public class OrthogonalTableResponseDTO {
    
    /**
     * 表格标题
     */
    private String title;
    
    /**
     * 使用的正交表类型（L4/L8/L9/L12/L16/L25）
     */
    private String tableType;
    
    /**
     * 因素列表
     */
    private List<String> factors;
    
    /**
     * 因素取值映射
     * 格式：{"因素名": ["取值1", "取值2", ...]}
     */
    private Map<String, List<String>> factorValues;
    
    /**
     * 正交表数据（表格格式）
     * 每行代表一个测试组合，列包括：因素1、因素2、...、用例编号
     */
    private List<Map<String, Object>> tableData;
    
    /**
     * 测试组合数量（行数）
     */
    private Integer combinationCount;
    
    /**
     * 理论最少用例数（如果不使用正交表）
     */
    private Long theoreticalMaxCases;
    
    /**
     * 用例缩减比例（百分比）
     */
    private Double reductionRate;
    
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

