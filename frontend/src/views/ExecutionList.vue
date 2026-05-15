<template>
  <div class="execution-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">执行记录</h2>
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
    </div>

    <el-table :data="executions" border v-loading="loading" stripe>
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
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button v-if="row.state === 'RUNNING'" size="small" type="danger" @click="stopExecution(row)">停止</el-button>
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

    <el-dialog v-model="detailVisible" title="执行详情" width="800px">
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
            <el-table-column prop="log" label="日志" min-width="300" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { projectApi, workflowApi, executionApi, type Project, type ProcessDefinition } from '@/api'

const executions = ref<any[]>([])
const projects = ref<Project[]>([])
const workflows = ref<ProcessDefinition[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const detailData = ref<any>(null)

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

const stateLabels: Record<string, string> = {
  RUNNING: '运行中',
  SUCCESS: '成功',
  FAILURE: '失败',
  WAITING: '等待',
  STOPPED: '已停止',
  TIMEOUT: '超时'
}

const stateTagTypes: Record<string, string> = {
  RUNNING: 'warning',
  SUCCESS: 'success',
  FAILURE: 'danger',
  WAITING: 'info',
  STOPPED: 'default',
  TIMEOUT: 'danger'
}

const getStateLabel = (state: string) => stateLabels[state] || state
const getStateTagType = (state: string) => stateTagTypes[state] || 'default'

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
    
    const data = res.data?.data || []
    const workflowMap = new Map()
    workflows.value.forEach(w => workflowMap.set(w.id, w.name))
    
    executions.value = data.map((e: any) => ({
      ...e,
      processDefinitionDefinitionName: workflowMap.get(e.processDefinitionId) || '未知',
      duration: calculateDuration(e.startTime, e.endTime)
    }))
    total.value = res.data?.total || 0
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

const stopExecution = async (execution: any) => {
  try {
    await executionApi.stop(execution.id)
    ElMessage.success('停止成功')
    loadExecutions()
  } catch (error) {
    ElMessage.error('停止失败')
  }
}

onMounted(async () => {
  await loadProjects()
  await loadWorkflows()
  loadExecutions()
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
</style>
