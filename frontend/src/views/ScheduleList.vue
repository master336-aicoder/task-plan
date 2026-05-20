<template>
  <div class="schedule-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">调度管理</h2>
      <div class="flex gap-4 flex-wrap">
        <el-select v-model="selectedProjectId" placeholder="选择项目" @change="loadWorkflows" style="width: 200px">
          <el-option :label="'全部项目'" :value="undefined" />
          <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
        </el-select>
        <el-select v-model="selectedWorkflowId" placeholder="选择工作流" @change="loadSchedules" style="width: 250px">
          <el-option v-for="workflow in workflows" :key="workflow.id" :label="workflow.name" :value="workflow.id" />
        </el-select>
        <el-button type="primary" @click="showForm()">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建调度
        </el-button>
      </div>
    </div>

    <el-table :data="schedules" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="processDefinitionName" label="工作流名称" min-width="150" />
      <el-table-column prop="cronExpression" label="Cron表达式" min-width="150" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="isValid" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isValid === 1 ? 'success' : 'info'">
            {{ row.isValid === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="owner" label="负责人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showForm(row)">编辑</el-button>
          <el-button v-if="row.isValid === 1" size="small" type="warning" @click="disableSchedule(row)">禁用</el-button>
          <el-button v-else size="small" type="success" @click="enableSchedule(row)">启用</el-button>
          <el-popconfirm title="确定要删除这个调度吗？" @confirm="deleteSchedule(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
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
      @size-change="loadSchedules"
      @current-change="loadSchedules"
    />

    <el-dialog v-model="formVisible" :title="editingSchedule ? '编辑调度' : '新建调度'" width="700px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="工作流" prop="processDefinitionId">
          <el-select v-model="form.processDefinitionId" placeholder="请选择工作流" class="w-full">
            <el-option v-for="workflow in availableWorkflows" :key="workflow.id" :label="workflow.name" :value="workflow.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cronExpression">
          <CronEditor v-model="form.cronExpression" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" />
        </el-form-item>
        <el-form-item label="时区">
          <el-select v-model="form.timezone" class="w-full">
            <el-option label="亚洲/上海" value="Asia/Shanghai" />
            <el-option label="UTC" value="UTC" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.owner" placeholder="请输入负责人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import CronEditor from '@/components/CronEditor.vue'
import { projectApi, workflowApi, scheduleApi, type Project, type ProcessDefinition, type ProcessSchedule } from '@/api'

const schedules = ref<any[]>([])
const projects = ref<Project[]>([])
const workflows = ref<ProcessDefinition[]>([])
const loading = ref(false)
const submitting = ref(false)
const formVisible = ref(false)
const editingSchedule = ref<ProcessSchedule | null>(null)
const formRef = ref()

const selectedProjectId = ref<number | undefined>(undefined)
const selectedWorkflowId = ref<number | undefined>(undefined)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const availableWorkflows = computed(() => {
  return workflows.value
})

const form = ref<ProcessSchedule>({
  projectId: 0,
  processDefinitionId: 0,
  cronExpression: '',
  startTime: '',
  endTime: '',
  timezone: 'Asia/Shanghai',
  isValid: 1,
  owner: ''
})

const rules = {
  processDefinitionId: [{ required: true, message: '请选择工作流', trigger: 'change' }],
  cronExpression: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }]
}

const loadProjects = async () => {
  try {
    const res = await projectApi.list()
    projects.value = res.data || res || []
    if (projects.value.length > 0) {
      selectedProjectId.value = projects.value[0].id
    }
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadWorkflows = async () => {
  try {
    if (selectedProjectId.value) {
      const data = await workflowApi.listByProject(selectedProjectId.value)
      workflows.value = data || []
    } else {
      const resultData = await workflowApi.list({ page: 1, size: 100 })
      workflows.value = resultData.data || []
    }
    selectedWorkflowId.value = undefined
    loadSchedules()
  } catch (error) {
    console.error('加载工作流列表失败', error)
  }
}

const loadSchedules = async () => {
  loading.value = true
  try {
    const resultData = await scheduleApi.list({
      projectId: selectedProjectId.value || undefined,
      processDefinitionId: selectedWorkflowId.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    const data = resultData.data || []
    
    const workflowMap = new Map()
    workflows.value.forEach(w => workflowMap.set(w.id, w.name))
    
    schedules.value = data.map((s: ProcessSchedule) => ({
      ...s,
      processDefinitionName: workflowMap.get(s.processDefinitionId) || '未知'
    }))
    total.value = resultData.total || 0
  } catch (error) {
    ElMessage.error('加载调度列表失败')
  } finally {
    loading.value = false
  }
}

const showForm = (schedule?: ProcessSchedule) => {
  editingSchedule.value = schedule || null
  if (schedule) {
    form.value = { ...schedule }
  } else {
    form.value = {
      projectId: selectedProjectId.value || 1,
      processDefinitionId: 0,
      cronExpression: '0 0 * * * ?',
      startTime: '',
      endTime: '',
      timezone: 'Asia/Shanghai',
      isValid: 1,
      owner: ''
    }
  }
  formVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const submitData = { ...form.value }
    
    if (submitData.startTime) {
      submitData.startTime = formatDateTime(submitData.startTime)
    }
    if (submitData.endTime) {
      submitData.endTime = formatDateTime(submitData.endTime)
    }

    if (editingSchedule.value?.id) {
      await scheduleApi.update(editingSchedule.value.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await scheduleApi.create(submitData)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadSchedules()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const formatDateTime = (date: any): string | null => {
  if (!date) return null
  if (date instanceof Date) {
    return date.toISOString().replace('T', ' ').substring(0, 19)
  } else if (typeof date === 'string') {
    if (date.includes('T')) {
      return date.replace('T', ' ').substring(0, 19)
    }
    return date
  }
  return date
}

const enableSchedule = async (schedule: any) => {
  try {
    await scheduleApi.enable(schedule.id)
    ElMessage.success('启用成功')
    loadSchedules()
  } catch (error) {
    ElMessage.error('启用失败')
  }
}

const disableSchedule = async (schedule: any) => {
  try {
    await scheduleApi.disable(schedule.id)
    ElMessage.success('禁用成功')
    loadSchedules()
  } catch (error) {
    ElMessage.error('禁用失败')
  }
}

const deleteSchedule = async (schedule: any) => {
  try {
    await scheduleApi.delete(schedule.id)
    ElMessage.success('删除成功')
    loadSchedules()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  await loadProjects()
  await loadWorkflows()
  loadSchedules()
})
</script>
