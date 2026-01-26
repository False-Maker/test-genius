package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.dto.UIScriptGenerationRequest;
import com.sinosoft.testdesign.dto.UIScriptGenerationResult;
import com.sinosoft.testdesign.entity.PageElementInfo;
import com.sinosoft.testdesign.service.UIScriptGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UI脚本生成控制器
 * 
 * @author sinosoft
 * @date 2024-01-17
 */
@Tag(name = "UI脚本生成", description = "UI自动化脚本生成相关接口")
@RestController
@RequestMapping("/v1/ui-script")
@RequiredArgsConstructor
public class UIScriptGenerationController {
    
    private final UIScriptGenerationService uiScriptGenerationService;
    
    @Operation(summary = "生成UI脚本", description = "根据自然语言描述和页面代码信息，生成可执行的UI自动化脚本")
    @PostMapping("/generate")
    public Result<UIScriptGenerationResult> generateScript(@Valid @RequestBody UIScriptGenerationRequest request) {
        String taskCode = uiScriptGenerationService.generateScript(request);
        UIScriptGenerationResult result = new UIScriptGenerationResult();
        result.setTaskCode(taskCode);
        result.setTaskStatus("PENDING");
        result.setProgress(0);
        return Result.success(result);
    }
    
    @Operation(summary = "查询生成任务状态", description = "根据任务编码查询UI脚本生成任务状态")
    @GetMapping("/tasks/{taskCode}")
    public Result<UIScriptGenerationResult> getTaskStatus(@PathVariable String taskCode) {
        UIScriptGenerationResult result = uiScriptGenerationService.getTaskStatus(taskCode);
        return Result.success(result);
    }
    
    @Operation(summary = "解析页面代码", description = "解析页面HTML/CSS/JavaScript代码，提取页面元素信息")
    @PostMapping("/parse-page")
    public Result<List<PageElementInfo>> parsePageCode(@RequestParam String pageCodeUrl) {
        List<PageElementInfo> elements = uiScriptGenerationService.parsePageCode(pageCodeUrl);
        return Result.success(elements);
    }
}

