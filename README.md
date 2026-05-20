# 任务计划工具箱系统

参照 Apache DolphinScheduler 设计的任务计划管理系统，支持任务创建、调度、执行、监控与告警等功能。

## 项目概述

### 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue | 3.2.45 |
| 前端 | TypeScript | 4.9.4 |
| 前端 | Vite | 4.0.1 |
| 前端 | Element Plus | 2.2.26 |
| 后端 | Spring Boot | 3.2.x |
| 后端 | Quartz | 2.3.x |
| 后端 | Spring Data JPA | 3.2.x |
| 数据库 | MySQL | 5.7+ |
| 工作流设计 | Vue Flow | 1.2.x |

### 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                      前端层 (Vue 3)                        │
│  ┌─────────┐ ┌──────────┐ ┌─────────┐ ┌─────────────────┐  │
│  │ 工作流设计│ │ 任务管理 │ │ 调度管理│ │   系统管理      │  │
│  └────┬────┘ └────┬─────┘ └────┬────┘ └────────┬────────┘  │
└───────┼───────────┼────────────┼───────────────┼───────────┘
        │           │            │               │
        ▼           ▼            ▼               ▼
┌─────────────────────────────────────────────────────────────┐
│                      API网关层                             │
│              Spring Security + JWT认证                      │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│                      业务逻辑层                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐   │
│  │ ProcessService│ │ TaskService │ │ ScheduleService    │   │
│  │ WorkflowService││ AuthService │ │ SystemConfigService│   │
│  └─────────────┘ └─────────────┘ └─────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据访问层                             │
│              Spring Data JPA + MySQL                       │
└─────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────┐
│                      基础设施层                             │
│  MySQL (数据存储)  │  Quartz (调度引擎)  │  文件存储         │
└─────────────────────────────────────────────────────────────┘
```

## 核心功能模块

### 1. 工作流设计模块
- 可视化DAG工作流设计器
- 任务节点拖拽添加
- 节点连线与依赖配置
- 任务详情双击编辑
- 支持多种任务类型（Shell/Python/Java/SQL/Spark/Hive等）

### 2. 任务管理模块
- 任务定义管理（创建/编辑/删除）
- 任务类型配置
- 脚本文件管理（上传/预览/编辑）
- 数据源配置管理

### 3. 调度管理模块
- Cron表达式调度配置
- 图形化Cron编辑器
- 调度计划管理
- 执行日志查看

### 4. 执行管理模块
- 工作流实例管理
- 任务实例管理
- 执行日志追踪
- 失败重试机制

### 5. 资源管理模块
- 项目管理
- 文件资源管理
- UDF函数管理
- 环境变量配置

### 6. 系统管理模块
- 用户管理（CRUD + 角色分配）
- 角色管理（权限配置）
- 菜单管理
- 服务器节点管理
- 系统配置管理

### 7. 数据源管理模块
- 数据源配置（MySQL/PostgreSQL/Oracle等）
- 连接测试
- 数据源类型管理

## 项目结构

```
task-plan/
├── backend/                              # 后端Spring Boot项目
│   ├── src/main/java/com/taskplan/       # Java源码
│   │   ├── controller/                   # REST API控制器
│   │   ├── service/                      # 业务逻辑层
│   │   ├── repository/                   # 数据访问层
│   │   ├── entity/                       # JPA实体
│   │   ├── config/                       # 配置类
│   │   ├── exception/                    # 异常处理
│   │   └── utils/                        # 工具类
│   ├── src/main/resources/               # 配置文件
│   ├── uploads/                          # 文件上传目录
│   └── pom.xml                           # Maven配置
├── frontend/                             # 前端Vue项目
│   ├── src/
│   │   ├── views/                        # 页面视图
│   │   ├── components/                   # 公共组件
│   │   ├── layout/                       # 布局组件
│   │   ├── router/                       # 路由配置
│   │   ├── api/                          # API接口定义
│   │   └── utils/                        # 工具函数
│   └── package.json                      # npm依赖
├── docs/                                 # 文档目录
│   ├── design.md                         # 设计文档
│   ├── api.md                            # API文档
│   └── usage.md                          # 使用说明
├── init-db.sql                           # 数据库初始化脚本
└── README.md                             # 项目说明
```

## 快速开始

### 前置要求

- JDK 17+
- Node.js 18+
- MySQL 5.7+ 或 MySQL 8.0+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE task_plan_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u username -p task_plan_db < init-db.sql
```

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

