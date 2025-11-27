package com.agentscanner.gateway.vo;

import lombok.Data;
import java.util.Date;

@Data
public class UserVO {
    
    private Long id;
    
    // 用户名
    private String username;
    
    // 角色：admin-管理员，operator-操作员
    private String role;
    
    // 角色描述
    private String roleDesc;
    
    // 状态：0-禁用，1-启用
    private Integer status;
    
    // 状态描述
    private String statusDesc;
    
    // 创建时间
    private Date createTime;
    
    // 更新时间
    private Date updateTime;
    
    // 最后登录时间
    private Date lastLoginTime;
}