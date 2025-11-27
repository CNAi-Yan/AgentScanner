package com.agentscanner.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS配置类
 * 用于解决跨域问题，允许前端应用从不同的域访问后端API
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有路径都支持跨域请求
        registry.addMapping("/**")
                // 允许的请求来源，使用allowedOriginPatterns代替allowedOrigins以支持通配符和credentials
                .allowedOriginPatterns("*")
                // 允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 是否允许携带凭证（如cookie）
                .allowCredentials(true)
                // 预检请求的缓存时间，单位为秒
                .maxAge(3600);
    }
}