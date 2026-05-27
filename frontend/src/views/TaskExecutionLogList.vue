<template>
  <div class="task-execution-log-list">
    <div class="page-header">
      <h2>作业日志</h2>
    </div>

    <div class="filter-section">
      <div class="flex gap-4 flex-wrap justify-between">
        <el-form :inline="true" :model="filters" class="filter-form">
          <el-form-item label="工作流实例ID">
            <el-input v-model="filters.processInstanceId" placeholder="请输入实例ID" class="w-48" />
          </el-form-item>
          <el-form-item label="任务类型">
            <el-select v-model="filters.taskType" placeholder="请选择任务类型" class="w-48">
              <el-option label="全部" value="" />
              <el-option label="Shell脚本" value="SHELL" />
              <el-option label="Python脚本" value="PYTHON" />
            </el-select>
          </el-form-item>
          <el-form-item label="执行状态">
            <el-select v-model="filters.status" placeholder="请选择状态" class="w-48">
              <el-option label="全部" value="" />
              <el-option label="成功" value="SUCCESS" />
              <el-option label="失败" value="FAILED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadLogs">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex gap-4">
          <el-button type="success" @click="exportAllLogs">全部导出</el-button>
          <el-popconfirm title="确定要删除全部日志吗？" @confirm="deleteAllLogs">
            <template #reference>
              <el-button type="danger">全部删除</el-button>
            </template>
          </el-popconfirm>
          <el-button v-if="selectedIds.length > 0" type="success" @click="exportLogs">导出选中</el-button>
          <el-popconfirm v-if="selectedIds.length > 0" title="确定要批量删除选中的日志吗？" @confirm="batchDelete">
            <template #reference>
              <el-button type="danger">删除选中</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <el-table :data="logs" border v-loading="loading" stripe style="width: 100%;" @select-all="handleSelectAll" @select="handleSelect">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="processInstanceId" label="工作流实例ID" width="120" />
      <el-table-column prop="taskNodeId" label="任务节点ID" width="160" />
      <el-table-column prop="taskName" label="任务名称" min-width="80" />
      <el-table-column prop="taskType" label="任务类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getTaskTypeTagType(row.taskType)">
            {{ getTaskTypeLabel(row.taskType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="serverIp" label="执行服务器" width="120" />
      <el-table-column prop="status" label="执行状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="执行时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showLogDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog v-model="detailVisible" title="日志详情" width="800px">
      <el-form :model="currentLog" label-width="120px">
        <el-form-item label="任务名称">
          {{ currentLog.taskName }}
        </el-form-item>
        <el-form-item label="任务类型">
          {{ currentLog.taskType }}
        </el-form-item>
        <el-form-item label="执行服务器">
          {{ currentLog.serverIp }}
        </el-form-item>
        <el-form-item label="执行状态">
          <el-tag :type="currentLog.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentLog.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="执行时间">
          {{ currentLog.createTime }}
        </el-form-item>
        <el-form-item label="输出内容">
          <pre class="log-output">{{ currentLog.output || '无' }}</pre>
        </el-form-item>
        <el-form-item label="错误信息">
          <pre class="log-output error">{{ currentLog.error || '无' }}</pre>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { taskExecutionLogApi, type TaskExecutionLog } from '@/api'

const logs = ref<TaskExecutionLog[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const currentLog = ref<TaskExecutionLog>({})
const selectedIds = ref<number[]>([])

const filters = reactive({
  processInstanceId: '',
  taskType: '',
  status: '',
  serverIp: ''
})

const taskTypeLabels: Record<string, string> = {
  SHELL: 'Shell',
  PYTHON: 'Python',
  SQL: 'SQL',
  SPARK: 'Spark',
  FLINK: 'Flink',
  HTTP: 'HTTP',
  JAVA: 'Java'
}

const taskTypeTagTypes: Record<string, string> = {
  SHELL: 'primary',
  PYTHON: 'success',
  SQL: 'warning',
  SPARK: 'info',
  FLINK: 'info',
  HTTP: 'default',
  JAVA: 'danger'
}

const getTaskTypeLabel = (taskType: string) => taskTypeLabels[taskType] || taskType
const getTaskTypeTagType = (taskType: string) => taskTypeTagTypes[taskType] || 'default'

const loadLogs = async () => {
  loading.value = true
  try {
    const resultData = await taskExecutionLogApi.list({
      processInstanceId: filters.processInstanceId ? Number(filters.processInstanceId) : undefined,
      taskType: filters.taskType || undefined,
      status: filters.status || undefined,
      serverIp: filters.serverIp || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    const data = resultData.data || []
    logs.value = data
    total.value = resultData.total || 0
  } catch (error) {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.processInstanceId = ''
  filters.taskType = ''
  filters.status = ''
  filters.serverIp = ''
  selectedIds.value = []
  loadLogs()
}

const handleSelectAll = (val: any[]) => {
  if (val.length === 0) {
    selectedIds.value = []
  } else {
    selectedIds.value = val.map(item => item.id)
  }
}

const handleSelect = (val: any[], row: any) => {
  if (val.length === 0) {
    selectedIds.value = selectedIds.value.filter(id => id !== row.id)
  } else {
    selectedIds.value.push(row.id)
  }
}

const exportLogs = async () => {
  try {
    const res = await taskExecutionLogApi.export({ ids: selectedIds.value })
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `task-execution-logs-${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const batchDelete = async () => {
  try {
    await taskExecutionLogApi.batchDelete(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    loadLogs()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const exportAllLogs = async () => {
  try {
    const params = {
      processInstanceId: filters.processInstanceId ? Number(filters.processInstanceId) : undefined,
      taskType: filters.taskType || undefined,
      status: filters.status || undefined,
      serverIp: filters.serverIp || undefined
    }
    const res = await taskExecutionLogApi.exportAll(params)
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `task-execution-logs-all-${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const deleteAllLogs = async () => {
  try {
    const params = {
      processInstanceId: filters.processInstanceId ? Number(filters.processInstanceId) : undefined,
      taskType: filters.taskType || undefined,
      status: filters.status || undefined,
      serverIp: filters.serverIp || undefined
    }
    await taskExecutionLogApi.deleteAll(params)
    ElMessage.success('全部删除成功')
    selectedIds.value = []
    loadLogs()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadLogs()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadLogs()
}

const showLogDetail = (log: TaskExecutionLog) => {
  currentLog.value = log
  detailVisible.value = true
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.task-execution-log-list {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.log-output {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}

.log-output.error {
  background: #fff5f5;
  color: #f56c6c;
}
</style>