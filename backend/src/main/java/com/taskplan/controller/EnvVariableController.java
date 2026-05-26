package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.EnvVariable;
import com.taskplan.service.EnvVariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/env-variables")
@RequiredArgsConstructor
public class EnvVariableController {

    private final EnvVariableService envVariableService;

    @GetMapping
    public Result<Map<String, Object>> listEnvVariables(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<EnvVariable> envPage = envVariableService.listEnvVariables(name, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", envPage.getContent());
        result.put("total", envPage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<EnvVariable>> listAll() {
        return Result.success(envVariableService.listAll());
    }

    @GetMapping("/group/{groupId}")
    public Result<List<EnvVariable>> listByGroupId(@PathVariable Long groupId) {
        return Result.success(envVariableService.listByGroupId(groupId));
    }

    @PostMapping("/groups")
    public Result<List<EnvVariable>> listByGroupIds(@RequestBody List<Long> groupIds) {
        return Result.success(envVariableService.listByGroupIds(groupIds));
    }

    @GetMapping("/{id}")
    public Result<EnvVariable> getEnvVariable(@PathVariable Long id) {
        return Result.success(envVariableService.getEnvVariable(id));
    }

    @PostMapping
    public Result<EnvVariable> createEnvVariable(@RequestBody EnvVariable envVariable) {
        return Result.success(envVariableService.createEnvVariable(envVariable));
    }

    @PostMapping("/batch")
    public Result<List<EnvVariable>> createEnvVariables(@RequestBody List<EnvVariable> envVariables) {
        List<EnvVariable> saved = new ArrayList<>();
        for (EnvVariable envVariable : envVariables) {
            if (envVariable.getId() != null) {
                saved.add(envVariableService.updateEnvVariable(envVariable.getId(), envVariable));
            } else {
                saved.add(envVariableService.createEnvVariable(envVariable));
            }
        }
        return Result.success(saved);
    }

    @PutMapping("/{id}")
    public Result<EnvVariable> updateEnvVariable(@PathVariable Long id, @RequestBody EnvVariable envVariable) {
        return Result.success(envVariableService.updateEnvVariable(id, envVariable));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteEnvVariable(@PathVariable Long id) {
        envVariableService.deleteEnvVariable(id);
        return Result.success();
    }

    @DeleteMapping("/group/{groupId}")
    public Result<Void> deleteByGroupId(@PathVariable Long groupId) {
        envVariableService.deleteByGroupId(groupId);
        return Result.success();
    }
}