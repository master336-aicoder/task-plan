package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_task_execution_log")
public class TaskExecutionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_instance_id")
    private Long processInstanceId;

    @Column(name = "task_node_id", length = 100)
    private String taskNodeId;

    @Column(name = "task_name", length = 200)
    private String taskName;

    @Column(name = "task_type", length = 50)
    private String taskType;

    @Column(name = "server_id")
    private Long serverId;

    @Column(name = "server_ip", length = 50)
    private String serverIp;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "status", length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(columnDefinition = "TEXT")
    private String error;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}