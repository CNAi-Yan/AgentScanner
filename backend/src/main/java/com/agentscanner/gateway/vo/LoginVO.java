package com.agentscanner.gateway.vo;

import lombok.Data;

@Data
public class LoginVO {
    
    // 登录令牌
    private String token;
    
    // 用户信息
    private UserVO userInfo;
}