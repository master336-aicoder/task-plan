import request from '@/utils/request'

export interface Project {
  id?: number
  name: string
  description?: string
  createTime?: string
  updateTime?: string
}

export interface TaskDefinition {
  id?: number
  projectId: number
  name: string
  taskType: string
  params?: string
  envParams?: string
  taskPriority?: number
  workerGroup?: string
  failRetryTimes?: number
  failRetryInterval?: number
  timeoutFlag?: number
  timeoutNotifyStrategy?: string
  timeout?: number
  owner?: string
  createTime?: string
  updateTime?: string
}

export interface ProcessDefinition {
  id?: number
  projectId: number
  name: string
  description?: string
  definitionJson?: string
  processType?: string
  releaseState?: string
  version?: number
  owner?: string
  createTime?: string
  updateTime?: string
}

export interface ProcessSchedule {
  id?: number
  projectId: number
  processDefinitionId: number
  cronExpression?: string
  startTime?: string
  endTime?: string
  timezone?: string
  delayTime?: number
  warningType?: string
  warningGroupId?: number
  failRetryTimes?: number
  failRetryInterval?: number
  isValid?: number
  owner?: string
  createTime?: string
  updateTime?: string
}

export interface ProcessInstance {
  id?: number
  projectId: number
  processDefinitionId: number
  processName?: string
  state?: string
  startTime?: string
  endTime?: string
  duration?: number
  commandType?: string
  commandParam?: string
  taskInstanceCount?: number
  successTaskCount?: number
  failedTaskCount?: number
  runningTaskCount?: number
  owner?: string
  createTime?: string
  updateTime?: string
}

export interface TaskInstance {
  id?: number
  projectId: number
  processInstanceId: number
  taskDefinitionId: number
  taskName?: string
  taskType?: string
  state?: string
  startTime?: string
  endTime?: string
  duration?: number
  retryTimes?: number
  logPath?: string
  errorMsg?: string
  executePath?: string
  host?: string
  owner?: string
  createTime?: string
  updateTime?: string
}

export const projectApi = {
  list: (params?: { keyword?: string }) => request.get<Project[]>('/projects', { params }),
  get: (id: number) => request.get<Project>(`/projects/${id}`),
  create: (data: Project) => request.post<Project>('/projects', data),
  update: (id: number, data: Project) => request.put<Project>(`/projects/${id}`, data),
  delete: (id: number) => request.delete(`/projects/${id}`)
}

export const taskApi = {
  list: (params: { projectId: number; name?: string; page?: number; size?: number }) => 
    request.get('/task-definitions', { params }),
  listByProject: (projectId: number) => request.get<TaskDefinition[]>(`/task-definitions/project/${projectId}`),
  get: (id: number) => request.get<TaskDefinition>(`/task-definitions/${id}`),
  create: (data: TaskDefinition) => request.post<TaskDefinition>('/task-definitions', data),
  update: (id: number, data: TaskDefinition) => request.put<TaskDefinition>(`/task-definitions/${id}`, data),
  delete: (id: number) => request.delete(`/task-definitions/${id}`)
}

export const workflowApi = {
  list: (params: { projectId?: number; name?: string; page?: number; size?: number }) => 
    request.get('/process-definitions', { params }),
  listByProject: (projectId: number) => request.get<ProcessDefinition[]>(`/process-definitions/project/${projectId}`),
  listOnline: (projectId: number) => request.get<ProcessDefinition[]>(`/process-definitions/project/${projectId}/online`),
  get: (id: number) => request.get<ProcessDefinition>(`/process-definitions/${id}`),
  create: (data: ProcessDefinition) => request.post<ProcessDefinition>('/process-definitions', data),
  saveDesign: (data: any) => request.post<ProcessDefinition>('/process-definitions/design', data),
  update: (id: number, data: ProcessDefinition) => request.put<ProcessDefinition>(`/process-definitions/${id}`, data),
  release: (id: number) => request.post<ProcessDefinition>(`/process-definitions/${id}/release`),
  offline: (id: number) => request.post<ProcessDefinition>(`/process-definitions/${id}/offline`),
  execute: (id: number) => request.post(`/process-definitions/${id}/execute`),
  delete: (id: number) => request.delete(`/process-definitions/${id}`)
}

