package com.agentscanner.gateway.dto;

import lombok.Data;
import java.util.Date;

@Data
public class UserDTO {
    
    // 用户名
    private String username;
    
    // 密码
    private String password;
    
    // 角色：admin-管理员，operator-操作员
    private String role;
    
    // 状态：0-禁用，1-启用
    private Integer status;
}