package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.TaskInstance;
import com.taskplan.service.TaskInstanceService;
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
@RequestMapping("/api/task-instances")
public class TaskInstanceController {

    @Autowired
    private TaskInstanceService taskInstanceService;

    @GetMapping
    public Result<Map<String, Object>> listTaskInstances(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<TaskInstance> taskPage = taskInstanceService.listTaskInstances(projectId, processInstanceId, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", taskPage.getContent());
        result.put("total", taskPage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/process/{processInstanceId}")
    public Result<List<TaskInstance>> listByProcess(@PathVariable Long processInstanceId) {
        return Result.success(taskInstanceService.listByProcessInstance(processInstanceId));
    }

    @GetMapping("/{id}")
    public Result<TaskInstance> getTaskInstance(@PathVariable Long id) {
        return Result.success(taskInstanceService.getInstance(id));
    }

    @PostMapping
    public Result<TaskInstance> createTaskInstance(
            @RequestParam Long processInstanceId,
            @RequestParam Long taskDefinitionId) {
        return Result.success(taskInstanceService.createTaskInstance(processInstanceId, taskDefinitionId));
    }

    @PostMapping("/{id}/start")
    public Result<TaskInstance> startTask(@PathVariable Long id) {
        return Result.success(taskInstanceService.startTask(id));
    }

    @PostMapping("/{id}/complete")
    public Result<TaskInstance> completeTask(
            @PathVariable Long id,
            @RequestParam String state,
            @RequestParam(required = false) String errorMsg) {
        return Result.success(taskInstanceService.completeTask(id, state, errorMsg));
    }

    @PostMapping("/{id}/retry")
    public Result<TaskInstance> retryTask(@PathVariable Long id) {
        return Result.success(taskInstanceService.retryTask(id));
    }
}
