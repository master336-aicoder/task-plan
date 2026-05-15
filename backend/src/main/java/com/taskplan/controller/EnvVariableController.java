package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.EnvVariable;
import com.taskplan.service.EnvVariableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/env-variables")
public class EnvVariableController {

    @Autowired
    private EnvVariableService envVariableService;

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

    @GetMapping("/category/{category}")
    public Result<List<EnvVariable>> listByCategory(@PathVariable String category) {
        return Result.success(envVariableService.listByCategory(category));
    }

    @GetMapping("/{id}")
    public Result<EnvVariable> getEnvVariable(@PathVariable Long id) {
        return Result.success(envVariableService.getEnvVariable(id));
    }

    @PostMapping
    public Result<EnvVariable> createEnvVariable(@RequestBody EnvVariable envVariable) {
        return Result.success(envVariableService.createEnvVariable(envVariable));
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
}
