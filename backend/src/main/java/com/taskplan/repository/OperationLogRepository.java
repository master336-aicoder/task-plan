package com.taskplan.repository;

import com.taskplan.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    
    Page<OperationLog> findByUsername(String username, Pageable pageable);
    
    Page<OperationLog> findByRequestUrl(String requestUrl, Pageable pageable);
    
    Page<OperationLog> findByRequestMethod(String requestMethod, Pageable pageable);
    
    Page<OperationLog> findByOperationTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<OperationLog> findByClientIp(String clientIp, Pageable pageable);
}
