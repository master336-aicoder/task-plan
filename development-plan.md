# 任务计划系统 - 研发计划

## 1. 项目概述

参照 Apache DolphinScheduler 设计模式，构建轻量级任务调度系统，专注核心调度功能。

## 2. 阶段划分

### 阶段一：基础架构搭建（已完成）

**时间：** 第1-2天

**任务：**
- ✅ 后端项目初始化（Spring Boot + JPA + MySQL）
- ✅ 前端项目初始化（Vue3 + Vite + Element Plus）
- ✅ 数据库配置与初始化
- ✅ 核心实体设计（Project、TaskDefinition、ProcessDefinition、ProcessSchedule、ProcessInstance、TaskInstance）

**交付物：**
- 项目基础框架
- 数据库表结构
- 技术文档

### 阶段二：核心功能开发（已完成）

**时间：** 第3-5天

**任务：**
- ✅ 项目管理模块
  - 创建/编辑/删除项目
  - 项目列表展示与搜索
- ✅ 任务定义模块
  - 任务类型支持（SHELL、PYTHON、SQL、HTTP）
  - 任务参数配置
  - 重试策略与超时配置
- ✅ 工作流定义模块
  - 可视化工作流设计画布
  - 任务节点管理
  - 节点连线功能
  - 工作流发布/下线
- ✅ 调度管理模块
  - Cron表达式配置
  - 调度启用/禁用
- ✅ 执行记录模块
  - 流程实例追踪
  - 任务实例详情
  - 执行日志查看

**交付物：**
- 各模块API接口
- 前端页面组件
- 单元测试

### 阶段三：集成测试与优化（进行中）

**时间：** 第6-7天

**任务：**
- 🔄 前后端联调测试
- 🔄 功能自测与BUG修复
- 🔄 性能优化
- ✅ 需求文档更新
- ✅ 研发计划更新

**交付物：**
- 测试报告
- 优化后的代码

### 阶段四：部署上线（待开始）

**时间：** 第8天

**任务：**
- 打包部署脚本
- 部署文档
- 用户手册

**交付物：**
- 可部署的应用包
- 部署指南

## 3. 核心功能优先级

| 优先级 | 功能模块 | 状态 |
|--------|----------|------|
| P0 | 项目管理 | ✅ 完成 |
| P0 | 任务定义 | ✅ 完成 |
| P0 | 工作流设计 | ✅ 完成 |
| P0 | 调度管理 | ✅ 完成 |
| P0 | 执行记录 | ✅ 完成 |
| P1 | 用户管理 | ⏳ 待定 |
| P1 | 权限控制 | ⏳ 待定 |
| P2 | 告警通知 | ⏳ 待定 |

## 4. 技术实现要点

### 4.1 后端设计

**核心实体关系：**
```
Project (项目)
  └── TaskDefinition (任务定义)
  └── ProcessDefinition (工作流定义)
        └── ProcessSchedule (调度)
        └── ProcessInstance (流程实例)
              └── TaskInstance (任务实例)
```

**关键服务：**
- `ProjectService` - 项目管理
- `TaskDefinitionService` - 任务定义管理
- `ProcessDefinitionService` - 工作流管理
- `ProcessScheduleService` - 调度管理
- `ProcessInstanceService` - 流程实例管理

### 4.2 前端设计

**页面结构：**
```
├── MainLayout (主布局)
│   ├── Sidebar (侧边导航)
│   └── Content (内容区域)
│       ├── ProjectList (项目列表)
│       ├── TaskList (任务列表)
│       ├── WorkflowList (工作流列表)
│       ├── WorkflowDesign (工作流设计画布)
│       ├── ScheduleList (调度列表)
│       └── ExecutionList (执行记录)
```

**核心组件：**
- `WorkflowCanvas` - 工作流可视化设计画布
- `TaskNode` - 任务节点组件
- `ConnectionLine` - 连线组件

## 5. 数据库设计

### 5.1 表结构总览

| 表名 | 说明 | 状态 |
|------|------|------|
| t_project | 项目表 | ✅ |
| t_task_definition | 任务定义表 | ✅ |
| t_process_definition | 工作流定义表 | ✅ |
| t_process_schedule | 调度表 | ✅ |
| t_process_instance | 流程实例表 | ✅ |
| t_task_instance | 任务实例表 | ✅ |

### 5.2 表关系图

```
t_project 1:N t_task_definition
t_project 1:N t_process_definition
t_process_definition 1:N t_process_schedule
t_process_definition 1:N t_process_instance
t_process_instance 1:N t_task_instance
```

## 6. API接口清单

### 6.1 项目管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/projects | 查询项目列表 |
| GET | /api/projects/{id} | 查询项目详情 |
| POST | /api/projects | 创建项目 |
| PUT | /api/projects/{id} | 更新项目 |
| DELETE | /api/projects/{id} | 删除项目 |

### 6.2 任务定义
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/tasks | 查询任务列表 |
| GET | /api/tasks/{id} | 查询任务详情 |
| POST | /api/tasks | 创建任务 |
| PUT | /api/tasks/{id} | 更新任务 |
| DELETE | /api/tasks/{id} | 删除任务 |

### 6.3 工作流定义
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/workflows | 查询工作流列表 |
| GET | /api/workflows/{id} | 查询工作流详情 |
| POST | /api/workflows | 创建工作流 |
| PUT | /api/workflows/{id} | 更新工作流 |
| DELETE | /api/workflows/{id} | 删除工作流 |
| POST | /api/workflows/{id}/release | 发布工作流 |
| POST | /api/workflows/{id}/offline | 下线工作流 |

### 6.4 调度管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/schedules | 查询调度列表 |
| GET | /api/schedules/{id} | 查询调度详情 |
| POST | /api/schedules | 创建调度 |
| PUT | /api/schedules/{id} | 更新调度 |
| DELETE | /api/schedules/{id} | 删除调度 |
| POST | /api/schedules/{id}/enable | 启用调度 |
| POST | /api/schedules/{id}/disable | 禁用调度 |

### 6.5 执行记录
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/executions | 查询流程实例列表 |
| GET | /api/executions/{id} | 查询流程实例详情 |
| POST | /api/executions/start | 启动流程 |
| POST | /api/executions/{id}/stop | 停止流程 |

## 7. 测试计划

### 7.1 测试范围

| 模块 | 测试项 |
|------|--------|
| 项目管理 | 创建、编辑、删除、查询 |
| 任务定义 | 创建、编辑、删除、查询、参数验证 |
| 工作流设计 | 节点添加、删除、移动、连线、保存、发布 |
| 调度管理 | 创建、编辑、删除、启用/禁用 |
| 执行记录 | 查询、详情、停止 |

### 7.2 测试方法

- **单元测试：** 后端Service层
- **集成测试：** API接口测试
- **功能测试：** 前端页面交互测试

## 8. 风险评估

| 风险 | 等级 | 应对措施 |
|------|------|----------|
| 工作流画布交互复杂 | 高 | 简化设计，采用成熟组件 |
| 调度精度问题 | 中 | 使用Quartz专业调度引擎 |
| 并发执行压力 | 中 | 异步处理，线程池配置 |
