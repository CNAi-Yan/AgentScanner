-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS agent_scanner DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE agent_scanner;

-- 创建agent表（智能体信息）
CREATE TABLE IF NOT EXISTS agent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '智能体名称',
    type VARCHAR(50) NOT NULL COMMENT '智能体类型',
    description TEXT COMMENT '智能体描述',
    api_key VARCHAR(255) NOT NULL COMMENT 'API密钥',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    group_name VARCHAR(100) COMMENT '所属分组',
    allowed_ips TEXT COMMENT '允许的IP列表',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_online_time DATETIME COMMENT '最后在线时间',
    total_requests BIGINT NOT NULL DEFAULT 0 COMMENT '总请求数',
    today_requests BIGINT NOT NULL DEFAULT 0 COMMENT '今日请求数',
    UNIQUE KEY uk_api_key (api_key),
    INDEX idx_name (name),
    INDEX idx_group_name (group_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体信息表';

-- 创建log表（日志信息）
CREATE TABLE IF NOT EXISTS log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    type INT NOT NULL COMMENT '日志类型：1-访问日志，2-审计日志',
    agent_id BIGINT COMMENT '智能体ID',
    agent_name VARCHAR(100) COMMENT '智能体名称',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    request_url VARCHAR(255) COMMENT '请求URL',
    http_method VARCHAR(20) COMMENT 'HTTP方法',
    request_params TEXT COMMENT '请求参数',
    request_headers TEXT COMMENT '请求头',
    response_status INT COMMENT '响应状态码',
    response_time BIGINT COMMENT '响应时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    operator VARCHAR(50) COMMENT '操作人',
    operation_content TEXT COMMENT '操作内容',
    log_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
    INDEX idx_type (type),
    INDEX idx_agent_id (agent_id),
    INDEX idx_log_time (log_time),
    INDEX idx_client_ip (client_ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日志信息表';

-- 创建policy表（策略信息）
CREATE TABLE IF NOT EXISTS policy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '策略名称',
    type INT NOT NULL COMMENT '策略类型：1-访问控制，2-流量控制，3-内容过滤',
    description TEXT COMMENT '策略描述',
    content TEXT NOT NULL COMMENT '策略内容（JSON格式）',
    priority INT NOT NULL DEFAULT 100 COMMENT '优先级：数字越小优先级越高',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    applicable_groups TEXT COMMENT '适用智能体分组',
    applicable_agents TEXT COMMENT '适用智能体ID列表',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='策略信息表';

-- 创建user表（用户信息）
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL COMMENT '角色：admin-管理员，operator-操作员',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    UNIQUE KEY uk_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 插入初始数据
-- 插入管理员用户
INSERT INTO user (username, password, role, status) VALUES ('admin', '$2a$10$7J6K5L4M3N2O1P0Q9R8S7T6U5V4W3X2Y1Z0A9B8C7D6E5F4G3H2I1J', 'admin', 1) ON DUPLICATE KEY UPDATE username=username;

-- 插入默认策略
INSERT INTO policy (name, type, description, content, priority, status) VALUES 
('默认访问控制策略', 1, '允许所有IP访问', '{"allowAll": true}', 100, 1) ON DUPLICATE KEY UPDATE name=name;
