package com.taskplan.service;

import com.taskplan.entity.ScheduleLog;
import com.taskplan.entity.TaskLog;
import com.taskplan.repository.ScheduleLogRepository;
import com.taskplan.repository.TaskLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class LogService {

    @Autowired
    private ScheduleLogRepository scheduleLogRepository;

    @Autowired
    private TaskLogRepository taskLogRepository;

    @Value("${app.log.schedule-path:./logs/schedule}")
    private String scheduleLogPath;

    @Value("${app.log.task-path:./logs/task}")
    private String taskLogPath;

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Async
    public void logSchedule(Long scheduleId, Long processDefinitionId, String level, String message) {
        ScheduleLog logEntry = new ScheduleLog();
        logEntry.setScheduleId(scheduleId);
        logEntry.setProcessDefinitionId(processDefinitionId);
        logEntry.setLogLevel(level);
        logEntry.setMessage(message);
        logEntry.setLogTime(LocalDateTime.now());
        
        scheduleLogRepository.save(logEntry);
        
        writeScheduleLogToFile(scheduleId, level, message);
    }

    @Async
    public void logTask(Long taskInstanceId, Long processInstanceId, String taskName, String taskType, String level, String message) {
        TaskLog logEntry = new TaskLog();
        logEntry.setTaskInstanceId(taskInstanceId);
        logEntry.setProcessInstanceId(processInstanceId);
        logEntry.setTaskName(taskName);
        logEntry.setTaskType(taskType);
        logEntry.setLogLevel(level);
        logEntry.setMessage(message);
        logEntry.setLogTime(LocalDateTime.now());
        
        taskLogRepository.save(logEntry);
        
        writeTaskLogToFile(taskInstanceId, processInstanceId, taskName, level, message);
    }

    private void writeScheduleLogToFile(Long scheduleId, String level, String message) {
        try {
            Path dirPath = Paths.get(scheduleLogPath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            String fileName = "schedule_" + LocalDate.now().format(FILE_DATE_FORMAT) + ".log";
            Path filePath = dirPath.resolve(fileName);
            
            String logLine = String.format("[%s] [SCHEDULE-%d] [%s] %s%n",
                    LocalDateTime.now().format(LOG_TIME_FORMAT),
                    scheduleId != null ? scheduleId : 0,
                    level,
                    message);
            
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(logLine);
            }
        } catch (IOException e) {
            log.error("Failed to write schedule log to file", e);
        }
    }

    private void writeTaskLogToFile(Long taskInstanceId, Long processInstanceId, String taskName, String level, String message) {
        try {
            Path dirPath = Paths.get(taskLogPath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            String fileName = "task_" + LocalDate.now().format(FILE_DATE_FORMAT) + ".log";
            Path filePath = dirPath.resolve(fileName);
            
            String logLine = String.format("[%s] [PROCESS-%d] [TASK-%d] [%s] [%s] %s%n",
                    LocalDateTime.now().format(LOG_TIME_FORMAT),
                    processInstanceId != null ? processInstanceId : 0,
                    taskInstanceId != null ? taskInstanceId : 0,
                    taskName != null ? taskName : "UNKNOWN",
                    level,
                    message);
            
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(logLine);
            }
        } catch (IOException e) {
            log.error("Failed to write task log to file", e);
        }
    }

    public Page<ScheduleLog> getScheduleLogs(Long scheduleId, Long processDefinitionId, Pageable pageable) {
        if (scheduleId != null) {
            return scheduleLogRepository.findByScheduleId(scheduleId, pageable);
        } else if (processDefinitionId != null) {
            return scheduleLogRepository.findByProcessDefinitionId(processDefinitionId, pageable);
        }
        return scheduleLogRepository.findAll(pageable);
    }

    public Page<TaskLog> getTaskLogs(Long taskInstanceId, Long processInstanceId, Pageable pageable) {
        if (taskInstanceId != null) {
            return taskLogRepository.findByTaskInstanceId(taskInstanceId, pageable);
        } else if (processInstanceId != null) {
            return taskLogRepository.findByProcessInstanceId(processInstanceId, pageable);
        }
        return taskLogRepository.findAll(pageable);
    }

    public void infoSchedule(Long scheduleId, Long processDefinitionId, String message) {
        logSchedule(scheduleId, processDefinitionId, "INFO", message);
    }

    public void warnSchedule(Long scheduleId, Long processDefinitionId, String message) {
        logSchedule(scheduleId, processDefinitionId, "WARN", message);
    }

    public void errorSchedule(Long scheduleId, Long processDefinitionId, String message) {
        logSchedule(scheduleId, processDefinitionId, "ERROR", message);
    }

    public void infoTask(Long taskInstanceId, Long processInstanceId, String taskName, String taskType, String message) {
        logTask(taskInstanceId, processInstanceId, taskName, taskType, "INFO", message);
    }

    public void warnTask(Long taskInstanceId, Long processInstanceId, String taskName, String taskType, String message) {
        logTask(taskInstanceId, processInstanceId, taskName, taskType, "WARN", message);
    }

    public void errorTask(Long taskInstanceId, Long processInstanceId, String taskName, String taskType, String message) {
        logTask(taskInstanceId, processInstanceId, taskName, taskType, "ERROR", message);
    }
}
