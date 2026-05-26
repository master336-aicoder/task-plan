package com.taskplan.service;

import com.taskplan.entity.TaskExecutionLog;
import com.taskplan.repository.TaskExecutionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TaskExecutionLogService {

    @Autowired
    private TaskExecutionLogRepository taskExecutionLogRepository;

    @Transactional
    public TaskExecutionLog saveLog(TaskExecutionLog logEntity) {
        return taskExecutionLogRepository.save(logEntity);
    }

    @Transactional
    public void saveLogs(List<TaskExecutionLog> logs) {
        taskExecutionLogRepository.saveAll(logs);
    }

    public TaskExecutionLog getLogById(Long id) {
        return taskExecutionLogRepository.findById(id).orElse(null);
    }

    public List<TaskExecutionLog> getLogsByProcessInstanceId(Long processInstanceId) {
        return taskExecutionLogRepository.findByProcessInstanceId(processInstanceId);
    }

    public Page<TaskExecutionLog> getLogsByProcessInstanceId(Long processInstanceId, Pageable pageable) {
        return taskExecutionLogRepository.findByProcessInstanceId(processInstanceId, pageable);
    }

    public Page<TaskExecutionLog> getLogsByConditions(Long processInstanceId, String taskType, 
                                                       String status, String serverIp, Pageable pageable) {
        return taskExecutionLogRepository.findByConditions(processInstanceId, taskType, status, serverIp, pageable);
    }
}