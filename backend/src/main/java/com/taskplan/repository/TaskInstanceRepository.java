package com.taskplan.repository;

import com.taskplan.entity.TaskInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskInstanceRepository extends JpaRepository<TaskInstance, Long> {
    List<TaskInstance> findByTaskIdOrderByCreatedTimeDesc(Long taskId);
    List<TaskInstance> findByStatusOrderByCreatedTimeDesc(String status);
}
