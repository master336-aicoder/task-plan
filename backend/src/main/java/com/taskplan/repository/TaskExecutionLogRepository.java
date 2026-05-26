package com.taskplan.repository;

import com.taskplan.entity.TaskExecutionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskExecutionLogRepository extends JpaRepository<TaskExecutionLog, Long> {
    
    Page<TaskExecutionLog> findByProcessInstanceId(Long processInstanceId, Pageable pageable);
    
    List<TaskExecutionLog> findByProcessInstanceId(Long processInstanceId);
    
    Page<TaskExecutionLog> findByTaskType(String taskType, Pageable pageable);
    
    Page<TaskExecutionLog> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT t FROM TaskExecutionLog t WHERE " +
           "(:processInstanceId IS NULL OR t.processInstanceId = :processInstanceId) AND " +
           "(:taskType IS NULL OR t.taskType = :taskType) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:serverIp IS NULL OR t.serverIp = :serverIp)")
    Page<TaskExecutionLog> findByConditions(
            @Param("processInstanceId") Long processInstanceId,
            @Param("taskType") String taskType,
            @Param("status") String status,
            @Param("serverIp") String serverIp,
            Pageable pageable);
}