package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.AgentSession;
import com.sinosoft.testdesign.service.AgentSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent会话管理控制器
 * 
 * @author sinosoft
 * @date 2026-01-27
 */
@RestController
@RequestMapping("/v1/agent-sessions")
@Tag(name = "Agent会话管理")
public class AgentSessionController {
    
    @Autowired
    private AgentSessionService sessionService;
    
    /**
     * 创建会话
     */
    @PostMapping
    @Operation(summary = "创建Agent会话")
    public Result<AgentSession> createSession(@RequestBody AgentSession session) {
        try {
            AgentSession createdSession = sessionService.createSession(session);
            return Result.success(createdSession, "创建成功");
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询会话
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询会话")
    public Result<AgentSession> getSessionById(@PathVariable Long id) {
        return sessionService.getSessionById(id)
                .map(session -> Result.success(session))
                .orElse(Result.error("会话不存在"));
    }
    
    /**
     * 根据编码查询会话
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码查询会话")
    public Result<AgentSession> getSessionByCode(@PathVariable String code) {
        return sessionService.getSessionByCode(code)
                .map(session -> Result.success(session))
                .orElse(Result.error("会话不存在"));
    }
    
    /**
     * 查询Agent的所有会话
     */
    @GetMapping("/agent/{agentId}")
    @Operation(summary = "查询Agent的所有会话")
    public Result<List<AgentSession>> getSessionsByAgentId(@PathVariable Long agentId) {
        List<AgentSession> sessions = sessionService.getSessionsByAgentId(agentId);
        return Result.success(sessions);
    }
    
    /**
     * 查询用户的所有会话
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户的所有会话")
    public Result<List<AgentSession>> getSessionsByUserId(@PathVariable Long userId) {
        List<AgentSession> sessions = sessionService.getSessionsByUserId(userId);
        return Result.success(sessions);
    }
    
    /**
     * 查询Agent和用户的所有会话
     */
    @GetMapping("/agent/{agentId}/user/{userId}")
    @Operation(summary = "查询Agent和用户的所有会话")
    public Result<List<AgentSession>> getSessionsByAgentIdAndUserId(
            @PathVariable Long agentId, 
            @PathVariable Long userId) {
        List<AgentSession> sessions = sessionService.getSessionsByAgentIdAndUserId(agentId, userId);
        return Result.success(sessions);
    }
    
    /**
     * 关闭会话
     */
    @PutMapping("/{id}/close")
    @Operation(summary = "关闭会话")
    public Result<AgentSession> closeSession(@PathVariable Long id) {
        try {
            AgentSession session = sessionService.closeSession(id);
            return Result.success(session, "关闭成功");
        } catch (Exception e) {
            return Result.error("关闭失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除会话")
    public Result<Void> deleteSession(@PathVariable Long id) {
        try {
            sessionService.deleteSession(id);
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}

