package com.taskplan.service;

import com.taskplan.entity.Datasource;
import com.taskplan.entity.DatasourceProbeLog;
import com.taskplan.entity.ServerNode;
import com.taskplan.entity.ServerProbeLog;
import com.taskplan.repository.DatasourceProbeLogRepository;
import com.taskplan.repository.DatasourceRepository;
import com.taskplan.repository.ServerNodeRepository;
import com.taskplan.repository.ServerProbeLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProbeService {

    @Autowired
    private ServerNodeRepository serverNodeRepository;

    @Autowired
    private ServerProbeLogRepository serverProbeLogRepository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private DatasourceProbeLogRepository datasourceProbeLogRepository;

    @Autowired
    private SshService sshService;

    @PostConstruct
    public void init() {
        Thread probeThread = new Thread(() -> {
            try {
                Thread.sleep(30000);
                log.info("启动初始探活任务");
                probeAllServers();
                probeAllDatasources();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        probeThread.start();
    }

    @Scheduled(fixedRate = 600000)
    public void scheduledProbe() {
        log.info("执行定时探活任务");
        probeAllServers();
        probeAllDatasources();
    }

    @Transactional
    public void probeAllServers() {
        List<ServerNode> servers = serverNodeRepository.findAll();
        log.info("开始探活 {} 台服务器", servers.size());
        
        for (ServerNode server : servers) {
            probeServer(server);
        }
    }

    @Transactional
    public void probeServer(ServerNode server) {
        ServerProbeLog probeLog = new ServerProbeLog();
        probeLog.setServerId(server.getId());
        probeLog.setServerName(server.getName());
        probeLog.setHostIp(server.getHostname());
        probeLog.setUsername(server.getUsername());
        probeLog.setProbeTime(LocalDateTime.now());

        long startTime = System.currentTimeMillis();
        try {
            boolean success = sshService.testSshConnection(server);
            long responseTime = System.currentTimeMillis() - startTime;
            
            probeLog.setProbeStatus(success ? "SUCCESS" : "FAILED");
            probeLog.setResponseTime(responseTime);
            
            if (success) {
                Map<String, Double> metrics = sshService.getServerMetrics(server);
                probeLog.setCpuUsage(metrics.get("cpuUsage"));
                probeLog.setMemoryUsage(metrics.get("memoryUsage"));
                probeLog.setDiskUsage(metrics.get("diskUsage"));
                log.info("服务器探活成功: {} ({}) - CPU: {}, MEM: {}, DISK: {}", 
                    server.getName(), server.getHostname(), 
                    probeLog.getCpuUsage(), probeLog.getMemoryUsage(), probeLog.getDiskUsage());
            } else {
                probeLog.setErrorMessage("SSH连接失败");
                log.warn("服务器探活失败: {} ({})", server.getName(), server.getHostname());
            }
        } catch (Exception e) {
            probeLog.setProbeStatus("FAILED");
            probeLog.setResponseTime(System.currentTimeMillis() - startTime);
            probeLog.setErrorMessage(e.getMessage());
            log.error("服务器探活异常: {} ({}) - {}", server.getName(), server.getHostname(), e.getMessage());
        }
        
        serverProbeLogRepository.save(probeLog);
    }

    @Transactional
    public void probeAllDatasources() {
        List<Datasource> datasources = datasourceRepository.findAll();
        log.info("开始探活 {} 个数据源", datasources.size());
        
        for (Datasource datasource : datasources) {
            probeDatasource(datasource);
        }
    }

    @Transactional
    public void probeDatasource(Datasource datasource) {
        DatasourceProbeLog probeLog = new DatasourceProbeLog();
        probeLog.setDatasourceId(datasource.getId());
        probeLog.setDatasourceName(datasource.getName());
        probeLog.setHost(datasource.getHost());
        probeLog.setPort(datasource.getPort());
        probeLog.setUsername(datasource.getUsername());
        probeLog.setProbeTime(LocalDateTime.now());
        probeLog.setDbType(datasource.getType());

        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> result = testDatasourceConnection(datasource);
            boolean success = (Boolean) result.get("success");
            long responseTime = System.currentTimeMillis() - startTime;
            
            probeLog.setProbeStatus(success ? "SUCCESS" : "FAILED");
            probeLog.setResponseTime(responseTime);
            
            if (success) {
                probeLog.setDbVersion((String) result.get("version"));
                probeLog.setDbCurrentTime((LocalDateTime) result.get("currentTime"));
                log.info("数据源探活成功: {} ({}) - 版本: {}", datasource.getName(), datasource.getHost(), probeLog.getDbVersion());
            } else {
                probeLog.setErrorMessage("数据库连接失败");
                log.warn("数据源探活失败: {} ({})", datasource.getName(), datasource.getHost());
            }
        } catch (Exception e) {
            probeLog.setProbeStatus("FAILED");
            probeLog.setResponseTime(System.currentTimeMillis() - startTime);
            probeLog.setErrorMessage(e.getMessage());
            log.error("数据源探活异常: {} ({}) - {}", datasource.getName(), datasource.getHost(), e.getMessage());
        }
        
        datasourceProbeLogRepository.save(probeLog);
    }

    private Map<String, Object> testDatasourceConnection(Datasource datasource) {
        String url = buildJdbcUrl(datasource);
        Map<String, Object> result = new java.util.HashMap<>();
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, datasource.getUsername(), datasource.getPassword())) {
            if (conn.isValid(3)) {
                result.put("success", true);
                
                java.sql.DatabaseMetaData metaData = conn.getMetaData();
                result.put("version", metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
                
                try (java.sql.Statement stmt = conn.createStatement()) {
                    java.sql.ResultSet rs = stmt.executeQuery("SELECT NOW()");
                    if (rs.next()) {
                        java.sql.Timestamp ts = rs.getTimestamp(1);
                        if (ts != null) {
                            result.put("currentTime", ts.toLocalDateTime());
                        }
                    }
                } catch (Exception e) {
                    result.put("currentTime", LocalDateTime.now());
                }
            } else {
                result.put("success", false);
            }
        } catch (Exception e) {
            result.put("success", false);
        }
        return result;
    }

    private String buildJdbcUrl(Datasource datasource) {
        String type = datasource.getType().toUpperCase();
        String host = datasource.getHost();
        int port = datasource.getPort();
        String database = datasource.getDatabaseName();
        
        switch (type) {
            case "MYSQL":
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&connectTimeout=3000&socketTimeout=3000", host, port, database);
            case "POSTGRESQL":
                return String.format("jdbc:postgresql://%s:%d/%s?connectTimeout=3&socketTimeout=3", host, port, database);
            case "ORACLE":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            case "SQLITE":
                return "jdbc:sqlite:" + database;
            default:
                return String.format("jdbc:mysql://%s:%d/%s?connectTimeout=3000", host, port, database);
        }
    }

    public List<ServerProbeLog> getLatestServerProbes() {
        List<ServerNode> servers = serverNodeRepository.findAll();
        return servers.stream()
                .map(server -> serverProbeLogRepository.findTopByServerIdOrderByProbeTimeDesc(server.getId()).orElse(null))
                .filter(log -> log != null)
                .collect(Collectors.toList());
    }

    public List<DatasourceProbeLog> getLatestDatasourceProbes() {
        List<Datasource> datasources = datasourceRepository.findAll();
        return datasources.stream()
                .map(datasource -> datasourceProbeLogRepository.findTopByDatasourceIdOrderByProbeTimeDesc(datasource.getId()).orElse(null))
                .filter(log -> log != null)
                .collect(Collectors.toList());
    }
}
