<template>
  <div>
    <div class="flex-between mb-4">
      <div class="flex items-center gap-4">
        <h2 class="text-xl font-bold">调度管理</h2>
        <el-select v-model="selectedTaskId" placeholder="选择任务" clearable @change="loadSchedules">
          <el-option v-for="task in tasks" :key="task.id" :label="task.name" :value="task.id" />
        </el-select>
      </div>
      <el-button type="primary" @click="showForm()" :disabled="!selectedTaskId">
        <el-icon class="mr-1"><Plus /></el-icon>
        新建调度
      </el-button>
    </div>
    <el-table :data="schedules" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="scheduleType" label="调度类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.scheduleType === 'CRON' ? 'primary' : 'success'">
            {{ row.scheduleType === 'CRON' ? 'Cron表达式' : '固定间隔' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="调度配置" min-width="150">
        <template #default="{ row }">
          {{ row.cronExpression || `${row.fixedInterval} ${getTimeUnitText(row.timeUnit)}` }}
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">
            {{ row.enabled ? '已启用' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="misfirePolicy" label="超时策略" width="120">
        <template #default="{ row }">
          {{ getMisfirePolicyText(row.misfirePolicy) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="warning" @click="toggleSchedule(row)">
            {{ row.enabled ? '暂停' : '启用' }}
          </el-button>
          <el-button size="small" @click="showForm(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteSchedule(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <ScheduleForm
      v-if="formVisible"
      :schedule="editingSchedule"
      :taskId="selectedTaskId"
      @close="formVisible = false"
      @success="loadSchedules"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { taskApi, scheduleApi, type Task, type TaskSchedule } from '@/api/task'
import ScheduleForm from './ScheduleForm.vue'

const route = useRoute()
const tasks = ref<Task[]>([])
const schedules = ref<TaskSchedule[]>([])
const selectedTaskId = ref<number>()
const loading = ref(false)
const formVisible = ref(false)
const editingSchedule = ref<TaskSchedule | null>(null)

const loadTasks = async () => {
  try {
    const res = await taskApi.list()
    tasks.value = res.data || []
    if (route.query.taskId) {
      selectedTaskId.value = Number(route.query.taskId)
    }
  } catch (error) {
    console.error('加载任务列表失败', error)
  }
}

const loadSchedules = async () => {
  if (!selectedTaskId.value) {
    schedules.value = []
    return
  }
  loading.value = true
  try {
    const res = await scheduleApi.list(selectedTaskId.value)
    schedules.value = res.data || []
  } catch (error) {
    console.error('加载调度列表失败', error)
  } finally {
    loading.value = false
  }
}

const getTimeUnitText = (unit?: string) => {
  const map: Record<string, string> = {
    SECONDS: '秒',
    MINUTES: '分钟',
    HOURS: '小时'
  }
  return map[unit || ''] || unit || ''
}

const getMisfirePolicyText = (policy?: number) => {
  const map: Record<number, string> = {
    0: '智能策略',
    1: '立即执行',
    2: '放弃'
  }
  return map[policy || 0] || '智能策略'
}

const toggleSchedule = async (schedule: TaskSchedule) => {
  await scheduleApi.toggle(schedule.id!, schedule.enabled ? 0 : 1)
  ElMessage.success('操作成功')
  loadSchedules()
}

const deleteSchedule = async (schedule: TaskSchedule) => {
  await ElMessageBox.confirm('确定删除该调度吗？', '提示')
  await scheduleApi.delete(schedule.id!)
  ElMessage.success('删除成功')
  loadSchedules()
}

const showForm = (schedule?: TaskSchedule) => {
  editingSchedule.value = schedule || null
  formVisible.value = true
}

onMounted(() => {
  loadTasks()
})
</script>
