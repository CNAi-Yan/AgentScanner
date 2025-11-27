package com.agentscanner.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("agent")
public class Agent implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 智能体名称
    private String name;
    
    // 智能体类型
    private String type;
    
    // 智能体描述
    private String description;
    
    // API密钥
    private String apiKey;
    
    // 状态：0-禁用，1-启用
    private Integer status;
    
    // 所属分组
    private String groupName;
    
    // 允许的IP列表
    private String allowedIps;
    
    // 创建时间
    private Date createTime;
    
    // 更新时间
    private Date updateTime;
    
    // 最后在线时间
    private Date lastOnlineTime;
    
    // 总请求数
    private Long totalRequests;
    
    // 今日请求数
    private Long todayRequests;
}