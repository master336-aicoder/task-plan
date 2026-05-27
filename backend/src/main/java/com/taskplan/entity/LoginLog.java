package com.taskplan.entity;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_login_log")
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "real_name", length = 100)
    private String realName;

    @Column(name = "login_result", length = 20)
    private String loginResult;

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

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
    }
}
