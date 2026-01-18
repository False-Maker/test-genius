package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.BaseControllerTest;
import com.sinosoft.testdesign.dto.UIScriptGenerationRequest;
import com.sinosoft.testdesign.dto.UIScriptGenerationResult;
import com.sinosoft.testdesign.entity.PageElementInfo;
import com.sinosoft.testdesign.service.UIScriptGenerationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UI脚本生成Controller集成测试
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@DisplayName("UI脚本生成Controller测试")
class UIScriptGenerationControllerTest extends BaseControllerTest {
    
    @MockBean
    private UIScriptGenerationService uiScriptGenerationService;
    
    @Test
    @DisplayName("生成UI脚本-成功")
    void testGenerateScript_Success() throws Exception {
        // Given
        UIScriptGenerationRequest request = new UIScriptGenerationRequest();
        request.setNaturalLanguageDesc("点击登录按钮");
        request.setPageCodeUrl("http://example.com/page");
        
        String taskCode = "TASK-20240117-001";
        when(uiScriptGenerationService.generateScript(any(UIScriptGenerationRequest.class)))
            .thenReturn(taskCode);
        
        // When & Then
        mockMvc.perform(post("/api/v1/ui-script/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskCode").value("TASK-20240117-001"));
    }
    
    @Test
    @DisplayName("查询生成任务状态-成功")
    void testGetTaskStatus_Success() throws Exception {
        // Given
        String taskCode = "TASK-20240117-001";
        UIScriptGenerationResult result = new UIScriptGenerationResult();
        result.setTaskCode(taskCode);
        result.setTaskStatus("SUCCESS");
        result.setProgress(100);
        
        when(uiScriptGenerationService.getTaskStatus(taskCode))
            .thenReturn(result);
        
        // When & Then
        mockMvc.perform(get("/api/v1/ui-script/tasks/{taskCode}", taskCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskCode").value(taskCode))
                .andExpect(jsonPath("$.data.taskStatus").value("SUCCESS"));
    }
    
    @Test
    @DisplayName("解析页面代码-成功")
    void testParsePageCode_Success() throws Exception {
        // Given
        String pageCodeUrl = "http://example.com/page";
        PageElementInfo element = new PageElementInfo();
        element.setId(1L);
        element.setElementCode("ELE-20240117-001");
        element.setElementText("登录按钮");
        
        List<PageElementInfo> elements = new ArrayList<>();
        elements.add(element);
        
        when(uiScriptGenerationService.parsePageCode(pageCodeUrl))
            .thenReturn(elements);
        
        // When & Then
        mockMvc.perform(post("/api/v1/ui-script/parse-page")
                .param("pageCodeUrl", pageCodeUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }
}

