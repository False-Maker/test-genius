package com.sinosoft.testdesign.service;

/**
 * 模型调用服务接口
 * 实际调用Python服务，具体实现可后续开发
 * 
 * @author sinosoft
 * @date 2024-01-01
 */
public interface ModelCallService {
    
    /**
     * 调用模型生成内容
     * @param request 模型调用请求
     * @return 模型响应结果
     */
    // ModelResponse callModel(ModelRequest request);
    
    /**
     * 多模型对比生成
     * @param request 模型调用请求
     * @param modelCodes 模型代码列表
     * @return 多个模型的响应结果
     */
    // Map<String, ModelResponse> compareModels(ModelRequest request, List<String> modelCodes);
    
    /**
     * 获取模型调用统计
     * @param modelCode 模型代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    // ModelCallStatistics getModelStatistics(String modelCode, Date startDate, Date endDate);
}

