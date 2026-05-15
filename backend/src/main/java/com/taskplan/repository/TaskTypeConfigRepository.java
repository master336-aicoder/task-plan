package com.taskplan.repository;

import com.taskplan.entity.TaskTypeConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskTypeConfigRepository extends JpaRepository<TaskTypeConfig, Long> {
    Page<TaskTypeConfig> findByNameContaining(String name, Pageable pageable);
    List<TaskTypeConfig> findByTaskType(String taskType);
    boolean existsByName(String name);
}