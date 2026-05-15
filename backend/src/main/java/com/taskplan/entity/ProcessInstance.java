package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_process_instance")
public class ProcessInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "process_definition_id", nullable = false)
    private Long processDefinitionId;

    @Column(name = "process_name", length = 200)
    private String processName;

    @Column(name = "state", length = 50)
    private String state = "RUNNING_EXECUTION";

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "command_type", length = 50)
    private String commandType;

    @Column(name = "command_param", length = 500)
    private String commandParam;

    @Column(name = "task_instance_count")
    private Integer taskInstanceCount = 0;

    @Column(name = "success_task_count")
    private Integer successTaskCount = 0;

    @Column(name = "failed_task_count")
    private Integer failedTaskCount = 0;

    @Column(name = "running_task_count")
    private Integer runningTaskCount = 0;

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
        startTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
