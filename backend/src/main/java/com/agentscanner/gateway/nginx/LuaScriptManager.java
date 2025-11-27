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
 * Lua脚本管理器
 * 用于管理和更新Lua脚本
 */
@Component
public class LuaScriptManager {
    
    @Autowired
    private AgentService agentService;
    
    @Autowired
    private PolicyService policyService;
    
    @Value("${nginx.lua-script-path}")
    private String luaScriptPath;
    
    /**
     * 更新所有Lua脚本
     */
    public void updateAllScripts() throws IOException {
        updateAuthScript();
        updatePolicyScript();
        updateRateLimitScript();
        updateLogScript();
    }
    
    /**
     * 更新认证脚本
     */
    public void updateAuthScript() throws IOException {
        StringBuilder script = new StringBuilder();
        
        // 引入依赖
        script.append("-- HTTP认证Lua脚本\n");
        script.append("local cjson = require \"cjson\"\n");
        script.append("local redis = require \"resty.redis\"\n\n");
        
        // 连接Redis函数
        script.append("-- 连接Redis\n");
        script.append("local function connect_redis()\n");
        script.append("    local red = redis:new()\n");
        script.append("    red:set_timeout(1000) -- 1秒超时\n\n");
        script.append("    local ok, err = red:connect(\"127.0.0.1\", 6379)\n");
        script.append("    if not ok then\n");
        script.append("        ngx.log(ngx.ERR, \"Failed to connect to Redis: \", err)\n");
        script.append("        return nil, err\n");
        script.append("    end\n\n");
        script.append("    return red, nil\n");
        script.append("end\n\n");
        
        // 从Redis获取智能体信息函数
        script.append("-- 从Redis获取智能体信息\n");
        script.append("local function get_agent_from_redis(api_key)\n");
        script.append("    local red, err = connect_redis()\n");
        script.append("    if not red then\n");
        script.append("        return nil, err\n");
        script.append("    end\n\n");
        script.append("    local agent_json, err = red:get(\"agent:\" .. api_key)\n");
        script.append("    if not agent_json then\n");
        script.append("        red:close()\n");
        script.append("        return nil, err\n");
        script.append("    end\n\n");
        script.append("    if agent_json == ngx.null then\n");
        script.append("        red:close()\n");
        script.append("        return nil, \"Agent not found\"\n");
        script.append("    end\n\n");
        script.append("    red:close()\n");
        script.append("    return cjson.decode(agent_json), nil\n");
        script.append("end\n\n");
        
        // 从缓存获取智能体信息函数
        script.append("-- 从缓存获取智能体信息\n");
        script.append("local function get_agent(api_key)\n");
        script.append("    -- 先从共享内存缓存获取\n");
        script.append("    local agent_cache = ngx.shared.agent_cache\n");
        script.append("    local agent_json = agent_cache:get(api_key)\n\n");
        script.append("    if agent_json then\n");
        script.append("        return cjson.decode(agent_json), nil\n");
        script.append("    end\n\n");
        script.append("    -- 缓存不存在，从Redis获取\n");
        script.append("    local agent, err = get_agent_from_redis(api_key)\n");
        script.append("    if not agent then\n");
        script.append("        return nil, err\n");
        script.append("    end\n\n");
        script.append("    -- 存入缓存，有效期5分钟\n");
        script.append("    agent_cache:set(api_key, cjson.encode(agent), 300)\n\n");
        script.append("    return agent, nil\n");
        script.append("end\n\n");
        
        // 主认证逻辑
        script.append("-- 主认证逻辑\n");
        script.append("local function main()\n");
        script.append("    -- 从请求头获取API密钥\n");
        script.append("    local api_key = ngx.req.get_headers()[\"X-Api-Key\"]\n");
        script.append("    if not api_key then\n");
        script.append("        -- 从查询参数获取API密钥\n");
        script.append("        api_key = ngx.var.arg_api_key\n");
        script.append("    end\n\n");
        script.append("    if not api_key then\n");
        script.append("        ngx.status = ngx.HTTP_UNAUTHORIZED\n");
        script.append("        ngx.header[\"Content-Type\"] = \"application/json\"\n");
        script.append("        ngx.say(cjson.encode({\"code\": 401, \"message\": \"Missing API key\"}))\n");
        script.append("        ngx.exit(ngx.HTTP_UNAUTHORIZED)\n");
        script.append("        return\n");
        script.append("    end\n\n");
        script.append("    -- 获取智能体信息\n");
        script.append("    local agent, err = get_agent(api_key)\n");
        script.append("    if not agent then\n");
        script.append("        ngx.status = ngx.HTTP_UNAUTHORIZED\n");
        script.append("        ngx.header[\"Content-Type\"] = \"application/json\"\n");
        script.append("        ngx.say(cjson.encode({\"code\": 401, \"message\": \"Invalid API key\"}))\n");
        script.append("        ngx.exit(ngx.HTTP_UNAUTHORIZED)\n");
        script.append("        return\n");
        script.append("    end\n\n");
        script.append("    -- 检查智能体状态\n");
        script.append("    if agent.status ~= 1 then\n");
        script.append("        ngx.status = ngx.HTTP_FORBIDDEN\n");
        script.append("        ngx.header[\"Content-Type\"] = \"application/json\"\n");
        script.append("        ngx.say(cjson.encode({\"code\": 403, \"message\": \"Agent is disabled\"}))\n");
        script.append("        ngx.exit(ngx.HTTP_FORBIDDEN)\n");
        script.append("        return\n");
        script.append("    end\n\n");
        script.append("    -- 检查IP白名单\n");
        script.append("    if agent.allowed_ips and agent.allowed_ips ~= \"\" then\n");
        script.append("        local allowed_ips = {}  -- 解析allowed_ips字符串为表\n");
        script.append("        for ip in string.gmatch(agent.allowed_ips, \"[^,]+\") do\n");
        script.append("            allowed_ips[ip:match(\"^%s*(.-)%s*$\")] = true\n");
        script.append("        end\n\n");
        script.append("        local client_ip = ngx.var.remote_addr\n");
        script.append("        if not allowed_ips[client_ip] then\n");
        script.append("            ngx.status = ngx.HTTP_FORBIDDEN\n");
        script.append("            ngx.header[\"Content-Type\"] = \"application/json\"\n");
        script.append("            ngx.say(cjson.encode({\"code\": 403, \"message\": \"IP not allowed\"}))\n");
        script.append("            ngx.exit(ngx.HTTP_FORBIDDEN)\n");
        script.append("            return\n");
        script.append("        end\n");
        script.append("    end\n\n");
        script.append("    -- 保存智能体信息到ngx.ctx\n");
        script.append("    ngx.ctx.agent = agent\n");
        script.append("    \n");
        script.append("    -- 解析目标URL（示例：从请求路径中解析）\n");
        script.append("    -- 实际使用中，可能需要根据配置或数据库中的映射关系来确定\n");
        script.append("    local target_url = ngx.var.uri:match(\"^/proxy/(.*)$\")\n");
        script.append("    if not target_url then\n");
        script.append("        ngx.status = ngx.HTTP_BAD_REQUEST\n");
        script.append("        ngx.header[\"Content-Type\"] = \"application/json\"\n");
        script.append("        ngx.say(cjson.encode({\"code\": 400, \"message\": \"Invalid target URL\"}))\n");
        script.append("        ngx.exit(ngx.HTTP_BAD_REQUEST)\n");
        script.append("        return\n");
        script.append("    end\n\n");
        script.append("    -- 设置上游URL\n");
        script.append("    ngx.var.upstream_url = \"http://\" .. target_url\n");
        script.append("end\n\n");
        
        // 执行主逻辑
        script.append("-- 执行主逻辑\n");
        script.append("main()\n");
        
        // 写入文件
        writeScriptFile(luaScriptPath + "auth.lua", script.toString());
    }
    
