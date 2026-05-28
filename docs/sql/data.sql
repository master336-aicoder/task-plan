-- ==============================================
-- 任务计划工具箱系统 - 初始化数据脚本
-- 版本: v1.0
-- 日期: 2026-05-28
-- 依赖: 需先执行 schema.sql
-- ==============================================

USE task_plan;

-- ==============================================
-- 插入系统角色
-- ==============================================
INSERT INTO sys_role (id, role_name, role_code, description, status, create_time, update_time) VALUES
(1, '系统管理员', 'ADMIN', '系统管理员角色，拥有所有权限', 1, NOW(), NOW()),
(2, '普通用户', 'USER', '普通用户角色，拥有基础操作权限', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name), description=VALUES(description);

-- ==============================================
-- 插入管理员用户（密码: 123456，BCrypt加密）
-- ==============================================
INSERT INTO sys_user (id, username, password, real_name, email, phone, status, is_deleted, create_time, update_time) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '系统管理员', 'admin@example.com', '13800138000', 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE real_name=VALUES(real_name), email=VALUES(email), phone=VALUES(phone);

-- ==============================================
-- 关联用户角色
-- ==============================================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

-- ==============================================
-- 插入系统菜单
-- ==============================================

-- 一级菜单
INSERT INTO sys_menu (id, menu_name, menu_code, path, component, icon, parent_id, sort_order, menu_type, permission, status, create_time, update_time) VALUES
(1, '工作台', 'dashboard', '/dashboard', 'dashboard/index', 'LayoutDashboard', NULL, 1, 0, 'dashboard:view', 1, NOW(), NOW()),
(2, '工作流管理', 'workflow', '/workflows', 'workflow/index', 'GitBranch', NULL, 2, 0, 'workflow:view', 1, NOW(), NOW()),
(3, '调度中心', 'schedule', '/schedules', 'schedule/index', 'Clock', NULL, 3, 0, 'schedule:view', 1, NOW(), NOW()),
(4, '执行管理', 'execution', '/executions', 'execution/index', 'Play', NULL, 4, 0, 'execution:view', 1, NOW(), NOW()),
(5, '资源管理', 'resource', '', '', 'FolderOpen', NULL, 5, 0, 'resource:view', 1, NOW(), NOW()),
(6, '数据源管理', 'datasource', '/datasources', 'datasource/index', 'Database', NULL, 6, 0, 'datasource:view', 1, NOW(), NOW()),
(7, '系统管理', 'system', '', '', 'Settings', NULL, 7, 0, 'system:view', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), path=VALUES(path), component=VALUES(component), icon=VALUES(icon);

-- 工作流管理子菜单
INSERT INTO sys_menu (id, menu_name, menu_code, path, component, icon, parent_id, sort_order, menu_type, permission, status, create_time, update_time) VALUES
(101, '工作流列表', 'workflow:list', '/workflows', 'workflow/index', 'List', 2, 1, 0, 'workflow:list', 1, NOW(), NOW()),
(102, '工作流设计', 'workflow:design', '/workflows/design', 'workflow/design', 'Edit', 2, 2, 0, 'workflow:design', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), path=VALUES(path), component=VALUES(component);

-- 资源管理子菜单
INSERT INTO sys_menu (id, menu_name, menu_code, path, component, icon, parent_id, sort_order, menu_type, permission, status, create_time, update_time) VALUES
(201, '项目管理', 'project:list', '/projects', 'project/index', 'Briefcase', 5, 1, 0, 'project:list', 1, NOW(), NOW()),
(202, '文件管理', 'file:list', '/files', 'file/index', 'FolderOpen', 5, 2, 0, 'file:list', 1, NOW(), NOW()),
(203, '任务类型', 'taskType:list', '/task-types', 'task-type/index', 'Tag', 5, 3, 0, 'taskType:list', 1, NOW(), NOW()),
(204, '环境变量', 'env:list', '/env-variables', 'env-variable/index', 'Variable', 5, 4, 0, 'env:list', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), path=VALUES(path), component=VALUES(component);

-- 系统管理子菜单
INSERT INTO sys_menu (id, menu_name, menu_code, path, component, icon, parent_id, sort_order, menu_type, permission, status, create_time, update_time) VALUES
(301, '用户管理', 'user:list', '/system/users', 'system/user/index', 'User', 7, 1, 0, 'user:list', 1, NOW(), NOW()),
(302, '角色管理', 'role:list', '/system/roles', 'system/role/index', 'UserGroup', 7, 2, 0, 'role:list', 1, NOW(), NOW()),
(303, '菜单管理', 'menu:list', '/system/menus', 'system/menu/index', 'Menu', 7, 3, 0, 'menu:list', 1, NOW(), NOW()),
(304, '系统配置', 'config:list', '/system/config', 'system/config/index', 'Settings', 7, 4, 0, 'config:list', 1, NOW(), NOW()),
(305, '服务器管理', 'server:list', '/system/servers', 'system/server/index', 'Server', 7, 5, 0, 'server:list', 1, NOW(), NOW()),
(306, '集群配置', 'cluster:list', '/cluster-configs', 'system/cluster/index', 'Network', 7, 6, 0, 'cluster:list', 1, NOW(), NOW()),
(307, '登录日志', 'loginLog:list', '/system/login-logs', 'system/login-log/index', 'FileSearch', 7, 7, 0, 'loginLog:list', 1, NOW(), NOW()),
(308, '操作日志', 'operationLog:list', '/system/operation-logs', 'system/operation-log/index', 'FileText', 7, 8, 0, 'operationLog:list', 1, NOW(), NOW()),
(309, '调度日志', 'scheduleLog:list', '/system/schedule-logs', 'system/schedule-log/index', 'Clock', 7, 9, 0, 'scheduleLog:list', 1, NOW(), NOW()),
(310, '任务执行日志', 'taskLog:list', '/system/task-execution-logs', 'system/task-execution-log/index', 'ScrollText', 7, 10, 0, 'taskLog:list', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE menu_name=VALUES(menu_name), path=VALUES(path), component=VALUES(component);

-- ==============================================
-- 角色菜单关联（管理员拥有所有菜单）
-- ==============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- 一级菜单
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7),
-- 工作流管理子菜单
(1, 101), (1, 102),
-- 资源管理子菜单
(1, 201), (1, 202), (1, 203), (1, 204),
-- 系统管理子菜单
(1, 301), (1, 302), (1, 303), (1, 304), (1, 305), (1, 306), (1, 307), (1, 308), (1, 309), (1, 310)
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);

