# 任务计划系统需求文档

## 1. 项目概述

本系统参照 Apache DolphinScheduler 开源调度工具设计，是一个轻量级的任务调度与工作流编排系统，提供可视化的工作流设计、灵活的任务调度和完整的执行监控能力。

## 2. 核心功能模块

### 2.1 项目管理 (Project Management)

**功能需求：**
- 项目的创建、编辑、删除
- 项目列表展示与搜索
- 项目关联任务和工作流

**数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 项目名称 |
| description | String | 项目描述 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.2 任务定义 (Task Definition)

**功能需求：**
- 任务定义的创建、编辑、删除
- 支持多种任务类型：SHELL、PYTHON、PERL、JAVA、HTTP、SQL、SPARK、FLINK、MAPREDUCE
- 任务参数配置
- 失败重试策略配置
- 超时时间配置
- 任务服务器选择（针对SHELL/PYTHON/PERL/JAVA任务）
- 数据源选择（针对SQL任务）
- 文件选择（脚本/JAR包，针对SHELL/PERL/PYTHON/JAVA任务）
- 大数据集群配置选择（针对SPARK/FLINK任务）

**任务类型说明：**

| 类型 | 说明 | 应用场景 |
|------|------|----------|
| SHELL | Shell脚本执行 | 系统命令、脚本运行 |
| PYTHON | Python脚本执行 | 数据处理、分析脚本 |
| PERL | Perl脚本执行 | 文本处理、系统管理 |
| JAVA | Java程序执行 | 企业级应用、大数据处理 |
| HTTP | HTTP请求 | API调用、数据采集 |
| SQL | SQL语句执行 | 数据库查询、数据操作 |
| SPARK | Spark任务执行 | 大数据批处理 |
| FLINK | Flink任务执行 | 实时流处理 |
| MAPREDUCE | MapReduce任务 | Hadoop批处理 |

**数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| projectId | Long | 所属项目ID |
| name | String | 任务名称 |
| taskType | String | 任务类型 |
| params | String | 任务参数(JSON) |
| envParams | String | 环境变量(JSON) |
| failRetryTimes | Integer | 失败重试次数 |
| failRetryInterval | Integer | 重试间隔(秒) |
| timeout | Integer | 超时时间(秒) |
| serverId | Long | 执行服务器ID |
| datasourceId | Long | 数据源ID |
| fileId | Long | 关联文件ID |
| clusterConfigId | Long | 集群配置ID |
| taskTypeId | Long | 任务类型配置ID |
| owner | String | 负责人 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.3 工作流定义 (Process Definition)

**功能需求：**
- 工作流的创建、编辑、删除
- 可视化工作流设计画布
- 任务节点的添加（拖拽方式）、删除、移动
- 节点间连线（依赖关系），连接点显示在节点边缘中间
- 工作流版本管理
- 工作流发布/下线（无任务的工作流不允许发布）
- 双击任务节点查看/编辑任务详情

**数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 工作流名称 |
| projectId | Long | 所属项目ID |
| definitionJson | String | 流程定义(JSON，包含nodes和connections) |
| releaseState | String | 发布状态(ONLINE/OFFLINE) |
| version | Integer | 版本号 |
| owner | String | 负责人 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.4 调度管理 (Process Schedule)

**功能需求：**
- 调度任务的创建、编辑、删除
- Cron表达式配置（图形化配置界面）
- 调度启用/禁用
- 调度时间范围设置
- 条件查询调度任务

**数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| projectId | Long | 所属项目ID |
| processDefinitionId | Long | 关联工作流ID |
| cronExpression | String | Cron表达式 |
| startTime | DateTime | 开始时间 |
| endTime | DateTime | 结束时间 |
| timezone | String | 时区 |
| isValid | Integer | 是否启用(1/0) |
| owner | String | 负责人 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.5 执行记录 (Execution Records)

**功能需求：**
- 流程实例列表查询
- 流程状态追踪（RUNNING/SUCCESS/FAILURE/WAITING/STOP）
- 任务实例详情查看
- 执行日志查看
- 流程停止操作

