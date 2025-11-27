package com.agentscanner.gateway.controller;

import com.agentscanner.gateway.service.AgentService;
import com.agentscanner.gateway.dto.AgentDTO;
import com.agentscanner.gateway.vo.AgentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent")
@Tag(name = "智能体管理", description = "智能体管理相关接口")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Operation(summary = "创建智能体", description = "创建新的智能体")
    @PostMapping
    public ResponseEntity<AgentVO> createAgent(@RequestBody AgentDTO agentDTO) {
        AgentVO agentVO = agentService.createAgent(agentDTO);
        return ResponseEntity.ok(agentVO);
    }

    @Operation(summary = "更新智能体", description = "更新指定ID的智能体信息")
    @PutMapping("/{id}")
    public ResponseEntity<AgentVO> updateAgent(@PathVariable Long id, @RequestBody AgentDTO agentDTO) {
        AgentVO agentVO = agentService.updateAgent(id, agentDTO);
        return ResponseEntity.ok(agentVO);
    }

    @Operation(summary = "删除智能体", description = "删除指定ID的智能体")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteAgent(@PathVariable Long id) {
        boolean result = agentService.deleteAgent(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取智能体详情", description = "获取指定ID的智能体详情")
    @GetMapping("/{id}")
    public ResponseEntity<AgentVO> getAgentById(@PathVariable Long id) {
        AgentVO agentVO = agentService.getAgentById(id);
        return ResponseEntity.ok(agentVO);
    }

    @Operation(summary = "获取智能体列表", description = "获取智能体列表，支持分页和筛选")
    @GetMapping
    public ResponseEntity<List<AgentVO>> getAgentList(@RequestParam Map<String, Object> params) {
        List<AgentVO> agentList = agentService.getAgentList(params);
        return ResponseEntity.ok(agentList);
    }

    @Operation(summary = "更新智能体状态", description = "更新指定ID的智能体状态")
    @PutMapping("/{id}/status")
    public ResponseEntity<Boolean> updateAgentStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = agentService.updateAgentStatus(id, status);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "统计智能体数量", description = "获取智能体总数量")
    @GetMapping("/count")
    public ResponseEntity<Long> countAgents() {
        Long count = agentService.countAgents();
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "获取在线智能体数量", description = "获取在线智能体数量")
    @GetMapping("/online/count")
    public ResponseEntity<Long> countOnlineAgents() {
        Long count = agentService.countOnlineAgents();
        return ResponseEntity.ok(count);
    }
}