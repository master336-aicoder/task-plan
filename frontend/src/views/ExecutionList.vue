<template>
  <div class="execution-list">
    <div class="page-header">
      <div class="flex justify-between items-center mb-3">
        <h2 class="text-xl font-bold">执行记录</h2>
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-500">自动刷新:</span>
          <el-select v-model="refreshInterval" placeholder="刷新间隔" style="width: 120px" @change="resetRefreshTimer">
            <el-option label="5秒" :value="5" />
            <el-option label="30秒" :value="30" />
            <el-option label="1分钟" :value="60" />
            <el-option label="2分钟" :value="120" />
            <el-option label="5分钟" :value="300" />
          </el-select>
        </div>
      </div>
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="项目">
          <el-select v-model="queryForm.projectId" placeholder="选择项目" @change="onProjectChange" style="width: 180px">
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作流">
          <el-select v-model="queryForm.workflowId" placeholder="选择工作流" clearable style="width: 200px">
            <el-option v-for="workflow in workflows" :key="workflow.id" :label="workflow.name" :value="workflow.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.state" placeholder="状态筛选" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="运行中" value="RUNNING" />
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILURE" />
            <el-option label="等待" value="WAITING" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="queryForm.startDate" type="date" placeholder="开始日期" style="width: 150px" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="queryForm.endDate" type="date" placeholder="结束日期" style="width: 150px" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="flex gap-4 flex-wrap justify-end mt-2">
        <el-button v-if="selectedIds.length > 0" type="warning" @click="batchStop" :loading="batchLoading">
          批量停止
        </el-button>
        <el-button v-if="selectedIds.length > 0" type="success" @click="batchRetry" :loading="batchLoading">
          批量重试
        </el-button>
        <el-popconfirm v-if="selectedIds.length > 0" title="确定要批量删除选中的执行记录吗？" @confirm="batchDelete">
          <template #reference>
            <el-button type="danger" :loading="batchLoading">
              批量删除
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </div>

    <el-table :data="executions" border v-loading="loading" stripe @select-all="handleSelectAll" @select="handleSelect">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="processDefinitionName" label="工作流名称" min-width="150" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="duration" label="耗时" width="120" />
      <el-table-column prop="state" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStateTagType(row.state)">{{ getStateLabel(row.state) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="host" label="执行主机" width="120" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button v-if="row.state === 'RUNNING_EXECUTION'" size="small" type="danger" @click="stopExecution(row)">停止</el-button>
          <el-button v-if="canRetry(row.state)" size="small" type="success" @click="retryExecution(row)">重试</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="mt-4"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadExecutions"
      @current-change="loadExecutions"
    />

    <el-dialog v-model="detailVisible" title="执行详情" width="60%" height="80%">
      <div v-if="detailData" class="detail-content">
        <div class="detail-section">
          <h3>基本信息</h3>
          <el-row :gutter="20">
            <el-col :span="8">工作流名称: {{ detailData.processDefinitionName }}</el-col>
            <el-col :span="8">状态: <el-tag :type="getStateTagType(detailData.state)">{{ getStateLabel(detailData.state) }}</el-tag></el-col>
            <el-col :span="8">执行主机: {{ detailData.host }}</el-col>
          </el-row>
          <el-row :gutter="20" class="mt-2">
            <el-col :span="8">开始时间: {{ detailData.startTime }}</el-col>
            <el-col :span="8">结束时间: {{ detailData.endTime }}</el-col>
            <el-col :span="8">耗时: {{ detailData.duration }}</el-col>
          </el-row>
        </div>
        <div class="detail-section mt-4">
          <h3>任务实例</h3>
          <el-table :data="detailData.taskInstances" border>
            <el-table-column prop="taskName" label="任务名称" />
            <el-table-column prop="taskType" label="任务类型" />
            <el-table-column prop="startTime" label="开始时间" />
            <el-table-column prop="endTime" label="结束时间" />
            <el-table-column prop="state" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStateTagType(row.state)">{{ getStateLabel(row.state) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="serverIp" label="执行服务器" />
            <el-table-column label="操作" min-width="200">
              <template #default="{ row }">
                <el-button size="small" @click="showTaskLog(row)">查看日志</el-button>
                <el-button size="small" type="primary" @click="showTaskContent(row)">任务详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <el-dialog v-model="taskLogVisible" title="任务日志详情" width="800px">
          <div v-if="currentTaskLog" class="task-log-detail">
            <div class="log-section">
              <h4>输出内容</h4>
              <pre class="log-content">{{ currentTaskLog.log || '无输出内容' }}</pre>
            </div>
            <div v-if="currentTaskLog.error" class="log-section">
              <h4>错误信息</h4>
              <pre class="log-content error">{{ currentTaskLog.error }}</pre>
            </div>
          </div>
          <template #footer>
            <el-button @click="taskLogVisible = false">关闭</el-button>
          </template>
        </el-dialog>
        
        <el-dialog v-model="taskContentVisible" title="任务执行内容" width="80%" height="80%">
          <div v-if="currentTaskContent" class="task-content-detail">
            <div class="content-section">
              <h4>任务名称: {{ currentTaskContent.taskName }}</h4>
              <h4>任务类型: {{ currentTaskContent.taskType }}</h4>
            </div>
            <div class="content-section mt-4">
              <h4>执行内容</h4>
              <el-input
                type="textarea"
                :value="currentTaskContent.resolvedContent || '无执行内容'"
                :rows="15"
                readonly
                class="w-full"
              />
            </div>
            <div v-if="currentTaskContent.taskType === 'HTTP' && currentTaskContent.httpInfo" class="content-section mt-4">
              <h4>HTTP请求信息</h4>
              <el-input
                type="textarea"
                :value="currentTaskContent.httpInfo"
                :rows="8"
                readonly
                class="w-full"
              />
            </div>
            <div v-if="currentTaskContent.taskType === 'JAVA' && currentTaskContent.commandInfo" class="content-section mt-4">
              <h4>Java命令信息</h4>
              <el-input
                type="textarea"
                :value="currentTaskContent.commandInfo"
                :rows="8"
                readonly
                class="w-full"
              />
            </div>
          </div>
          <template #footer>
            <el-button @click="taskContentVisible = false">关闭</el-button>
          </template>
        </el-dialog>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { projectApi, workflowApi, executionApi, type Project, type ProcessDefinition } from '@/api'

