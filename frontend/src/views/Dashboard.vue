<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>工作台</h2>
      <div class="user-info">
        <span>欢迎回来，{{ userInfo.realName || userInfo.username }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
    
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon :size="30"><FolderOpened /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.projectCount }}</div>
              <div class="stat-label">项目数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon :size="30"><Connection /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.workflowCount }}</div>
              <div class="stat-label">工作流数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon :size="30"><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.scheduleCount }}</div>
              <div class="stat-label">调度任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon :size="30"><List /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.executionCount }}</div>
              <div class="stat-label">执行记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-4">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近执行记录</span>
          </template>
          <el-table :data="recentExecutions" border stripe>
            <el-table-column prop="processDefinitionName" label="工作流" />
            <el-table-column prop="state" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStateType(row.state)">{{ getStateLabel(row.state) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/projects/add')">新建项目</el-button>
            <el-button type="success" @click="$router.push('/workflows/add')">新建工作流</el-button>
            <el-button type="warning" @click="$router.push('/schedules/add')">新建调度</el-button>
            <el-button type="info" @click="$router.push('/datasources/add')">添加数据源</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { FolderOpened, Connection, Clock, List } from '@element-plus/icons-vue'
import { authApi, projectApi, workflowApi, scheduleApi, executionApi } from '@/api'

const router = useRouter()
const userInfo = reactive({
  userId: 0,
  username: '',
  realName: '',
  roles: [] as number[]
})

const stats = reactive({
  projectCount: 0,
  workflowCount: 0,
  scheduleCount: 0,
  executionCount: 0
})

const recentExecutions = ref<any[]>([])

const getStateType = (state: string) => {
  const types: Record<string, string> = {
    SUCCESS: 'success',
    FAILURE: 'danger',
    RUNNING_EXECUTION: 'warning',
    PENDING: 'info',
    STOP: 'default',
    TIMEOUT: 'danger'
  }
  return types[state] || 'info'
}

const getStateLabel = (state: string) => {
  const labels: Record<string, string> = {
    SUCCESS: '成功',
    FAILURE: '失败',
    RUNNING_EXECUTION: '运行中',
    PENDING: '等待',
    STOP: '已停止',
    TIMEOUT: '超时'
  }
  return labels[state] || state
}

const loadUserInfo = async () => {
  try {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      Object.assign(userInfo, JSON.parse(stored))
    }
  } catch (error) {
    console.error('Load user info failed:', error)
  }
}

const loadStats = async () => {
  try {
    const projectRes = await projectApi.list()
    const projectData = projectRes.data || projectRes
    stats.projectCount = Array.isArray(projectData) ? projectData.length : (projectRes.total || 0)
    
    const workflowRes = await workflowApi.list({})
    const workflowData = workflowRes.data || workflowRes
    stats.workflowCount = Array.isArray(workflowData) ? workflowData.length : (workflowRes.total || 0)
    
    const scheduleRes = await scheduleApi.list({ projectId: 1 })
    const scheduleData = scheduleRes.data || scheduleRes
    stats.scheduleCount = Array.isArray(scheduleData) ? scheduleData.length : (scheduleRes.total || 0)
    
    const executionRes = await executionApi.list({ projectId: 1 })
    const executionData = executionRes.data || executionRes
    stats.executionCount = Array.isArray(executionData) ? executionData.length : (executionRes.total || 0)
  } catch (error) {
    console.error('Load stats failed:', error)
  }
}

const loadRecentExecutions = async () => {
  try {
    const userInfoStored = localStorage.getItem('userInfo')
    if (userInfoStored) {
      const user = JSON.parse(userInfoStored)
      const res = await executionApi.list({ projectId: user.projectId || 1, page: 1, size: 5 })
      recentExecutions.value = res.data || []
    }
  } catch (error) {
    console.error('Load executions failed:', error)
  }
}

const handleLogout = async () => {
  try {
    await authApi.logout()
  } catch (error) {
    console.error('Logout failed:', error)
  }
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  loadUserInfo()
  loadStats()
  loadRecentExecutions()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.mt-4 {
  margin-top: 20px;
}
</style>
