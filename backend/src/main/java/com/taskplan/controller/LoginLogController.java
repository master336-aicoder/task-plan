package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.LoginLog;
import com.taskplan.repository.LoginLogRepository;
import com.taskplan.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/login-logs")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    @GetMapping
    public Result<Map<String, Object>> listLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String loginResult,
            @RequestParam(required = false) String clientIp,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "loginTime"));
        Page<LoginLog> logPage = loginLogService.listLogs(username, loginResult, clientIp, startTime, endTime, pageable);
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", logPage.getContent());
        result.put("total", logPage.getTotalElements());
        
        return Result.success(result);
    }

    @GetMapping("/export")
    public ResponseEntity<Result<List<LoginLog>>> exportLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String loginResult,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "loginTime"));
        Page<LoginLog> logPage = loginLogService.listLogs(username, loginResult, null, startTime, endTime, pageable);
        
        return ResponseEntity.ok(Result.success(logPage.getContent()));
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportSelectedLogs(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        List<LoginLog> logs = loginLogRepository.findAllById(ids);
        String csv = convertToCsv(logs);
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "login-logs-selected.csv");
        
        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @GetMapping("/export-all")
    public ResponseEntity<byte[]> exportAllLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String loginResult,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "loginTime"));
        Page<LoginLog> logPage = loginLogService.listLogs(username, loginResult, null, startTime, endTime, pageable);
        String csv = convertToCsv(logPage.getContent());
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "login-logs-all.csv");
        
        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        loginLogRepository.deleteAllById(ids);
        return Result.success();
    }

    @DeleteMapping("/all")
    public Result<Void> deleteAll(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String loginResult,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<LoginLog> logPage = loginLogService.listLogs(username, loginResult, null, startTime, endTime, pageable);
        List<Long> ids = logPage.getContent().stream().map(LoginLog::getId).collect(Collectors.toList());
        loginLogRepository.deleteAllById(ids);
        return Result.success();
    }

    private String convertToCsv(List<LoginLog> logs) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,用户名,姓名,登录结果,客户端IP,浏览器,User-Agent,错误信息,登录时间\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (LoginLog log : logs) {
            sb.append(log.getId()).append(",");
            sb.append(escapeCsv(log.getUsername())).append(",");
            sb.append(escapeCsv(log.getRealName())).append(",");
            sb.append(log.getLoginResult()).append(",");
            sb.append(log.getClientIp()).append(",");
            sb.append(escapeCsv(log.getBrowserInfo())).append(",");
            sb.append(escapeCsv(log.getUserAgent())).append(",");
            sb.append(escapeCsv(log.getErrorMessage())).append(",");
            sb.append(log.getLoginTime() != null ? log.getLoginTime().format(formatter) : "").append("\n");
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
    private LoginLogRepository loginLogRepository;
}
