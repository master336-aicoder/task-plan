package com.taskplan.repository;

import com.taskplan.entity.TaskExecutionContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskExecutionContentRepository extends JpaRepository<TaskExecutionContent, Long> {
    
    List<TaskExecutionContent> findByProcessInstanceId(Long processInstanceId);
    
    List<TaskExecutionContent> findByProcessInstanceIdAndTaskNodeId(Long processInstanceId, String taskNodeId);
}
