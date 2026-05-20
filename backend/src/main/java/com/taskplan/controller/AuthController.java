package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.SysUser;
import com.taskplan.entity.SysRole;
import com.taskplan.service.AuthService;
import com.taskplan.service.SysUserService;
import com.taskplan.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        if (username == null || password == null) {
            return Result.error("用户名或密码不能为空");
        }
        
        Map<String, Object> result = authService.login(username, password);
        return Result.success(result);
    }

    @GetMapping("/userinfo")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            String username = jwtUtils.getUsernameFromToken(token);
            SysUser user = sysUserService.getUserByUsername(username);
            List<SysRole> roles = sysUserService.getUserRoles(user.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("email", user.getEmail());
            result.put("phone", user.getPhone());
            result.put("avatar", user.getAvatar());
            result.put("roles", roles);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取用户信息失败");
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @PostMapping("/register")
    public Result<SysUser> register(@RequestBody SysUser user) {
        SysUser created = sysUserService.createUser(user);
        created.setPassword(null);
        return Result.success(created);
    }
}
