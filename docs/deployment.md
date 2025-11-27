# AgentScanner 服务部署手册

## 1. 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| Node.js | 16+ | 前端运行环境 |
| npm | 8+ | 前端包管理工具 |
| MySQL | 8.0+ | 数据库 |
| Nginx | 1.20+ | 反向代理服务器 |

## 2. 后端部署

### 2.1 配置数据库

1. 创建数据库：
   ```sql
   CREATE DATABASE agent_scanner CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 初始化数据库：
   ```bash
   # 执行初始化脚本
   mysql -u root -p agent_scanner < backend/src/main/resources/db/init.sql
   ```

### 2.2 配置文件修改

编辑 `backend/src/main/resources/application.yml` 文件，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agent_scanner?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 2.3 构建和运行

1. 进入后端目录：
   ```bash
   cd backend
   ```

2. 构建项目：
   ```bash
   mvn clean package -DskipTests
   ```

3. 运行项目：
   ```bash
   java -jar target/gateway-1.0.0.jar
   ```

   或使用Spring Boot Maven插件运行：
   ```bash
   mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

## 3. 前端部署

### 3.1 安装依赖

进入前端目录并安装依赖：

```bash
cd frontend
npm install
```

### 3.2 构建项目

```bash
npm run build
```

构建完成后，生成的静态文件将位于 `dist` 目录。

## 4. Nginx配置

### 4.1 配置文件修改

编辑 `nginx/conf/nginx.conf` 文件，确保配置正确：

```nginx
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    keepalive_timeout  65;

    # 前端静态资源配置
    server {
        listen       80;
        server_name  localhost;

        location / {
            root   /path/to/frontend/dist;
            index  index.html index.htm;
            try_files $uri $uri/ /index.html;
        }

        # 后端API代理
        location /api {
            proxy_pass http://localhost:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```

### 4.2 启动Nginx

```bash
# 进入nginx目录
cd nginx

# 启动nginx
./nginx

# 重新加载配置
./nginx -s reload

# 停止nginx
./nginx -s stop
```

## 5. 启动顺序

1. 启动MySQL数据库
2. 启动后端服务
3. 启动Nginx服务
4. 访问前端页面：http://localhost

## 6. 常用命令

### 6.1 后端

```bash
# 查看日志
tail -f logs/agent-scanner.log

# 重启服务
# 先停止当前服务，再启动
```

### 6.2 前端

```bash
# 开发模式运行
npm run dev

# 预览构建结果
npm run preview
```

## 7. 注意事项

1. 确保防火墙已开放相关端口（80、8080、3306等）
2. 生产环境建议使用HTTPS协议
3. 定期备份数据库
4. 监控系统运行状态，及时处理异常
5. 定期更新依赖包，修复安全漏洞
