package com.agentscanner.gateway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agentscanner.gateway.entity.Policy;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PolicyMapper extends BaseMapper<Policy> {
}