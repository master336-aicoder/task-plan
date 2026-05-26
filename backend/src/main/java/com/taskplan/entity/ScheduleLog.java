package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_schedule_log")
public class ScheduleLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "process_definition_id")
    private Long processDefinitionId;

    @Column(name = "process_instance_id")
    private Long processInstanceId;

    @Column(name = "task_node_id", length = 100)
    private String taskNodeId;

    @Column(name = "task_name", length = 200)
    private String taskName;

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
