package com.agentscanner.gateway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.agentscanner.gateway.entity.Policy;
import com.agentscanner.gateway.dto.PolicyDTO;
import com.agentscanner.gateway.vo.PolicyVO;

import java.util.List;
import java.util.Map;

public interface PolicyService extends IService<Policy> {
    
    // 创建策略
    PolicyVO createPolicy(PolicyDTO policyDTO);
    
    // 更新策略
    PolicyVO updatePolicy(Long id, PolicyDTO policyDTO);
    
    // 删除策略
    boolean deletePolicy(Long id);
    
    // 获取策略详情
    PolicyVO getPolicyById(Long id);
    
    // 获取策略列表
    List<PolicyVO> getPolicyList(Map<String, Object> params);
    
    // 更新策略状态
    boolean updatePolicyStatus(Long id, Integer status);
    
    // 统计策略数量
    Long countPolicies();
    
    // 获取启用的策略列表
    List<Policy> getEnabledPolicies();
    
    // 根据智能体ID获取适用的策略列表
    List<Policy> getApplicablePolicies(Long agentId, String groupName);
}