**流程实例数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| projectId | Long | 所属项目ID |
| processDefinitionId | Long | 关联工作流ID |
| state | String | 状态 |
| startTime | DateTime | 开始时间 |
| endTime | DateTime | 结束时间 |
| host | String | 执行主机 |
| createTime | DateTime | 创建时间 |

**任务实例数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| processInstanceId | Long | 关联流程实例ID |
| taskName | String | 任务名称 |
| taskType | String | 任务类型 |
| state | String | 状态 |
| startTime | DateTime | 开始时间 |
| endTime | DateTime | 结束时间 |
| log | String | 执行日志 |
| host | String | 执行主机 |

### 2.6 数据源管理 (Datasource Management)

**功能需求：**
- 数据源的创建、编辑、删除
- 支持多种数据库类型（MySQL、PostgreSQL、Oracle、SQLite等）
- 数据源连接测试
- 数据源列表展示与搜索
- 为SQL任务提供数据源选择

**数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 数据源名称 |
| type | String | 数据库类型(MYSQL/POSTGRESQL/ORACLE/SQLITE) |
| host | String | 主机地址 |
| port | Integer | 端口 |
| database | String | 数据库名称 |
| username | String | 用户名 |
| password | String | 密码(加密存储) |
| params | String | 连接参数(JSON) |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.7 服务器管理 (Server Management)

**功能需求：**
- 服务器节点的创建、编辑、删除
- 服务器状态监控
- 服务器分组管理
- 为SHELL/PYTHON/PERL/JAVA任务提供执行服务器选择

**数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 服务器名称 |
| hostname | String | 主机名/IP地址 |
| port | Integer | SSH端口 |
| username | String | SSH用户名 |
| password | String | SSH密码/密钥(加密存储) |
| groupName | String | 服务器分组 |
| status | String | 状态(ONLINE/OFFLINE) |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.8 文件管理 (File Management)

**功能需求：**
- 文件上传、下载、删除
- 支持脚本文件(.sh/.py/.pl)和JAR包(.jar)
- 文件列表展示与搜索
- 文件版本管理
- 为SHELL/PERL/PYTHON/JAVA任务提供文件选择

**数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 文件名称 |
| path | String | 文件存储路径 |
| size | Long | 文件大小(字节) |
| type | String | 文件类型(SCRIPT/JAR/OTHER) |
| version | Integer | 版本号 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.9 大数据配置管理 (Big Data Configuration)

**功能需求：**
- Flink集群配置管理
- YARN环境配置管理
- Spark配置管理
- 为FLINK/SPARK任务提供运行环境支持

**数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 配置名称 |
| type | String | 配置类型(FLINK/YARN/SPARK) |
| configJson | String | 配置内容(JSON) |
| description | String | 配置描述 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

### 2.10 任务类型配置 (Task Type Configuration)

**功能需求：**
- 任务类型的创建、编辑、删除
- 支持配置运行程序路径
- 默认参数配置
- 环境变量支持
- 支持同一语言的多个版本（如Python2/Python3）
- 支持不同JDK版本的Java任务

**数据模型：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| name | String | 类型名称(如Python2/Python3/Java8) |
| taskType | String | 基础任务类型(SHELL/PYTHON/PERL/JAVA) |
| programPath | String | 程序执行路径 |
| defaultParams | String | 默认参数(JSON) |
| envParams | String | 环境变量(JSON) |
| description | String | 描述 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

## 3. 核心业务流程

### 3.1 工作流设计流程

```
创建项目 → 添加数据源/服务器/文件 → 创建任务定义 → 设计工作流(拖拽任务到画布) → 配置连线 → 发布工作流 → 创建调度 → 执行监控
```

### 3.2 调度执行流程

```
调度触发 → 创建流程实例 → 执行任务节点(根据配置选择服务器/数据源/文件) → 更新状态 → 记录日志
```

## 4. 非功能需求

### 4.1 性能需求
- 支持100+并发工作流执行
- 调度精度达到秒级

### 4.2 可靠性需求
- 任务失败自动重试
- 执行状态持久化存储

### 4.3 可用性需求
- 可视化工作流设计（拖拽添加任务）
- 实时状态监控
- 友好的错误提示
- 连接点显示在节点边缘中间位置

