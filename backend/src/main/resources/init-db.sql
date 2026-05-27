-- 任务计划工具箱系统 - 数据库初始化脚本
-- 版本: v1.0
-- 日期: 2026-05-20

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS task_plan_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE task_plan_db;

-- ==============================================
-- 系统用户表
-- ==============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    real_name VARCHAR(100) NULL COMMENT '真实姓名',
    email VARCHAR(255) NULL COMMENT '邮箱',
    phone VARCHAR(50) NULL COMMENT '手机号',
    status INT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ==============================================
-- 系统角色表
-- ==============================================
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description TEXT NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间'
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
    name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    path VARCHAR(255) NULL COMMENT '路由路径',
    icon VARCHAR(100) NULL COMMENT '图标',
    parent_id BIGINT DEFAULT NULL COMMENT '父菜单ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    type INT DEFAULT 0 COMMENT '类型(0-菜单,1-按钮)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
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
CREATE TABLE IF NOT EXISTS sys_config (
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
CREATE TABLE IF NOT EXISTS project (
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
CREATE TABLE IF NOT EXISTS process_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(255) NOT NULL COMMENT '工作流名称',
    description TEXT NULL COMMENT '描述',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    tasks TEXT NULL COMMENT '任务列表(JSON)',
    connections TEXT NULL COMMENT '连接关系(JSON)',
    status INT DEFAULT 0 COMMENT '状态(0-草稿,1-已发布)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流定义表';

-- ==============================================
-- 任务定义表
-- ==============================================
CREATE TABLE IF NOT EXISTS task_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(255) NOT NULL COMMENT '任务名称',
    type VARCHAR(50) NOT NULL COMMENT '任务类型',
    project_id BIGINT NOT NULL COMMENT '所属项目ID',
    config TEXT NULL COMMENT '任务配置(JSON)',
    description TEXT NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务定义表';

-- ==============================================
-- 调度计划表
-- ==============================================
CREATE TABLE IF NOT EXISTS process_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    process_definition_id BIGINT NOT NULL COMMENT '关联工作流定义',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    start_time DATETIME NULL COMMENT '开始时间',
    end_time DATETIME NULL COMMENT '结束时间',
    status INT DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (process_definition_id) REFERENCES process_definition(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度计划表';

-- ==============================================
-- 工作流实例表
-- ==============================================
CREATE TABLE IF NOT EXISTS process_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    process_definition_id BIGINT NOT NULL COMMENT '关联工作流定义',
    name VARCHAR(255) NOT NULL COMMENT '实例名称',
    status VARCHAR(50) NOT NULL COMMENT '状态',
    params TEXT NULL COMMENT '运行参数(JSON)',
    start_time DATETIME NULL COMMENT '开始时间',
    end_time DATETIME NULL COMMENT '结束时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    FOREIGN KEY (process_definition_id) REFERENCES process_definition(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流实例表';

-- ==============================================
-- 任务实例表
-- ==============================================
CREATE TABLE IF NOT EXISTS task_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    process_instance_id BIGINT NOT NULL COMMENT '关联工作流实例',
    task_definition_id BIGINT NOT NULL COMMENT '关联任务定义',
    name VARCHAR(255) NOT NULL COMMENT '任务名称',
    type VARCHAR(50) NOT NULL COMMENT '任务类型',
    status VARCHAR(50) NOT NULL COMMENT '状态',
    start_time DATETIME NULL COMMENT '开始时间',
    end_time DATETIME NULL COMMENT '结束时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    FOREIGN KEY (process_instance_id) REFERENCES process_instance(id) ON DELETE CASCADE,
    FOREIGN KEY (task_definition_id) REFERENCES task_definition(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务实例表';

-- ==============================================
-- 任务日志表
-- ==============================================
CREATE TABLE IF NOT EXISTS task_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    task_instance_id BIGINT NOT NULL COMMENT '关联任务实例',
    log_content TEXT NULL COMMENT '日志内容',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    FOREIGN KEY (task_instance_id) REFERENCES task_instance(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务日志表';

-- ==============================================
-- 文件资源表
-- ==============================================
CREATE TABLE IF NOT EXISTS file_resource (
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
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES file_resource(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资源表';

-- ==============================================
-- 数据源表
-- ==============================================
CREATE TABLE IF NOT EXISTS datasource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(255) NOT NULL UNIQUE COMMENT '数据源名称',
    type VARCHAR(50) NOT NULL COMMENT '数据源类型',
    host VARCHAR(255) NOT NULL COMMENT '主机地址',
    port INT NOT NULL COMMENT '端口',
    database_name VARCHAR(255) NOT NULL COMMENT '数据库名',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    project_id BIGINT NULL COMMENT '所属项目ID',
    description TEXT NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
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
CREATE TABLE IF NOT EXISTS server_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '节点名称',
    host VARCHAR(255) NOT NULL COMMENT '主机地址',
    port INT DEFAULT 22 COMMENT 'SSH端口',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NULL COMMENT '密码',
    status INT DEFAULT 1 COMMENT '状态(0-离线,1-在线)',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器节点表';

-- ==============================================
-- 任务类型配置表
-- ==============================================
CREATE TABLE IF NOT EXISTS task_type_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    type VARCHAR(50) NOT NULL UNIQUE COMMENT '任务类型',
    name VARCHAR(100) NOT NULL COMMENT '类型名称',
    icon VARCHAR(100) NULL COMMENT '图标',
    description TEXT NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务类型配置表';

-- ==============================================
-- 集群配置表
-- ==============================================
CREATE TABLE IF NOT EXISTS cluster_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '配置名称',
    config_value TEXT NOT NULL COMMENT '配置值(JSON)',
    description TEXT NULL COMMENT '描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集群配置表';

-- ==============================================
-- 调度日志表
-- ==============================================
CREATE TABLE IF NOT EXISTS schedule_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    process_schedule_id BIGINT NOT NULL COMMENT '关联调度计划',
    process_instance_id BIGINT NULL COMMENT '关联工作流实例',
    status VARCHAR(50) NOT NULL COMMENT '状态',
    message TEXT NULL COMMENT '消息',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    FOREIGN KEY (process_schedule_id) REFERENCES process_schedule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度日志表';

-- ==============================================
-- 插入初始数据
-- ==============================================

-- 插入管理员角色
INSERT INTO sys_role (id, name, code, description, create_time) 
VALUES (1, '系统管理员', 'ADMIN', '系统管理员角色，拥有所有权限', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- 插入普通用户角色
INSERT INTO sys_role (id, name, code, description, create_time) 
VALUES (2, '普通用户', 'USER', '普通用户角色', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- 插入管理员用户（密码: 123456，BCrypt加密）
INSERT INTO sys_user (id, username, password, real_name, email, phone, status, create_time, update_time) 
VALUES (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '系统管理员', 'admin@example.com', '13800138000', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE real_name=VALUES(real_name), email=VALUES(email), phone=VALUES(phone);

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- 插入系统菜单
-- 一级菜单
INSERT INTO sys_menu (id, name, path, icon, parent_id, sort_order, type, create_time) VALUES
(1, '工作台', '/dashboard', 'Dashboard', NULL, 1, 0, NOW()),
(2, '工作流管理', '/workflows', 'Workflow', NULL, 2, 0, NOW()),
(3, '调度中心', '/schedules', 'Clock', NULL, 3, 0, NOW()),
(4, '资源管理', '', 'FolderOpen', NULL, 4, 0, NOW()),
(5, '系统管理', '', 'Settings', NULL, 5, 0, NOW()),
(6, '数据源管理', '/datasources', 'Database', NULL, 6, 0, NOW()),
(7, '执行管理', '/executions', 'Play', NULL, 7, 0, NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), path=VALUES(path), icon=VALUES(icon);

-- 工作流管理子菜单
INSERT INTO sys_menu (id, name, path, icon, parent_id, sort_order, type, create_time) VALUES
(101, '工作流列表', '/workflows', 'List', 2, 1, 0, NOW()),
(102, '工作流设计', '/workflows/design', 'Edit', 2, 2, 0, NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), path=VALUES(path);

-- 资源管理子菜单
INSERT INTO sys_menu (id, name, path, icon, parent_id, sort_order, type, create_time) VALUES
(201, '项目管理', '/projects', 'Briefcase', 4, 1, 0, NOW()),
(202, '文件管理', '/files', 'FileText', 4, 2, 0, NOW()),
(203, '任务类型', '/task-types', 'Tag', 4, 3, 0, NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), path=VALUES(path);

-- 系统管理子菜单
INSERT INTO sys_menu (id, name, path, icon, parent_id, sort_order, type, create_time) VALUES
(301, '用户管理', '/system/users', 'User', 5, 1, 0, NOW()),
(302, '角色管理', '/system/roles', 'UserGroup', 5, 2, 0, NOW()),
(303, '菜单管理', '/system/menus', 'Menu', 5, 3, 0, NOW()),
(304, '系统配置', '/system/config', 'Settings', 5, 4, 0, NOW()),
(305, '服务器管理', '/system/servers', 'Server', 5, 5, 0, NOW()),
(306, '登录日志', '/system/login-logs', 'FileSearch', 5, 6, 0, NOW()),
(307, '操作日志', '/system/operation-logs', 'FileText', 5, 7, 0, NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), path=VALUES(path);

-- 角色菜单关联（管理员拥有所有菜单）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7),
(1, 101), (1, 102), (1, 201), (1, 202), (1, 203),
(1, 301), (1, 302), (1, 303), (1, 304), (1, 305), (1, 306), (1, 307)
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);

-- 插入任务类型配置
INSERT INTO task_type_config (type, name, icon, description, create_time) VALUES
('SHELL', 'Shell脚本', 'Terminal', '执行Shell脚本任务', NOW()),
('PYTHON', 'Python脚本', 'Code', '执行Python脚本任务', NOW()),
('JAVA', 'Java程序', 'Coffee', '运行Java类', NOW()),
('SQL', 'SQL查询', 'Database', '执行数据库SQL', NOW()),
('HTTP', 'HTTP请求', 'Globe', '调用REST API', NOW()),
('SPARK', 'Spark任务', 'Zap', '运行Spark作业', NOW()),
('HIVE', 'Hive任务', 'Database', '执行Hive SQL', NOW()),
('MR', 'MapReduce任务', 'Zap', '运行MapReduce作业', NOW()),
('FLINK', 'Flink任务', 'Activity', '运行Flink作业', NOW()),
('PIG', 'Pig任务', 'Code', '执行Pig脚本', NOW()),
('HBASE', 'HBase任务', 'Database', '操作HBase', NOW()),
('KAFKA', 'Kafka任务', 'MessageSquare', 'Kafka消息任务', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), icon=VALUES(icon), description=VALUES(description);

-- 插入系统配置
INSERT INTO sys_config (config_key, config_value, description, create_time, update_time) VALUES
('upload.path', './uploads', '文件上传路径', NOW(), NOW()),
('upload.maxSize', '104857600', '最大文件大小(字节)', NOW(), NOW()),
('upload.allowedTypes', 'txt,sh,py,sql,java,xml,json,yaml,yml,md,html,css,js', '允许的文件类型', NOW(), NOW()),
('upload.maxSizeMB', '100', '最大文件大小(MB)', NOW(), NOW()),
('upload.maxChunks', '100', '最大分块数', NOW(), NOW()),
('schedule.log.path', './logs/scheduler', '调度日志路径', NOW(), NOW()),
('task.log.path', './logs/tasks', '任务日志路径', NOW(), NOW()),
('task.instance.path', './instances', '任务实例保存路径', NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value), description=VALUES(description);

-- 插入默认项目
INSERT INTO project (id, name, description, status, create_time, update_time) VALUES
(1, '默认项目', '系统默认项目', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_process_definition_project_id ON process_definition(project_id);
CREATE INDEX IF NOT EXISTS idx_task_definition_project_id ON task_definition(project_id);
CREATE INDEX IF NOT EXISTS idx_process_schedule_process_id ON process_schedule(process_definition_id);
CREATE INDEX IF NOT EXISTS idx_process_instance_process_id ON process_instance(process_definition_id);
CREATE INDEX IF NOT EXISTS idx_task_instance_process_instance_id ON task_instance(process_instance_id);
CREATE INDEX IF NOT EXISTS idx_file_resource_project_id ON file_resource(project_id);
CREATE INDEX IF NOT EXISTS idx_file_resource_parent_id ON file_resource(parent_id);
CREATE INDEX IF NOT EXISTS idx_datasource_project_id ON datasource(project_id);
CREATE INDEX IF NOT EXISTS idx_sys_user_username ON sys_user(username);

COMMIT;

-- ==============================================
-- 初始化完成
-- ==============================================
SELECT '数据库初始化完成' AS message;
