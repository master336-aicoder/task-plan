package com.taskplan.entity;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_url", length = 500)
    private String requestUrl;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "response_content", columnDefinition = "TEXT")
    private String responseContent;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "client_port")
    private Integer clientPort;

    @Column(name = "http_header", columnDefinition = "TEXT")
    private String httpHeader;

    @Column(name = "browser_info", length = 500)
    private String browserInfo;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "request_time")
    private Long requestTime;

    @Column(name = "operation_time")
    private LocalDateTime operationTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (operationTime == null) {
            operationTime = LocalDateTime.now();
        }
    }
}
