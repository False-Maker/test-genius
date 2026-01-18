package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.dto.EquivalenceTableRequestDTO;
import com.sinosoft.testdesign.dto.EquivalenceTableResponseDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableRequestDTO;
import com.sinosoft.testdesign.dto.OrthogonalTableResponseDTO;

import java.io.OutputStream;

/**
 * 数据文档生成服务接口
 * 提供等价类表和正交表生成功能
 * 
 * @author sinosoft
 * @date 2024-01-XX
 */
public interface DataDocumentGenerationService {
    
    /**
     * 生成等价类表
     * @param request 等价类表生成请求
     * @return 等价类表生成响应
     */
    EquivalenceTableResponseDTO generateEquivalenceTable(EquivalenceTableRequestDTO request);
    
    /**
     * 生成正交表
     * @param request 正交表生成请求
     * @return 正交表生成响应
     */
    OrthogonalTableResponseDTO generateOrthogonalTable(OrthogonalTableRequestDTO request);
    
    /**
     * 导出等价类表到Excel
     * @param response 等价类表响应数据
     * @param outputStream 输出流
     */
    void exportEquivalenceTableToExcel(EquivalenceTableResponseDTO response, OutputStream outputStream);
    
    /**
     * 导出等价类表到Word
     * @param response 等价类表响应数据
     * @param outputStream 输出流
     */
    void exportEquivalenceTableToWord(EquivalenceTableResponseDTO response, OutputStream outputStream);
    
    /**
     * 导出正交表到Excel
     * @param response 正交表响应数据
     * @param outputStream 输出流
     */
    void exportOrthogonalTableToExcel(OrthogonalTableResponseDTO response, OutputStream outputStream);
    
    /**
     * 导出正交表到Word
     * @param response 正交表响应数据
     * @param outputStream 输出流
     */
    void exportOrthogonalTableToWord(OrthogonalTableResponseDTO response, OutputStream outputStream);
}
