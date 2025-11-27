package com.agentscanner.gateway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.agentscanner.gateway.entity.Agent;
import com.agentscanner.gateway.dto.AgentDTO;
import com.agentscanner.gateway.vo.AgentVO;

import java.util.List;
import java.util.Map;

public interface AgentService extends IService<Agent> {
    
    // 创建智能体
    AgentVO createAgent(AgentDTO agentDTO);
    
    // 更新智能体
    AgentVO updateAgent(Long id, AgentDTO agentDTO);
    
    // 删除智能体
    boolean deleteAgent(Long id);
    
    // 获取智能体详情
    AgentVO getAgentById(Long id);
    
    // 获取智能体列表
    List<AgentVO> getAgentList(Map<String, Object> params);
    
    // 生成API密钥
    String generateApiKey();
    
    // 更新智能体状态
    boolean updateAgentStatus(Long id, Integer status);
    
    // 统计智能体数量
    Long countAgents();
    
    // 获取在线智能体数量
    Long countOnlineAgents();
}