<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>工作台</h2>
      <div class="user-info">
        <span>欢迎回来，{{ userInfo.realName || userInfo.username }}</span>
        <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon bg-blue">
              <el-icon :size="28"><FolderOpened /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ stats.projectCount }}</div>
              <div class="stat-label">项目数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon bg-green">
              <el-icon :size="28"><Connection /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ stats.workflowCount }}</div>
              <div class="stat-label">工作流数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon bg-yellow">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ stats.scheduleCount }}</div>
              <div class="stat-label">调度任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon bg-red">
              <el-icon :size="28"><List /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ stats.executionCount }}</div>
              <div class="stat-label">执行记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon bg-gray">
              <el-icon :size="28"><Coin /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ stats.datasourceCount }}</div>
              <div class="stat-label">数据源数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon bg-secondary">
              <el-icon :size="28"><Cpu /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ stats.serverCount }}</div>
              <div class="stat-label">服务器数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon bg-emerald">
              <el-icon :size="28"><Check /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ stats.successCount }}</div>
              <div class="stat-label">成功执行</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon bg-destructive">
              <el-icon :size="28"><Tools /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ stats.failedCount }}</div>
              <div class="stat-label">失败执行</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :span="7">
        <el-card class="info-card">
          <template #header>
            <span class="card-title">快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="small" @click="$router.push('/projects/add')">新建项目</el-button>
            <el-button type="success" size="small" @click="$router.push('/workflows/add')">新建工作流</el-button>
            <el-button type="warning" size="small" @click="$router.push('/schedules/add')">新建调度</el-button>
            <el-button type="info" size="small" @click="$router.push('/datasources/add')">添加数据源</el-button>
          </div>
        </el-card>

        <el-card class="info-card mt-4">
          <template #header>
            <span class="card-title">最近执行记录</span>
          </template>
          <el-table :data="recentExecutions" size="small" border :fit="true" style="width: 100%;">
            <el-table-column prop="processName" label="工作流" />
            <el-table-column prop="state" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getStateType(row.state)" size="small">{{ getStateLabel(row.state) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="时间" width="120" />
          </el-table>
          <div v-if="recentExecutions.length === 0" class="empty-state">
            暂无执行记录
          </div>
        </el-card>

        <el-card class="info-card mt-4">
          <template #header>
            <span class="card-title">数据源状态</span>
          </template>
          <div v-if="datasourceStatus.length > 0" class="datasource-grid">
            <div v-for="ds in datasourceStatus" :key="ds.datasourceName" class="datasource-card">
              <div class="datasource-header">
                <span class="datasource-name">{{ ds.datasourceName }}</span>
                <el-tag :type="ds.probeStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">
                  {{ ds.probeStatus === 'SUCCESS' ? '正常' : '异常' }}
                </el-tag>
              </div>
              <div class="datasource-info">
                <span class="datasource-host">{{ ds.host }}:{{ ds.port }}</span>
                <span class="datasource-type">{{ ds.dbType || '-' }}</span>
              </div>
              <div class="datasource-details">
                <div class="detail-item">
                  <span class="detail-label">版本</span>
                  <span class="detail-value">{{ ds.dbVersion || '-' }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">耗时</span>
                  <span class="detail-value">{{ ds.responseTime }}ms</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            暂无数据源
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <span class="card-title">JVM 内存</span>
          </template>
          <div class="jvm-memory">
            <div class="memory-item">
              <div class="memory-header">
                <span>堆内存</span>
                <span class="memory-value">{{ jvmInfo.heapMemory?.usedFormatted || '-' }}</span>
              </div>
              <div class="memory-bar-container">
                <div 
                  class="memory-bar" 
                  :style="{ width: (parseFloat(jvmInfo.heapMemory?.usedPercent || '0')) + '%', background: getMemoryColor(jvmInfo.heapMemory?.usedPercent) }"
                ></div>
              </div>
              <div class="memory-percent">{{ jvmInfo.heapMemory?.usedPercent || '0' }}%</div>
            </div>
            <div class="memory-item">
              <div class="memory-header">
                <span>非堆内存</span>
                <span class="memory-value">{{ jvmInfo.nonHeapMemory?.usedFormatted || '-' }}</span>
              </div>
              <div class="memory-bar-container">
                <div 
                  class="memory-bar" 
                  :style="{ width: getNonHeapPercent() + '%', background: getMemoryColor(getNonHeapPercent()) }"
                ></div>
              </div>
              <div class="memory-percent">{{ getNonHeapPercent() }}%</div>
            </div>
            <div class="memory-item">
              <div class="memory-header">
                <span>运行时</span>
                <span class="memory-value">{{ jvmInfo.memory?.usedMemoryFormatted || '-' }}</span>
              </div>
              <div class="memory-bar-container">
                <div 
                  class="memory-bar" 
                  :style="{ width: (parseFloat(jvmInfo.memory?.usedMemoryPercent || '0')) + '%', background: getMemoryColor(jvmInfo.memory?.usedMemoryPercent) }"
                ></div>
              </div>
              <div class="memory-percent">{{ jvmInfo.memory?.usedMemoryPercent || '0' }}%</div>
            </div>
          </div>
        </el-card>

        <el-card class="info-card mt-4">
          <template #header>
            <span class="card-title">系统资源</span>
          </template>
          <div class="system-resources">
            <div class="resource-grid">
              <div class="resource-item">
                <span class="resource-label">CPU核心</span>
                <span class="resource-value">{{ jvmInfo.os?.availableProcessors || '-' }}核</span>
              </div>
              <div class="resource-item">
                <span class="resource-label">系统负载</span>
                <span class="resource-value">{{ jvmInfo.os?.systemLoadAverage || '-' }}</span>
              </div>
              <div class="resource-item">
                <span class="resource-label">进程CPU</span>
                <span class="resource-value">{{ jvmInfo.os?.processCpuLoad || '-' }}%</span>
              </div>
              <div class="resource-item">
                <span class="resource-label">系统CPU</span>
                <span class="resource-value">{{ jvmInfo.os?.systemCpuLoad || '-' }}</span>
              </div>
              <div class="resource-item">
                <span class="resource-label">线程数</span>
                <span class="resource-value">{{ jvmInfo.threads?.threadCount || '-' }}</span>
              </div>
              <div class="resource-item">
                <span class="resource-label">守护线程</span>
                <span class="resource-value">{{ jvmInfo.threads?.daemonThreadCount || '-' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <span class="card-title">JVM 信息</span>
          </template>
          <div class="jvm-info">
            <div class="info-row">
              <span class="info-label">Java 版本</span>
              <span class="info-value">{{ jvmInfo.javaVersion || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">JVM 名称</span>
              <span class="info-value">{{ jvmInfo.javaVmName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">JVM 版本</span>
              <span class="info-value">{{ jvmInfo.javaVmVersion || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">JVM 厂商</span>
              <span class="info-value">{{ jvmInfo.javaVmVendor || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Java Home</span>
              <span class="info-value truncate">{{ jvmInfo.javaHome || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">操作系统</span>
              <span class="info-value">{{ jvmInfo.osName || '-' }} {{ jvmInfo.osArch || '' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">OS 版本</span>
              <span class="info-value">{{ jvmInfo.osVersion || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">工作目录</span>
              <span class="info-value truncate">{{ jvmInfo.userDir || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ jvmInfo.userName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">文件编码</span>
              <span class="info-value">{{ jvmInfo.fileEncoding || '-' }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="info-card mt-4">
          <template #header>
            <span class="card-title">环境变量</span>
            <el-button size="mini" @click="showAllEnv = !showAllEnv">
              {{ showAllEnv ? '收起' : '展开' }}
            </el-button>
          </template>
          <div class="env-variables">
            <div 
              v-for="(value, key) in displayedEnvVars" 
              :key="key" 
              class="env-item"
            >
              <span class="env-key">{{ key }}</span>
              <span class="env-value truncate">{{ value }}</span>
            </div>
            <div v-if="envVariables && Object.keys(envVariables).length === 0" class="empty-state">
              暂无环境变量信息
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="server-row mt-4">
      <el-col :span="24">
        <el-card class="info-card">
          <template #header>
            <span class="card-title">服务器状态</span>
          </template>
          <div v-if="serverStatus.length > 0" class="server-grid">
            <div v-for="server in serverStatus" :key="server.serverName" class="server-card">
              <div class="server-header">
                <span class="server-name">{{ server.serverName }}</span>
                <el-tag :type="server.probeStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">
                  {{ server.probeStatus === 'SUCCESS' ? '在线' : '离线' }}
                </el-tag>
              </div>
              <div class="server-info">
                <span class="server-ip">{{ server.hostIp }}</span>
                <span class="server-time">{{ server.probeTime }}</span>
              </div>
              <div class="server-metrics">
                <div class="metric">
                  <span class="metric-label">CPU</span>
                  <div class="metric-bar-wrap">
                    <div 
                      class="metric-bar" 
                      :style="{ width: (server.cpuUsage || 0) + '%', background: getMetricColor(server.cpuUsage) }"
                    ></div>
                  </div>
                  <span class="metric-value">{{ (server.cpuUsage || 0).toFixed(1) }}%</span>
                </div>
                <div class="metric">
                  <span class="metric-label">内存</span>
                  <div class="metric-bar-wrap">
                    <div 
                      class="metric-bar" 
                      :style="{ width: (server.memoryUsage || 0) + '%', background: getMetricColor(server.memoryUsage) }"
                    ></div>
                  </div>
                  <span class="metric-value">{{ (server.memoryUsage || 0).toFixed(1) }}%</span>
                </div>
                <div class="metric">
                  <span class="metric-label">磁盘</span>
                  <div class="metric-bar-wrap">
                    <div 
                      class="metric-bar" 
                      :style="{ width: (server.diskUsage || 0) + '%', background: getMetricColor(server.diskUsage) }"
                    ></div>
                  </div>
                  <span class="metric-value">{{ (server.diskUsage || 0).toFixed(1) }}%</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            暂无服务器信息
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { FolderOpened, Connection, Clock, List, Tools, Check, Coin, Cpu } from '@element-plus/icons-vue'
import { authApi, projectApi, workflowApi, scheduleApi, executionApi, datasourceApi, serverApi, probeApi, systemApi } from '@/api'

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
  executionCount: 0,
  datasourceCount: 0,
  serverCount: 0,
  successCount: 0,
  failedCount: 0
})

const jvmInfo = reactive<any>({})
const envVariables = ref<Record<string, string>>({})
const showAllEnv = ref(false)
const recentExecutions = ref<any[]>([])
const serverStatus = ref<any[]>([])
const datasourceStatus = ref<any[]>([])
const loadingServer = ref(false)
const loadingDatasource = ref(false)

const displayedEnvVars = computed(() => {
  const allVars = envVariables.value
  const keys = Object.keys(allVars)
  if (showAllEnv.value) {
    return allVars
  }
  const limited: Record<string, string> = {}
  keys.slice(0, 10).forEach(key => {
    limited[key] = allVars[key]
  })
  return limited
})

const getStateType = (state: string) => {
  const types: Record<string, string> = {
    SUCCESS: 'success',
    FAILURE: 'danger',
    RUNNING_EXECUTION: 'warning',
    PENDING: 'info',
    STOP: 'default',
    TIMEOUT: 'danger',
    PARTIAL_SUCCESS: 'warning'
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
    TIMEOUT: '超时',
    PARTIAL_SUCCESS: '部分成功'
  }
  return labels[state] || state
}

const getMetricColor = (value: number | undefined) => {
  if (value === undefined) return '#ccc'
  if (value < 60) return '#67c23a'
  if (value < 80) return '#e6a23c'
  return '#f56c6c'
}

const getMemoryColor = (value: string | undefined) => {
  if (!value) return '#ccc'
  const numValue = parseFloat(value)
  if (numValue < 60) return '#67c23a'
  if (numValue < 80) return '#e6a23c'
  return '#f56c6c'
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch {
    return timeStr
  }
}

const getNonHeapPercent = () => {
  const used = parseFloat(jvmInfo.nonHeapMemory?.used || '0')
  const max = parseFloat(jvmInfo.nonHeapMemory?.max || '1')
  if (max <= 0) return '0'
  return ((used / max) * 100).toFixed(2)
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
    stats.projectCount = Array.isArray(projectData) ? projectData.length : ((projectRes as any).total || 0)
    
    const workflowRes = await workflowApi.list({})
    const workflowData = workflowRes.data || []
    stats.workflowCount = Array.isArray(workflowData) ? workflowData.length : ((workflowRes as any).total || 0)
    
    const scheduleRes = await scheduleApi.list({ projectId: 1 })
    const scheduleData = scheduleRes.data || []
    stats.scheduleCount = Array.isArray(scheduleData) ? scheduleData.length : ((scheduleRes as any).total || 0)
    
    const executionRes = await executionApi.list({ projectId: 1 })
    const executionData = executionRes.data || []
    stats.executionCount = Array.isArray(executionData) ? executionData.length : ((executionRes as any).total || 0)
    
    const datasourceRes = await datasourceApi.list({})
    const datasourceData = datasourceRes.data || []
    stats.datasourceCount = Array.isArray(datasourceData) ? datasourceData.length : ((datasourceRes as any).total || 0)
    
    const serverRes = await serverApi.list({})
    const serverData = serverRes.data || []
    stats.serverCount = Array.isArray(serverData) ? serverData.length : ((serverRes as any).total || 0)
    
    const successRes = await executionApi.list({ projectId: 1, state: 'SUCCESS' })
    const successData = successRes.data || []
    stats.successCount = Array.isArray(successData) ? successData.length : ((successRes as any).total || 0)
    
    const failedRes = await executionApi.list({ projectId: 1, state: 'FAILURE' })
    const failedData = failedRes.data || []
    stats.failedCount = Array.isArray(failedData) ? failedData.length : ((failedRes as any).total || 0)
  } catch (error) {
    console.error('Load stats failed:', error)
  }
}

const loadRecentExecutions = async () => {
  try {
    const userInfoStored = localStorage.getItem('userInfo')
    if (userInfoStored) {
      const user = JSON.parse(userInfoStored)
      const res = await executionApi.list({ projectId: user.projectId || 1, page: 1, size: 8 })
      recentExecutions.value = res.data || []
    }
  } catch (error) {
    console.error('Load executions failed:', error)
  }
}

const loadServerStatus = async () => {
  loadingServer.value = true
  try {
    const res = await probeApi.getServerStatus()
    serverStatus.value = res || []
  } catch (error) {
    console.error('Load server status failed:', error)
  } finally {
    loadingServer.value = false
  }
}

const loadDatasourceStatus = async () => {
  loadingDatasource.value = true
  try {
    const res = await probeApi.getDatasourceStatus()
    datasourceStatus.value = res || []
  } catch (error) {
    console.error('Load datasource status failed:', error)
  } finally {
    loadingDatasource.value = false
  }
}

const loadJvmInfo = async () => {
  try {
    const res = await systemApi.getJvmInfo()
    Object.assign(jvmInfo, res)
  } catch (error) {
    console.error('Load JVM info failed:', error)
  }
}

const loadEnvVariables = async () => {
  try {
    const res = await systemApi.getEnvVariables()
    envVariables.value = res || {}
  } catch (error) {
    console.error('Load env variables failed:', error)
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
  loadServerStatus()
  loadDatasourceStatus()
  loadJvmInfo()
  loadEnvVariables()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stats-row {
  margin-bottom: 24px;
  width: 100%;
  overflow-x: auto;
}

.stat-card {
  height: 100%;
  border-radius: 12px;
}

.stat-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.bg-blue { background: #409eff; }
.stat-icon.bg-green { background: #67c23a; }
.stat-icon.bg-yellow { background: #e6a23c; }
.stat-icon.bg-red { background: #f56c6c; }
.stat-icon.bg-gray { background: #909399; }
.stat-icon.bg-secondary { background: #6c757d; }
.stat-icon.bg-emerald { background: #10b981; }
.stat-icon.bg-destructive { background: #ef4444; }

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.content-row {
  margin-bottom: 24px;
  width: 100%;
  overflow-x: hidden;
}

.info-card {
  border-radius: 12px;
}

.card-title {
  font-weight: 600;
  font-size: 14px;
}

.jvm-memory {
  padding: 8px 0;
}

.memory-item {
  margin-bottom: 16px;
}

.memory-item:last-child {
  margin-bottom: 0;
}

.memory-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.memory-value {
  font-size: 12px;
  color: #666;
  font-family: monospace;
}

.memory-bar-container {
  height: 12px;
  background: #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

.memory-bar {
  height: 100%;
  border-radius: 6px;
  transition: width 0.3s ease;
}

.memory-percent {
  text-align: right;
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.system-resources {
  padding: 8px 0;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.resource-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 8px;
  background: #f8f9fa;
  border-radius: 6px;
}

.resource-label {
  font-size: 12px;
  color: #666;
}

.resource-value {
  font-size: 12px;
  color: #333;
  font-weight: 500;
}

.jvm-info {
  padding: 8px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 13px;
  color: #666;
  min-width: 100px;
}

.info-value {
  font-size: 13px;
  color: #333;
  text-align: right;
  flex: 1;
}

.info-value.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.env-variables {
  padding: 8px 0;
  max-height: 200px;
  overflow-y: auto;
}

.env-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid #f5f5f5;
}

.env-key {
  font-size: 12px;
  color: #409eff;
  font-weight: 500;
  min-width: 120px;
}

.env-value {
  font-size: 12px;
  color: #666;
  flex: 1;
  text-align: right;
}

.env-value.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-state {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 13px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 0;
}

.server-row {
  margin-bottom: 20px;
  width: 100%;
  overflow-x: hidden;
}

.server-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 12px;
  width: 100%;
}

.server-card {
  background: #fafafa;
  border-radius: 10px;
  padding: 16px;
}

.server-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.server-name {
  font-weight: 600;
  font-size: 14px;
}

.server-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 12px;
  color: #666;
}

.server-metrics {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 8px;
}

.metric-label {
  font-size: 12px;
  color: #666;
  width: 36px;
}

.metric-bar-wrap {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.metric-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.metric-value {
  font-size: 12px;
  color: #333;
  font-weight: 500;
  width: 50px;
  text-align: right;
}

.datasource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  width: 100%;
}

.datasource-card {
  background: #fafafa;
  border-radius: 10px;
  padding: 14px;
}

.datasource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.datasource-name {
  font-weight: 600;
  font-size: 14px;
}

.datasource-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 12px;
  color: #666;
}

.datasource-host {
  font-family: monospace;
}

.datasource-type {
  background: #e8f4fd;
  color: #409eff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.datasource-details {
  display: flex;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.detail-label {
  font-size: 11px;
  color: #999;
}

.detail-value {
  font-size: 12px;
  color: #333;
  font-weight: 500;
}

.mt-4 {
  margin-top: 20px;
}
</style>