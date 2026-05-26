package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.TaskExecutionLog;
import com.taskplan.service.TaskExecutionLogService;
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
@RequestMapping("/api/task-execution-logs")
public class TaskExecutionLogController {

    @Autowired
    private TaskExecutionLogService taskExecutionLogService;

    @GetMapping("/{id}")
    public Result<TaskExecutionLog> getLog(@PathVariable Long id) {
        return Result.success(taskExecutionLogService.getLogById(id));
    }

    @GetMapping("/process/{processInstanceId}")
    public Result<List<TaskExecutionLog>> getLogsByProcessInstance(@PathVariable Long processInstanceId) {
        return Result.success(taskExecutionLogService.getLogsByProcessInstanceId(processInstanceId));
    }

    @GetMapping
    public Result<Map<String, Object>> listLogs(
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serverIp,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TaskExecutionLog> logPage = taskExecutionLogService.getLogsByConditions(
                processInstanceId, taskType, status, serverIp, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", logPage.getContent());
        result.put("total", logPage.getTotalElements());
        
        return Result.success(result);
    }
}