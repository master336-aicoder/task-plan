package com.taskplan.repository;

import com.taskplan.entity.TaskLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {
    Page<TaskLog> findByTaskInstanceId(Long taskInstanceId, Pageable pageable);
    
    Page<TaskLog> findByProcessInstanceId(Long processInstanceId, Pageable pageable);
    
    List<TaskLog> findByTaskInstanceIdOrderByLogTimeDesc(Long taskInstanceId);
    
    List<TaskLog> findByProcessInstanceIdOrderByLogTimeDesc(Long processInstanceId);
    
    List<TaskLog> findByLogTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    Page<TaskLog> findByLogLevel(String logLevel, Pageable pageable);
    
    Page<TaskLog> findByTaskType(String taskType, Pageable pageable);
}
