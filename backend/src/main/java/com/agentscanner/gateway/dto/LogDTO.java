package com.agentscanner.gateway.dto;

import lombok.Data;
import java.util.Date;

@Data
public class LogDTO {
    
    // 日志类型：1-访问日志，2-审计日志
    private Integer type;
    
    // 智能体ID
    private Long agentId;
    
    // 智能体名称
    private String agentName;
    
    // 客户端IP
    private String clientIp;
    
    // 请求URL
    private String requestUrl;
    
    // HTTP方法
    private String httpMethod;
    
    // 请求参数
    private String requestParams;
    
    // 请求头
    private String requestHeaders;
    
    // 响应状态码
    private Integer responseStatus;
    
    // 响应时间（毫秒）
    private Long responseTime;
    
    // 错误信息
    private String errorMessage;
    
    // 操作人
    private String operator;
    
    // 操作内容
    private String operationContent;
}