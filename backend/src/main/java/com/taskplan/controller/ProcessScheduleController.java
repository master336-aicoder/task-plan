package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.ProcessSchedule;
import com.taskplan.service.ProcessScheduleService;
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
@RequestMapping("/api/process-schedules")
public class ProcessScheduleController {

    @Autowired
    private ProcessScheduleService processScheduleService;

    @GetMapping
    public Result<Map<String, Object>> listSchedules(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long processDefinitionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<ProcessSchedule> schedulePage = processScheduleService.listSchedules(projectId, processDefinitionId, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", schedulePage.getContent());
        result.put("total", schedulePage.getTotalElements());
        return Result.success(result);
    }

    @GetMapping("/process/{processDefinitionId}")
    public Result<List<ProcessSchedule>> listByProcess(@PathVariable Long processDefinitionId) {
        return Result.success(processScheduleService.listSchedulesByProcess(processDefinitionId));
    }

    @GetMapping("/{id}")
    public Result<ProcessSchedule> getSchedule(@PathVariable Long id) {
        return Result.success(processScheduleService.getSchedule(id));
    }

    @PostMapping
    public Result<ProcessSchedule> createSchedule(@RequestBody ProcessSchedule schedule) {
        return Result.success(processScheduleService.createSchedule(schedule));
    }

    @PutMapping("/{id}")
    public Result<ProcessSchedule> updateSchedule(@PathVariable Long id, @RequestBody ProcessSchedule schedule) {
        return Result.success(processScheduleService.updateSchedule(id, schedule));
    }

    @PostMapping("/{id}/enable")
    public Result<ProcessSchedule> enableSchedule(@PathVariable Long id) {
        return Result.success(processScheduleService.enableSchedule(id));
    }

    @PostMapping("/{id}/disable")
    public Result<ProcessSchedule> disableSchedule(@PathVariable Long id) {
        return Result.success(processScheduleService.disableSchedule(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        processScheduleService.deleteSchedule(id);
        return Result.success();
    }
}