export const scheduleApi = {
  list: (params: { projectId: number; processDefinitionId?: number; page?: number; size?: number }) => 
    request.get('/process-schedules', { params }),
  listByProcess: (processDefinitionId: number) => request.get<ProcessSchedule[]>(`/process-schedules/process/${processDefinitionId}`),
  get: (id: number) => request.get<ProcessSchedule>(`/process-schedules/${id}`),
  create: (data: ProcessSchedule) => request.post<ProcessSchedule>('/process-schedules', data),
  update: (id: number, data: ProcessSchedule) => request.put<ProcessSchedule>(`/process-schedules/${id}`, data),
  enable: (id: number) => request.post<ProcessSchedule>(`/process-schedules/${id}/enable`),
  disable: (id: number) => request.post<ProcessSchedule>(`/process-schedules/${id}/disable`),
  delete: (id: number) => request.delete(`/process-schedules/${id}`)
}

export const instanceApi = {
  list: (params: { projectId: number; state?: string; processDefinitionId?: number; page?: number; size?: number }) => 
    request.get('/process-instances', { params }),
  get: (id: number) => request.get<ProcessInstance>(`/process-instances/${id}`),
  start: (processDefinitionId: number) => request.post<ProcessInstance>('/process-instances/start', null, { params: { processDefinitionId } }),
  stop: (id: number) => request.post<ProcessInstance>(`/process-instances/${id}/stop`),
  success: (id: number) => request.post<ProcessInstance>(`/process-instances/${id}/success`),
  fail: (id: number) => request.post<ProcessInstance>(`/process-instances/${id}/fail`)
}

export const executionApi = instanceApi

export const taskInstanceApi = {
  list: (params: { projectId: number; processInstanceId?: number; page?: number; size?: number }) => 
    request.get('/task-instances', { params }),
  listByProcess: (processInstanceId: number) => request.get<TaskInstance[]>(`/task-instances/process/${processInstanceId}`),
  get: (id: number) => request.get<TaskInstance>(`/task-instances/${id}`),
  create: (params: { processInstanceId: number; taskDefinitionId: number }) => 
    request.post<TaskInstance>('/task-instances', null, { params }),
  start: (id: number) => request.post<TaskInstance>(`/task-instances/${id}/start`),
  complete: (id: number, params: { state: string; errorMsg?: string }) => 
    request.post<TaskInstance>(`/task-instances/${id}/complete`, null, { params }),
  retry: (id: number) => request.post<TaskInstance>(`/task-instances/${id}/retry`)
}

export const taskTypes = [
  { label: 'Shell脚本', value: 'SHELL' },
  { label: 'Python脚本', value: 'PYTHON' },
  { label: 'Perl脚本', value: 'PERL' },
  { label: 'Java程序', value: 'JAVA' },
  { label: 'HTTP请求', value: 'HTTP' },
  { label: 'SQL脚本', value: 'SQL' },
  { label: 'Spark任务', value: 'SPARK' },
  { label: 'Flink任务', value: 'FLINK' },
  { label: 'MR任务', value: 'MAPREDUCE' }
]

export const processStates = [
  { label: '运行中', value: 'RUNNING_EXECUTION' },
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAILURE' },
  { label: '停止', value: 'STOP' },
  { label: '等待中', value: 'PENDING' }
]

export const taskStates = [
  { label: '提交成功', value: 'SUBMITTED_SUCCESS' },
  { label: '运行中', value: 'RUNNING_EXECUTION' },
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAILURE' },
  { label: '等待中', value: 'PENDING' },
  { label: '重试', value: 'NEED_FAULT_TOLERANCE' }
]

