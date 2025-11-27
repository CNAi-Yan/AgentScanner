package com.agentscanner.gateway.nginx;

import com.agentscanner.gateway.entity.Agent;
import com.agentscanner.gateway.entity.Policy;
import com.agentscanner.gateway.service.AgentService;
import com.agentscanner.gateway.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Nginx配置生成器
 * 用于根据数据库中的配置动态生成Nginx配置文件
 */
@Component
public class NginxConfigGenerator {
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private PolicyService policyService;
    
    @Value("${nginx.conf-path}")
    private String nginxConfPath;
    
    @Value("${nginx.lua-script-path}")
    private String luaScriptPath;
    
    /**
     * 生成主配置文件
     */
    public void generateMainConfig() throws IOException {
        StringBuilder config = new StringBuilder();
        
        // 基本配置
        config.append("worker_processes  1;\n\n");
        config.append("events {\n");
        config.append("    worker_connections  1024;\n");
        config.append("}\n\n");
        
        // 引入Lua模块
        config.append("# 引入Lua模块\n");
        config.append("lua_package_path \"").append(luaScriptPath).append("?.lua;;\";\n\n");
        
        // 配置共享内存
        config.append("# 配置共享内存\n");
        config.append("lua_shared_dict agent_cache 10m;\n");
        config.append("lua_shared_dict policy_cache 10m;\n");
        config.append("lua_shared_dict rate_limit 10m;\n\n");
        
        // HTTP配置
        config.append("# HTTP配置\n");
        config.append("http {\n");
        config.append("    include       mime.types;\n");
        config.append("    default_type  application/octet-stream;\n\n");
        config.append("    sendfile        on;\n");
        config.append("    keepalive_timeout  65;\n\n");
        
        // 日志格式
        config.append("    # 日志格式\n");
        config.append("    log_format  main  '$remote_addr - $remote_user [$time_local] \"$request\" '\n");
        config.append("                      '$status $body_bytes_sent \"$http_referer\" '\n");
        config.append("                      '\"$http_user_agent\" \"$http_x_forwarded_for\" '\n");
        config.append("                      '$request_time';\n\n");
        
        config.append("    access_log  logs/access.log  main;\n");
        config.append("    error_log  logs/error.log  error;\n\n");
        
        // 引入代理配置
        config.append("    # 引入代理配置\n");
        config.append("    include conf.d/proxy.conf;\n\n");
        
        // 引入安全配置
        config.append("    # 引入安全配置\n");
        config.append("    include conf.d/security.conf;\n\n");
        
        // Nginx状态监控
        config.append("    # Nginx状态监控\n");
        config.append("    server {\n");
        config.append("        listen       8081;\n");
        config.append("        server_name  localhost;\n\n");
        config.append("        location /status {\n");
        config.append("            stub_status on;\n");
        config.append("            access_log off;\n");
        config.append("            allow 127.0.0.1;\n");
        config.append("            deny all;\n");
        config.append("        }\n");
        config.append("    }\n");
        config.append("}\n\n");
        
        // TCP配置
        config.append("# TCP配置\n");
        config.append("tcp {\n");
        config.append("    include conf.d/tcp_proxy.conf;\n");
        config.append("}\n\n");
        
        // UDP配置
        config.append("# UDP配置\n");
        config.append("udp {\n");
        config.append("    include conf.d/udp_proxy.conf;\n");
        config.append("}\n");
        
        // 写入文件
        writeConfigFile(nginxConfPath, config.toString());
    }
    
