package com.agentscanner.gateway.dto;

import lombok.Data;
import java.util.Date;

@Data
public class AgentDTO {
    
    // 智能体名称
    private String name;
    
    // 智能体类型
    private String type;
    
    // 智能体描述
    private String description;
    
    // 状态：0-禁用，1-启用
    private Integer status;
    
    // 所属分组
    private String groupName;
    
    // 允许的IP列表
    private String allowedIps;
}