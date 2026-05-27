package com.taskplan.repository;

import com.taskplan.entity.ProcessDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessDefinitionRepository extends JpaRepository<ProcessDefinition, Long> {
    Page<ProcessDefinition> findByProjectId(Long projectId, Pageable pageable);
    Page<ProcessDefinition> findByProjectIdAndNameContaining(Long projectId, String name, Pageable pageable);
    Page<ProcessDefinition> findByNameContaining(String name, Pageable pageable);
    List<ProcessDefinition> findByProjectId(Long projectId);
    List<ProcessDefinition> findByProjectIdAndReleaseState(Long projectId, String releaseState);
    boolean existsByProjectIdAndName(Long projectId, String name);
    ProcessDefinition findByProjectIdAndName(Long projectId, String name);
    
    ProcessDefinition findTopByProjectIdAndNameOrderByVersionDesc(Long projectId, String name);
}
