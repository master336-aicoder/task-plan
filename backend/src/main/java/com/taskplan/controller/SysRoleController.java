package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.SysMenu;
import com.taskplan.entity.SysRole;
import com.taskplan.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping
    public Result<List<SysRole>> listRoles() {
        return Result.success(sysRoleService.listRoles());
    }

    @GetMapping("/active")
    public Result<List<SysRole>> listActiveRoles() {
        return Result.success(sysRoleService.listActiveRoles());
    }

    @GetMapping("/{id}")
    public Result<SysRole> getRole(@PathVariable Long id) {
        return Result.success(sysRoleService.getRoleById(id));
    }

    @PostMapping
    public Result<SysRole> createRole(@RequestBody SysRole role) {
        return Result.success(sysRoleService.createRole(role));
    }

    @PutMapping("/{id}")
    public Result<SysRole> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        return Result.success(sysRoleService.updateRole(id, role));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    @GetMapping("/{id}/menus")
    public Result<List<SysMenu>> getRoleMenus(@PathVariable Long id) {
        return Result.success(sysRoleService.getRoleMenus(id));
    }

    @PostMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        sysRoleService.assignMenus(id, menuIds);
        return Result.success();
    }
}
