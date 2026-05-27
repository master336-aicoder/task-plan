package com.taskplan.repository;

import com.taskplan.entity.ScheduleLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleLogRepository extends JpaRepository<ScheduleLog, Long> {
    Page<ScheduleLog> findByScheduleId(Long scheduleId, Pageable pageable);
    
    Page<ScheduleLog> findByProcessDefinitionId(Long processDefinitionId, Pageable pageable);
    
    Page<ScheduleLog> findByProcessInstanceId(Long processInstanceId, Pageable pageable);
    
    List<ScheduleLog> findByScheduleIdOrderByLogTimeDesc(Long scheduleId);
    
    List<ScheduleLog> findByLogTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    Page<ScheduleLog> findByLogLevel(String logLevel, Pageable pageable);
}
