package com.agentscanner.gateway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.agentscanner.gateway.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    // 根据用户名查询用户
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);
}