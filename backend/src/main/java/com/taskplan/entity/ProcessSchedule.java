package com.taskplan.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_process_schedule")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "process_definition_id", nullable = false)
    private Long processDefinitionId;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Shanghai";

    @Column(name = "delay_time")
    private Integer delayTime = 0;

    @Column(name = "warning_type", length = 50)
    private String warningType = "NONE";

    @Column(name = "warning_group_id")
    private Long warningGroupId;

    @Column(name = "fail_retry_times")
    private Integer failRetryTimes = 0;

    @Column(name = "fail_retry_interval")
    private Integer failRetryInterval = 1;

    @Column(name = "is_valid")
    private Integer isValid = 1;

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
