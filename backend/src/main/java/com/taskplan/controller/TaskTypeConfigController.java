package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.TaskTypeConfig;
import com.taskplan.service.TaskTypeConfigService;
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
@RequestMapping("/api/task-types")
public class TaskTypeConfigController {

    @Autowired
    private TaskTypeConfigService taskTypeConfigService;

    @GetMapping
    public Result<Map<String, Object>> listConfigs(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TaskTypeConfig> configPage = taskTypeConfigService.listConfigs(name, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", configPage.getContent());
        result.put("total", configPage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/all")
    public Result<List<TaskTypeConfig>> listAll() {
        return Result.success(taskTypeConfigService.listAll());
    }

    @GetMapping("/task-type/{taskType}")
    public Result<List<TaskTypeConfig>> listByTaskType(@PathVariable String taskType) {
        return Result.success(taskTypeConfigService.listByTaskType(taskType));
    }

    @GetMapping("/{id}")
    public Result<TaskTypeConfig> getConfig(@PathVariable Long id) {
        return Result.success(taskTypeConfigService.getConfig(id));
    }

    @PostMapping
    public Result<TaskTypeConfig> createConfig(@RequestBody TaskTypeConfig config) {
        return Result.success(taskTypeConfigService.createConfig(config));
    }

    @PutMapping("/{id}")
    public Result<TaskTypeConfig> updateConfig(@PathVariable Long id, @RequestBody TaskTypeConfig config) {
        return Result.success(taskTypeConfigService.updateConfig(id, config));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteConfig(@PathVariable Long id) {
        taskTypeConfigService.deleteConfig(id);
        return Result.success();
    }
}