export interface Datasource {
  id?: number
  name: string
  type: string
  host: string
  port: number
  databaseName: string
  username: string
  password?: string
  params?: string
  createTime?: string
  updateTime?: string
}

export interface ServerNode {
  id?: number
  name: string
  hostname: string
  port?: number
  username: string
  password?: string
  groupName?: string
  status?: string
  createTime?: string
  updateTime?: string
}

export interface FileResource {
  id?: number
  name: string
  path?: string
  size?: number
  type?: string
  version?: number
  projectId?: number
  isDirectory?: number
  parentId?: number | null
  createTime?: string
  updateTime?: string
}

export interface ClusterConfig {
  id?: number
  name: string
  type: string
  configJson?: string
  description?: string
  createTime?: string
  updateTime?: string
}

export interface TaskTypeConfig {
  id?: number
  name: string
  taskType: string
  programPath?: string
  defaultParams?: string
  envParams?: string
  description?: string
  createTime?: string
  updateTime?: string
}

export const datasourceApi = {
  list: (params?: { name?: string; page?: number; size?: number }) => request.get('/datasources', { params }),
  listAll: () => request.get<Datasource[]>('/datasources/all'),
  listByType: (type: string) => request.get<Datasource[]>(`/datasources/type/${type}`),
  get: (id: number) => request.get<Datasource>(`/datasources/${id}`),
  create: (data: Datasource) => request.post<Datasource>('/datasources', data),
  update: (id: number, data: Datasource) => request.put<Datasource>(`/datasources/${id}`, data),
  delete: (id: number) => request.delete(`/datasources/${id}`),
  test: (id: number) => request.post<boolean>(`/datasources/${id}/test`),
  testConnection: (data: Datasource) => request.post('/datasources/test-connection', data)
}

export const serverApi = {
  list: (params?: { name?: string; page?: number; size?: number }) => request.get('/servers', { params }),
  listAll: () => request.get<ServerNode[]>('/servers/all'),
  listOnline: () => request.get<ServerNode[]>('/servers/online'),
  listByGroup: (groupName: string) => request.get<ServerNode[]>(`/servers/group/${groupName}`),
  get: (id: number) => request.get<ServerNode>(`/servers/${id}`),
  create: (data: ServerNode) => request.post<ServerNode>('/servers', data),
  update: (id: number, data: ServerNode) => request.put<ServerNode>(`/servers/${id}`, data),
  delete: (id: number) => request.delete(`/servers/${id}`),
  ping: (id: number) => request.post<boolean>(`/servers/${id}/ping`),
  testConnection: (data: ServerNode) => request.post('/servers/test-connection', data)
}

export const fileApi = {
  list: (params?: { name?: string; projectId?: number; page?: number; size?: number }) => request.get('/files', { params }),
  listAll: () => request.get<FileResource[]>('/files/all'),
  listByType: (type: string) => request.get<FileResource[]>(`/files/type/${type}`),
  get: (id: number) => request.get<FileResource>(`/files/${id}`),
  getDirectory: (parentId?: number | null) => request.get('/files/directory', { params: { parentId } }),
  upload: (file: FormData) => request.post<FileResource>('/files/upload', file),
  delete: (id: number) => request.delete(`/files/${id}`)
}

export const clusterConfigApi = {
  list: (params?: { name?: string; page?: number; size?: number }) => request.get('/cluster-configs', { params }),
  listAll: () => request.get<ClusterConfig[]>('/cluster-configs/all'),
  listByType: (type: string) => request.get<ClusterConfig[]>(`/cluster-configs/type/${type}`),
  get: (id: number) => request.get<ClusterConfig>(`/cluster-configs/${id}`),
  create: (data: ClusterConfig) => request.post<ClusterConfig>('/cluster-configs', data),
  update: (id: number, data: ClusterConfig) => request.put<ClusterConfig>(`/cluster-configs/${id}`, data),
  delete: (id: number) => request.delete(`/cluster-configs/${id}`)
}

