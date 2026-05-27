package com.taskplan.entity;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "task_execution_content")
public class TaskExecutionContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_instance_id", nullable = false)
    private Long processInstanceId;

    @Column(name = "task_node_id", length = 64)
    private String taskNodeId;

    @Column(name = "task_name", length = 255)
    private String taskName;

    @Column(name = "task_type", length = 50)
    private String taskType;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;

    @Column(name = "resolved_content", columnDefinition = "TEXT")
    private String resolvedContent;

    @Column(name = "server_id")
    private Long serverId;

    @Column(name = "server_ip", length = 100)
    private String serverIp;

    @Column(name = "datasource_id")
    private Long datasourceId;

    @Column(name = "http_url", length = 500)
    private String httpUrl;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "http_body", columnDefinition = "TEXT")
    private String httpBody;

    @Column(name = "java_command", columnDefinition = "TEXT")
    private String javaCommand;

    @Column(name = "jar_file_path", length = 500)
    private String jarFilePath;

    @Column(name = "main_class", length = 500)
    private String mainClass;

    @Column(name = "program_args", length = 1000)
    private String programArgs;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
