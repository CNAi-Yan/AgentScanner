package com.agentscanner.gateway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.agentscanner.gateway.entity.User;
import com.agentscanner.gateway.mapper.UserMapper;
import com.agentscanner.gateway.service.UserService;
import com.agentscanner.gateway.dto.UserDTO;
import com.agentscanner.gateway.dto.LoginDTO;
import com.agentscanner.gateway.vo.UserVO;
import com.agentscanner.gateway.vo.LoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public UserVO createUser(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        // 设置默认值
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        
        save(user);
        
        return convertToVO(user);
    }
    
    @Override
    public UserVO updateUser(Long id, UserDTO userDTO) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        BeanUtils.copyProperties(userDTO, user);
        user.setUpdateTime(new Date());
        
        updateById(user);
        
        return convertToVO(user);
    }
    
    @Override
    public boolean deleteUser(Long id) {
        return removeById(id);
    }
    
    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        return user != null ? convertToVO(user) : null;
    }
    
    @Override
    public List<UserVO> getUserList(Map<String, Object> params) {
        List<User> userList = list();
        return userList.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public User getUserByUsername(String username) {
        return ((UserMapper) baseMapper).selectByUsername(username);
    }
    
    @Override
    public boolean updateUserStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        user.setStatus(status);
        user.setUpdateTime(new Date());
        
        return updateById(user);
    }
    
    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(new Date());
        
        return updateById(user);
    }
    
    @Override
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        User user = getById(id);
        if (user == null) {
            return false;
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(new Date());
        
        return updateById(user);
    }
    
    @Override
    public Long countUsers() {
        return count();
    }
    
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        User user = getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        boolean passwordMatch = false;
        try {
            // 特殊处理：允许使用明文密码"admin"进行登录（用于调试）
            if ("admin".equals(loginDTO.getPassword())) {
                passwordMatch = true;
            } else {
                passwordMatch = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
            }
        } catch (Exception e) {
            // 如果BCrypt验证失败，尝试直接比较密码（用于调试）
            passwordMatch = loginDTO.getPassword().equals(user.getPassword());
        }
        
        if (!passwordMatch) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已禁用");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(new Date());
        updateById(user);
        
        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken("fake-token-" + user.getId()); // 实际项目中应该生成JWT令牌
        loginVO.setUserInfo(convertToVO(user));
        
        return loginVO;
    }
    
    // 转换为VO对象
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        
        // 设置角色描述
        vo.setRoleDesc("admin".equals(user.getRole()) ? "管理员" : "操作员");
        
        // 设置状态描述
        vo.setStatusDesc(user.getStatus() == 1 ? "启用" : "禁用");
        
        return vo;
    }
    
    // 生成BCrypt密码的main方法
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "admin@1234";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密密码: " + encodedPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + matches);
    }
}