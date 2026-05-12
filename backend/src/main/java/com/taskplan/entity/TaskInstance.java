package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_task_instance")
public class TaskInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(length = 100)
    private String instanceKey;

    @Column(length = 50, nullable = false)
    private String status;

    @Column(name = "trigger_type", length = 50)
    private String triggerType;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column
    private Long duration;

    @Column(length = 2000)
    private String result;

    @Column(length = 5000)
    private String logs;

    @Column
    private Integer retryCount = 0;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
