package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_task_definition")
public class TaskDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;

    @Column(columnDefinition = "TEXT")
    private String params;

    @Column(columnDefinition = "TEXT")
    private String envParams;

    @Column(name = "task_priority")
    private Integer taskPriority = 0;

    @Column(name = "worker_group", length = 100)
    private String workerGroup = "default";

    @Column(name = "fail_retry_times")
    private Integer failRetryTimes = 0;

    @Column(name = "fail_retry_interval")
    private Integer failRetryInterval = 1;

    @Column(name = "timeout_flag")
    private Integer timeoutFlag = 0;

    @Column(name = "timeout_notify_strategy", length = 50)
    private String timeoutNotifyStrategy = "NONE";

    @Column(name = "timeout")
    private Integer timeout = 0;

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
