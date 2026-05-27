package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.ScheduleLog;
import com.taskplan.repository.ScheduleLogRepository;
import com.taskplan.service.ScheduleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedule-logs")
public class ScheduleLogController {

    @Autowired
    private ScheduleLogService scheduleLogService;

    @GetMapping
    public Result<Map<String, Object>> listLogs(
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Long processDefinitionId,
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "logTime"));
        Page<ScheduleLog> logPage = scheduleLogService.listLogs(scheduleId, processDefinitionId, processInstanceId, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", logPage.getContent());
        result.put("total", logPage.getTotalElements());
        return Result.success(result);
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportSelectedLogs(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        List<ScheduleLog> logs = scheduleLogRepository.findAllById(ids);
        String csv = convertToCsv(logs);
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "schedule-logs-selected.csv");
        
        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @GetMapping("/export-all")
    public ResponseEntity<byte[]> exportAllLogs(
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Long processDefinitionId,
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(required = false) String logLevel) {
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "logTime"));
        Page<ScheduleLog> logPage = scheduleLogService.listLogs(scheduleId, processDefinitionId, processInstanceId, pageable);
        
        if (logLevel != null && !logLevel.isEmpty()) {
            logPage = scheduleLogService.listLogsByLevel(logLevel, pageable);
        }
        
        String csv = convertToCsv(logPage.getContent());
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "schedule-logs-all.csv");
        
        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        scheduleLogRepository.deleteAllById(ids);
        return Result.success();
    }

    @DeleteMapping("/all")
    public Result<Void> deleteAll(
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) Long processDefinitionId,
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(required = false) String logLevel) {
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<ScheduleLog> logPage = scheduleLogService.listLogs(scheduleId, processDefinitionId, processInstanceId, pageable);
        
        if (logLevel != null && !logLevel.isEmpty()) {
            logPage = scheduleLogService.listLogsByLevel(logLevel, pageable);
        }
        
        List<Long> ids = logPage.getContent().stream().map(ScheduleLog::getId).collect(Collectors.toList());
        scheduleLogRepository.deleteAllById(ids);
        return Result.success();
    }

    private String convertToCsv(List<ScheduleLog> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,调度ID,工作流ID,实例ID,任务节点ID,任务名称,日志级别,日志内容,时间\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (ScheduleLog log : logs) {
            sb.append(log.getId()).append(",");
            sb.append(log.getScheduleId() != null ? log.getScheduleId() : "").append(",");
            sb.append(log.getProcessDefinitionId() != null ? log.getProcessDefinitionId() : "").append(",");
            sb.append(log.getProcessInstanceId() != null ? log.getProcessInstanceId() : "").append(",");
            sb.append(escapeCsv(log.getTaskNodeId())).append(",");
            sb.append(escapeCsv(log.getTaskName())).append(",");
            sb.append(log.getLogLevel()).append(",");
            sb.append(escapeCsv(log.getMessage())).append(",");
            sb.append(log.getLogTime() != null ? log.getLogTime().format(formatter) : "").append("\n");
        }
        
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Autowired
    private ScheduleLogRepository scheduleLogRepository;
}