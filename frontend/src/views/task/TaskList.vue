<template>
  <div>
    <div class="flex-between mb-4">
      <h2 class="text-xl font-bold">任务管理</h2>
      <el-button type="primary" @click="showForm()">
        <el-icon class="mr-1"><Plus /></el-icon>
        新建任务
      </el-button>
    </div>
    <el-table :data="tasks" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="任务名称" min-width="150" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.type)">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="command" label="命令" min-width="200" show-overflow-tooltip />
      <el-table-column prop="tags" label="标签" width="150">
        <template #default="{ row }">
          <el-tag v-if="row.tags" size="small" class="mr-1" v-for="tag in row.tags.split(',')" :key="tag">
            {{ tag }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showForm(row)">编辑</el-button>
          <el-button size="small" type="success" @click="manageSchedule(row)">调度</el-button>
          <el-button size="small" type="danger" @click="deleteTask(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <TaskForm
      v-if="formVisible"
      :task="editingTask"
      @close="formVisible = false"
      @success="loadTasks"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskApi, type Task } from '@/api/task'
import TaskForm from './TaskForm.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const tasks = ref<Task[]>([])
const loading = ref(false)
const formVisible = ref(false)
const editingTask = ref<Task | null>(null)

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await taskApi.list()
    tasks.value = res.data || []
  } catch (error) {
    console.error('加载任务失败', error)
  } finally {
    loading.value = false
  }
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    SHELL: 'info',
    PYTHON: 'success',
    HTTP: 'warning'
  }
  return map[type] || 'info'
}

const showForm = (task?: Task) => {
  editingTask.value = task || null
  formVisible.value = true
}

const deleteTask = async (task: Task) => {
  await ElMessageBox.confirm('确定删除该任务吗？相关调度也将被删除', '提示')
  await taskApi.delete(task.id!)
  ElMessage.success('删除成功')
  loadTasks()
}

const manageSchedule = (task: Task) => {
  router.push(`/schedules?taskId=${task.id}`)
}

onMounted(() => loadTasks())
</script>
