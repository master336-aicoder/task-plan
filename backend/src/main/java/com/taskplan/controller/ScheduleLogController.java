package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.ScheduleLog;
import com.taskplan.service.ScheduleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule-logs")
public class ScheduleLogController {

    @Autowired
    private ScheduleLogService scheduleLogService;

    @GetMapping
    public Result<Map<String, Object>> listLogs(
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Long processDefinitionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "logTime"));
        Page<ScheduleLog> logPage = scheduleLogService.listLogs(scheduleId, processDefinitionId, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", logPage.getContent());
        result.put("total", logPage.getTotalElements());
        return Result.success(result);
    }
}