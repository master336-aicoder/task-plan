package com.taskplan.repository;

import com.taskplan.entity.DatasourceProbeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DatasourceProbeLogRepository extends JpaRepository<DatasourceProbeLog, Long> {
    
    List<DatasourceProbeLog> findByDatasourceIdOrderByProbeTimeDesc(Long datasourceId);
    
    @Query("SELECT p FROM DatasourceProbeLog p WHERE p.datasourceId = :datasourceId ORDER BY p.probeTime DESC LIMIT 1")
    DatasourceProbeLog findLatestByDatasourceId(Long datasourceId);
    
    List<DatasourceProbeLog> findByProbeTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
