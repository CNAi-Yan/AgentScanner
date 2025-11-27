package com.agentscanner.gateway.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("policy")
public class Policy implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 策略名称
    private String name;
    
    // 策略类型：1-访问控制，2-流量控制，3-内容过滤
    private Integer type;
    
    // 策略描述
    private String description;
    
    // 策略内容（JSON格式）
    private String content;
    
    // 优先级：数字越小优先级越高
    private Integer priority;
    
    // 状态：0-禁用，1-启用
    private Integer status;
    
    // 适用智能体分组
    private String applicableGroups;
    
    // 适用智能体ID列表
    private String applicableAgents;
    
    // 创建时间
    private Date createTime;
    
    // 更新时间
    private Date updateTime;
}