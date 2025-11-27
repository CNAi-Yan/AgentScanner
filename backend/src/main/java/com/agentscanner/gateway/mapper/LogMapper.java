package com.agentscanner.gateway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agentscanner.gateway.entity.Log;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogMapper extends BaseMapper<Log> {
}