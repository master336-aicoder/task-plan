CREATE TABLE IF NOT EXISTS t_datasource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    database_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    params TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_server_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    port INT DEFAULT 22,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    group_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'OFFLINE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_server_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_file_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(500),
    size BIGINT DEFAULT 0,
    type VARCHAR(50) DEFAULT 'OTHER',
    version INT DEFAULT 1,
    project_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES t_project(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_cluster_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    config_json TEXT,
    description VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cluster_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_task_type_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    program_path VARCHAR(500),
    default_params TEXT,
    env_params TEXT,
    description VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task_type_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO t_task_type_config (name, task_type, program_path, description) VALUES
('Shell默认', 'SHELL', '/bin/bash', '默认Shell脚本执行器'),
('Python3', 'PYTHON', '/usr/bin/python3', 'Python3脚本执行器'),
('Python2', 'PYTHON', '/usr/bin/python2', 'Python2脚本执行器'),
('Perl', 'PERL', '/usr/bin/perl', 'Perl脚本执行器'),
('Java8', 'JAVA', '/usr/lib/jvm/java-8-openjdk-amd64/bin/java', 'Java8运行环境'),
('Java11', 'JAVA', '/usr/lib/jvm/java-11-openjdk-amd64/bin/java', 'Java11运行环境');

CREATE TABLE IF NOT EXISTS t_env_variable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_name VARCHAR(200),
    name VARCHAR(200) NOT NULL,
    value VARCHAR(500),
    category VARCHAR(100),
    description VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_env_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO t_cluster_config (name, type, description) VALUES
('Local YARN', 'YARN', '本地YARN集群'),
('Local Flink', 'FLINK', '本地Flink集群'),
('Local Spark', 'SPARK', '本地Spark配置');
