-- 添加数据源探活日志表的新字段
USE task_plan;

ALTER TABLE t_datasource_probe_log 
ADD COLUMN db_type VARCHAR(50) NULL,
ADD COLUMN db_version VARCHAR(100) NULL,
ADD COLUMN db_current_time DATETIME NULL;

COMMIT;

SELECT '字段添加完成' AS message;