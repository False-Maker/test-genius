package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.dto.TestCaseExcelDTO;
import com.sinosoft.testdesign.entity.TestCase;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 测试用例导入导出服务接口
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface TestCaseImportExportService {
    
    /**
     * 导出用例到Excel
     * @param testCases 用例列表
     * @param outputStream 输出流
     * @throws IOException IO异常
     */
    void exportToExcel(List<TestCase> testCases, OutputStream outputStream) throws IOException;
    
    /**
     * 导出用例模板到Excel
     * @param outputStream 输出流
     * @throws IOException IO异常
     */
    void exportTemplate(OutputStream outputStream) throws IOException;
    
    /**
     * 从Excel导入用例
     * @param file Excel文件
     * @return 导入结果（成功数量、失败数量、错误信息列表）
     * @throws IOException IO异常
     */
    ImportResult importFromExcel(MultipartFile file) throws IOException;
    
    /**
     * 导入结果
     */
    class ImportResult {
        private int successCount;
        private int failureCount;
        private List<String> errorMessages;
        private List<TestCase> importedCases;
        
        public ImportResult(int successCount, int failureCount, List<String> errorMessages, List<TestCase> importedCases) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.errorMessages = errorMessages;
            this.importedCases = importedCases;
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public int getFailureCount() {
            return failureCount;
        }
        
        public List<String> getErrorMessages() {
            return errorMessages;
        }
        
        public List<TestCase> getImportedCases() {
            return importedCases;
        }
    }
}

