package com.agentscanner.gateway.nginx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Nginx命令执行器
 * 用于执行Nginx相关命令，如重载配置、测试配置等
 */
@Component
public class NginxCommandExecutor {
    
    @Value("${nginx.reload-command}")
    private String reloadCommand;
    
    /**
     * 重载Nginx配置
     * @return 执行结果
     */
    public String reloadNginx() throws IOException, InterruptedException {
        return executeCommand(reloadCommand);
    }
    
    /**
     * 测试Nginx配置
     * @param confPath 配置文件路径
     * @return 执行结果
     */
    public String testConfig(String confPath) throws IOException, InterruptedException {
        // 提取Nginx可执行文件路径
        String nginxPath = reloadCommand.split(" ")[0];
        String testCommand = nginxPath + " -t -c " + confPath;
        return executeCommand(testCommand);
    }
    
    /**
     * 启动Nginx
     * @return 执行结果
     */
    public String startNginx() throws IOException, InterruptedException {
        // 提取Nginx可执行文件路径
        String nginxPath = reloadCommand.split(" ")[0];
        String startCommand = nginxPath;
        return executeCommand(startCommand);
    }
    
    /**
     * 停止Nginx
     * @return 执行结果
     */
    public String stopNginx() throws IOException, InterruptedException {
        // 提取Nginx可执行文件路径
        String nginxPath = reloadCommand.split(" ")[0];
        String stopCommand = nginxPath + " -s stop";
        return executeCommand(stopCommand);
    }
    
    /**
     * 执行系统命令
     * @param command 命令字符串
     * @return 命令执行结果
     */
    private String executeCommand(String command) throws IOException, InterruptedException {
        StringBuilder result = new StringBuilder();
        
        // 执行命令
        Process process = Runtime.getRuntime().exec(command);
        
        // 读取标准输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        
        // 读取错误输出
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        
        // 等待命令执行完成
        int exitCode = process.waitFor();
        result.append("Exit Code: ").append(exitCode).append("\n");
        
        return result.toString();
    }
}