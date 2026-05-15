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

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "process_instance_id", nullable = false)
    private Long processInstanceId;

    @Column(name = "task_definition_id", nullable = false)
    private Long taskDefinitionId;

    @Column(name = "task_name", length = 200)
    private String taskName;

    @Column(name = "task_type", length = 50)
    private String taskType;

    @Column(name = "state", length = 50)
    private String state = "SUBMITTED_SUCCESS";

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "retry_times")
    private Integer retryTimes = 0;

    @Column(columnDefinition = "TEXT")
    private String logPath;

    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    @Column(columnDefinition = "TEXT")
    private String executePath;

    @Column(length = 100)
    private String host;

    @Column(length = 100)
    private String owner;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
