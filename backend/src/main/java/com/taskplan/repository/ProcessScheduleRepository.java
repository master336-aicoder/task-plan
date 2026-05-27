package com.taskplan.repository;

import com.taskplan.entity.ProcessSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessScheduleRepository extends JpaRepository<ProcessSchedule, Long> {
    Page<ProcessSchedule> findByProjectId(Long projectId, Pageable pageable);
    Page<ProcessSchedule> findByProjectIdAndProcessDefinitionId(Long projectId, Long processDefinitionId, Pageable pageable);
    List<ProcessSchedule> findByProcessDefinitionId(Long processDefinitionId);
    Page<ProcessSchedule> findByProcessDefinitionId(Long processDefinitionId, Pageable pageable);
    List<ProcessSchedule> findByIsValid(Integer isValid);
    
    @Modifying
    @Query("UPDATE ProcessSchedule s SET s.isValid = :isValid WHERE s.processDefinitionId = :processDefinitionId")
    void updateScheduleStatusByProcessId(Long processDefinitionId, Integer isValid);
}
