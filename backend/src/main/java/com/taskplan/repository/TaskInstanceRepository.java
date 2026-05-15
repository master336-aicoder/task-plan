package com.taskplan.repository;

import com.taskplan.entity.TaskInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskInstanceRepository extends JpaRepository<TaskInstance, Long> {
    Page<TaskInstance> findByProcessInstanceId(Long processInstanceId, Pageable pageable);
    List<TaskInstance> findByProcessInstanceId(Long processInstanceId);
    List<TaskInstance> findByTaskDefinitionId(Long taskDefinitionId);
    List<TaskInstance> findByState(String state);
    Page<TaskInstance> findByProjectId(Long projectId, Pageable pageable);
}
