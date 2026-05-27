package com.taskplan.entity;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_server_probe_log")
public class ServerProbeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @Column(name = "server_name", length = 100)
    private String serverName;

    @Column(name = "host_ip", length = 100)
    private String hostIp;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "probe_time")
    private LocalDateTime probeTime;

    @Column(name = "probe_status", length = 20)
    private String probeStatus;

    @Column(name = "response_time")
    private Long responseTime;

    @Column(name = "cpu_usage")
    private Double cpuUsage;

    @Column(name = "memory_usage")
    private Double memoryUsage;

    @Column(name = "disk_usage")
    private Double diskUsage;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (probeTime == null) {
            probeTime = LocalDateTime.now();
        }
    }
}