    /**
     * 更新策略脚本
     */
    public void updatePolicyScript() throws IOException {
        StringBuilder script = new StringBuilder();
        
        // 策略脚本内容
        script.append("-- 策略Lua脚本\n");
        script.append("local cjson = require \"cjson\"\n");
        script.append("local redis = require \"resty.redis\"\n\n");
        script.append("-- 主策略逻辑\n");
        script.append("local function main()\n");
        script.append("    -- 从ngx.ctx获取智能体信息\n");
        script.append("    local agent = ngx.ctx.agent\n");
        script.append("    if not agent then\n");
        script.append("        return\n");
        script.append("    end\n\n");
        script.append("    -- TODO: 实现策略逻辑\n");
        script.append("    -- 1. 从Redis或共享内存获取策略\n");
        script.append("    -- 2. 根据策略类型执行相应的检查\n");
        script.append("    -- 3. 如果策略不允许访问，返回403\n");
        script.append("end\n\n");
        script.append("-- 执行主逻辑\n");
        script.append("main()\n");
        
        // 写入文件
        writeScriptFile(luaScriptPath + "policy.lua", script.toString());
    }
    
    /**
     * 更新速率限制脚本
     */
    public void updateRateLimitScript() throws IOException {
        StringBuilder script = new StringBuilder();
        
        // 速率限制脚本内容
        script.append("-- 速率限制Lua脚本\n");
        script.append("local cjson = require \"cjson\"\n");
        script.append("local redis = require \"resty.redis\"\n\n");
        script.append("-- 主速率限制逻辑\n");
        script.append("local function main()\n");
        script.append("    -- 从ngx.ctx获取智能体信息\n");
        script.append("    local agent = ngx.ctx.agent\n");
        script.append("    if not agent then\n");
        script.append("        return\n");
        script.append("    end\n\n");
        script.append("    -- TODO: 实现速率限制逻辑\n");
        script.append("    -- 1. 根据智能体ID或IP进行速率限制\n");
        script.append("    -- 2. 使用共享内存或Redis实现令牌桶算法\n");
        script.append("    -- 3. 如果超过速率限制，返回429\n");
        script.append("end\n\n");
        script.append("-- 执行主逻辑\n");
        script.append("main()\n");
        
        // 写入文件
        writeScriptFile(luaScriptPath + "rate_limit.lua", script.toString());
    }
    
