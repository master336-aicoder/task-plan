package com.taskplan.service;

import com.taskplan.entity.ScheduleLog;
import com.taskplan.repository.ScheduleLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ScheduleLogService {

    @Autowired
    private ScheduleLogRepository scheduleLogRepository;

    @Transactional
    public ScheduleLog saveLog(Long scheduleId, Long processDefinitionId, Long processInstanceId, 
                               String taskNodeId, String taskName, String logLevel, String message) {
        ScheduleLog logEntry = new ScheduleLog();
        logEntry.setScheduleId(scheduleId);
        logEntry.setProcessDefinitionId(processDefinitionId);
        logEntry.setProcessInstanceId(processInstanceId);
        logEntry.setTaskNodeId(taskNodeId);
        logEntry.setTaskName(taskName);
        logEntry.setLogLevel(logLevel);
        logEntry.setMessage(message);
        logEntry.setLogTime(LocalDateTime.now());
        
        return scheduleLogRepository.save(logEntry);
    }

    @Transactional
    public void info(Long scheduleId, Long processDefinitionId, String message) {
        saveLog(scheduleId, processDefinitionId, null, null, null, "INFO", message);
        log.info("[Schedule] scheduleId={}, processDefinitionId={}, message={}", scheduleId, processDefinitionId, message);
    }

    @Transactional
    public void info(Long scheduleId, Long processDefinitionId, Long processInstanceId, 
                     String taskNodeId, String taskName, String message) {
        saveLog(scheduleId, processDefinitionId, processInstanceId, taskNodeId, taskName, "INFO", message);
        log.info("[Schedule] scheduleId={}, processDefinitionId={}, processInstanceId={}, taskNodeId={}, taskName={}, message={}", 
                 scheduleId, processDefinitionId, processInstanceId, taskNodeId, taskName, message);
    }

    @Transactional
    public void error(Long scheduleId, Long processDefinitionId, String message) {
        saveLog(scheduleId, processDefinitionId, null, null, null, "ERROR", message);
        log.error("[Schedule] scheduleId={}, processDefinitionId={}, message={}", scheduleId, processDefinitionId, message);
    }

    @Transactional
    public void error(Long scheduleId, Long processDefinitionId, Long processInstanceId, 
                      String taskNodeId, String taskName, String message) {
        saveLog(scheduleId, processDefinitionId, processInstanceId, taskNodeId, taskName, "ERROR", message);
        log.error("[Schedule] scheduleId={}, processDefinitionId={}, processInstanceId={}, taskNodeId={}, taskName={}, message={}", 
                  scheduleId, processDefinitionId, processInstanceId, taskNodeId, taskName, message);
    }

    @Transactional
    public void warn(Long scheduleId, Long processDefinitionId, String message) {
        saveLog(scheduleId, processDefinitionId, null, null, null, "WARN", message);
        log.warn("[Schedule] scheduleId={}, processDefinitionId={}, message={}", scheduleId, processDefinitionId, message);
    }

    @Transactional
    public void warn(Long scheduleId, Long processDefinitionId, Long processInstanceId, 
                     String taskNodeId, String taskName, String message) {
        saveLog(scheduleId, processDefinitionId, processInstanceId, taskNodeId, taskName, "WARN", message);
        log.warn("[Schedule] scheduleId={}, processDefinitionId={}, processInstanceId={}, taskNodeId={}, taskName={}, message={}", 
                 scheduleId, processDefinitionId, processInstanceId, taskNodeId, taskName, message);
    }

    public Page<ScheduleLog> listLogs(Long scheduleId, Long processDefinitionId, Long processInstanceId, Pageable pageable) {
        if (processInstanceId != null) {
            return scheduleLogRepository.findByProcessInstanceId(processInstanceId, pageable);
        } else if (scheduleId != null) {
            return scheduleLogRepository.findByScheduleId(scheduleId, pageable);
        } else if (processDefinitionId != null) {
            return scheduleLogRepository.findByProcessDefinitionId(processDefinitionId, pageable);
        }
        return scheduleLogRepository.findAll(pageable);
    }

    public Page<ScheduleLog> listLogsByLevel(String logLevel, Pageable pageable) {
        return scheduleLogRepository.findByLogLevel(logLevel, pageable);
    }
}