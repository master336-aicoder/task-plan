package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String tags;

    @Column(length = 50, nullable = false)
    private String type;

    @Column(length = 500)
    private String command;

    @Column(length = 500)
    private String parameters;

    @Column(length = 500)
    private String envVars;

    @Column(length = 200)
    private String workingDir;

    @Column(length = 50)
    private String owner;

    @Column(nullable = false)
    private Integer status = 1;

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
