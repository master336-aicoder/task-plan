package com.taskplan.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_file_resource")
public class FileResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String path;

    @Column
    private Long size;

    @Column(length = 50)
    private String type;

    @Column(name = "is_directory")
    private Integer isDirectory = 0;

    @Column(name = "parent_id")
    private Long parentId;

    @Column
    private Integer version;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (version == null) {
            version = 1;
        }
        if (isDirectory == null) {
            isDirectory = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
