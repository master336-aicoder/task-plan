import request from '@/utils/request'

export interface Task {
  id?: number
  name: string
  description?: string
  type: string
  command?: string
  parameters?: string
  envVars?: string
  workingDir?: string
  tags?: string
  owner?: string
  status?: number
  createdTime?: string
  updatedTime?: string
}

export interface TaskSchedule {
  id?: number
  taskId: number
  scheduleType: string
  cronExpression?: string
  fixedInterval?: number
  timeUnit?: string
  startTime?: string
  endTime?: string
  timezone?: string
  misfirePolicy?: number
  enabled?: number
  maxConcurrent?: number
  createdTime?: string
}

export interface TaskInstance {
  id?: number
  taskId: number
  scheduleId?: number
  instanceKey?: string
  status: string
  triggerType?: string
  startTime?: string
  endTime?: string
  duration?: number
  result?: string
  logs?: string
  retryCount?: number
  createdTime?: string
}

export const taskApi = {
  list: () => request.get<Task[]>('/tasks'),
  get: (id: number) => request.get<Task>(`/tasks/${id}`),
  create: (data: Task) => request.post<Task>('/tasks', data),
  update: (id: number, data: Task) => request.put<Task>(`/tasks/${id}`, data),
  delete: (id: number) => request.delete(`/tasks/${id}`)
}

export const scheduleApi = {
  list: (taskId?: number) => request.get<TaskSchedule[]>('/schedules', { params: { taskId } }),
  get: (id: number) => request.get<TaskSchedule>(`/schedules/${id}`),
  create: (data: TaskSchedule) => request.post<TaskSchedule>('/schedules', data),
  update: (id: number, data: TaskSchedule) => request.put<TaskSchedule>(`/schedules/${id}`, data),
  delete: (id: number) => request.delete(`/schedules/${id}`),
  toggle: (id: number, enabled: number) => request.put(`/schedules/${id}/toggle`, null, { params: { enabled } })
}

export const instanceApi = {
  list: (taskId?: number) => request.get<TaskInstance[]>('/instances', { params: { taskId } }),
  get: (id: number) => request.get<TaskInstance>(`/instances/${id}`),
  stop: (id: number) => request.put(`/instances/${id}/stop`),
  retry: (id: number) => request.post(`/instances/${id}/retry`)
}
