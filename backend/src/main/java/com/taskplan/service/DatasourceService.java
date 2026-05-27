package com.taskplan.service;

import com.taskplan.entity.Datasource;
import com.taskplan.exception.BusinessException;
import com.taskplan.repository.DatasourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DatasourceService {

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Transactional
    public Datasource createDatasource(Datasource datasource) {
        validateDatasource(datasource);
        if (datasourceRepository.existsByName(datasource.getName())) {
            throw new BusinessException("数据源名称已存在");
        }
        return datasourceRepository.save(datasource);
    }

    @Transactional
    public Datasource updateDatasource(Long id, Datasource datasource) {
        Datasource existing = getDatasource(id);

        if (!existing.getName().equals(datasource.getName()) &&
            datasourceRepository.existsByName(datasource.getName())) {
            throw new BusinessException("数据源名称已存在");
        }

        existing.setName(datasource.getName());
        existing.setType(datasource.getType());
        existing.setHost(datasource.getHost());
        existing.setPort(datasource.getPort());
        existing.setDatabaseName(datasource.getDatabaseName());
        existing.setUsername(datasource.getUsername());
        existing.setPassword(datasource.getPassword());
        existing.setParams(datasource.getParams());

        return datasourceRepository.save(existing);
    }

    @Transactional
    public void deleteDatasource(Long id) {
        if (!datasourceRepository.existsById(id)) {
            throw new BusinessException("数据源不存在");
        }
        datasourceRepository.deleteById(id);
    }

    public Datasource getDatasource(Long id) {
        return datasourceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("数据源不存在"));
    }

    public Page<Datasource> listDatasources(String name, Pageable pageable) {
        if (name != null && !name.isEmpty()) {
            return datasourceRepository.findByNameContaining(name, pageable);
        }
        return datasourceRepository.findAll(pageable);
    }

    public List<Datasource> listByType(String type) {
        return datasourceRepository.findByType(type);
    }

    public List<Datasource> listAll() {
        return datasourceRepository.findAll();
    }

    public boolean testConnection(Datasource datasource) {
        String url = buildJdbcUrl(datasource);
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, datasource.getUsername(), datasource.getPassword())) {
            return conn.isValid(5);
        } catch (Exception e) {
            log.error("Failed to connect to database: {}", e.getMessage());
            return false;
        }
    }

    public String executeSql(Long datasourceId, String sql) {
        Datasource datasource = getDatasource(datasourceId);
        String url = buildJdbcUrl(datasource);

        try (java.sql.Connection conn = DriverManager.getConnection(url, datasource.getUsername(), datasource.getPassword())) {
            String trimmedSql = sql.trim().toLowerCase();

            if (trimmedSql.startsWith("select") || trimmedSql.startsWith("show") || trimmedSql.startsWith("describe")) {
                return executeQuery(conn, sql);
            } else {
                return executeUpdate(conn, sql);
            }
        } catch (SQLException e) {
            log.error("SQL执行失败: {}", e.getMessage(), e);
            return "SQL执行失败: " + e.getMessage();
        }
    }

    private String executeQuery(Connection conn, String sql) throws SQLException {
        StringBuilder result = new StringBuilder();
        result.append("=============== SQL查询结果 ===============\n");
        result.append("SQL: ").append(sql).append("\n\n");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<String> headers = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                headers.add(metaData.getColumnLabel(i) + " (" + metaData.getColumnTypeName(i) + ")");
            }
            result.append("列信息: ").append(String.join(", ", headers)).append("\n\n");

            result.append("-------------------- 结果 --------------------\n");

            int rowCount = 0;
            int maxRows = 1000;

            while (rs.next()) {
                if (rowCount >= maxRows) {
                    result.append("\n... (结果超过").append(maxRows).append("行，仅显示前").append(maxRows).append("行)");
                    break;
                }

                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) result.append(" | ");
                    Object value = rs.getObject(i);
                    String strValue = value == null ? "NULL" : value.toString();
                    if (strValue.length() > 100) {
                        strValue = strValue.substring(0, 100) + "...";
                    }
                    result.append(strValue);
                }
                result.append("\n");
                rowCount++;
            }

            result.append("\n--------------------------------------------\n");
            result.append("共 ").append(rowCount).append(" 行结果");

            log.info("SQL查询执行成功: 影响 {} 行", rowCount);
        }

        return result.toString();
    }

    private String executeUpdate(Connection conn, String sql) throws SQLException {
        StringBuilder result = new StringBuilder();
        result.append("=============== SQL执行结果 ===============\n");
        result.append("SQL: ").append(sql).append("\n\n");

        try (Statement stmt = conn.createStatement()) {
            boolean hasResultSet = stmt.execute(sql);

            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    result.append(executeQuery(conn, sql));
                    return result.toString();
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                result.append("-------------------- 结果 --------------------\n");
                result.append("执行成功！\n");
                result.append("影响行数: ").append(updateCount).append("\n");
                result.append("--------------------------------------------\n");

                log.info("SQL更新执行成功: 影响 {} 行", updateCount);
            }
        }

        return result.toString();
    }

    private String buildJdbcUrl(Datasource datasource) {
        String type = datasource.getType().toUpperCase();
        String host = datasource.getHost();
        int port = datasource.getPort();
        String database = datasource.getDatabaseName();

        switch (type) {
            case "MYSQL":
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", host, port, database);
            case "POSTGRESQL":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "ORACLE":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            case "SQLITE":
                return "jdbc:sqlite:" + database;
            default:
                return String.format("jdbc:mysql://%s:%d/%s", host, port, database);
        }
    }

    private void validateDatasource(Datasource datasource) {
        if (datasource.getName() == null || datasource.getName().trim().isEmpty()) {
            throw new BusinessException("数据源名称不能为空");
        }
        if (datasource.getType() == null || datasource.getType().trim().isEmpty()) {
            throw new BusinessException("数据源类型不能为空");
        }
        if (datasource.getHost() == null || datasource.getHost().trim().isEmpty()) {
            throw new BusinessException("主机地址不能为空");
        }
        if (datasource.getPort() == null) {
            throw new BusinessException("端口不能为空");
        }
        if (datasource.getDatabaseName() == null || datasource.getDatabaseName().trim().isEmpty()) {
            throw new BusinessException("数据库名称不能为空");
        }
        if (datasource.getUsername() == null || datasource.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
    }
}