服务启动后访问：http://localhost:8080

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问：http://localhost:3000

### 默认账号

- 用户名：admin
- 密码：123456

## API接口说明

### 认证接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/me` | GET | 获取当前用户信息 |

### 工作流接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/workflows` | GET | 获取工作流列表 |
| `/api/workflows` | POST | 创建工作流 |
| `/api/workflows/{id}` | GET | 获取工作流详情 |
| `/api/workflows/{id}` | PUT | 更新工作流 |
| `/api/workflows/{id}` | DELETE | 删除工作流 |
| `/api/workflows/{id}/deploy` | POST | 发布工作流 |
| `/api/workflows/{id}/start` | POST | 启动工作流 |

### 任务接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/tasks` | GET | 获取任务列表 |
| `/api/tasks` | POST | 创建任务 |
| `/api/tasks/{id}` | GET | 获取任务详情 |
| `/api/tasks/{id}` | PUT | 更新任务 |
| `/api/tasks/{id}` | DELETE | 删除任务 |

### 调度接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/schedules` | GET | 获取调度列表 |
| `/api/schedules` | POST | 创建调度 |
| `/api/schedules/{id}` | GET | 获取调度详情 |
| `/api/schedules/{id}` | PUT | 更新调度 |
| `/api/schedules/{id}` | DELETE | 删除调度 |
| `/api/schedules/{id}/enable` | POST | 启用调度 |
| `/api/schedules/{id}/disable` | POST | 禁用调度 |

### 文件管理接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/files` | GET | 获取文件列表 |
| `/api/files/upload` | POST | 上传文件 |
| `/api/files/{id}` | GET | 获取文件详情 |
| `/api/files/{id}` | DELETE | 删除文件 |
| `/api/files/{id}/content` | GET | 获取文件内容 |
| `/api/files/{id}/content` | PUT | 更新文件内容 |
| `/api/files/directory` | GET | 获取目录结构 |

### 系统管理接口
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/users` | GET/POST | 用户管理 |
| `/api/roles` | GET/POST | 角色管理 |
| `/api/menus` | GET/POST | 菜单管理 |
| `/api/configs` | GET/POST | 系统配置 |

## 核心实体关系

```
Project (项目)
    │
    ├── ProcessDefinition (工作流定义)
    │       │
    │       ├── TaskDefinition (任务定义)
    │       │       └── TaskTypeConfig (任务类型配置)
    │       │
    │       ├── ProcessSchedule (调度计划)
    │       │
    │       └── ProcessInstance (工作流实例)
    │               └── TaskInstance (任务实例)
    │                       └── TaskLog (任务日志)
    │
    ├── FileResource (文件资源)
    │
    ├── Datasource (数据源)
    │
    └── EnvVariable (环境变量)

SysUser (系统用户)
    │
    ├── SysUserRole (用户角色关联)
    │       └── SysRole (系统角色)
    │               └── SysRoleMenu (角色菜单关联)
    │                       └── SysMenu (系统菜单)
```

## 安全特性

- JWT无状态认证
- 基于角色的访问控制(RBAC)
- 密码加密存储(BCrypt)
- 请求频率限制
- SQL注入防护
- XSS攻击防护

## 开发指南

### 代码规范

- Java代码遵循阿里巴巴Java开发手册
- TypeScript代码遵循Airbnb风格指南
- 提交信息遵循Conventional Commits规范

### 开发流程

1. Fork项目
2. 创建功能分支
3. 开发并测试
4. 提交Pull Request
5. Code Review
6. 合并到主分支

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交Issue或发送邮件。
