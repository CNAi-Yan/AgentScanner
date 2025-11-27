package com.agentscanner.gateway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.agentscanner.gateway.entity.Policy;
import com.agentscanner.gateway.mapper.PolicyMapper;
import com.agentscanner.gateway.service.PolicyService;
import com.agentscanner.gateway.dto.PolicyDTO;
import com.agentscanner.gateway.vo.PolicyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PolicyServiceImpl extends ServiceImpl<PolicyMapper, Policy> implements PolicyService {
    
    @Override
    public PolicyVO createPolicy(PolicyDTO policyDTO) {
        Policy policy = new Policy();
        BeanUtils.copyProperties(policyDTO, policy);
        
        // 设置默认值
        policy.setCreateTime(new Date());
        policy.setUpdateTime(new Date());
        
        save(policy);
        
        return convertToVO(policy);
    }
    
    @Override
    public PolicyVO updatePolicy(Long id, PolicyDTO policyDTO) {
        Policy policy = getById(id);
        if (policy == null) {
            throw new RuntimeException("策略不存在");
        }
        
        BeanUtils.copyProperties(policyDTO, policy);
        policy.setUpdateTime(new Date());
        
        updateById(policy);
        
        return convertToVO(policy);
    }
    
    @Override
    public boolean deletePolicy(Long id) {
        return removeById(id);
    }
    
    @Override
    public PolicyVO getPolicyById(Long id) {
        Policy policy = getById(id);
        return policy != null ? convertToVO(policy) : null;
    }
    
    @Override
    public List<PolicyVO> getPolicyList(Map<String, Object> params) {
        List<Policy> policies = list();
        return policies.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public boolean updatePolicyStatus(Long id, Integer status) {
        Policy policy = getById(id);
        if (policy == null) {
            return false;
        }
        
        policy.setStatus(status);
        policy.setUpdateTime(new Date());
        
        return updateById(policy);
    }
    
    @Override
    public Long countPolicies() {
        return count();
    }
    
    @Override
    public List<Policy> getEnabledPolicies() {
        return lambdaQuery().eq(Policy::getStatus, 1).orderByAsc(Policy::getPriority).list();
    }
    
    @Override
    public List<Policy> getApplicablePolicies(Long agentId, String groupName) {
        // 这里简化处理，实际应该根据applicableAgents和applicableGroups字段进行筛选
        return lambdaQuery()
                .eq(Policy::getStatus, 1)
                .orderByAsc(Policy::getPriority)
                .list();
    }
    
    // 转换为VO对象
    private PolicyVO convertToVO(Policy policy) {
        PolicyVO vo = new PolicyVO();
        BeanUtils.copyProperties(policy, vo);
        
        // 设置策略类型描述
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
        
        // 设置状态描述
        vo.setStatusDesc(policy.getStatus() == 1 ? "启用" : "禁用");
        
        return vo;
    }
}