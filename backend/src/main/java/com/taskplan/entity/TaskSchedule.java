package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_task_schedule")
public class TaskSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(length = 50, nullable = false)
    private String scheduleType;

    @Column(length = 100)
    private String cronExpression;

    @Column
    private Long fixedInterval;

    @Column(length = 20)
    private String timeUnit;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(length = 50)
    private String timezone;

    @Column(name = "misfire_policy")
    private Integer misfirePolicy = 0;

    @Column(nullable = false)
    private Integer enabled = 1;

    @Column(name = "max_concurrent")
    private Integer maxConcurrent = 1;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