## 5. 技术选型

| 分类 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.x |
| 数据库 | MySQL | 8.0+ |
| 调度引擎 | Quartz | 2.3.x |
| 前端框架 | Vue | 3.x |
| UI组件 | Element Plus | 2.x |
| 构建工具 | Vite | 6.x |

## 6. 接口设计概述

### 6.1 项目管理 API
- `GET /api/projects` - 查询项目列表
- `GET /api/projects/{id}` - 查询项目详情
- `POST /api/projects` - 创建项目
- `PUT /api/projects/{id}` - 更新项目
- `DELETE /api/projects/{id}` - 删除项目

### 6.2 任务定义 API
- `GET /api/task-definitions` - 查询任务列表
- `GET /api/task-definitions/{id}` - 查询任务详情
- `POST /api/task-definitions` - 创建任务
- `PUT /api/task-definitions/{id}` - 更新任务
- `DELETE /api/task-definitions/{id}` - 删除任务

### 6.3 工作流定义 API
- `GET /api/process-definitions` - 查询工作流列表
- `GET /api/process-definitions/{id}` - 查询工作流详情
- `POST /api/process-definitions` - 创建工作流
- `POST /api/process-definitions/design` - 保存工作流设计
- `PUT /api/process-definitions/{id}` - 更新工作流
- `DELETE /api/process-definitions/{id}` - 删除工作流
- `POST /api/process-definitions/{id}/release` - 发布工作流
- `POST /api/process-definitions/{id}/offline` - 下线工作流
- `POST /api/process-definitions/{id}/execute` - 立即执行

### 6.4 调度管理 API
- `GET /api/process-schedules` - 查询调度列表
- `GET /api/process-schedules/{id}` - 查询调度详情
- `POST /api/process-schedules` - 创建调度
- `PUT /api/process-schedules/{id}` - 更新调度
- `DELETE /api/process-schedules/{id}` - 删除调度
- `POST /api/process-schedules/{id}/enable` - 启用调度
- `POST /api/process-schedules/{id}/disable` - 禁用调度

### 6.5 执行记录 API
- `GET /api/process-instances` - 查询流程实例列表
- `GET /api/process-instances/{id}` - 查询流程实例详情
- `POST /api/process-instances/start` - 启动流程
- `POST /api/process-instances/{id}/stop` - 停止流程

### 6.6 数据源管理 API
- `GET /api/datasources` - 查询数据源列表
- `GET /api/datasources/{id}` - 查询数据源详情
- `POST /api/datasources` - 创建数据源
- `PUT /api/datasources/{id}` - 更新数据源
- `DELETE /api/datasources/{id}` - 删除数据源
- `POST /api/datasources/{id}/test` - 测试数据源连接

### 6.7 服务器管理 API
- `GET /api/servers` - 查询服务器列表
- `GET /api/servers/{id}` - 查询服务器详情
- `POST /api/servers` - 创建服务器
- `PUT /api/servers/{id}` - 更新服务器
- `DELETE /api/servers/{id}` - 删除服务器
- `POST /api/servers/{id}/ping` - 测试服务器连接

### 6.8 文件管理 API
- `GET /api/files` - 查询文件列表
- `GET /api/files/{id}` - 查询文件详情
- `POST /api/files/upload` - 上传文件
- `GET /api/files/{id}/download` - 下载文件
- `DELETE /api/files/{id}` - 删除文件

### 6.9 大数据配置 API
- `GET /api/cluster-configs` - 查询集群配置列表
- `GET /api/cluster-configs/{id}` - 查询集群配置详情
- `POST /api/cluster-configs` - 创建集群配置
- `PUT /api/cluster-configs/{id}` - 更新集群配置
- `DELETE /api/cluster-configs/{id}` - 删除集群配置

### 6.10 任务类型配置 API
- `GET /api/task-types` - 查询任务类型列表
- `GET /api/task-types/{id}` - 查询任务类型详情
- `POST /api/task-types` - 创建任务类型
- `PUT /api/task-types/{id}` - 更新任务类型
- `DELETE /api/task-types/{id}` - 删除任务类型