-- ==============================================
-- 插入系统配置
-- ==============================================
INSERT INTO t_sys_config (config_key, config_value, description, create_time, update_time) VALUES
('file.rootpath', './', '文件存储根路径', NOW(), NOW()),
('upload.path', './uploads', '文件上传路径', NOW(), NOW()),
('upload.maxSize', '104857600', '最大文件大小(字节)', NOW(), NOW()),
('upload.maxSizeMB', '100', '最大文件大小(MB)', NOW(), NOW()),
('upload.allowedTypes', 'txt,doc,docx,xls,xlsx,ppt,pptx,pdf,jpg,jpeg,png,gif,bmp,zip,rar,7z,tgz,tar,sh,py,pl,jar,sql', '允许的文件类型', NOW(), NOW()),
('upload.maxChunks', '100', '最大分块数', NOW(), NOW()),
('schedule.log.path', './logs/scheduler', '调度日志路径', NOW(), NOW()),
('task.log.path', './logs/tasks', '任务日志路径', NOW(), NOW()),
('task.instance.path', './instances', '任务实例保存路径', NOW(), NOW()),
('mail.host', '', '邮件服务器地址', NOW(), NOW()),
('mail.port', '25', '邮件服务器端口', NOW(), NOW()),
('mail.username', '', '邮件服务器用户名', NOW(), NOW()),
('mail.password', '', '邮件服务器密码', NOW(), NOW()),
('mail.from', '', '发件人邮箱', NOW(), NOW())
ON DUPLICATE KEY UPDATE config_value=VALUES(config_value), description=VALUES(description);

-- ==============================================
-- 插入任务类型配置
-- ==============================================
INSERT INTO t_task_type_config (name, task_type, program_path, default_params, env_params, description, create_time, update_time) VALUES
('Shell默认', 'SHELL', '/bin/bash', '', '', '默认Shell脚本执行器', NOW(), NOW()),
('Python3', 'PYTHON', '/usr/bin/python3', '', '', 'Python3脚本执行器', NOW(), NOW()),
('Java8', 'JAVA', '/usr/lib/jvm/java-8-openjdk-amd64/bin/java', '', '', 'Java8运行环境', NOW(), NOW()),
('MySQL', 'SQL', '', '', '', 'MySQL数据库SQL执行', NOW(), NOW()),
('PostgreSQL', 'SQL', '', '', '', 'PostgreSQL数据库SQL执行', NOW(), NOW()),
('HTTP请求', 'HTTP', '', '', '', 'HTTP REST API调用', NOW(), NOW()),
('Spark', 'SPARK', '/usr/bin/spark-submit', '', '', 'Spark作业提交', NOW(), NOW()),
('Flink', 'FLINK', '/usr/bin/flink', '', '', 'Flink作业提交', NOW(), NOW()),
('Hive', 'HIVE', '/usr/bin/hive', '', '', 'Hive SQL执行', NOW(), NOW()),
('Kafka', 'KAFKA', '', '', '', 'Kafka消息任务', NOW(), NOW())
ON DUPLICATE KEY UPDATE program_path=VALUES(program_path), description=VALUES(description);

-- ==============================================
-- 插入集群配置
-- ==============================================
INSERT INTO t_cluster_config (name, type, config_json, description, create_time, update_time) VALUES
('Local Standalone', 'STANDALONE', '{}', '本地单机模式', NOW(), NOW()),
('Local YARN', 'YARN', '{}', '本地YARN集群', NOW(), NOW()),
('Local Flink', 'FLINK', '{}', '本地Flink集群', NOW(), NOW())
ON DUPLICATE KEY UPDATE type=VALUES(type), description=VALUES(description);

-- ==============================================
-- 插入默认项目
-- ==============================================
INSERT INTO t_project (id, name, description, status, create_time, update_time) VALUES
(1, '默认项目', '系统默认项目', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ==============================================
-- 插入环境变量配置组
-- ==============================================
INSERT INTO t_env_config_group (name, description, owner, create_time, update_time) VALUES
('默认配置组', '系统默认环境变量配置组', 'admin', NOW(), NOW()),
('开发环境', '开发环境变量配置', 'admin', NOW(), NOW()),
('生产环境', '生产环境变量配置', 'admin', NOW(), NOW())
ON DUPLICATE KEY UPDATE description=VALUES(description);

COMMIT;

-- ==============================================
-- 初始化完成
-- ==============================================
SELECT '初始化数据完成' AS message;
SELECT '默认管理员账号: admin / 123456' AS login_info;