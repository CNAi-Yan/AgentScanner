package com.agentscanner.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 用户名
    private String username;
    
    // 密码
    private String password;
    
    // 角色：admin-管理员，operator-操作员
    private String role;
    
    // 状态：0-禁用，1-启用
    private Integer status;
    
    // 创建时间
    private Date createTime;
    
    // 更新时间
    private Date updateTime;
    
    // 最后登录时间
    private Date lastLoginTime;
}