package com.agentscanner.gateway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.agentscanner.gateway.entity.Agent;
import com.agentscanner.gateway.mapper.AgentMapper;
import com.agentscanner.gateway.service.AgentService;
import com.agentscanner.gateway.dto.AgentDTO;
import com.agentscanner.gateway.vo.AgentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {
    
    @Override
    public AgentVO createAgent(AgentDTO agentDTO) {
        Agent agent = new Agent();
        BeanUtils.copyProperties(agentDTO, agent);
        
        // 生成API密钥
        agent.setApiKey(generateApiKey());
        
        // 设置默认值
        agent.setCreateTime(new Date());
        agent.setUpdateTime(new Date());
        agent.setTotalRequests(0L);
        agent.setTodayRequests(0L);
        
        save(agent);
        
        return convertToVO(agent);
    }
    
    @Override
    public AgentVO updateAgent(Long id, AgentDTO agentDTO) {
        Agent agent = getById(id);
        if (agent == null) {
            throw new RuntimeException("智能体不存在");
        }
        
        BeanUtils.copyProperties(agentDTO, agent);
        agent.setUpdateTime(new Date());
        
        updateById(agent);
        
        return convertToVO(agent);
    }
    
    @Override
    public boolean deleteAgent(Long id) {
        return removeById(id);
    }
    
    @Override
    public AgentVO getAgentById(Long id) {
        Agent agent = getById(id);
        return agent != null ? convertToVO(agent) : null;
    }
    
    @Override
    public List<AgentVO> getAgentList(Map<String, Object> params) {
        List<Agent> agents = list();
        return agents.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    @Override
    public boolean updateAgentStatus(Long id, Integer status) {
        Agent agent = getById(id);
        if (agent == null) {
            return false;
        }
        
        agent.setStatus(status);
        agent.setUpdateTime(new Date());
        
        return updateById(agent);
    }
    
    @Override
    public Long countAgents() {
        return count();
    }
    
    @Override
    public Long countOnlineAgents() {
        // 这里简化处理，实际应该根据lastOnlineTime判断
        return count();
    }
    
    // 转换为VO对象
    private AgentVO convertToVO(Agent agent) {
        AgentVO vo = new AgentVO();
        BeanUtils.copyProperties(agent, vo);
        
        // 设置状态描述
        if (agent.getStatus() == 1) {
            vo.setStatusDesc("启用");
        } else {
            vo.setStatusDesc("禁用");
        }
        
        return vo;
    }
}