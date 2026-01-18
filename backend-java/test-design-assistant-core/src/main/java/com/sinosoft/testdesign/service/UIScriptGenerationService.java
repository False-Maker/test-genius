package com.sinosoft.testdesign.service;

import com.sinosoft.testdesign.dto.UIScriptGenerationRequest;
import com.sinosoft.testdesign.dto.UIScriptGenerationResult;
import com.sinosoft.testdesign.entity.PageElementInfo;

import java.util.List;

/**
 * UI脚本生成服务接口
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
public interface UIScriptGenerationService {
    
    /**
     * 生成UI脚本
     * @param request 脚本生成请求
     * @return 生成任务ID
     */
    String generateScript(UIScriptGenerationRequest request);
    
    /**
     * 查询生成任务状态
     * @param taskCode 任务编码
     * @return 任务状态信息
     */
    UIScriptGenerationResult getTaskStatus(String taskCode);
    
    /**
     * 解析页面代码
     * @param pageCodeUrl 页面代码URL或文件路径
     * @return 页面元素信息列表
     */
    List<PageElementInfo> parsePageCode(String pageCodeUrl);
}

