package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.ProcessDefinition;
import com.taskplan.entity.ProcessDefinitionHistory;
import com.taskplan.entity.ProcessDefinitionVersionDiff;
import com.taskplan.service.ProcessDefinitionService;
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
@RequestMapping("/api/process-definitions")
public class ProcessDefinitionController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @GetMapping
    public Result<Map<String, Object>> listProcesses(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<ProcessDefinition> processPage = processDefinitionService.listProcesses(projectId, name, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", processPage.getContent());
        result.put("total", processPage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/project/{projectId}")
    public Result<List<ProcessDefinition>> listByProject(@PathVariable Long projectId) {
        return Result.success(processDefinitionService.listProcessesByProject(projectId));
    }

    @GetMapping("/project/{projectId}/online")
    public Result<List<ProcessDefinition>> listOnline(@PathVariable Long projectId) {
        return Result.success(processDefinitionService.listOnlineProcesses(projectId));
    }

    @GetMapping("/{id}")
    public Result<ProcessDefinition> getProcess(@PathVariable Long id) {
        return Result.success(processDefinitionService.getProcess(id));
    }

    @PostMapping
    public Result<ProcessDefinition> createProcess(@RequestBody ProcessDefinition process) {
        return Result.success(processDefinitionService.createProcess(process));
    }

    @PostMapping("/design")
    public Result<ProcessDefinition> createProcessWithDesign(@RequestBody Map<String, Object> request) {
        Long id = request.get("id") != null ? Long.parseLong(request.get("id").toString()) : null;
        Long projectId = request.get("projectId") != null ? Long.parseLong(request.get("projectId").toString()) : null;
        String name = (String) request.get("name");
        String releaseState = (String) request.get("releaseState");
        List<Map<String, Object>> taskNodes = (List<Map<String, Object>>) request.get("taskNodes");
        List<Map<String, Object>> connections = (List<Map<String, Object>>) request.get("connections");
        
        return Result.success(processDefinitionService.saveProcessWithNodes(id, projectId, name, releaseState, taskNodes, connections));
    }

    @PutMapping("/{id}")
    public Result<ProcessDefinition> updateProcess(@PathVariable Long id, @RequestBody ProcessDefinition process) {
        return Result.success(processDefinitionService.updateProcess(id, process));
    }

    @PostMapping("/{id}/release")
    public Result<ProcessDefinition> releaseProcess(@PathVariable Long id) {
        return Result.success(processDefinitionService.releaseProcess(id));
    }

    @PostMapping("/{id}/offline")
    public Result<ProcessDefinition> offlineProcess(@PathVariable Long id) {
        return Result.success(processDefinitionService.offlineProcess(id));
    }

    @PostMapping("/{id}/execute")
    public Result<Void> executeProcess(@PathVariable Long id) {
        processDefinitionService.executeProcess(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProcess(@PathVariable Long id) {
        processDefinitionService.deleteProcess(id);
        return Result.success();
    }

    @GetMapping("/{id}/history")
    public Result<List<ProcessDefinitionHistory>> getProcessHistory(@PathVariable Long id) {
        return Result.success(processDefinitionService.getHistoryByProcessId(id));
    }

    @GetMapping("/{id}/history/{version}")
    public Result<ProcessDefinitionHistory> getProcessHistoryByVersion(@PathVariable Long id, @PathVariable Integer version) {
        return Result.success(processDefinitionService.getHistoryByVersion(id, version));
    }

    @GetMapping("/{id}/history/compare")
    public Result<ProcessDefinitionVersionDiff> compareVersions(
            @PathVariable Long id,
            @RequestParam Integer sourceVersion,
            @RequestParam Integer targetVersion) {
        return Result.success(processDefinitionService.compareVersions(id, sourceVersion, targetVersion));
    }

    @GetMapping("/{id}/history/compare-with-current")
    public Result<ProcessDefinitionVersionDiff> compareWithCurrent(
            @PathVariable Long id,
            @RequestParam Integer version) {
        return Result.success(processDefinitionService.compareWithCurrent(id, version));
    }

    @PostMapping("/{id}/history/{version}/rollback")
    public Result<ProcessDefinition> rollbackToVersion(@PathVariable Long id, @PathVariable Integer version) {
        return Result.success(processDefinitionService.rollbackToVersion(id, version));
    }
}