    /**
     * 生成代理配置文件
     */
    public void generateProxyConfig() throws IOException {
        StringBuilder config = new StringBuilder();
        
        // HTTP代理配置
        config.append("# HTTP代理配置\n");
        config.append("server {\n");
        config.append("    listen       8080;\n");
        config.append("    server_name  localhost;\n\n");
        
        // 代理前缀
        config.append("    # 代理前缀\n");
        config.append("    location /proxy/ {\n");
        config.append("        # 移除/proxy/前缀\n");
        config.append("        rewrite ^/proxy/(.*)$ /$1 break;\n\n");
        
        // 调用认证Lua脚本
        config.append("        # 调用认证Lua脚本\n");
        config.append("        access_by_lua_file ").append(luaScriptPath).append("auth.lua;\n\n");
        
        // 调用策略Lua脚本
        config.append("        # 调用策略Lua脚本\n");
        config.append("        access_by_lua_file ").append(luaScriptPath).append("policy.lua;\n\n");
        
        // 调用速率限制Lua脚本
        config.append("        # 调用速率限制Lua脚本\n");
        config.append("        access_by_lua_file ").append(luaScriptPath).append("rate_limit.lua;\n\n");
        
        // 设置代理头
        config.append("        # 设置代理头\n");
        config.append("        proxy_set_header Host $host;\n");
        config.append("        proxy_set_header X-Real-IP $remote_addr;\n");
        config.append("        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n");
        config.append("        proxy_set_header X-Forwarded-Proto $scheme;\n\n");
        
        // 代理到后端服务器
        config.append("        # 代理到后端服务器\n");
        config.append("        proxy_pass $upstream_url;\n\n");
        
        // 调用日志Lua脚本
        config.append("        # 调用日志Lua脚本\n");
        config.append("        log_by_lua_file ").append(luaScriptPath).append("log.lua;\n");
        config.append("    }\n\n");
        
        // WebSocket代理配置
        config.append("    # WebSocket代理配置\n");
        config.append("    location /ws/ {\n");
        config.append("        # 移除/ws/前缀\n");
        config.append("        rewrite ^/ws/(.*)$ /$1 break;\n\n");
        
        // 调用认证Lua脚本
        config.append("        # 调用认证Lua脚本\n");
        config.append("        access_by_lua_file ").append(luaScriptPath).append("auth.lua;\n\n");
        
        // WebSocket支持
        config.append("        # WebSocket支持\n");
        config.append("        proxy_http_version 1.1;\n");
        config.append("        proxy_set_header Upgrade $http_upgrade;\n");
        config.append("        proxy_set_header Connection \"upgrade\";\n\n");
        
        // 代理到后端WebSocket服务器
        config.append("        # 代理到后端WebSocket服务器\n");
        config.append("        proxy_pass $upstream_url;\n\n");
        
        // 调用日志Lua脚本
        config.append("        # 调用日志Lua脚本\n");
        config.append("        log_by_lua_file ").append(luaScriptPath).append("log.lua;\n");
        config.append("    }\n");
        config.append("}\n");
        
        // 写入文件
        writeConfigFile(nginxConfPath.replace("nginx.conf", "conf.d/proxy.conf"), config.toString());
    }
    
    /**
     * 生成安全配置文件
     */
    public void generateSecurityConfig() throws IOException {
        StringBuilder config = new StringBuilder();
        
        // 安全配置
        config.append("# 安全配置\n\n");
        
        // 防止XSS攻击
        config.append("# 防止XSS攻击\n");
        config.append("add_header X-XSS-Protection \"1; mode=block\";\n\n");
        
        // 防止Clickjacking
        config.append("# 防止Clickjacking\n");
        config.append("add_header X-Frame-Options \"SAMEORIGIN\";\n\n");
        
        // 防止MIME类型嗅探
        config.append("# 防止MIME类型嗅探\n");
        config.append("add_header X-Content-Type-Options \"nosniff\";\n\n");
        
        // 启用HSTS
        config.append("# 启用HSTS\n");
        config.append("add_header Strict-Transport-Security \"max-age=31536000; includeSubDomains\" always;\n\n");
        
        // 限制请求方法
        config.append("# 限制请求方法\n");
        config.append("if ($request_method !~ ^(GET|POST|PUT|DELETE|OPTIONS)$ ) {\n");
        config.append("    return 405;\n");
        config.append("}\n\n");
        
        // 限制请求体大小
        config.append("# 限制请求体大小\n");
        config.append("client_max_body_size 10m;\n\n");
        
        // 限制请求速率
        config.append("# 限制请求速率\n");
        config.append("limit_req_zone $binary_remote_addr zone=mylimit:10m rate=10r/s;\n\n");
        
        // 限制并发连接数
        config.append("# 限制并发连接数\n");
        config.append("limit_conn_zone $binary_remote_addr zone=perip:10m;\n");
        config.append("limit_conn_zone $server_name zone=perserver:10m;\n\n");
        
        // 禁止访问隐藏文件
        config.append("# 禁止访问隐藏文件\n");
        config.append("location ~ /\\. {\n");
        config.append("    deny all;\n");
        config.append("    access_log off;\n");
        config.append("    log_not_found off;\n");
        config.append("}\n\n");
        
        // 禁止访问敏感文件
        config.append("# 禁止访问敏感文件\n");
        config.append("location ~* (\\.log|\\.pid|\\.conf)$ {\n");
        config.append("    deny all;\n");
        config.append("    access_log off;\n");
        config.append("    log_not_found off;\n");
        config.append("}\n");
        
        // 写入文件
        writeConfigFile(nginxConfPath.replace("nginx.conf", "conf.d/security.conf"), config.toString());
    }
    
