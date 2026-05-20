package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.SysMenu;
import com.taskplan.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping
    public Result<List<SysMenu>> listMenus() {
        return Result.success(sysMenuService.listMenus());
    }

    @GetMapping("/tree")
    public Result<List<SysMenu>> getMenuTree() {
        return Result.success(sysMenuService.getMenuTree());
    }

    @GetMapping("/{id}")
    public Result<SysMenu> getMenu(@PathVariable Long id) {
        return Result.success(sysMenuService.getMenuById(id));
    }

    @PostMapping
    public Result<SysMenu> createMenu(@RequestBody SysMenu menu) {
        return Result.success(sysMenuService.createMenu(menu));
    }

    @PutMapping("/{id}")
    public Result<SysMenu> updateMenu(@PathVariable Long id, @RequestBody SysMenu menu) {
        return Result.success(sysMenuService.updateMenu(id, menu));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        sysMenuService.deleteMenu(id);
        return Result.success();
    }
}
