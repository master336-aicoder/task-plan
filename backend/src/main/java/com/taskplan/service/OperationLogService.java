package com.taskplan.service;

import com.taskplan.entity.OperationLog;
import com.taskplan.repository.OperationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Async
    @Transactional
    public void saveLogAsync(Long userId, String username, String requestMethod, String requestUrl,
                            String requestParams, String responseContent, String clientIp,
                            Integer clientPort, String httpHeader, String browserInfo,
                            String userAgent, Integer statusCode, Long requestTime) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(userId);
            operationLog.setUsername(username);
            operationLog.setRequestMethod(requestMethod);
            operationLog.setRequestUrl(requestUrl);
            operationLog.setRequestParams(truncate(requestParams, 2000));
            operationLog.setResponseContent(truncate(responseContent, 2000));
            operationLog.setClientIp(clientIp);
            operationLog.setClientPort(clientPort);
            operationLog.setHttpHeader(truncate(httpHeader, 1000));
            operationLog.setBrowserInfo(browserInfo);
            operationLog.setUserAgent(userAgent);
            operationLog.setStatusCode(statusCode);
            operationLog.setRequestTime(requestTime);
            operationLog.setOperationTime(LocalDateTime.now());
            
            operationLogRepository.save(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage());
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    public Page<OperationLog> listLogs(String username, String requestUrl, String requestMethod,
                                       String clientIp, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (username != null && !username.isEmpty()) {
            return operationLogRepository.findByUsername(username, pageable);
        }
        if (requestUrl != null && !requestUrl.isEmpty()) {
            return operationLogRepository.findByRequestUrl(requestUrl, pageable);
        }
        if (requestMethod != null && !requestMethod.isEmpty()) {
            return operationLogRepository.findByRequestMethod(requestMethod, pageable);
        }
        if (clientIp != null && !clientIp.isEmpty()) {
            return operationLogRepository.findByClientIp(clientIp, pageable);
        }
        if (startTime != null && endTime != null) {
            return operationLogRepository.findByOperationTimeBetween(startTime, endTime, pageable);
        }
        return operationLogRepository.findAll(pageable);
    }
}
