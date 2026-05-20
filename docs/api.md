# 任务计划工具箱系统 - API文档

## 目录

1. [认证接口](#1-认证接口)
2. [工作流接口](#2-工作流接口)
3. [任务接口](#3-任务接口)
4. [调度接口](#4-调度接口)
5. [文件管理接口](#5-文件管理接口)
6. [项目管理接口](#6-项目管理接口)
7. [数据源接口](#7-数据源接口)
8. [系统管理接口](#8-系统管理接口)
9. [响应格式](#9-响应格式)

---

## 1. 认证接口

### 1.1 POST /api/auth/login

**功能描述**：用户登录

**请求体**：
```json
{
  "username": "string (必填，用户名)",
  "password": "string (必填，密码)"
}
```

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "email": "admin@example.com",
      "phone": "13800138000",
      "status": 1
    }
  },
  "timestamp": 1672531200000
}
```

**失败响应** (401)：
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": 1672531200000
}
```

---

### 1.2 POST /api/auth/logout

**功能描述**：用户登出

**请求头**：
```
Authorization: Bearer {token}
```

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1672531200000
}
```

---

### 1.3 GET /api/auth/me

**功能描述**：获取当前用户信息

**请求头**：
```
Authorization: Bearer {token}
```

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "status": 1,
    "roles": ["ADMIN", "USER"]
  },
  "timestamp": 1672531200000
}
```

---

## 2. 工作流接口

### 2.1 GET /api/process-definitions

**功能描述**：获取工作流列表

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | string | 否 | 工作流名称（模糊搜索） |
| projectId | number | 否 | 项目ID |
| page | number | 否 | 页码（默认1） |
| size | number | 否 | 每页条数（默认20） |

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "数据同步工作流",
        "description": "每日数据同步任务",
        "projectId": 1,
        "status": 1,
        "taskCount": 5,
        "createTime": "2024-01-01 10:00:00",
        "updateTime": "2024-01-02 15:30:00"
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "number": 0,
    "size": 20
  },
  "timestamp": 1672531200000
}
```

---

### 2.2 POST /api/process-definitions

**功能描述**：创建工作流

**请求头**：
```
Authorization: Bearer {token}
```

**请求体**：
```json
{
  "name": "string (必填，工作流名称)",
  "description": "string (选填，描述)",
  "projectId": "number (必填，项目ID)",
  "tasks": "[{...}] (选填，任务列表JSON)",
  "connections": "[{...}] (选填，连接关系JSON)"
}
```

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "数据同步工作流",
    "description": "每日数据同步任务",
    "projectId": 1,
    "status": 0,
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  },
  "timestamp": 1672531200000
}
```

---

### 2.3 GET /api/process-definitions/{id}

**功能描述**：获取工作流详情

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 工作流ID |

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "数据同步工作流",
    "description": "每日数据同步任务",
    "projectId": 1,
    "tasks": "[{\"id\":1,\"name\":\"Task1\",\"type\":\"SHELL\"}]",
    "connections": "[{\"source\":1,\"target\":2}]",
    "status": 1,
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-02 15:30:00"
  },
  "timestamp": 1672531200000
}
```

**失败响应** (404)：
```json
{
  "code": 404,
  "message": "工作流不存在",
  "data": null,
  "timestamp": 1672531200000
}
```

---

### 2.4 PUT /api/process-definitions/{id}

**功能描述**：更新工作流

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 工作流ID |

**请求体**：
```json
{
  "name": "string (选填)",
  "description": "string (选填)",
  "tasks": "[{...}] (选填)",
  "connections": "[{...}] (选填)"
}
```

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "数据同步工作流(更新)",
    "description": "更新后的描述",
    "projectId": 1,
    "status": 0,
    "updateTime": "2024-01-03 09:00:00"
  },
  "timestamp": 1672531200000
}
```

---

### 2.5 DELETE /api/process-definitions/{id}

**功能描述**：删除工作流

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 工作流ID |

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1672531200000
}
```

---

### 2.6 POST /api/process-definitions/{id}/deploy

**功能描述**：发布工作流

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 工作流ID |

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "id": 1,
    "status": 1
  },
  "timestamp": 1672531200000
}
```

---

### 2.7 POST /api/process-definitions/{id}/start

**功能描述**：启动工作流

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 工作流ID |

