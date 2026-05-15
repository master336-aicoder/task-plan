package com.taskplan.repository;

import com.taskplan.entity.ProcessInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessInstanceRepository extends JpaRepository<ProcessInstance, Long> {
    Page<ProcessInstance> findByProjectId(Long projectId, Pageable pageable);
    Page<ProcessInstance> findByProjectIdAndState(Long projectId, String state, Pageable pageable);
    Page<ProcessInstance> findByProcessDefinitionId(Long processDefinitionId, Pageable pageable);
    List<ProcessInstance> findByState(String state);
    List<ProcessInstance> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
