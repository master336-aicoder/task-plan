package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.ProcessInstance;
import com.taskplan.service.ProcessInstanceService;
import com.taskplan.service.ScheduleExecutorService;
import com.taskplan.service.TaskExecutionLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/process-instances")
public class ProcessInstanceController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private ScheduleExecutorService scheduleExecutorService;

    @Autowired
    private TaskExecutionLogService taskExecutionLogService;

    @GetMapping
    public Result<Map<String, Object>> listInstances(
            @RequestParam Long projectId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Long processDefinitionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "startTime"));
        Page<ProcessInstance> instancePage = processInstanceService.listInstances(projectId, state, processDefinitionId, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", instancePage.getContent());
        result.put("total", instancePage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getInstance(@PathVariable Long id) {
        ProcessInstance instance = processInstanceService.getInstance(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", instance.getId());
        result.put("projectId", instance.getProjectId());
        result.put("processDefinitionId", instance.getProcessDefinitionId());
        result.put("processDefinitionName", instance.getProcessName());
        result.put("state", instance.getState());
        result.put("startTime", instance.getStartTime());
        result.put("endTime", instance.getEndTime());
        result.put("duration", instance.getDuration());
        result.put("owner", instance.getOwner());
        result.put("createTime", instance.getCreateTime());
        
        List<Map<String, Object>> taskInstances = taskExecutionLogService.getLogsByProcessInstanceId(id)
                .stream()
                .map(log -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("taskNodeId", log.getTaskNodeId());
                    taskMap.put("taskName", log.getTaskName());
                    taskMap.put("taskType", log.getTaskType());
                    taskMap.put("serverIp", log.getServerIp());
                    taskMap.put("state", log.getStatus());
                    taskMap.put("startTime", log.getCreateTime());
                    taskMap.put("endTime", log.getCreateTime());
                    taskMap.put("log", log.getOutput());
                    taskMap.put("error", log.getError());
                    return taskMap;
                })
                .collect(java.util.stream.Collectors.toList());
        
        result.put("taskInstances", taskInstances);
        
        return Result.success(result);
    }

    @PostMapping("/start")
    public Result<ProcessInstance> startProcess(@RequestParam Long processDefinitionId) {
        ProcessInstance instance = processInstanceService.startProcess(processDefinitionId, "MANUAL_START");
        
        new Thread(() -> {
            try {
                scheduleExecutorService.executeProcessWithInstance(processDefinitionId, instance.getId());
                log.info("工作流异步执行完成: processDefinitionId={}", processDefinitionId);
            } catch (Exception e) {
                log.error("工作流异步执行失败: processDefinitionId={}, error={}", processDefinitionId, e.getMessage());
            }
        }).start();
        
        return Result.success(instance);
    }

    @PostMapping("/{id}/stop")
    public Result<ProcessInstance> stopProcess(@PathVariable Long id) {
        return Result.success(processInstanceService.endProcess(id, "STOP"));
    }

    @PostMapping("/{id}/success")
    public Result<ProcessInstance> successProcess(@PathVariable Long id) {
        return Result.success(processInstanceService.endProcess(id, "SUCCESS"));
    }

    @PostMapping("/{id}/fail")
    public Result<ProcessInstance> failProcess(@PathVariable Long id) {
        return Result.success(processInstanceService.endProcess(id, "FAILURE"));
    }

    @PostMapping("/{id}/retry")
    public Result<ProcessInstance> retryProcess(@PathVariable Long id) {
        ProcessInstance instance = processInstanceService.retryProcess(id);
        
        new Thread(() -> {
            try {
                scheduleExecutorService.executeProcessWithInstance(instance.getProcessDefinitionId(), instance.getId());
                log.info("工作流重试执行完成: processDefinitionId={}", instance.getProcessDefinitionId());
            } catch (Exception e) {
                log.error("工作流重试执行失败: processDefinitionId={}, error={}", instance.getProcessDefinitionId(), e.getMessage());
            }
        }).start();
        
        return Result.success(instance);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteInstance(@PathVariable Long id) {
        processInstanceService.deleteInstance(id);
        return Result.success();
    }
}
