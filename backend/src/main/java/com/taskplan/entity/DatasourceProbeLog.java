package com.taskplan.entity;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_datasource_probe_log")
public class DatasourceProbeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datasource_id", nullable = false)
    private Long datasourceId;

    @Column(name = "datasource_name", length = 100)
    private String datasourceName;

    @Column(name = "host", length = 100)
    private String host;

    @Column(name = "port")
    private Integer port;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "probe_time")
    private LocalDateTime probeTime;

    @Column(name = "probe_status", length = 20)
    private String probeStatus;

    @Column(name = "response_time")
    private Long responseTime;

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