**请求体**：
```json
{
  "params": "{\"key\": \"value\"} (选填，运行参数)"
}
```

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "启动成功",
  "data": {
    "processInstanceId": 1001,
    "status": "RUNNING"
  },
  "timestamp": 1672531200000
}
```

---

## 3. 任务接口

### 3.1 GET /api/task-definitions

**功能描述**：获取任务列表

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | string | 否 | 任务名称 |
| type | string | 否 | 任务类型 |
| projectId | number | 否 | 项目ID |
| page | number | 否 | 页码 |
| size | number | 否 | 每页条数 |

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "数据清洗任务",
        "type": "SHELL",
        "projectId": 1,
        "config": "{\"script\":\"echo hello\"}",
        "description": "数据清洗脚本",
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  },
  "timestamp": 1672531200000
}
```

---

### 3.2 POST /api/task-definitions

**功能描述**：创建任务

**请求体**：
```json
{
  "name": "string (必填)",
  "type": "string (必填，任务类型)",
  "projectId": "number (必填)",
  "config": "string (选填，JSON配置)",
  "description": "string (选填)"
}
```

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "数据清洗任务",
    "type": "SHELL",
    "projectId": 1
  },
  "timestamp": 1672531200000
}
```

---

### 3.3 GET /api/task-definitions/{id}

**功能描述**：获取任务详情

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "数据清洗任务",
    "type": "SHELL",
    "projectId": 1,
    "config": "{\"script\":\"echo hello\"}",
    "description": "数据清洗脚本",
    "createTime": "2024-01-01 10:00:00",
    "updateTime": "2024-01-01 10:00:00"
  },
  "timestamp": 1672531200000
}
```

---

### 3.4 PUT /api/task-definitions/{id}

**功能描述**：更新任务

**请求体**：
```json
{
  "name": "string (选填)",
  "type": "string (选填)",
  "config": "string (选填)",
  "description": "string (选填)"
}
```

---

### 3.5 DELETE /api/task-definitions/{id}

**功能描述**：删除任务

---

## 4. 调度接口

### 4.1 GET /api/schedules

**功能描述**：获取调度列表

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| processDefinitionId | number | 否 | 工作流ID |
| status | number | 否 | 状态(0-禁用,1-启用) |

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "processDefinitionId": 1,
      "processDefinitionName": "数据同步工作流",
      "cronExpression": "0 0 2 * * ?",
      "startTime": "2024-01-01 00:00:00",
      "endTime": null,
      "status": 1,
      "createTime": "2024-01-01 10:00:00"
    }
  ],
  "timestamp": 1672531200000
}
```

---

### 4.2 POST /api/schedules

**功能描述**：创建调度

**请求体**：
```json
{
  "processDefinitionId": "number (必填)",
  "cronExpression": "string (必填)",
  "startTime": "string (选填，格式: yyyy-MM-dd HH:mm:ss)",
  "endTime": "string (选填)"
}
```

---

### 4.3 GET /api/schedules/{id}

**功能描述**：获取调度详情

---

### 4.4 PUT /api/schedules/{id}

**功能描述**：更新调度

**请求体**：
```json
{
  "cronExpression": "string (选填)",
  "startTime": "string (选填)",
  "endTime": "string (选填)"
}
```

---

### 4.5 DELETE /api/schedules/{id}

**功能描述**：删除调度

---

### 4.6 POST /api/schedules/{id}/enable

**功能描述**：启用调度

---

### 4.7 POST /api/schedules/{id}/disable

**功能描述**：禁用调度

---

## 5. 文件管理接口

### 5.1 GET /api/files

**功能描述**：获取文件列表

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | string | 否 | 文件名 |
| type | string | 否 | 文件类型 |
| projectId | number | 否 | 项目ID |

---

### 5.2 POST /api/files/upload

**功能描述**：上传文件

**请求体** (multipart/form-data)：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 文件 |
| projectId | number | 否 | 项目ID |

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 1,
    "name": "script.sh",
    "path": "/uploads/script.sh",
    "size": 1024,
    "type": "SHELL",
    "createTime": "2024-01-01 10:00:00"
  },
  "timestamp": 1672531200000
}
```

---

### 5.3 GET /api/files/{id}

**功能描述**：获取文件详情

---

### 5.4 DELETE /api/files/{id}

**功能描述**：删除文件

---

### 5.5 GET /api/files/{id}/content

