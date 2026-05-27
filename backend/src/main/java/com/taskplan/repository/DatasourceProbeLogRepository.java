package com.taskplan.repository;

import com.taskplan.entity.DatasourceProbeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatasourceProbeLogRepository extends JpaRepository<DatasourceProbeLog, Long> {
    
    List<DatasourceProbeLog> findByDatasourceIdOrderByProbeTimeDesc(Long datasourceId);
    
    Optional<DatasourceProbeLog> findTopByDatasourceIdOrderByProbeTimeDesc(Long datasourceId);
    
    List<DatasourceProbeLog> findByProbeTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
