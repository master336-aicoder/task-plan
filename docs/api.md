# 任务计划工具箱系统 - API文档

## 目录

1. [认证接口](#1-认证接口)
2. [工作流接口](#2-工作流接口)
3. [任务接口](#3-任务接口)
4. [调度接口](#4-调度接口)
5. [执行接口](#5-执行接口)
6. [文件管理接口](#6-文件管理接口)
7. [项目接口](#7-项目接口)
8. [数据源接口](#8-数据源接口)
9. [环境变量接口](#9-环境变量接口)
10. [服务器节点接口](#10-服务器节点接口)
11. [系统管理接口](#11-系统管理接口)
12. [响应格式](#12-响应格式)
13. [错误码](#13-错误码)

---

## 1. 认证接口

### 1.1 用户登录

**POST** `/api/auth/login`

请求体：
```json
{
  "username": "admin",
  "password": "123456"
}
```

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "管理员",
      "roles": ["admin"]
    }
  }
}
```

### 1.2 用户登出

**POST** `/api/auth/logout`

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": null
}
```

### 1.3 获取当前用户信息

**GET** `/api/auth/userinfo`

请求头：`Authorization: Bearer {token}`

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "email": "admin@example.com",
    "roles": ["admin"],
    "menus": [...]
  }
}
```

---

## 2. 工作流接口

### 2.1 获取工作流列表

**GET** `/api/process-definitions`

查询参数：
| 参数名 | 类型 | 说明 |
|--------|------|------|
| name | String | 工作流名称(模糊查询) |
| projectId | Long | 项目ID |
| page | Integer | 页码(默认1) |
| size | Integer | 每页大小(默认10) |

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": {
    "data": [
      {
        "id": 1,
        "name": "数据处理工作流",
        "description": "每日数据处理任务",
        "projectId": 1,
        "status": 1,
        "version": 3,
        "createTime": "2026-05-20 10:00:00",
        "updateTime": "2026-05-22 15:30:00"
      }
    ],
    "total": 100
  }
}
```

### 2.2 创建工作流

**POST** `/api/process-definitions`

请求体：
```json
{
  "name": "新建工作流",
  "description": "工作流描述",
  "projectId": 1,
  "tasks": [],
  "connections": []
}
```

### 2.3 获取工作流详情

**GET** `/api/process-definitions/{id}`

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": {
    "id": 1,
    "name": "数据处理工作流",
    "description": "每日数据处理任务",
    "projectId": 1,
    "tasks": [
      {
        "id": "task1",
        "name": "Shell任务",
        "type": "SHELL",
        "config": {"script": "echo 'hello'"}
      }
    ],
    "connections": [
      {"from": "task1", "to": "task2"}
    ],
    "status": 1,
    "version": 3
  }
}
```

### 2.4 更新工作流

**PUT** `/api/process-definitions/{id}`

请求体：同创建工作流

### 2.5 删除工作流

**DELETE** `/api/process-definitions/{id}`

### 2.6 发布工作流

**POST** `/api/process-definitions/{id}/release`

### 2.7 下线工作流

**POST** `/api/process-definitions/{id}/offline`

### 2.8 获取历史版本列表

**GET** `/api/process-definitions/{id}/history`

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": [
    {"id": 1, "version": 3, "createTime": "2026-05-22 15:30:00"},
    {"id": 2, "version": 2, "createTime": "2026-05-21 10:00:00"},
    {"id": 3, "version": 1, "createTime": "2026-05-20 09:00:00"}
  ]
}
```

### 2.9 获取指定版本

**GET** `/api/process-definitions/{id}/history/{version}`

### 2.10 版本对比

**GET** `/api/process-definitions/{id}/history/{version}/compare`

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": {
    "sourceVersion": 2,
    "targetVersion": 3,
    "addedTasks": [...],
    "removedTasks": [...],
    "modifiedTasks": [...],
    "addedConnections": [...],
    "removedConnections": [...]
  }
}
```

### 2.11 版本回滚

**POST** `/api/process-definitions/{id}/history/{version}/rollback`

---

## 3. 任务接口

### 3.1 获取任务类型列表

**GET** `/api/task-types`

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": [
    {"type": "SHELL", "label": "Shell脚本", "icon": "terminal"},
    {"type": "PYTHON", "label": "Python脚本", "icon": "code"},
    {"type": "SQL", "label": "SQL任务", "icon": "database"},
    {"type": "HTTP", "label": "HTTP请求", "icon": "globe"},
    {"type": "SPARK", "label": "Spark任务", "icon": "zap"},
    {"type": "FLINK", "label": "Flink任务", "icon": "activity"}
  ]
}
```

### 3.2 获取任务定义列表

**GET** `/api/task-definitions`

查询参数：
| 参数名 | 类型 | 说明 |
|--------|------|------|
| name | String | 任务名称 |
| type | String | 任务类型 |
| projectId | Long | 项目ID |

### 3.3 创建任务定义

**POST** `/api/task-definitions`

请求体：
```json
{
  "name": "数据清洗任务",
  "type": "PYTHON",
  "projectId": 1,
  "config": {
    "scriptPath": "/data/scripts/clean.py",
    "params": ["--input", "/data/input"]
  },
  "description": "数据清洗脚本"
}
```

---

## 4. 调度接口

### 4.1 获取调度列表

**GET** `/api/process-schedules`

查询参数：
| 参数名 | 类型 | 说明 |
|--------|------|------|
| processDefinitionId | Long | 工作流ID |
| isValid | Integer | 状态(0-禁用,1-启用) |
| page | Integer | 页码 |
| size | Integer | 每页大小 |

### 4.2 创建调度

**POST** `/api/process-schedules`

请求体：
```json
{
  "processDefinitionId": 1,
  "cronExpression": "0 0 2 * * ?",
  "startTime": "2026-05-20 00:00:00",
  "endTime": "2026-12-31 23:59:59",
  "timezone": "Asia/Shanghai",
  "isValid": 1
}
```

### 4.3 更新调度

**PUT** `/api/process-schedules/{id}`

请求体：同创建调度

### 4.4 删除调度

**DELETE** `/api/process-schedules/{id}`

### 4.5 启用调度

**POST** `/api/process-schedules/{id}/enable`

### 4.6 禁用调度

**POST** `/api/process-schedules/{id}/disable`

---

## 5. 执行接口

### 5.1 获取工作流实例列表

**GET** `/api/process-instances`

查询参数：
| 参数名 | 类型 | 说明 |
|--------|------|------|
| processDefinitionId | Long | 工作流ID |
| status | String | 状态 |
| startTime | String | 开始时间 |
| endTime | String | 结束时间 |
| page | Integer | 页码 |
| size | Integer | 每页大小 |

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": {
    "data": [
      {
        "id": 1,
        "processDefinitionId": 1,
        "processDefinitionName": "数据处理工作流",
        "status": "RUNNING",
        "startTime": "2026-05-22 10:00:00",
        "endTime": null,
        "taskInstances": [...]
      }
    ],
    "total": 50
  }
}
```

### 5.2 获取工作流实例详情

**GET** `/api/process-instances/{id}`

### 5.3 启动工作流

**POST** `/api/process-instances/start`

请求体：
```json
{
  "processDefinitionId": 1,
  "params": {}
}
```

### 5.4 停止工作流

**POST** `/api/process-instances/{id}/stop`

### 5.5 重试工作流

**POST** `/api/process-instances/{id}/retry`

---

## 6. 文件管理接口

### 6.1 获取目录结构

**GET** `/api/files/directory`

查询参数：
| 参数名 | 类型 | 说明 |
|--------|------|------|
| path | String | 目录路径(默认/) |

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "scripts",
      "path": "/scripts",
      "isDirectory": 1,
      "createTime": "2026-05-20 09:00:00",
      "updateTime": "2026-05-20 09:00:00"
    },
    {
      "id": 2,
      "name": "data.txt",
      "path": "/data.txt",
      "isDirectory": 0,
      "size": 1024,
      "createTime": "2026-05-21 10:00:00",
      "updateTime": "2026-05-21 11:00:00"
    }
  ]
}
```

### 6.2 上传文件

**POST** `/api/files/upload`

请求体：`multipart/form-data`

| 参数名 | 类型 | 说明 |
|--------|------|------|
| file | File | 文件 |
| path | String | 目标路径 |

### 6.3 获取文件内容

**GET** `/api/files/{id}/content`

### 6.4 更新文件内容

**PUT** `/api/files/{id}/content`

请求体：
```json
{
  "content": "file content here..."
}
```

### 6.5 删除文件

**DELETE** `/api/files/{id}`

---

## 7. 项目接口

### 7.1 获取项目列表

**GET** `/api/projects`

### 7.2 创建项目

**POST** `/api/projects`

请求体：
```json
{
  "name": "数据分析项目",
  "description": "用于数据分析的项目",
  "owner": "admin"
}
```

### 7.3 更新项目

**PUT** `/api/projects/{id}`

### 7.4 删除项目

**DELETE** `/api/projects/{id}`

---

## 8. 数据源接口

### 8.1 获取数据源列表

**GET** `/api/datasources`

### 8.2 创建数据源

**POST** `/api/datasources`

请求体：
```json
{
  "name": "MySQL数据源",
  "type": "MYSQL",
  "host": "localhost",
  "port": 3306,
  "database": "task_plan_db",
  "username": "admin",
  "password": "password",
  "projectId": 1
}
```

### 8.3 更新数据源

**PUT** `/api/datasources/{id}`

### 8.4 删除数据源

**DELETE** `/api/datasources/{id}`

### 8.5 测试数据源连接

**POST** `/api/datasources/{id}/test`

---

## 9. 环境变量接口

### 9.1 获取环境变量列表

**GET** `/api/env-variables`

查询参数：
| 参数名 | 类型 | 说明 |
|--------|------|------|
| name | String | 变量名 |
| category | String | 分类 |
| page | Integer | 页码 |
| size | Integer | 每页大小 |

响应体：
```json
{
  "code": 200000,
  "msg": "success",
  "data": {
    "data": [
      {
        "id": 1,
        "configName": "数据库配置",
        "name": "DB_HOST",
        "value": "localhost",
        "category": "DATABASE",
        "description": "数据库主机地址"
      }
    ],
    "total": 20
  }
}
```

### 9.2 创建环境变量

**POST** `/api/env-variables`

请求体：
```json
{
  "configName": "数据库配置",
  "name": "DB_PORT",
  "value": "3306",
  "category": "DATABASE",
  "description": "数据库端口"
}
```

### 9.3 更新环境变量

**PUT** `/api/env-variables/{id}`

### 9.4 删除环境变量

**DELETE** `/api/env-variables/{id}`

---

## 10. 服务器节点接口

### 10.1 获取服务器列表

**GET** `/api/server-nodes`

### 10.2 创建服务器

**POST** `/api/server-nodes`

请求体：
```json
{
  "name": "生产服务器",
  "ip": "192.168.1.100",
  "port": 22,
  "username": "root",
  "password": "password",
  "description": "生产环境服务器"
}
```

### 10.3 更新服务器

**PUT** `/api/server-nodes/{id}`

### 10.4 删除服务器

**DELETE** `/api/server-nodes/{id}`

### 10.5 测试SSH连接

**POST** `/api/server-nodes/{id}/test`

---

## 11. 系统管理接口

### 11.1 用户管理

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/users` | GET | 获取用户列表 |
| `/api/users` | POST | 创建用户 |
| `/api/users/{id}` | GET | 获取用户详情 |
| `/api/users/{id}` | PUT | 更新用户 |
| `/api/users/{id}` | DELETE | 删除用户 |
| `/api/users/{id}/roles` | GET | 获取用户角色 |
| `/api/users/{id}/roles` | PUT | 更新用户角色 |

### 11.2 角色管理

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/roles` | GET | 获取角色列表 |
| `/api/roles` | POST | 创建角色 |
| `/api/roles/{id}` | PUT | 更新角色 |
| `/api/roles/{id}` | DELETE | 删除角色 |
| `/api/roles/{id}/menus` | GET | 获取角色菜单 |
| `/api/roles/{id}/menus` | PUT | 更新角色菜单 |

### 11.3 菜单管理

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/menus` | GET | 获取菜单列表 |
| `/api/menus` | POST | 创建菜单 |
| `/api/menus/{id}` | PUT | 更新菜单 |
| `/api/menus/{id}` | DELETE | 删除菜单 |

### 11.4 系统配置

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/api/system-config` | GET | 获取系统配置 |
| `/api/system-config` | PUT | 更新系统配置 |

---

## 12. 响应格式

### 12.1 成功响应

```json
{
  "code": 200000,
  "msg": "success",
  "data": {...},
  "timestamp": 1672531200000
}
```

### 12.2 分页响应

```json
{
  "code": 200000,
  "msg": "success",
  "data": {
    "data": [...],
    "total": 100
  },
  "timestamp": 1672531200000
}
```

### 12.3 失败响应

```json
{
  "code": 500000,
  "msg": "系统错误: 错误信息",
  "data": null,
  "timestamp": 1672531200000
}
```

---

## 13. 错误码

| 错误码 | 含义 | 说明 |
|--------|------|------|
| 200000 | 成功 | 操作成功 |
| 400000 | 请求参数错误 | 参数校验失败 |
| 401000 | 未认证 | 用户未登录或Token过期 |
| 403000 | 无权限 | 用户无操作权限 |
| 404000 | 资源不存在 | 请求的资源不存在 |
| 500000 | 服务器内部错误 | 系统异常 |
| 500001 | 数据库错误 | 数据库操作失败 |
| 500002 | 文件操作失败 | 文件上传/下载失败 |
| 500003 | SSH连接失败 | 远程服务器连接失败 |

---

**文档版本**: v1.1  
**最后更新**: 2026-05-22
