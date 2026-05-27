package com.taskplan.service;

import com.taskplan.entity.LoginLog;
import com.taskplan.repository.LoginLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class LoginLogService {

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Transactional
    public LoginLog saveLog(Long userId, String username, String realName, String loginResult,
                           String clientIp, Integer clientPort, String httpHeader,
                           String browserInfo, String userAgent, String errorMessage) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setRealName(realName);
        loginLog.setLoginResult(loginResult);
        loginLog.setClientIp(clientIp);
        loginLog.setClientPort(clientPort);
        loginLog.setHttpHeader(httpHeader);
        loginLog.setBrowserInfo(browserInfo);
        loginLog.setUserAgent(userAgent);
        loginLog.setErrorMessage(errorMessage);
        loginLog.setLoginTime(LocalDateTime.now());
        
        return loginLogRepository.save(loginLog);
    }

    public Page<LoginLog> listLogs(String username, String loginResult, String clientIp,
                                   LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (username != null && !username.isEmpty()) {
            return loginLogRepository.findByUsername(username, pageable);
        }
        if (loginResult != null && !loginResult.isEmpty()) {
            return loginLogRepository.findByLoginResult(loginResult, pageable);
        }
        if (clientIp != null && !clientIp.isEmpty()) {
            return loginLogRepository.findByClientIp(clientIp, pageable);
        }
        if (startTime != null && endTime != null) {
            return loginLogRepository.findByLoginTimeBetween(startTime, endTime, pageable);
        }
        return loginLogRepository.findAll(pageable);
    }
}