const executions = ref<any[]>([])
const projects = ref<Project[]>([])
const workflows = ref<ProcessDefinition[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const detailData = ref<any>(null)
const batchLoading = ref(false)
const taskLogVisible = ref(false)
const currentTaskLog = ref<any>(null)
const taskContentVisible = ref(false)
const currentTaskContent = ref<any>(null)
const refreshInterval = ref(5)
let refreshTimer: number | null = null

const queryForm = ref({
  projectId: undefined as number | undefined,
  workflowId: undefined as number | undefined,
  state: '',
  startDate: '',
  endDate: ''
})
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref<number[]>([])

const stateLabels: Record<string, string> = {
  RUNNING_EXECUTION: '运行中',
  SUCCESS: '成功',
  FAILURE: '失败',
  PENDING: '等待',
  STOP: '已停止',
  TIMEOUT: '超时',
  PARTIAL_SUCCESS: '部分成功'
}

const stateTagTypes: Record<string, string> = {
  RUNNING_EXECUTION: 'warning',
  SUCCESS: 'success',
  FAILURE: 'danger',
  PENDING: 'info',
  STOP: 'default',
  TIMEOUT: 'danger',
  PARTIAL_SUCCESS: 'warning'
}

const getStateLabel = (state: string) => stateLabels[state] || state
const getStateTagType = (state: string) => stateTagTypes[state] || 'default'

const canRetry = (state: string) => {
  return ['FAILURE', 'STOP', 'TIMEOUT'].includes(state)
}

const loadProjects = async () => {
  try {
    const res = await projectApi.list()
    projects.value = res.data || res || []
    if (projects.value.length > 0 && !queryForm.value.projectId) {
      queryForm.value.projectId = projects.value[0].id
      await loadWorkflows()
    }
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const onProjectChange = async () => {
  queryForm.value.workflowId = undefined
  await loadWorkflows()
  handleQuery()
}

const loadWorkflows = async () => {
  if (!queryForm.value.projectId) return
  try {
    const res = await workflowApi.listByProject(queryForm.value.projectId)
    workflows.value = res.data || res || []
  } catch (error) {
    console.error('加载工作流列表失败', error)
  }
}

const handleQuery = () => {
  currentPage.value = 1
  loadExecutions()
}

const handleReset = () => {
  queryForm.value = {
    projectId: projects.value[0]?.id,
    workflowId: undefined,
    state: '',
    startDate: '',
    endDate: ''
  }
  handleQuery()
}

const loadExecutions = async () => {
  if (!queryForm.value.projectId) return
  
  loading.value = true
  try {
    const res = await executionApi.list({
      projectId: queryForm.value.projectId,
      processDefinitionId: queryForm.value.workflowId || undefined,
      state: queryForm.value.state || undefined,
      startDate: queryForm.value.startDate || undefined,
      endDate: queryForm.value.endDate || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    
    const data = res.data || []
    const workflowMap = new Map()
    workflows.value.forEach(w => workflowMap.set(w.id, w.name))
    
    executions.value = data.map((e: any) => ({
      ...e,
      processDefinitionName: workflowMap.get(e.processDefinitionId) || e.processName || '未知',
      duration: calculateDuration(e.startTime, e.endTime)
    }))
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载执行记录失败')
  } finally {
    loading.value = false
  }
}

const calculateDuration = (startTime: string, endTime: string) => {
  if (!startTime || !endTime) return '-'
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  const diff = end - start
  const hours = Math.floor(diff / 3600000)
  const minutes = Math.floor((diff % 3600000) / 60000)
  const seconds = Math.floor((diff % 60000) / 1000)
  if (hours > 0) {
    return `${hours}h ${minutes}m ${seconds}s`
  } else if (minutes > 0) {
    return `${minutes}m ${seconds}s`
  } else {
    return `${seconds}s`
  }
}

const viewDetail = async (execution: any) => {
  try {
    const res = await executionApi.get(execution.id)
    detailData.value = res.data || res
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

const showTaskLog = (task: any) => {
  currentTaskLog.value = task
  taskLogVisible.value = true
}

const showTaskContent = async (task: any) => {
  try {
    const res = await executionApi.getTaskContent(detailData.value.id, task.taskNodeId)
    currentTaskContent.value = res.data || res
    taskContentVisible.value = true
  } catch (error) {
    ElMessage.error('获取任务内容失败')
  }
}

const stopExecution = async (execution: any) => {
  try {
    await executionApi.stop(execution.id)
    ElMessage.success('停止成功')
    loadExecutions()
  } catch (error) {
    ElMessage.error('停止失败')
  }
}

const handleSelectAll = (val: any[]) => {
  selectedIds.value = val.map((item: any) => item.id)
}

const handleSelect = (val: any[], row: any) => {
  const idx = selectedIds.value.indexOf(row.id)
  if (idx > -1) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(row.id)
  }
}

const batchStop = async () => {
  batchLoading.value = true
  try {
    for (const id of selectedIds.value) {
      await executionApi.stop(id)
    }
    ElMessage.success(`成功停止 ${selectedIds.value.length} 个执行记录`)
    selectedIds.value = []
    loadExecutions()
  } catch (error) {
    ElMessage.error('批量停止失败')
  } finally {
    batchLoading.value = false
  }
}

const batchDelete = async () => {
  batchLoading.value = true
  try {
    for (const id of selectedIds.value) {
      await executionApi.delete(id)
    }
    ElMessage.success(`成功删除 ${selectedIds.value.length} 个执行记录`)
    selectedIds.value = []
    loadExecutions()
  } catch (error) {
    ElMessage.error('批量删除失败')
  } finally {
    batchLoading.value = false
  }
}

const retryExecution = async (execution: any) => {
  try {
    await executionApi.retry(execution.id)
    ElMessage.success('重试成功')
    loadExecutions()
  } catch (error) {
    ElMessage.error('重试失败')
  }
}

const batchRetry = async () => {
  batchLoading.value = true
  const retryableIds = []
  for (const id of selectedIds.value) {
    const execution = executions.value.find(e => e.id === id)
    if (execution && canRetry(execution.state)) {
      retryableIds.push(id)
    }
  }
  
  if (retryableIds.length === 0) {
    ElMessage.warning('没有可重试的执行记录（仅失败、已停止、超时的任务可以重试）')
    batchLoading.value = false
    return
  }
  
  try {
    for (const id of retryableIds) {
      await executionApi.retry(id)
    }
    ElMessage.success(`成功重试 ${retryableIds.length} 个执行记录`)
    selectedIds.value = []
    loadExecutions()
  } catch (error) {
    ElMessage.error('批量重试失败')
  } finally {
    batchLoading.value = false
  }
}

const startRefreshTimer = () => {
  stopRefreshTimer()
  refreshTimer = window.setInterval(() => {
    loadExecutions()
  }, refreshInterval.value * 1000)
}

const stopRefreshTimer = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const resetRefreshTimer = () => {
  startRefreshTimer()
}

onMounted(async () => {
  await loadProjects()
  await loadWorkflows()
  loadExecutions()
  startRefreshTimer()
})

onUnmounted(() => {
  stopRefreshTimer()
})
</script>

<style scoped>
.detail-section {
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
}
.detail-section h3 {
  margin-bottom: 12px;
  font-weight: bold;
}

.task-log-detail {
  max-height: 500px;
  overflow-y: auto;
}

.log-section {
  margin-bottom: 16px;
}

.log-section h4 {
  margin-bottom: 8px;
  font-weight: bold;
  color: #606266;
}

.log-content {
  max-height: 300px;
  overflow-y: auto;
  padding: 12px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: monospace;
  font-size: 13px;
  color: #303133;
}

.log-content.error {
  background: #fff5f5;
  border-color: #f56c6c;
  color: #f56c6c;
}
</style>
