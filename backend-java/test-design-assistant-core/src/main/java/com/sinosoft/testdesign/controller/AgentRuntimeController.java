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
 * Agent运行时公开控制器
 * 统一承接前端对Agent运行时的访问，避免前端直接依赖Python路径。
 */
@Slf4j
@RestController
@RequestMapping("/v1/agent")
@RequiredArgsConstructor
@Tag(name = "Agent运行时")
public class AgentRuntimeController {

    private final AIServiceClient aiServiceClient;

    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Operation(summary = "Agent对话")
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> response = aiServiceClient.post(aiServiceUrl + "/api/v1/agent/chat", request);
            if (response == null) {
                return Result.error("Agent服务返回空响应");
            }

            if (isErrorResponse(response)) {
                return Result.error(extractMessage(response));
            }

            return Result.success(response);
        } catch (Exception e) {
            log.error("Agent对话失败", e);
            return Result.error("Agent对话失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取Agent会话历史")
    @GetMapping("/sessions/{sessionId}/history")
    public Result<Object> getSessionHistory(@PathVariable Long sessionId,
                                            @RequestParam(required = false) Integer limit) {
        try {
            StringBuilder url = new StringBuilder(aiServiceUrl)
                    .append("/api/v1/agent/sessions/")
                    .append(sessionId)
                    .append("/history");

            if (limit != null) {
                url.append("?limit=").append(limit);
            }

            Map<String, Object> response = aiServiceClient.get(url.toString());
            if (response == null) {
                return Result.error("Agent服务返回空响应");
            }

            if (isErrorResponse(response)) {
                return Result.error(extractMessage(response));
            }

            Object data = response.containsKey("data") ? response.get("data") : response;
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取Agent会话历史失败: sessionId={}", sessionId, e);
            return Result.error("获取Agent会话历史失败: " + e.getMessage());
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

        return "Agent服务调用失败";
    }
}
