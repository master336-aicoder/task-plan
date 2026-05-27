package com.taskplan.repository;

import com.taskplan.entity.ServerProbeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServerProbeLogRepository extends JpaRepository<ServerProbeLog, Long> {
    
    List<ServerProbeLog> findByServerIdOrderByProbeTimeDesc(Long serverId);
    
    @Query("SELECT p FROM ServerProbeLog p WHERE p.serverId = :serverId ORDER BY p.probeTime DESC LIMIT 1")
    ServerProbeLog findLatestByServerId(Long serverId);
    
    List<ServerProbeLog> findByProbeTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