export const taskTypeApi = {
  list: (params?: { name?: string; page?: number; size?: number }) => request.get('/task-types', { params }),
  listAll: () => request.get<TaskTypeConfig[]>('/task-types/all'),
  listByTaskType: (taskType: string) => request.get<TaskTypeConfig[]>(`/task-types/task-type/${taskType}`),
  get: (id: number) => request.get<TaskTypeConfig>(`/task-types/${id}`),
  create: (data: TaskTypeConfig) => request.post<TaskTypeConfig>('/task-types', data),
  update: (id: number, data: TaskTypeConfig) => request.put<TaskTypeConfig>(`/task-types/${id}`, data),
  delete: (id: number) => request.delete(`/task-types/${id}`)
}

export interface EnvVariable {
  id?: number
  name: string
  value?: string
  description?: string
  category?: string
  createTime?: string
  updateTime?: string
}

export const envVariableApi = {
  list: (params?: { name?: string; page?: number; size?: number }) => request.get('/env-variables', { params }),
  listAll: () => request.get<EnvVariable[]>('/env-variables/all'),
  listByCategory: (category: string) => request.get<EnvVariable[]>(`/env-variables/category/${category}`),
  get: (id: number) => request.get<EnvVariable>(`/env-variables/${id}`),
  create: (data: EnvVariable) => request.post<EnvVariable>('/env-variables', data),
  update: (id: number, data: EnvVariable) => request.put<EnvVariable>(`/env-variables/${id}`, data),
  delete: (id: number) => request.delete(`/env-variables/${id}`)
}

export interface LoginForm {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  userId: number
  username: string
  realName: string
  roles: number[]
  menus: any[]
}

export interface SysUser {
  id?: number
  username: string
  password?: string
  realName?: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface SysRole {
  id?: number
  roleCode: string
  roleName: string
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface SysMenu {
  id?: number
  menuName: string
  menuCode: string
  menuType: string
  path: string
  component?: string
  parentId?: number
  icon?: string
  sortOrder?: number
  status?: number
  permission?: string
  createTime?: string
  updateTime?: string
  children?: SysMenu[]
}

export const authApi = {
  login: (data: LoginForm) => request.post<LoginResult>('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getUserInfo: () => request.get('/auth/userinfo')
}

export const userApi = {
  list: () => request.get<SysUser[]>('/users'),
  get: (id: number) => request.get<SysUser>(`/users/${id}`),
  create: (data: SysUser) => request.post<SysUser>('/users', data),
  update: (id: number, data: SysUser) => request.put<SysUser>(`/users/${id}`, data),
  delete: (id: number) => request.delete(`/users/${id}`),
  getRoles: (id: number) => request.get<SysRole[]>(`/users/${id}/roles`),
  assignRoles: (id: number, roleIds: number[]) => request.post(`/users/${id}/roles`, roleIds)
}

export const roleApi = {
  list: () => request.get<SysRole[]>('/roles'),
  listActive: () => request.get<SysRole[]>('/roles/active'),
  get: (id: number) => request.get<SysRole>(`/roles/${id}`),
  create: (data: SysRole) => request.post<SysRole>('/roles', data),
  update: (id: number, data: SysRole) => request.put<SysRole>(`/roles/${id}`, data),
  delete: (id: number) => request.delete(`/roles/${id}`),
  getMenus: (id: number) => request.get<SysMenu[]>(`/roles/${id}/menus`),
  assignMenus: (id: number, menuIds: number[]) => request.post(`/roles/${id}/menus`, menuIds)
}

export const menuApi = {
  list: () => request.get<SysMenu[]>('/menus'),
  getTree: () => request.get<SysMenu[]>('/menus/tree'),
  get: (id: number) => request.get<SysMenu>(`/menus/${id}`),
  create: (data: SysMenu) => request.post<SysMenu>('/menus', data),
  update: (id: number, data: SysMenu) => request.put<SysMenu>(`/menus/${id}`, data),
  delete: (id: number) => request.delete(`/menus/${id}`)
}
