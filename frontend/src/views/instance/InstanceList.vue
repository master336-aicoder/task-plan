<template>
  <div>
    <div class="flex-between mb-4">
      <div class="flex items-center gap-4">
        <h2 class="text-xl font-bold">执行记录</h2>
        <el-select v-model="selectedTaskId" placeholder="选择任务" clearable @change="loadInstances">
          <el-option v-for="task in tasks" :key="task.id" :label="task.name" :value="task.id" />
        </el-select>
        <el-select v-model="selectedStatus" placeholder="执行状态" clearable @change="loadInstances">
          <el-option label="等待" value="PENDING" />
          <el-option label="运行中" value="RUNNING" />
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="取消" value="CANCELLED" />
        </el-select>
      </div>
      <el-button @click="loadInstances">
        <el-icon class="mr-1"><Refresh /></el-icon>
        刷新
      </el-button>
    </div>
    <el-table :data="instances" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="instanceKey" label="实例ID" width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="triggerType" label="触发方式" width="100" />
      <el-table-column prop="startTime" label="开始时间" width="180">
        <template #default="{ row }">
          {{ row.startTime ? formatTime(row.startTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="180">
        <template #default="{ row }">
          {{ row.endTime ? formatTime(row.endTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="耗时" width="100">
        <template #default="{ row }">
          {{ row.duration ? formatDuration(row.duration) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">详情</el-button>
          <el-button
            v-if="row.status === 'RUNNING'"
            size="small"
            type="danger"
            @click="stopInstance(row)"
          >
            停止
          </el-button>
          <el-button
            v-if="row.status === 'FAILED'"
            size="small"
            type="warning"
            @click="retryInstance(row)"
          >
            重试
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="执行详情" width="800px">
      <el-descriptions :column="2" border v-if="currentInstance">
        <el-descriptions-item label="实例ID">{{ currentInstance.instanceKey }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentInstance.status)">
            {{ getStatusText(currentInstance.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="触发方式">{{ currentInstance.triggerType }}</el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ currentInstance.retryCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentInstance.startTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentInstance.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentInstance.duration ? formatDuration(currentInstance.duration) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行结果" :span="2">
          <pre class="whitespace-pre-wrap text-sm">{{ currentInstance.result || '-' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="执行日志" :span="2">
          <el-input
            type="textarea"
            :rows="10"
            readonly
            :model-value="currentInstance.logs || '暂无日志'"
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { taskApi, instanceApi, type Task, type TaskInstance } from '@/api/task'
import dayjs from 'dayjs'
import duration from 'dayjs/plugin/duration'

dayjs.extend(duration)

const tasks = ref<Task[]>([])
const instances = ref<TaskInstance[]>([])
const selectedTaskId = ref<number>()
const selectedStatus = ref<string>()
const loading = ref(false)
const detailVisible = ref(false)
const currentInstance = ref<TaskInstance | null>(null)

const loadTasks = async () => {
  try {
    const res = await taskApi.list()
    tasks.value = res.data || []
  } catch (error) {
    console.error('加载任务列表失败', error)
  }
}

const loadInstances = async () => {
  loading.value = true
  try {
    const res = await instanceApi.list(selectedTaskId.value)
    let data = res.data || []
    if (selectedStatus.value) {
      data = data.filter((i) => i.status === selectedStatus.value)
    }
    instances.value = data
  } catch (error) {
    console.error('加载执行记录失败', error)
  } finally {
    loading.value = false
  }
}

const getStatusTagType = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: 'info',
    RUNNING: 'primary',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELLED: 'warning'
  }
  return map[status || ''] || 'info'
}

const getStatusText = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: '等待',
    RUNNING: '运行中',
    SUCCESS: '成功',
    FAILED: '失败',
    CANCELLED: '取消'
  }
  return map[status || ''] || status || ''
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const formatDuration = (ms: number) => {
  return dayjs.duration(ms).format('HH:mm:ss')
}

const viewDetail = (instance: TaskInstance) => {
  currentInstance.value = instance
  detailVisible.value = true
}

const stopInstance = async (instance: TaskInstance) => {
  try {
    await instanceApi.stop(instance.id!)
    ElMessage.success('已停止')
    loadInstances()
  } catch (error) {
    console.error('停止失败', error)
  }
}

const retryInstance = async (instance: TaskInstance) => {
  try {
    await instanceApi.retry(instance.id!)
    ElMessage.success('已重试')
    loadInstances()
  } catch (error) {
    console.error('重试失败', error)
  }
}

onMounted(() => {
  loadTasks()
})
</script>
