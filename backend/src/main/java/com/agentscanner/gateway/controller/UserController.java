package com.agentscanner.gateway.controller;

import com.agentscanner.gateway.service.UserService;
import com.agentscanner.gateway.dto.UserDTO;
import com.agentscanner.gateway.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "创建用户", description = "创建新的用户")
    @PostMapping
    public ResponseEntity<UserVO> createUser(@RequestBody UserDTO userDTO) {
        UserVO userVO = userService.createUser(userDTO);
        return ResponseEntity.ok(userVO);
    }

    @Operation(summary = "更新用户", description = "更新指定ID的用户信息")
    @PutMapping("/{id}")
    public ResponseEntity<UserVO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserVO userVO = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(userVO);
    }

    @Operation(summary = "删除用户", description = "删除指定ID的用户")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable Long id) {
        boolean result = userService.deleteUser(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取用户详情", description = "获取指定ID的用户详情")
    @GetMapping("/{id}")
    public ResponseEntity<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return ResponseEntity.ok(userVO);
    }

    @Operation(summary = "获取用户列表", description = "获取用户列表，支持分页和筛选")
    @GetMapping
    public ResponseEntity<List<UserVO>> getUserList(@RequestParam Map<String, Object> params) {
        List<UserVO> userList = userService.getUserList(params);
        return ResponseEntity.ok(userList);
    }

    @Operation(summary = "更新用户状态", description = "更新指定ID的用户状态")
    @PutMapping("/{id}/status")
    public ResponseEntity<Boolean> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean result = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "重置用户密码", description = "重置指定ID的用户密码")
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Boolean> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        boolean result = userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "更新用户密码", description = "更新指定ID的用户密码")
    @PutMapping("/{id}/password")
    public ResponseEntity<Boolean> updatePassword(@PathVariable Long id, @RequestParam String oldPassword, @RequestParam String newPassword) {
        boolean result = userService.updatePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "统计用户数量", description = "获取用户总数量")
    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        Long count = userService.countUsers();
        return ResponseEntity.ok(count);
    }
}