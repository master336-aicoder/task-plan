# 任务计划工具箱系统 - 设计文档

## 1. 需求分析

### 1.1 业务背景

参照 **Apache DolphinScheduler** 设计，构建一个企业级任务计划管理系统，支持：
- 可视化工作流编排与设计
- 多种任务类型支持（Shell、Python、Java、SQL、HTTP、Spark、Flink等）
- 灵活的Cron表达式调度配置
- 完整的RBAC权限管理
- 远程SSH任务执行
- 工作流版本管理与对比回滚

### 1.2 功能需求

| 模块 | 功能点 | 需求描述 | 优先级 | 状态 |
|------|--------|----------|--------|------|
| 工作流设计 | DAG编辑器 | 可视化拖拽设计工作流 | 高 | 已实现 |
| 工作流设计 | 任务节点 | 支持多种任务类型 | 高 | 已实现 |
| 工作流设计 | 连线配置 | 定义任务依赖关系 | 高 | 已实现 |
| 工作流设计 | 双击编辑 | 任务详情编辑 | 高 | 已实现 |
| 工作流设计 | 版本管理 | 版本递增、对比、回滚 | 高 | 已实现 |
| 任务管理 | 任务定义 | 创建/编辑/删除任务 | 高 | 已实现 |
| 任务管理 | 脚本管理 | 脚本文件上传与编辑 | 高 | 已实现 |
| 调度管理 | Cron配置 | 图形化Cron编辑器 | 高 | 已实现 |
| 调度管理 | 计划管理 | 调度计划的CRUD | 高 | 已实现 |
| 调度管理 | 立即执行 | 手动触发工作流 | 高 | 已实现 |
| 执行管理 | 实例监控 | 工作流/任务实例查看 | 中 | 已实现 |
| 执行管理 | 日志追踪 | 任务执行日志查看 | 中 | 已实现 |
| 执行管理 | 失败重试 | 任务重试机制 | 中 | 已实现 |
| 远程执行 | SSH连接 | 远程服务器执行 | 中 | 已实现 |
| 资源管理 | 项目管理 | 项目创建与配置 | 中 | 已实现 |
| 资源管理 | 文件管理 | 文件上传与管理 | 中 | 已实现 |
| 资源管理 | 环境变量 | 多组配置值管理 | 中 | 已实现 |
| 系统管理 | 用户管理 | 用户CRUD与角色分配 | 高 | 已实现 |
| 系统管理 | 角色管理 | 角色定义与权限配置 | 高 | 已实现 |
| 系统管理 | 菜单管理 | 菜单配置 | 中 | 已实现 |
| 系统管理 | 服务器管理 | SSH节点配置 | 中 | 已实现 |
| 数据源管理 | 数据源配置 | 多类型数据源支持 | 中 | 已实现 |

### 1.3 非功能需求

| 需求类型 | 描述 |
|----------|------|
| 性能 | 支持1000+并发任务执行 |
| 可用性 | 99.9%系统可用性 |
| 安全性 | JWT认证、RBAC权限控制 |
| 扩展性 | 支持任务类型插件化扩展 |

## 2. 架构设计

### 2.1 技术选型

| 层级 | 技术 | 版本 | 选型理由 |
|------|------|------|----------|
| 前端框架 | Vue | 3.4.x | 响应式设计，生态成熟 |
| 前端语言 | TypeScript | 5.4.x | 类型安全，开发效率高 |
| 构建工具 | Vite | 4.5.x | 快速构建，热更新 |
| UI组件 | Element Plus | 2.6.x | 组件丰富，文档完善 |
| 后端框架 | Spring Boot | 2.7.x | 社区成熟，生态完善 |
| 调度引擎 | Quartz | 2.3.x | 成熟稳定，功能强大 |
| 数据库 | MySQL | 8.0+ | 性能优异，社区活跃 |
| ORM框架 | Spring Data JPA | 2.7.x | 简化数据访问 |
| 认证机制 | JWT | 0.12.x | 无状态认证，易于扩展 |
| SSH客户端 | JSch | 0.1.55 | 成熟的Java SSH库 |

### 2.2 架构风格

采用经典的分层架构，职责清晰：