    /**
     * 生成TCP代理配置文件
     */
    public void generateTcpProxyConfig() throws IOException {
        StringBuilder config = new StringBuilder();
        
        // TCP代理配置
        config.append("# TCP代理配置\n");
        config.append("stream {\n");
        
        // 示例：代理MySQL服务
        config.append("    # 示例：代理MySQL服务\n");
        config.append("    server {\n");
        config.append("        listen 3307;\n\n");
        config.append("        # 调用TCP认证Lua脚本\n");
        config.append("        access_by_lua_file ").append(luaScriptPath).append("tcp_auth.lua;\n\n");
        config.append("        # 代理到MySQL服务器\n");
        config.append("        proxy_pass 127.0.0.1:3306;\n\n");
        config.append("        # 调用TCP日志Lua脚本\n");
        config.append("        log_by_lua_file ").append(luaScriptPath).append("tcp_log.lua;\n");
        config.append("    }\n\n");
        
        // 示例：代理Redis服务
        config.append("    # 示例：代理Redis服务\n");
        config.append("    server {\n");
        config.append("        listen 6380;\n\n");
        config.append("        # 调用TCP认证Lua脚本\n");
        config.append("        access_by_lua_file ").append(luaScriptPath).append("tcp_auth.lua;\n\n");
        config.append("        # 代理到Redis服务器\n");
        config.append("        proxy_pass 127.0.0.1:6379;\n\n");
        config.append("        # 调用TCP日志Lua脚本\n");
        config.append("        log_by_lua_file ").append(luaScriptPath).append("tcp_log.lua;\n");
        config.append("    }\n");
        config.append("}\n");
        
        // 写入文件
        writeConfigFile(nginxConfPath.replace("nginx.conf", "conf.d/tcp_proxy.conf"), config.toString());
    }
    
    /**
     * 生成UDP代理配置文件
     */
    public void generateUdpProxyConfig() throws IOException {
        StringBuilder config = new StringBuilder();
        
        // UDP代理配置
        config.append("# UDP代理配置\n");
        config.append("stream {\n");
        
        // 示例：代理DNS服务
        config.append("    # 示例：代理DNS服务\n");
        config.append("    server {\n");
        config.append("        listen 53 udp;\n\n");
        config.append("        # 调用UDP认证Lua脚本\n");
        config.append("        access_by_lua_file ").append(luaScriptPath).append("udp_auth.lua;\n\n");
        config.append("        # 代理到DNS服务器\n");
        config.append("        proxy_pass 8.8.8.8:53;\n\n");
        config.append("        # UDP代理超时设置\n");
        config.append("        proxy_timeout 10s;\n\n");
        config.append("        # 调用UDP日志Lua脚本\n");
        config.append("        log_by_lua_file ").append(luaScriptPath).append("udp_log.lua;\n");
        config.append("    }\n\n");
        
        // 示例：代理Syslog服务
        config.append("    # 示例：代理Syslog服务\n");
        config.append("    server {\n");
        config.append("        listen 514 udp;\n\n");
        config.append("        # 调用UDP认证Lua脚本\n");
        config.append("        access_by_lua_file ").append(luaScriptPath).append("udp_auth.lua;\n\n");
        config.append("        # 代理到Syslog服务器\n");
        config.append("        proxy_pass 127.0.0.1:5140;\n\n");
        config.append("        # UDP代理超时设置\n");
        config.append("        proxy_timeout 10s;\n\n");
        config.append("        # 调用UDP日志Lua脚本\n");
        config.append("        log_by_lua_file ").append(luaScriptPath).append("udp_log.lua;\n");
        config.append("    }\n");
        config.append("}\n");
        
        // 写入文件
        writeConfigFile(nginxConfPath.replace("nginx.conf", "conf.d/udp_proxy.conf"), config.toString());
    }
    
    /**
     * 生成所有配置文件
     */
    public void generateAllConfigs() throws IOException {
        generateMainConfig();
        generateProxyConfig();
        generateSecurityConfig();
        generateTcpProxyConfig();
        generateUdpProxyConfig();
    }
    
    /**
     * 将配置写入文件
     */
    private void writeConfigFile(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
}