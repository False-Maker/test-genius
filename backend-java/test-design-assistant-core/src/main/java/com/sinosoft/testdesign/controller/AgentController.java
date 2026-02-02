package com.sinosoft.testdesign.controller;

import com.sinosoft.testdesign.common.Result;
import com.sinosoft.testdesign.entity.Agent;
import com.sinosoft.testdesign.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent管理控制器
 *
 * @author sinosoft
 * @date 2026-01-27
 */
@RestController
@RequestMapping("/v1/agents")
@Tag(name = "Agent管理")
public class AgentController {

    @Autowired
    private AgentService agentService;

    /**
     * 创建Agent
     */
    @PostMapping
    @Operation(summary = "创建Agent")
    public Result<Agent> createAgent(@RequestBody Agent agent) {
        try {
            Agent createdAgent = agentService.createAgent(agent);
            return Result.success("创建成功", createdAgent);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新Agent
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新Agent")
    public Result<Agent> updateAgent(@PathVariable Long id, @RequestBody Agent agent) {
        try {
            agent.setId(id);
            Agent updatedAgent = agentService.updateAgent(agent);
            return Result.success("更新成功", updatedAgent);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询Agent
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询Agent")
    public Result<Agent> getAgentById(@PathVariable Long id) {
        return agentService.getAgentById(id)
                .map(agent -> Result.success(agent))
                .orElse(Result.error("Agent不存在"));
    }

    /**
     * 根据编码查询Agent
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码查询Agent")
    public Result<Agent> getAgentByCode(@PathVariable String code) {
        return agentService.getAgentByCode(code)
                .map(agent -> Result.success(agent))
                .orElse(Result.error("Agent不存在"));
    }

    /**
     * 查询所有启用的Agent
     */
    @GetMapping("/active")
    @Operation(summary = "查询所有启用的Agent")
    public Result<List<Agent>> getAllActiveAgents() {
        List<Agent> agents = agentService.getAllActiveAgents();
        return Result.success(agents);
    }

    /**
     * 查询指定类型的Agent
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "查询指定类型的Agent")
    public Result<List<Agent>> getAgentsByType(@PathVariable String type) {
        List<Agent> agents = agentService.getAgentsByType(type);
        return Result.success(agents);
    }

    /**
     * 删除Agent
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除Agent")
    public Result<Void> deleteAgent(@PathVariable Long id) {
        try {
            agentService.deleteAgent(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用Agent
     */
    @PutMapping("/{id}/active")
    @Operation(summary = "启用/禁用Agent")
    public Result<Agent> toggleAgentActive(@PathVariable Long id, @RequestParam boolean active) {
        try {
            Agent agent = agentService.toggleAgentActive(id, active);
            return Result.success(active ? "启用成功" : "禁用成功", agent);
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
}