```
┌─────────────────────────────────────────────────────────────┐
│                    表现层 (Presentation)                    │
│  Vue 3 + TypeScript + Element Plus                         │
├─────────────────────────────────────────────────────────────┤
│                    控制层 (Controller)                      │
│  REST API + Spring Security + JWT                          │
├─────────────────────────────────────────────────────────────┤
│                    业务层 (Service)                         │
│  业务逻辑处理 + 事务管理 + Quartz调度                       │
├─────────────────────────────────────────────────────────────┤
│                    数据访问层 (Repository)                  │
│  Spring Data JPA                                           │
├─────────────────────────────────────────────────────────────┤
│                    数据存储层 (Database)                    │
│  MySQL + 文件系统 + Redis缓存                              │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 核心模块划分

| 模块 | 包名 | 职责 |
|------|------|------|
| 认证模块 | `controller/auth`, `service/auth` | 用户认证、JWT管理 |
| 工作流模块 | `controller/process`, `service/process` | 工作流定义、版本管理 |
| 任务模块 | `controller/task`, `service/task` | 任务定义与管理 |
| 调度模块 | `controller/schedule`, `service/schedule` | 调度计划、Quartz集成 |
| 文件模块 | `controller/file`, `service/file` | 文件资源管理 |
| 执行模块 | `service/execution` | 任务执行、SSH远程执行 |
| 系统模块 | `controller/sys`, `service/sys` | 用户/角色/菜单管理 |

### 2.4 关键设计决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| 认证方式 | JWT无状态认证 | 易于水平扩展，无session依赖 |
| 权限控制 | RBAC模型 | 灵活的角色权限管理 |
| 工作流引擎 | 自研轻量级引擎 | 避免引入复杂依赖，易于定制 |
| 文件存储 | 本地文件系统 | 简单直接，后期可扩展到分布式存储 |
| 远程执行 | JSch SSH客户端 | 成熟稳定的Java SSH实现 |
| 版本管理 | 历史版本快照 | 支持版本对比与回滚 |

## 3. 数据库设计

### 3.1 核心实体

| 实体 | 表名 | 说明 |
|------|------|------|
| Project | `project` | 项目信息 |
| ProcessDefinition | `process_definition` | 工作流定义 |
| ProcessDefinitionHistory | `process_definition_history` | 工作流历史版本 |
| TaskDefinition | `task_definition` | 任务定义 |
| ProcessSchedule | `process_schedule` | 调度计划 |
| ProcessInstance | `process_instance` | 工作流实例 |
| TaskInstance | `task_instance` | 任务实例 |
| TaskExecutionLog | `task_execution_log` | 任务执行日志 |
| FileResource | `file_resource` | 文件资源 |
| Datasource | `datasource` | 数据源配置 |
| EnvVariable | `t_env_variable` | 环境变量 |
| ServerNode | `server_node` | 服务器节点 |
| SysUser | `sys_user` | 系统用户 |
| SysRole | `sys_role` | 系统角色 |
| SysMenu | `sys_menu` | 系统菜单 |

### 3.2 实体关系图

```
Project 1 ─── * ProcessDefinition
                │
                ├── * ProcessDefinitionHistory (历史版本)
                ├── * TaskDefinition
                ├── * ProcessSchedule
                └── * ProcessInstance
                        └── * TaskInstance
                                └── * TaskExecutionLog

Project 1 ─── * FileResource
Project 1 ─── * Datasource

SysUser 1 ─── * SysUserRole * ─── 1 SysRole 1 ─── * SysRoleMenu * ─── 1 SysMenu

