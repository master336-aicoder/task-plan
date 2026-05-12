package com.taskplan.repository;

import com.taskplan.entity.TaskSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskScheduleRepository extends JpaRepository<TaskSchedule, Long> {
    List<TaskSchedule> findByTaskId(Long taskId);
    List<TaskSchedule> findByTaskIdAndEnabled(Long taskId, Integer enabled);
    List<TaskSchedule> findByEnabled(Integer enabled);
}
