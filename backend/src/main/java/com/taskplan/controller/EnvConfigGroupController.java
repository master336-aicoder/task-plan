package com.taskplan.controller;

import com.taskplan.entity.EnvConfigGroup;
import com.taskplan.service.EnvConfigGroupService;
import com.taskplan.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/env-config-groups")
@RequiredArgsConstructor
public class EnvConfigGroupController {

    private final EnvConfigGroupService envConfigGroupService;

    @GetMapping
    public Result<List<EnvConfigGroup>> list() {
        return Result.success(envConfigGroupService.listAll());
    }

    @GetMapping("/{id}")
    public Result<EnvConfigGroup> get(@PathVariable Long id) {
        return envConfigGroupService.findById(id)
                .map(Result::success)
                .orElse(Result.error("配置组不存在"));
    }

    @PostMapping
    public Result<EnvConfigGroup> create(@RequestBody EnvConfigGroup group) {
        return Result.success(envConfigGroupService.create(group));
    }

    @PutMapping("/{id}")
    public Result<EnvConfigGroup> update(@PathVariable Long id, @RequestBody EnvConfigGroup group) {
        return Result.success(envConfigGroupService.update(id, group));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        envConfigGroupService.delete(id);
        return Result.success();
    }
}