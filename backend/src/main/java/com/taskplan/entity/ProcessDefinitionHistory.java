package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_process_definition_history")
public class ProcessDefinitionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_definition_id", nullable = false)
    private Long processDefinitionId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String definitionJson;

    @Column(name = "process_type", length = 50)
    private String processType;

    @Column(name = "release_state", length = 50)
    private String releaseState;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String owner;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "history_time")
    private LocalDateTime historyTime;
}