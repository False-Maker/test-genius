package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.service.AIServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * LLM公开控制器
 * 将前端对模型调用的公开入口统一收敛到Java层。
 */
@Slf4j
@RestController
@RequestMapping("/v1/llm")
@RequiredArgsConstructor
@Tag(name = "LLM调用")
public class LlmController {

    private final AIServiceClient aiServiceClient;

    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Operation(summary = "调用单个模型")
    @PostMapping("/call")
    public Result<Map<String, Object>> callModel(@RequestBody Map<String, Object> request) {
        return proxyPost("/api/v1/llm/call", request);
    }

    @Operation(summary = "并行调用多个模型")
    @PostMapping("/parallel-call")
    public Result<Map<String, Object>> parallelCall(@RequestBody Map<String, Object> request) {
        return proxyPost("/api/v1/llm/parallel-call", request);
    }

    private Result<Map<String, Object>> proxyPost(String path, Map<String, Object> request) {
        try {
            Map<String, Object> response = aiServiceClient.post(aiServiceUrl + path, request);
            if (response == null) {
                return Result.error("AI服务返回空响应");
            }

            if (isErrorResponse(response)) {
                return Result.error(extractMessage(response));
            }

            return Result.success(response);
        } catch (Exception e) {
            log.error("调用AI模型失败: path={}", path, e);
            return Result.error("调用AI模型失败: " + e.getMessage());
        }
    }

    private boolean isErrorResponse(Map<String, Object> response) {
        Object success = response.get("success");
        if (Boolean.FALSE.equals(success)) {
            return true;
        }

        Object status = response.get("status");
        if ("error".equals(status) || "failed".equals(status)) {
            return true;
        }

        Object code = response.get("code");
        if (code instanceof Number number) {
            int statusCode = number.intValue();
            return statusCode != 0 && statusCode != 200;
        }

        return false;
    }

    private String extractMessage(Map<String, Object> response) {
        Object message = response.get("message");
        if (message != null) {
            return message.toString();
        }

        Object error = response.get("error");
        if (error != null) {
            return error.toString();
        }

        return "AI服务调用失败";
    }
}
