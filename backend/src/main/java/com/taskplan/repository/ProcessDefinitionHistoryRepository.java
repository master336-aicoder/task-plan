package com.taskplan.repository;

import com.taskplan.entity.ProcessDefinitionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessDefinitionHistoryRepository extends JpaRepository<ProcessDefinitionHistory, Long> {
    
    List<ProcessDefinitionHistory> findByProcessDefinitionIdOrderByVersionDesc(Long processDefinitionId);
    
    Page<ProcessDefinitionHistory> findByProcessDefinitionId(Long processDefinitionId, Pageable pageable);
    
    Optional<ProcessDefinitionHistory> findByProcessDefinitionIdAndVersion(Long processDefinitionId, Integer version);
    
    List<ProcessDefinitionHistory> findByProjectIdOrderByNameAscVersionDesc(Long projectId);
    
    void deleteByProcessDefinitionId(Long processDefinitionId);
}