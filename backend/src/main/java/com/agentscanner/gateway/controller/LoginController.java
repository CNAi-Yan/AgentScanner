package com.agentscanner.gateway.controller;

import com.agentscanner.gateway.dto.LoginDTO;
import com.agentscanner.gateway.vo.LoginVO;
import com.agentscanner.gateway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Tag(name = "登录管理", description = "登录相关接口")
public class LoginController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录", description = "用户登录接口")
    @PostMapping
    public ResponseEntity<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return ResponseEntity.ok(loginVO);
    }
}