EnvVariable (环境变量)
ServerNode (服务器节点)
TaskTypeConfig (任务类型配置)
ClusterConfig (集群配置)
```

### 3.3 核心表结构

#### 3.3.1 process_definition (工作流定义)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| name | VARCHAR(255) | NOT NULL | 工作流名称 |
| description | TEXT | NULL | 描述 |
| project_id | BIGINT | FOREIGN KEY | 所属项目ID |
| tasks | TEXT | NULL | 任务列表(JSON) |
| connections | TEXT | NULL | 连接关系(JSON) |
| status | INT | DEFAULT 0 | 状态(0-草稿,1-已发布) |
| version | INT | DEFAULT 1 | 版本号 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |

#### 3.3.2 process_definition_history (工作流历史版本)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| process_definition_id | BIGINT | FOREIGN KEY | 关联工作流定义 |
| version | INT | NOT NULL | 历史版本号 |
| name | VARCHAR(255) | NOT NULL | 工作流名称 |
| tasks | TEXT | NULL | 任务列表(JSON) |
| connections | TEXT | NULL | 连接关系(JSON) |
| create_time | DATETIME | NOT NULL | 创建时间 |

#### 3.3.3 task_definition (任务定义)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| name | VARCHAR(255) | NOT NULL | 任务名称 |
| type | VARCHAR(50) | NOT NULL | 任务类型 |
| project_id | BIGINT | FOREIGN KEY | 所属项目ID |
| config | TEXT | NULL | 任务配置(JSON) |
| description | TEXT | NULL | 描述 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |

#### 3.3.4 process_schedule (调度计划)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| process_definition_id | BIGINT | FOREIGN KEY | 关联工作流定义 |
| cron_expression | VARCHAR(100) | NOT NULL | Cron表达式 |
| start_time | DATETIME | NULL | 开始时间 |
| end_time | DATETIME | NULL | 结束时间 |
| timezone | VARCHAR(50) | NULL | 时区 |
| status | INT | DEFAULT 1 | 状态(0-禁用,1-启用) |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |

#### 3.3.5 t_env_variable (环境变量)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| config_name | VARCHAR(200) | NULL | 配置名称(分组) |
| name | VARCHAR(200) | NOT NULL | 变量名 |
| value | VARCHAR(500) | NULL | 变量值 |
| category | VARCHAR(100) | NULL | 分类 |
| description | VARCHAR(500) | NULL | 描述 |
| create_time | DATETIME | NOT NULL | 创建时间 |
| update_time | DATETIME | NOT NULL | 更新时间 |

#### 3.3.6 task_execution_log (任务执行日志)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| process_instance_id | BIGINT | FOREIGN KEY | 关联工作流实例 |
| task_node_id | VARCHAR(100) | NULL | 任务节点ID |
| task_name | VARCHAR(255) | NULL | 任务名称 |
| task_type | VARCHAR(50) | NULL | 任务类型 |
| server_ip | VARCHAR(50) | NULL | 执行服务器IP |
| status | VARCHAR(50) | NULL | 执行状态 |
| output | TEXT | NULL | 输出内容 |
| error | TEXT | NULL | 错误信息 |
| start_time | DATETIME | NULL | 开始时间 |
| end_time | DATETIME | NULL | 结束时间 |
| create_time | DATETIME | NOT NULL | 创建时间 |

## 4. API接口设计

### 4.1 认证接口

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| `/api/auth/login` | POST | `AuthController.java` | 用户登录 |
| `/api/auth/logout` | POST | `AuthController.java` | 用户登出 |
| `/api/auth/userinfo` | GET | `AuthController.java` | 获取当前用户信息 |

### 4.2 工作流接口

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| `/api/process-definitions` | GET | `ProcessDefinitionController.java` | 获取工作流列表 |
| `/api/process-definitions` | POST | `ProcessDefinitionController.java` | 创建工作流 |
| `/api/process-definitions/{id}` | GET | `ProcessDefinitionController.java` | 获取工作流详情 |
| `/api/process-definitions/{id}` | PUT | `ProcessDefinitionController.java` | 更新工作流 |
| `/api/process-definitions/{id}` | DELETE | `ProcessDefinitionController.java` | 删除工作流 |
| `/api/process-definitions/{id}/release` | POST | `ProcessDefinitionController.java` | 发布工作流 |
| `/api/process-definitions/{id}/offline` | POST | `ProcessDefinitionController.java` | 下线工作流 |
| `/api/process-definitions/{id}/history` | GET | `ProcessDefinitionController.java` | 获取历史版本 |
| `/api/process-definitions/{id}/history/{version}` | GET | `ProcessDefinitionController.java` | 获取指定版本 |
| `/api/process-definitions/{id}/history/compare-with-current` | GET | `ProcessDefinitionController.java` | 版本对比 |
| `/api/process-definitions/{id}/history/{version}/rollback` | POST | `ProcessDefinitionController.java` | 版本回滚 |

### 4.3 调度接口

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| `/api/process-schedules` | GET | `ProcessScheduleController.java` | 获取调度列表 |
| `/api/process-schedules` | POST | `ProcessScheduleController.java` | 创建调度 |
| `/api/process-schedules/{id}` | PUT | `ProcessScheduleController.java` | 更新调度 |
| `/api/process-schedules/{id}` | DELETE | `ProcessScheduleController.java` | 删除调度 |
| `/api/process-schedules/{id}/enable` | POST | `ProcessScheduleController.java` | 启用调度 |
| `/api/process-schedules/{id}/disable` | POST | `ProcessScheduleController.java` | 禁用调度 |

### 4.4 执行接口

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| `/api/process-instances` | GET | `ProcessInstanceController.java` | 获取实例列表 |
| `/api/process-instances/{id}` | GET | `ProcessInstanceController.java` | 获取实例详情 |
| `/api/process-instances/start` | POST | `ProcessInstanceController.java` | 启动工作流 |
| `/api/process-instances/{id}/stop` | POST | `ProcessInstanceController.java` | 停止工作流 |
| `/api/process-instances/{id}/retry` | POST | `ProcessInstanceController.java` | 重试工作流 |

### 4.5 任务执行日志接口

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| `/api/task-execution-logs` | GET | `TaskExecutionLogController.java` | 获取执行日志列表 |
| `/api/task-execution-logs/{id}` | GET | `TaskExecutionLogController.java` | 获取日志详情 |
| `/api/task-execution-logs/process/{processInstanceId}` | GET | `TaskExecutionLogController.java` | 按实例ID查询 |

### 4.6 环境变量接口

| API路径 | HTTP方法 | 所属文件 | 功能描述 |
|---------|----------|----------|----------|
| `/api/env-variables` | GET | `EnvVariableController.java` | 获取环境变量列表 |
| `/api/env-variables` | POST | `EnvVariableController.java` | 创建环境变量 |
| `/api/env-variables/{id}` | PUT | `EnvVariableController.java` | 更新环境变量 |
| `/api/env-variables/{id}` | DELETE | `EnvVariableController.java` | 删除环境变量 |

### 4.7 响应格式

统一响应格式：
```json
{
  "code": 200000,
  "msg": "success",
  "data": {},
  "timestamp": 1672531200000
}
```

分页响应格式：
```json
{
  "code": 200000,
  "msg": "success",
  "data": {
    "data": [],
    "total": 100
  },
  "timestamp": 1672531200000
}
```

## 5. 前端架构设计

### 5.1 项目结构

```
frontend/src/
├── views/                    # 页面视图
│   ├── Dashboard.vue        # 首页仪表盘
│   ├── WorkflowDesign.vue   # 工作流设计器
│   ├── WorkflowList.vue     # 工作流列表
│   ├── ScheduleList.vue     # 调度管理
│   ├── ExecutionList.vue    # 执行管理
│   ├── ProjectList.vue      # 项目管理
│   ├── FileList.vue         # 文件管理
│   ├── DatasourceList.vue   # 数据源管理
│   ├── ServerList.vue       # 服务器管理
│   ├── EnvVariableList.vue  # 环境变量管理
│   ├── UserList.vue         # 用户管理
│   ├── RoleList.vue         # 角色管理
│   ├── MenuList.vue         # 菜单管理
│   ├── SysConfigList.vue    # 系统配置
│   ├── TaskTypeList.vue     # 任务类型配置
│   ├── ScheduleLogList.vue  # 调度日志
│   ├── TaskExecutionLogList.vue # 任务执行日志
│   └── Login.vue            # 登录页面
├── components/              # 公共组件
│   ├── CronEditor.vue       # Cron编辑器
│   └── ...
├── layout/                  # 布局组件
│   └── MainLayout.vue       # 主布局
├── router/                  # 路由配置
│   └── index.ts
├── api/                     # API接口定义
│   └── index.ts
├── utils/                   # 工具函数
│   └── request.ts           # HTTP请求封装
├── App.vue                  # 根组件
├── main.ts                  # 入口文件
└── style.css                # 全局样式
```

### 5.2 路由配置

| 路径 | 组件 | 权限要求 |
|------|------|----------|
| `/login` | `Login.vue` | 无 |
| `/dashboard` | `Dashboard.vue` | 登录用户 |
| `/workflows` | `WorkflowList.vue` | 登录用户 |
| `/workflow-design` | `WorkflowDesign.vue` | 登录用户 |
| `/workflow-design/:id` | `WorkflowDesign.vue` | 登录用户 |
| `/workflow-design/:id/version/:version` | `WorkflowDesign.vue` | 登录用户 |
| `/schedules` | `ScheduleList.vue` | 登录用户 |
| `/executions` | `ExecutionList.vue` | 登录用户 |
| `/projects` | `ProjectList.vue` | 登录用户 |
| `/files` | `FileList.vue` | 登录用户 |
| `/datasources` | `DatasourceList.vue` | 登录用户 |
| `/servers` | `ServerList.vue` | 登录用户 |
| `/env-variables` | `EnvVariableList.vue` | 登录用户 |
| `/system/users` | `UserList.vue` | 管理员 |
| `/system/roles` | `RoleList.vue` | 管理员 |
| `/system/menus` | `MenuList.vue` | 管理员 |
| `/system/config` | `SysConfigList.vue` | 管理员 |
| `/logs/schedule` | `ScheduleLogList.vue` | 登录用户 |
| `/logs/task-execution` | `TaskExecutionLogList.vue` | 登录用户 |

## 6. 安全设计

### 6.1 认证机制

- JWT Token认证
- Token存储在localStorage
- 请求头携带Authorization: Bearer {token}
- Token过期时间：2小时
- 支持自动刷新Token

### 6.2 权限控制

- RBAC模型
- 角色-菜单关联
- 角色-用户关联
- 接口级权限校验

### 6.3 密码安全

- BCrypt加密存储
- 密码强度校验
- 禁止明文传输密码

### 6.4 其他安全措施

- SQL注入防护（JPA预编译）
- XSS攻击防护（前端转义）
- 请求频率限制
- 敏感信息脱敏
- HTTPS支持

## 7. 部署与集成

### 7.1 依赖与环境

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot Starter Web | 2.7.x | Web框架 |
| Spring Boot Starter Data JPA | 2.7.x | ORM |
| Spring Boot Starter Security | 2.7.x | 安全框架 |
| Quartz | 2.3.x | 调度引擎 |
| MySQL Connector | 8.0.x | MySQL驱动 |
| JJWT | 0.12.x | JWT支持 |
| JSch | 0.1.55 | SSH客户端 |
| Lombok | 1.18.x | 简化代码 |

### 7.2 启动方式

**开发环境：**

后端：
```bash
cd backend
mvn spring-boot:run
```

前端：
```bash
cd frontend
npm install
npm run dev
```

**生产环境：**

后端：
```bash
cd backend
mvn clean package
java -jar target/task-plan-1.0.1.jar
```

前端：
```bash
cd frontend
npm run build
# 部署dist目录到静态服务器
```

## 8. 代码规范

### 8.1 Java代码规范

- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 异常统一处理
- 日志使用SLF4J

### 8.2 TypeScript代码规范

- 遵循Airbnb风格指南
- 使用TypeScript严格模式
- 组件命名使用PascalCase
- 变量命名使用camelCase

### 8.3 提交规范

遵循Conventional Commits：

```
feat: 新增功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试代码
chore: 构建/工具更新
```

## 9. 附录

### 9.1 任务类型支持

| 任务类型 | 说明 | 状态 |
|----------|------|------|
| Shell | Shell脚本任务 | 已实现 |
| Python | Python脚本任务 | 已实现 |
| Java | Java程序任务 | 已实现 |
| SQL | SQL查询任务 | 已实现 |
| HTTP | HTTP请求任务 | 已实现 |
| Spark | Spark任务 | 已实现 |
| Flink | Flink任务 | 已实现 |
| Hive | Hive任务 | 已实现 |

### 9.2 Cron表达式格式

Quartz 6-field格式：
```
秒 分 时 日 月 周
```

示例：
- `0 0 2 * * ?` - 每天凌晨2点执行
- `0 0/30 * * * ?` - 每30分钟执行
- `0 0 12 * * MON-FRI` - 工作日中午12点执行

### 9.3 状态码定义

| 状态码 | 含义 |
|--------|------|
| 200000 | 成功 |
| 400000 | 请求参数错误 |
| 401000 | 未认证 |
| 403000 | 无权限 |
| 404000 | 资源不存在 |
| 500000 | 服务器内部错误 |

---

**文档版本**: v1.1  
**创建日期**: 2026-05-20  
**最后更新**: 2026-05-22
