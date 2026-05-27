package com.taskplan.repository;

import com.taskplan.entity.ServerProbeLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServerProbeLogRepository extends JpaRepository<ServerProbeLog, Long> {
    
    List<ServerProbeLog> findByServerIdOrderByProbeTimeDesc(Long serverId);
    
    Optional<ServerProbeLog> findTopByServerIdOrderByProbeTimeDesc(Long serverId);
    
    List<ServerProbeLog> findByProbeTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
