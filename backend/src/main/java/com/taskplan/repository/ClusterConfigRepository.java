package com.taskplan.repository;

import com.taskplan.entity.ClusterConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterConfigRepository extends JpaRepository<ClusterConfig, Long> {
    Page<ClusterConfig> findByNameContaining(String name, Pageable pageable);
    List<ClusterConfig> findByType(String type);
    boolean existsByName(String name);
}