**功能描述**：获取文件内容

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "script.sh",
    "content": "#!/bin/bash\necho \"Hello World\"",
    "size": 26
  },
  "timestamp": 1672531200000
}
```

---

### 5.6 PUT /api/files/{id}/content

**功能描述**：更新文件内容

**请求体**：
```json
{
  "content": "string (必填，文件内容)"
}
```

---

### 5.7 GET /api/files/directory

**功能描述**：获取目录结构

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| parentId | number | 否 | 父目录ID（null表示根目录） |

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "directories": [
      {
        "id": 1,
        "name": "shell",
        "isDirectory": 1,
        "parentId": null,
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "files": [
      {
        "id": 2,
        "name": "test.sh",
        "isDirectory": 0,
        "parentId": null,
        "size": 100,
        "createTime": "2024-01-01 10:00:00"
      }
    ]
  },
  "timestamp": 1672531200000
}
```

---

## 6. 项目管理接口

### 6.1 GET /api/projects

**功能描述**：获取项目列表

---

### 6.2 POST /api/projects

**功能描述**：创建项目

**请求体**：
```json
{
  "name": "string (必填)",
  "description": "string (选填)"
}
```

---

### 6.3 GET /api/projects/{id}

**功能描述**：获取项目详情

---

### 6.4 PUT /api/projects/{id}

**功能描述**：更新项目

---

### 6.5 DELETE /api/projects/{id}

**功能描述**：删除项目

---

## 7. 数据源接口

### 7.1 GET /api/datasources

**功能描述**：获取数据源列表

---

### 7.2 POST /api/datasources

**功能描述**：创建数据源

**请求体**：
```json
{
  "name": "string (必填)",
  "type": "string (必填，如: MYSQL, POSTGRESQL)",
  "host": "string (必填)",
  "port": "number (必填)",
  "database": "string (必填)",
  "username": "string (必填)",
  "password": "string (必填)",
  "projectId": "number (选填)"
}
```

---

### 7.3 GET /api/datasources/{id}

**功能描述**：获取数据源详情

---

### 7.4 PUT /api/datasources/{id}

**功能描述**：更新数据源

---

### 7.5 DELETE /api/datasources/{id}

**功能描述**：删除数据源

---

### 7.6 POST /api/datasources/{id}/test

**功能描述**：测试数据源连接

**成功响应** (200)：
```json
{
  "code": 200,
  "message": "连接成功",
  "data": null,
  "timestamp": 1672531200000
}
```

---

## 8. 系统管理接口

### 8.1 用户管理

#### GET /api/users
#### POST /api/users
#### GET /api/users/{id}
#### PUT /api/users/{id}
#### DELETE /api/users/{id}

**创建用户请求体**：
```json
{
  "username": "string (必填)",
  "password": "string (必填)",
  "realName": "string (选填)",
  "email": "string (选填)",
  "phone": "string (选填)",
  "status": "number (选填，默认1)"
}
```

#### GET /api/users/{id}/roles
**功能描述**：获取用户角色

#### POST /api/users/{id}/roles
**功能描述**：分配角色

**请求体**：
```json
{
  "roleIds": "[number] (角色ID列表)"
}
```

---

### 8.2 角色管理

#### GET /api/roles
#### POST /api/roles
#### GET /api/roles/{id}
#### PUT /api/roles/{id}
#### DELETE /api/roles/{id}

**创建角色请求体**：
```json
{
  "name": "string (必填)",
  "code": "string (必填)",
  "description": "string (选填)"
}
```

---

### 8.3 菜单管理

#### GET /api/menus
#### POST /api/menus
#### PUT /api/menus/{id}

**创建菜单请求体**：
```json
{
  "name": "string (必填)",
  "path": "string (选填)",
  "icon": "string (选填)",
  "parentId": "number (选填)",
  "sortOrder": "number (选填)",
  "type": "number (选填，0-菜单,1-按钮)"
}
```

---

### 8.4 系统配置

#### GET /api/configs
#### POST /api/configs
#### PUT /api/configs/{id}

**创建配置请求体**：
```json
{
  "configKey": "string (必填)",
  "configValue": "string (必填)",
  "description": "string (选填)"
}
```

---

## 9. 响应格式

### 9.1 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1672531200000
}
```

### 9.2 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [],
    "totalElements": 100,
    "totalPages": 5,
    "number": 0,
    "size": 20
  },
  "timestamp": 1672531200000
}
```

### 9.3 错误响应

```json
{
  "code": 400,
  "message": "错误描述信息",
  "data": null,
  "timestamp": 1672531200000
}
```

### 9.4 状态码说明

| 状态码 | 含义 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

**文档版本**: v1.0  
**创建日期**: 2026-05-20  
**最后更新**: 2026-05-20
