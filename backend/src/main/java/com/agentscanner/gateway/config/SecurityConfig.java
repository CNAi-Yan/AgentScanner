package com.agentscanner.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security配置类
 * 用于配置Spring Security，允许登录接口的访问
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护，因为我们使用的是JWT令牌
                .csrf(AbstractHttpConfigurer::disable)
                // 允许跨域请求
                .cors(AbstractHttpConfigurer::disable)
                // 配置请求授权规则
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // 允许所有请求访问，实际项目中应该根据需求配置更严格的规则
                                .anyRequest().permitAll()
                )
                // 禁用默认的登录表单
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用默认的HTTP Basic认证
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}