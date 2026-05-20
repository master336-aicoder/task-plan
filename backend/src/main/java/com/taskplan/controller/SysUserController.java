package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.SysUser;
import com.taskplan.entity.SysRole;
import com.taskplan.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping
    public Result<List<SysUser>> listUsers() {
        List<SysUser> users = sysUserService.listUsers();
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getUser(@PathVariable Long id) {
        SysUser user = sysUserService.getUserById(id);
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping
    public Result<SysUser> createUser(@RequestBody SysUser user) {
        SysUser created = sysUserService.createUser(user);
        created.setPassword(null);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        SysUser updated = sysUserService.updateUser(id, user);
        updated.setPassword(null);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    public Result<List<SysRole>> getUserRoles(@PathVariable Long id) {
        return Result.success(sysUserService.getUserRoles(id));
    }

    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        sysUserService.assignRoles(id, roleIds);
        return Result.success();
    }
}
