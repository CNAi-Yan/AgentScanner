package com.agentscanner.gateway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agentscanner.gateway.entity.Agent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentMapper extends BaseMapper<Agent> {
}