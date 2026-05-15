package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.ProcessInstance;
import com.taskplan.service.ProcessInstanceService;
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
@RequestMapping("/api/process-instances")
public class ProcessInstanceController {

    @Autowired
    private ProcessInstanceService processInstanceService;

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
    public Result<ProcessInstance> getInstance(@PathVariable Long id) {
        return Result.success(processInstanceService.getInstance(id));
    }

    @PostMapping("/start")
    public Result<ProcessInstance> startProcess(@RequestParam Long processDefinitionId) {
        return Result.success(processInstanceService.startProcess(processDefinitionId, "MANUAL_START"));
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
}
