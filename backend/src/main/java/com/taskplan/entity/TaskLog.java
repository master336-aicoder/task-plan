package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_task_log")
public class TaskLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_instance_id")
    private Long taskInstanceId;

    @Column(name = "process_instance_id")
    private Long processInstanceId;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "task_type")
    private String taskType;

    @Column(name = "log_level")
    private String logLevel;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "log_time")
    private LocalDateTime logTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (logTime == null) {
            logTime = LocalDateTime.now();
        }
    }
}
