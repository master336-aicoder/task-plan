-- ==============================================
-- 任务计划工具箱系统 - 数据库表结构初始化脚本
-- 版本: v1.0
-- 日期: 2026-05-28
-- 数据库: MySQL 5.7+ / 8.0+
-- ==============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS task_plan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE task_plan;

-- ==============================================
-- 系统用户表
-- ==============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码(BCrypt)',
    real_name VARCHAR(100) NULL COMMENT '真实姓名',
    email VARCHAR(255) NULL COMMENT '邮箱',
    phone VARCHAR(50) NULL COMMENT '手机号',
    status INT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    is_deleted INT DEFAULT 0 COMMENT '是否删除(0-否,1-是)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ==============================================
-- 系统角色表
-- ==============================================
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_name VARCHAR(100) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description TEXT NULL COMMENT '描述',
    status INT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- ==============================================
-- 用户角色关联表
-- ==============================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ==============================================
-- 系统菜单表
-- ==============================================
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(100) NULL COMMENT '菜单编码',
    path VARCHAR(255) NULL COMMENT '路由路径',
    component VARCHAR(255) NULL COMMENT '组件路径',
    icon VARCHAR(100) NULL COMMENT '图标',
    parent_id BIGINT DEFAULT NULL COMMENT '父菜单ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    menu_type INT DEFAULT 0 COMMENT '类型(0-菜单,1-按钮)',
    permission VARCHAR(200) NULL COMMENT '权限标识',
    status INT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (parent_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- ==============================================
-- 角色菜单关联表
-- ==============================================
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ==============================================
-- 系统配置表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    description TEXT NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ==============================================
-- 项目表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(255) NOT NULL UNIQUE COMMENT '项目名称',
    description TEXT NULL COMMENT '描述',
    status INT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- ==============================================
-- 工作流定义表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_process_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(255) NOT NULL COMMENT '工作流名称',
    description TEXT NULL COMMENT '描述',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    tasks TEXT NULL COMMENT '任务列表(JSON)',
    connections TEXT NULL COMMENT '连接关系(JSON)',
    status INT DEFAULT 0 COMMENT '状态(0-草稿,1-已发布)',
    version INT DEFAULT 1 COMMENT '版本号',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (project_id) REFERENCES t_project(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流定义表';

-- ==============================================
-- 调度计划表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_process_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    process_definition_id BIGINT NOT NULL COMMENT '关联工作流定义',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    start_time DATETIME NULL COMMENT '开始时间',
    end_time DATETIME NULL COMMENT '结束时间',
    status INT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    owner VARCHAR(100) NULL COMMENT '负责人',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (process_definition_id) REFERENCES t_process_definition(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度计划表';

-- ==============================================
-- 工作流实例表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_process_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    process_definition_id BIGINT NOT NULL COMMENT '关联工作流定义',
    process_definition_version INT DEFAULT 1 COMMENT '工作流版本',
    name VARCHAR(255) NOT NULL COMMENT '实例名称',
    status VARCHAR(50) NOT NULL COMMENT '状态',
    params TEXT NULL COMMENT '运行参数(JSON)',
    start_time DATETIME NULL COMMENT '开始时间',
    end_time DATETIME NULL COMMENT '结束时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    FOREIGN KEY (process_definition_id) REFERENCES t_process_definition(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流实例表';

-- ==============================================
-- 任务实例表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_task_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    process_instance_id BIGINT NOT NULL COMMENT '关联工作流实例',
    name VARCHAR(255) NOT NULL COMMENT '任务名称',
    type VARCHAR(50) NOT NULL COMMENT '任务类型',
    status VARCHAR(50) NOT NULL COMMENT '状态',
    exit_code INT NULL COMMENT '退出码',
    start_time DATETIME NULL COMMENT '开始时间',
    end_time DATETIME NULL COMMENT '结束时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    FOREIGN KEY (process_instance_id) REFERENCES t_process_instance(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务实例表';

-- ==============================================
-- 任务执行日志表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_task_execution_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    task_instance_id BIGINT NOT NULL COMMENT '关联任务实例',
    log_content TEXT NULL COMMENT '日志内容',
    log_time DATETIME NOT NULL COMMENT '日志时间',
    FOREIGN KEY (task_instance_id) REFERENCES t_task_instance(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

-- ==============================================
-- 文件资源表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_file_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(255) NOT NULL COMMENT '文件名',
    path VARCHAR(500) NULL COMMENT '文件路径',
    size BIGINT DEFAULT 0 COMMENT '文件大小',
    type VARCHAR(50) NULL COMMENT '文件类型',
    version INT DEFAULT 1 COMMENT '版本',
    project_id BIGINT NULL COMMENT '所属项目ID',
    is_directory INT DEFAULT 0 COMMENT '是否目录(0-文件,1-目录)',
    parent_id BIGINT NULL COMMENT '父目录ID',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (project_id) REFERENCES t_project(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES t_file_resource(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资源表';

-- ==============================================
-- 数据源表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_datasource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '数据源名称',
    type VARCHAR(50) NOT NULL COMMENT '数据源类型',
    host VARCHAR(255) NOT NULL COMMENT '主机地址',
    port INT NOT NULL COMMENT '端口',
    database_name VARCHAR(100) NOT NULL COMMENT '数据库名',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NULL COMMENT '密码',
    params TEXT NULL COMMENT '额外参数(JSON)',
    project_id BIGINT NULL COMMENT '所属项目ID',
    description TEXT NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (project_id) REFERENCES t_project(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源表';

-- ==============================================
-- 环境变量配置组表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_env_config_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(200) NOT NULL UNIQUE COMMENT '配置组名称',
    description VARCHAR(500) NULL COMMENT '配置组描述',
    owner VARCHAR(100) NULL COMMENT '负责人',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='环境变量配置组表';

-- ==============================================
-- 环境变量表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_env_variable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    group_id BIGINT NULL COMMENT '关联配置组ID',
    name VARCHAR(200) NOT NULL COMMENT '变量名',
    value VARCHAR(500) NULL COMMENT '变量值',
    description VARCHAR(500) NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (group_id) REFERENCES t_env_config_group(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='环境变量表';

-- ==============================================
-- 服务器节点表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_server_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '节点名称',
    hostname VARCHAR(255) NOT NULL COMMENT '主机地址',
    port INT DEFAULT 22 COMMENT 'SSH端口',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NULL COMMENT '密码',
    group_name VARCHAR(100) NULL COMMENT '分组名称',
    status VARCHAR(20) DEFAULT 'OFFLINE' COMMENT '状态',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器节点表';

-- ==============================================
-- 任务类型配置表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_task_type_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型',
    program_path VARCHAR(500) NULL COMMENT '程序路径',
    default_params TEXT NULL COMMENT '默认参数',
    env_params TEXT NULL COMMENT '环境参数',
    description VARCHAR(500) NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_task_type_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务类型配置表';

-- ==============================================
-- 集群配置表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_cluster_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '配置名称',
    type VARCHAR(50) NOT NULL COMMENT '配置类型',
    config_json TEXT NULL COMMENT '配置值(JSON)',
    description VARCHAR(500) NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集群配置表';

-- ==============================================
-- 调度日志表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_schedule_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    schedule_id BIGINT NOT NULL COMMENT '关联调度计划',
    process_definition_id BIGINT NULL COMMENT '关联工作流定义',
    process_instance_id BIGINT NULL COMMENT '关联工作流实例',
    status VARCHAR(50) NOT NULL COMMENT '状态',
    message TEXT NULL COMMENT '消息',
    log_time DATETIME NOT NULL COMMENT '日志时间',
    FOREIGN KEY (schedule_id) REFERENCES t_process_schedule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度日志表';

-- ==============================================
-- 登录日志表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    ip_address VARCHAR(100) NULL COMMENT '登录IP',
    user_agent TEXT NULL COMMENT 'User-Agent',
    status INT NOT NULL COMMENT '登录状态(0-失败,1-成功)',
    error_message VARCHAR(500) NULL COMMENT '错误信息',
    login_time DATETIME NOT NULL COMMENT '登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ==============================================
-- 操作日志表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    module VARCHAR(100) NULL COMMENT '操作模块',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    content TEXT NULL COMMENT '操作内容',
    ip_address VARCHAR(100) NULL COMMENT '操作IP',
    user_agent TEXT NULL COMMENT 'User-Agent',
    status INT DEFAULT 1 COMMENT '操作状态(0-失败,1-成功)',
    error_message VARCHAR(500) NULL COMMENT '错误信息',
    operation_time DATETIME NOT NULL COMMENT '操作时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ==============================================
-- 数据源探活日志表
-- ==============================================
CREATE TABLE IF NOT EXISTS t_datasource_probe_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    datasource_name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    status VARCHAR(20) NOT NULL COMMENT '状态(ONLINE/OFFLINE)',
    response_time INT NULL COMMENT '响应时间(ms)',
    db_type VARCHAR(50) NULL COMMENT '数据库类型',
    db_version VARCHAR(100) NULL COMMENT '数据库版本',
    db_current_time DATETIME NULL COMMENT '数据库当前时间',
    error_message VARCHAR(500) NULL COMMENT '错误信息',
    probe_time DATETIME NOT NULL COMMENT '探活时间',
    FOREIGN KEY (datasource_id) REFERENCES t_datasource(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源探活日志表';

-- ==============================================
-- 创建索引
-- ==============================================
CREATE INDEX IF NOT EXISTS idx_sys_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_t_project_name ON t_project(name);
CREATE INDEX IF NOT EXISTS idx_t_process_definition_project_id ON t_process_definition(project_id);
CREATE INDEX IF NOT EXISTS idx_t_process_schedule_process_id ON t_process_schedule(process_definition_id);
CREATE INDEX IF NOT EXISTS idx_t_process_instance_process_id ON t_process_instance(process_definition_id);
CREATE INDEX IF NOT EXISTS idx_t_task_instance_process_id ON t_task_instance(process_instance_id);
CREATE INDEX IF NOT EXISTS idx_t_file_resource_project_id ON t_file_resource(project_id);
CREATE INDEX IF NOT EXISTS idx_t_datasource_name ON t_datasource(name);
CREATE INDEX IF NOT EXISTS idx_t_login_log_username ON t_login_log(username);
CREATE INDEX IF NOT EXISTS idx_t_operation_log_user_id ON t_operation_log(user_id);
CREATE INDEX IF NOT EXISTS idx_t_schedule_log_schedule_id ON t_schedule_log(schedule_id);

COMMIT;

-- ==============================================
-- 表结构创建完成
-- ==============================================
SELECT '表结构创建完成' AS message;