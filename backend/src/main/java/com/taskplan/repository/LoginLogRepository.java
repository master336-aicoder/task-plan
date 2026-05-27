package com.taskplan.repository;

import com.taskplan.entity.LoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    
    Page<LoginLog> findByUsername(String username, Pageable pageable);
    
    Page<LoginLog> findByLoginResult(String loginResult, Pageable pageable);
    
    Page<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    Page<LoginLog> findByClientIp(String clientIp, Pageable pageable);
    
    List<LoginLog> findByUserId(Long userId);
}
