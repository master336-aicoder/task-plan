package com.taskplan.repository;

import com.taskplan.entity.TaskDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskDefinitionRepository extends JpaRepository<TaskDefinition, Long> {
    Page<TaskDefinition> findByProjectId(Long projectId, Pageable pageable);
    Page<TaskDefinition> findByProjectIdAndNameContaining(Long projectId, String name, Pageable pageable);
    List<TaskDefinition> findByProjectId(Long projectId);
    boolean existsByProjectIdAndName(Long projectId, String name);
}
