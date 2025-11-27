package com.agentscanner.gateway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.agentscanner.gateway.entity.User;
import com.agentscanner.gateway.dto.UserDTO;
import com.agentscanner.gateway.dto.LoginDTO;
import com.agentscanner.gateway.vo.UserVO;
import com.agentscanner.gateway.vo.LoginVO;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {
    
    // 创建用户
    UserVO createUser(UserDTO userDTO);
    
    // 更新用户
    UserVO updateUser(Long id, UserDTO userDTO);
    
    // 删除用户
    boolean deleteUser(Long id);
    
    // 获取用户详情
    UserVO getUserById(Long id);
    
    // 获取用户列表
    List<UserVO> getUserList(Map<String, Object> params);
    
    // 根据用户名获取用户
    User getUserByUsername(String username);
    
    // 更新用户状态
    boolean updateUserStatus(Long id, Integer status);
    
    // 重置用户密码
    boolean resetPassword(Long id, String newPassword);
    
    // 更新用户密码
    boolean updatePassword(Long id, String oldPassword, String newPassword);
    
    // 统计用户数量
    Long countUsers();
    
    // 用户登录
    LoginVO login(LoginDTO loginDTO);
}