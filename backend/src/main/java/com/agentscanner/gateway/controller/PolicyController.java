package com.agentscanner.gateway.controller;

import com.agentscanner.gateway.service.PolicyService;
import com.agentscanner.gateway.dto.PolicyDTO;
import com.agentscanner.gateway.vo.PolicyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/policy")
@Tag(name = "防护策略管理", description = "防护策略管理相关接口")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @Operation(summary = "创建策略", description = "创建新的防护策略")
    @PostMapping
    public ResponseEntity<PolicyVO> createPolicy(@RequestBody PolicyDTO policyDTO) {
        PolicyVO policyVO = policyService.createPolicy(policyDTO);
        return ResponseEntity.ok(policyVO);
    }

    @Operation(summary = "更新策略", description = "更新指定ID的防护策略信息")
    @PutMapping("/{id}")
    public ResponseEntity<PolicyVO> updatePolicy(@PathVariable Long id, @RequestBody PolicyDTO policyDTO) {
        PolicyVO policyVO = policyService.updatePolicy(id, policyDTO);
        return ResponseEntity.ok(policyVO);
    }

    @Operation(summary = "删除策略", description = "删除指定ID的防护策略")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deletePolicy(@PathVariable Long id) {
        boolean result = policyService.deletePolicy(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取策略详情", description = "获取指定ID的防护策略详情")
    @GetMapping("/{id}")
    public ResponseEntity<PolicyVO> getPolicyById(@PathVariable Long id) {
        PolicyVO policyVO = policyService.getPolicyById(id);
        return ResponseEntity.ok(policyVO);
    }

    @Operation(summary = "获取策略列表", description = "获取防护策略列表，支持分页和筛选")
    @GetMapping
    public ResponseEntity<List<PolicyVO>> getPolicyList(@RequestParam Map<String, Object> params) {
        List<PolicyVO> policyList = policyService.getPolicyList(params);
        return ResponseEntity.ok(policyList);
    }

    @Operation(summary = "更新策略状态", description = "更新指定ID的防护策略状态")
    @PutMapping("/{id}/status")
    public ResponseEntity<Boolean> updatePolicyStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = policyService.updatePolicyStatus(id, status);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "统计策略数量", description = "获取防护策略总数量")
    @GetMapping("/count")
    public ResponseEntity<Long> countPolicies() {
        Long count = policyService.countPolicies();
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "获取启用的策略列表", description = "获取所有启用的防护策略列表")
    @GetMapping("/enabled")
    public ResponseEntity<List<PolicyVO>> getEnabledPolicies() {
        List<PolicyVO> policyList = policyService.getEnabledPolicies().stream()
                .map(policy -> {
                    PolicyVO vo = new PolicyVO();
                    org.springframework.beans.BeanUtils.copyProperties(policy, vo);
                    // 设置类型描述和状态描述
                    switch (policy.getType()) {
                        case 1:
                            vo.setTypeDesc("访问控制");
                            break;
                        case 2:
                            vo.setTypeDesc("流量控制");
                            break;
                        case 3:
                            vo.setTypeDesc("内容过滤");
                            break;
                        default:
                            vo.setTypeDesc("未知类型");
                    }
                    vo.setStatusDesc(policy.getStatus() == 1 ? "启用" : "禁用");
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(policyList);
    }

    @Operation(summary = "根据智能体获取适用策略", description = "根据智能体ID获取适用的防护策略列表")
    @GetMapping("/applicable")
    public ResponseEntity<List<PolicyVO>> getApplicablePolicies(@RequestParam Long agentId, @RequestParam String groupName) {
        List<PolicyVO> policyList = policyService.getApplicablePolicies(agentId, groupName).stream()
                .map(policy -> {
                    PolicyVO vo = new PolicyVO();
                    org.springframework.beans.BeanUtils.copyProperties(policy, vo);
                    // 设置类型描述和状态描述
                    switch (policy.getType()) {
                        case 1:
                            vo.setTypeDesc("访问控制");
                            break;
                        case 2:
                            vo.setTypeDesc("流量控制");
                            break;
                        case 3:
                            vo.setTypeDesc("内容过滤");
                            break;
                        default:
                            vo.setTypeDesc("未知类型");
                    }
                    vo.setStatusDesc(policy.getStatus() == 1 ? "启用" : "禁用");
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(policyList);
    }
}