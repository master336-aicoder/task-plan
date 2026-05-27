package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.TaskExecutionLog;
import com.taskplan.repository.TaskExecutionLogRepository;
import com.taskplan.service.TaskExecutionLogService;
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

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportSelectedLogs(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        List<TaskExecutionLog> logs = taskExecutionLogRepository.findAllById(ids);
        String csv = convertToCsv(logs);
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "task-execution-logs-selected.csv");
        
        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @GetMapping("/export-all")
    public ResponseEntity<byte[]> exportAllLogs(
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serverIp) {
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<TaskExecutionLog> logPage = taskExecutionLogService.getLogsByConditions(
                processInstanceId, taskType, status, serverIp, pageable);
        String csv = convertToCsv(logPage.getContent());
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "task-execution-logs-all.csv");
        
        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        taskExecutionLogRepository.deleteAllById(ids);
        return Result.success();
    }

    @DeleteMapping("/all")
    public Result<Void> deleteAll(
            @RequestParam(required = false) Long processInstanceId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String serverIp) {
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<TaskExecutionLog> logPage = taskExecutionLogService.getLogsByConditions(
                processInstanceId, taskType, status, serverIp, pageable);
        List<Long> ids = logPage.getContent().stream().map(TaskExecutionLog::getId).collect(Collectors.toList());
        taskExecutionLogRepository.deleteAllById(ids);
        return Result.success();
    }

    private String convertToCsv(List<TaskExecutionLog> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,工作流实例ID,任务节点ID,任务名称,任务类型,执行服务器,执行状态,执行时间,输出内容,错误信息\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (TaskExecutionLog log : logs) {
            sb.append(log.getId()).append(",");
            sb.append(log.getProcessInstanceId() != null ? log.getProcessInstanceId() : "").append(",");
            sb.append(escapeCsv(log.getTaskNodeId())).append(",");
            sb.append(escapeCsv(log.getTaskName())).append(",");
            sb.append(log.getTaskType()).append(",");
            sb.append(log.getServerIp()).append(",");
            sb.append(log.getStatus()).append(",");
            sb.append(log.getCreateTime() != null ? log.getCreateTime().format(formatter) : "").append(",");
            sb.append(escapeCsv(log.getOutput())).append(",");
            sb.append(escapeCsv(log.getError())).append("\n");
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
    private TaskExecutionLogRepository taskExecutionLogRepository;
}