    /**
     * 更新日志脚本
     */
    public void updateLogScript() throws IOException {
        StringBuilder script = new StringBuilder();
        
        // 日志脚本内容
        script.append("-- 日志Lua脚本\n");
        script.append("local cjson = require \"cjson\"\n");
        script.append("local redis = require \"resty.redis\"\n\n");
        script.append("-- 主日志逻辑\n");
        script.append("local function main()\n");
        script.append("    -- 从ngx.ctx获取智能体信息\n");
        script.append("    local agent = ngx.ctx.agent\n");
        script.append("    if not agent then\n");
        script.append("        return\n");
        script.append("    end\n\n");
        script.append("    -- 构建日志数据\n");
        script.append("    local log_data = {\n");
        script.append("        type = 1, -- 1-访问日志\n");
        script.append("        agent_id = agent.id,\n");
        script.append("        agent_name = agent.name,\n");
        script.append("        client_ip = ngx.var.remote_addr,\n");
        script.append("        request_url = ngx.var.request_uri,\n");
        script.append("        http_method = ngx.var.request_method,\n");
        script.append("        request_params = ngx.var.args or \"\",\n");
        script.append("        response_status = ngx.var.status,\n");
        script.append("        response_time = ngx.var.request_time,\n");
        script.append("        log_time = ngx.localtime()\n");
        script.append("    }\n\n");
        script.append("    -- TODO: 将日志发送到Redis或消息队列\n");
        script.append("    -- 例如：使用Redis的LPUSH命令将日志加入队列\n");
        script.append("end\n\n");
        script.append("-- 执行主逻辑\n");
        script.append("main()\n");
        
        // 写入文件
        writeScriptFile(luaScriptPath + "log.lua", script.toString());
    }
    
    /**
     * 将脚本写入文件
     */
    private void writeScriptFile(String filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
}