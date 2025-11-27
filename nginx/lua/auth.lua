-- HTTP认证Lua脚本
local cjson = require "cjson"
local redis = require "resty.redis"

-- 连接Redis
local function connect_redis()
    local red = redis:new()
    red:set_timeout(1000) -- 1秒超时
    
    local ok, err = red:connect("127.0.0.1", 6379)
    if not ok then
        ngx.log(ngx.ERR, "Failed to connect to Redis: ", err)
        return nil, err
    end
    
    return red, nil
end

-- 从Redis获取智能体信息
local function get_agent_from_redis(api_key)
    local red, err = connect_redis()
    if not red then
        return nil, err
    end
    
    local agent_json, err = red:get("agent:" .. api_key)
    if not agent_json then
        red:close()
        return nil, err
    end
    
    if agent_json == ngx.null then
        red:close()
        return nil, "Agent not found"
    end
    
    red:close()
    return cjson.decode(agent_json), nil
end

-- 从缓存获取智能体信息
local function get_agent(api_key)
    -- 先从共享内存缓存获取
    local agent_cache = ngx.shared.agent_cache
    local agent_json = agent_cache:get(api_key)
    
    if agent_json then
        return cjson.decode(agent_json), nil
    end
    
    -- 缓存不存在，从Redis获取
    local agent, err = get_agent_from_redis(api_key)
    if not agent then
        return nil, err
    end
    
    -- 存入缓存，有效期5分钟
    agent_cache:set(api_key, cjson.encode(agent), 300)
    
    return agent, nil
end

-- 主认证逻辑
local function main()
    -- 从请求头获取API密钥
    local api_key = ngx.req.get_headers()["X-Api-Key"]
    if not api_key then
        -- 从查询参数获取API密钥
        api_key = ngx.var.arg_api_key
    end
    
    if not api_key then
        ngx.status = ngx.HTTP_UNAUTHORIZED
        ngx.header["Content-Type"] = "application/json"
        ngx.say(cjson.encode({"code": 401, "message": "Missing API key"}))
        ngx.exit(ngx.HTTP_UNAUTHORIZED)
        return
    end
    
    -- 获取智能体信息
    local agent, err = get_agent(api_key)
    if not agent then
        ngx.status = ngx.HTTP_UNAUTHORIZED
        ngx.header["Content-Type"] = "application/json"
        ngx.say(cjson.encode({"code": 401, "message": "Invalid API key"}))
        ngx.exit(ngx.HTTP_UNAUTHORIZED)
        return
    end
    
    -- 检查智能体状态
    if agent.status ~= 1 then
        ngx.status = ngx.HTTP_FORBIDDEN
        ngx.header["Content-Type"] = "application/json"
        ngx.say(cjson.encode({"code": 403, "message": "Agent is disabled"}))
        ngx.exit(ngx.HTTP_FORBIDDEN)
        return
    end
    
    -- 检查IP白名单
    if agent.allowed_ips and agent.allowed_ips ~= "" then
        local allowed_ips = {}  -- 解析allowed_ips字符串为表
        for ip in string.gmatch(agent.allowed_ips, "[^,]+%") do
            allowed_ips[ip:match("^%s*(.-)%s*$")] = true
        end
        
        local client_ip = ngx.var.remote_addr
        if not allowed_ips[client_ip] then
            ngx.status = ngx.HTTP_FORBIDDEN
            ngx.header["Content-Type"] = "application/json"
            ngx.say(cjson.encode({"code": 403, "message": "IP not allowed"}))
            ngx.exit(ngx.HTTP_FORBIDDEN)
            return
        end
    end
    
    -- 保存智能体信息到ngx.ctx
    ngx.ctx.agent = agent
    
    -- 解析目标URL（示例：从请求路径中解析）
    -- 实际使用中，可能需要根据配置或数据库中的映射关系来确定
    local target_url = ngx.var.uri:match("^/proxy/(.*)$")
    if not target_url then
        ngx.status = ngx.HTTP_BAD_REQUEST
        ngx.header["Content-Type"] = "application/json"
        ngx.say(cjson.encode({"code": 400, "message": "Invalid target URL"}))
        ngx.exit(ngx.HTTP_BAD_REQUEST)
        return
    end
    
    -- 设置上游URL
    ngx.var.upstream_url = "http://" .. target_url
end

-- 执行主逻辑
main()