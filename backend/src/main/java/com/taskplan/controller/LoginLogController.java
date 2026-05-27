package com.taskplan.controller;

import com.taskplan.common.Result;
import com.taskplan.entity.LoginLog;
import com.taskplan.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/login-logs")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listLogs(
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
        result.put("code", 200);
        result.put("msg", "success");
        result.put("data", logPage.getContent());
        result.put("total", logPage.getTotalElements());
        
        return ResponseEntity.ok(result);
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
}
