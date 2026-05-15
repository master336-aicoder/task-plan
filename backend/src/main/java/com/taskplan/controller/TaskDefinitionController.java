package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.TaskDefinition;
import com.taskplan.service.TaskDefinitionService;
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
@RequestMapping("/api/task-definitions")
public class TaskDefinitionController {

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    @GetMapping
    public Result<Map<String, Object>> listTasks(
            @RequestParam Long projectId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<TaskDefinition> taskPage = taskDefinitionService.listTasks(projectId, name, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", taskPage.getContent());
        result.put("total", taskPage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/project/{projectId}")
    public Result<List<TaskDefinition>> listByProject(@PathVariable Long projectId) {
        return Result.success(taskDefinitionService.listTasksByProject(projectId));
    }

    @GetMapping("/{id}")
    public Result<TaskDefinition> getTask(@PathVariable Long id) {
        return Result.success(taskDefinitionService.getTask(id));
    }

    @PostMapping
    public Result<TaskDefinition> createTask(@RequestBody TaskDefinition task) {
        return Result.success(taskDefinitionService.createTask(task));
    }

    @PutMapping("/{id}")
    public Result<TaskDefinition> updateTask(@PathVariable Long id, @RequestBody TaskDefinition task) {
        return Result.success(taskDefinitionService.updateTask(id, task));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        taskDefinitionService.deleteTask(id);
        return Result.success();
    }
}
