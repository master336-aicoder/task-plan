<template>
  <div class="workflow-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">工作流定义管理</h2>
      <div class="flex gap-4 flex-wrap justify-between">
        <div class="flex gap-4 flex-wrap">
          <el-select v-model="selectedProjectId" placeholder="全部项目" @change="loadWorkflows" style="width: 200px">
            <el-option label="全部项目" :value="0" />
            <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索工作流名称"
            style="width: 300px"
            @keyup.enter="loadWorkflows"
            clearable
          >
            <template #append>
              <el-button @click="loadWorkflows">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="flex gap-4 flex-wrap">
          <el-button v-if="selectedIds.length > 0" type="success" @click="batchRelease" :loading="batchLoading">
            批量发布
          </el-button>
          <el-button v-if="selectedIds.length > 0" type="warning" @click="batchOffline" :loading="batchLoading">
            批量下线
          </el-button>
          <el-popconfirm v-if="selectedIds.length > 0" title="确定要批量删除选中的工作流吗？" @confirm="batchDelete">
            <template #reference>
              <el-button type="danger" :loading="batchLoading">批量删除</el-button>
            </template>
          </el-popconfirm>
          <el-button type="primary" @click="goToDesign()">
            <el-icon class="mr-1"><Plus /></el-icon>
            新建工作流
          </el-button>
        </div>
      </div>
    </div>

    <el-table :data="workflows" border v-loading="loading" stripe @select-all="handleSelectAll" @select="handleSelect">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="工作流名称" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="releaseState" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.releaseState === 'ONLINE' ? 'success' : 'info'">
            {{ row.releaseState === 'ONLINE' ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column prop="owner" label="负责人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="400" fixed="right">
        <template #default="{ row }">
          <el-button 
            size="small" 
            @click="goToDesign(row)"
            :disabled="row.releaseState === 'ONLINE'"
            :title="row.releaseState === 'ONLINE' ? '已发布的工作流不允许设计，请先下线' : ''"
          >
            {{ row.releaseState === 'ONLINE' ? '已发布' : '设计' }}
          </el-button>
          <el-button size="small" @click="goToSchedule(row)">调度</el-button>
          <el-button size="small" type="info" @click="openVersionManagement(row)">版本管理</el-button>
          <el-button v-if="row.releaseState === 'OFFLINE'" size="small" type="success" @click="releaseWorkflow(row)">发布</el-button>
          <el-button v-else size="small" type="warning" @click="offlineWorkflow(row)">下线</el-button>
          <el-popconfirm title="确定要删除这个工作流吗？" @confirm="deleteWorkflow(row)">
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
      @size-change="loadWorkflows"
      @current-change="loadWorkflows"
    />

    <el-dialog v-model="versionDialogVisible" title="版本管理" width="950px">
      <div v-if="currentWorkflow" class="version-management">
        <div class="version-header">
          <h4>工作流：{{ currentWorkflow.name }}</h4>
          <p>当前版本：{{ currentWorkflow.version }}</p>
        </div>
        
        <div class="version-list-section">
          <h4>历史版本</h4>
          <el-table :data="versionHistory" border>
            <el-table-column prop="version" label="版本号" width="80" />
            <el-table-column prop="releaseState" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.releaseState === 'ONLINE' ? 'success' : 'info'">
                  {{ row.releaseState === 'ONLINE' ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="historyTime" label="创建时间" width="160" />
            <el-table-column prop="owner" label="操作人" width="90" />
            <el-table-column label="操作" width="320">
              <template #default="{ row }">
                <el-button size="small" type="info" @click="viewVersionDetail(row)">查看画布</el-button>
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="compareWithCurrent(row)"
                  :disabled="row.version === currentWorkflow.version"
                >
                  {{ row.version === currentWorkflow.version ? '当前版本' : '与当前对比' }}
                </el-button>
                <el-button 
                  size="small" 
                  type="warning" 
                  @click="rollbackToVersion(row)"
                  :disabled="row.version === currentWorkflow.version"
                >
                  {{ row.version === currentWorkflow.version ? '-' : '回滚' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="versionDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="versionCompareVisible" title="版本对比" width="1200px">
      <div v-if="versionDiff" class="version-compare">
        <div class="compare-header">
          <span>对比版本: 历史版本 {{ versionDiff.sourceVersion }} vs 当前版本 {{ versionDiff.targetVersion }}</span>
        </div>
        
        <div class="compare-container">
          <div class="compare-panel">
            <h4>历史版本 {{ versionDiff.sourceVersion }}</h4>
            <div v-if="versionDiff.targetNodes && versionDiff.targetNodes.length > 0" class="node-list">
              <div v-for="node in versionDiff.targetNodes" :key="node.id" class="node-card" :class="{ 'node-removed': node.isRemoved }">
                <div class="node-card-header">
                  <span class="node-name">{{ node.name }}</span>
                  <el-tag size="small">{{ node.taskType }}</el-tag>
                </div>
                <div class="node-card-body">
                  <div v-if="node.script" class="config-row">
                    <span class="config-label">脚本:</span>
                    <span class="config-value">{{ node.script }}</span>
                  </div>
                  <div v-if="node.serverId" class="config-row">
                    <span class="config-label">服务器ID:</span>
                    <span class="config-value">{{ node.serverId }}</span>
                  </div>
                  <div v-if="node.datasourceId" class="config-row">
                    <span class="config-label">数据源ID:</span>
                    <span class="config-value">{{ node.datasourceId }}</span>
                  </div>
                  <div v-if="node.sql" class="config-row">
                    <span class="config-label">SQL:</span>
                    <span class="config-value sql-content">{{ node.sql }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-panel">
              <span>暂无节点</span>
            </div>
            
            <h5 class="mt-3">连线</h5>
            <div v-if="versionDiff.targetConnections && versionDiff.targetConnections.length > 0" class="connection-list">
              <div v-for="conn in versionDiff.targetConnections" :key="conn.id" class="connection-item" :class="{ 'connection-removed': isConnectionRemoved(conn) }">
                <span v-if="isConnectionRemoved(conn)" class="connection-status removed">删除</span>
                <span class="connection-arrow">
                  <span class="node-label">{{ getNodeName(conn.fromNodeId || conn.source) }}</span>
                  <span class="arrow-icon">→</span>
                  <span class="node-label">{{ getNodeName(conn.toNodeId || conn.target) }}</span>
                </span>
              </div>
            </div>
            <div v-else class="empty-panel">
              <span>暂无连线</span>
            </div>
          </div>
          
          <div class="compare-panel">
            <h4>当前版本 {{ versionDiff.targetVersion }}</h4>
            <div v-if="versionDiff.currentNodes && versionDiff.currentNodes.length > 0" class="node-list">
              <div v-for="node in versionDiff.currentNodes" :key="node.id" class="node-card" :class="{ 'node-added': node.isAdded, 'node-modified': node.isModified }">
                <div class="node-card-header">
                  <span class="node-name">{{ node.name }}</span>
                  <el-tag size="small">{{ node.taskType }}</el-tag>
                </div>
                <div class="node-card-body">
                  <div v-if="node.script" class="config-row">
                    <span class="config-label">脚本:</span>
                    <span class="config-value">{{ node.script }}</span>
                  </div>
                  <div v-if="node.serverId" class="config-row">
                    <span class="config-label">服务器ID:</span>
                    <span class="config-value">{{ node.serverId }}</span>
                  </div>
                  <div v-if="node.datasourceId" class="config-row">
                    <span class="config-label">数据源ID:</span>
                    <span class="config-value">{{ node.datasourceId }}</span>
                  </div>
                  <div v-if="node.sql" class="config-row">
                    <span class="config-label">SQL:</span>
                    <span class="config-value sql-content">{{ node.sql }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-panel">
              <span>暂无节点</span>
            </div>
            
            <h5 class="mt-3">连线</h5>
            <div v-if="versionDiff.currentConnections && versionDiff.currentConnections.length > 0" class="connection-list">
              <div v-for="conn in versionDiff.currentConnections" :key="conn.id" class="connection-item" :class="{ 'connection-added': isConnectionAdded(conn) }">
                <span v-if="isConnectionAdded(conn)" class="connection-status added">新增</span>
                <span class="connection-arrow">
                  <span class="node-label">{{ getNodeName2(conn.fromNodeId || conn.source) }}</span>
                  <span class="arrow-icon">→</span>
                  <span class="node-label">{{ getNodeName2(conn.toNodeId || conn.target) }}</span>
                </span>
              </div>
            </div>
            <div v-else class="empty-panel">
              <span>暂无连线</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="versionCompareVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { projectApi, workflowApi, type Project, type ProcessDefinition } from '@/api'

const workflows = ref<ProcessDefinition[]>([])
const projects = ref<Project[]>([])
const loading = ref(false)

const selectedProjectId = ref<number>(0)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref<number[]>([])

const router = useRouter()

const batchLoading = ref(false)

const versionDialogVisible = ref(false)
const versionCompareVisible = ref(false)
const currentWorkflow = ref<ProcessDefinition | null>(null)
const versionHistory = ref<any[]>([])
const versionDiff = ref<any>(null)

const loadProjects = async () => {
  try {
    const res = await projectApi.list()
    projects.value = res.data || res || []
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadWorkflows = async () => {
  loading.value = true
  try {
    const res = await workflowApi.list({
      projectId: selectedProjectId.value && selectedProjectId.value !== 0 ? selectedProjectId.value : undefined,
      name: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    workflows.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载工作流列表失败')
  } finally {
    loading.value = false
  }
}

const goToDesign = (workflow?: ProcessDefinition) => {
  if (workflow) {
    sessionStorage.setItem('currentWorkflow', JSON.stringify(workflow))
    router.push(`/workflow-design/${workflow.id}`)
  } else {
    sessionStorage.setItem('currentProjectId', String(selectedProjectId.value))
    router.push('/workflow-design')
  }
}

const goToSchedule = (workflow: ProcessDefinition) => {
  sessionStorage.setItem('currentWorkflow', JSON.stringify(workflow))
  router.push('/schedules')
}

const releaseWorkflow = async (workflow: ProcessDefinition) => {
  try {
    await workflowApi.release(workflow.id!)
    ElMessage.success('发布成功')
    loadWorkflows()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

const offlineWorkflow = async (workflow: ProcessDefinition) => {
  try {
    await workflowApi.offline(workflow.id!)
    ElMessage.success('已下线')
    loadWorkflows()
  } catch (error) {
    ElMessage.error('下线失败')
  }
}

const deleteWorkflow = async (workflow: ProcessDefinition) => {
  try {
    await workflowApi.delete(workflow.id!)
    ElMessage.success('删除成功')
    loadWorkflows()
  } catch (error) {
    ElMessage.error('删除失败')
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

const batchRelease = async () => {
  batchLoading.value = true
  try {
    for (const id of selectedIds.value) {
      await workflowApi.release(id)
    }
    ElMessage.success(`成功发布 ${selectedIds.value.length} 个工作流`)
    selectedIds.value = []
    loadWorkflows()
  } catch (error) {
    ElMessage.error('批量发布失败')
  } finally {
    batchLoading.value = false
  }
}

const batchOffline = async () => {
  batchLoading.value = true
  try {
    for (const id of selectedIds.value) {
      await workflowApi.offline(id)
    }
    ElMessage.success(`成功下线 ${selectedIds.value.length} 个工作流`)
    selectedIds.value = []
    loadWorkflows()
  } catch (error) {
    ElMessage.error('批量下线失败')
  } finally {
    batchLoading.value = false
  }
}

const batchDelete = async () => {
  batchLoading.value = true
  try {
    for (const id of selectedIds.value) {
      await workflowApi.delete(id)
    }
    ElMessage.success(`成功删除 ${selectedIds.value.length} 个工作流`)
    selectedIds.value = []
    loadWorkflows()
  } catch (error) {
    ElMessage.error('批量删除失败')
  } finally {
    batchLoading.value = false
  }
}

const openVersionManagement = async (workflow: ProcessDefinition) => {
  currentWorkflow.value = workflow
  versionDialogVisible.value = true
  await loadVersionHistory(workflow.id!)
}

const loadVersionHistory = async (workflowId: number) => {
  try {
    const res = await workflowApi.getHistory(workflowId)
    versionHistory.value = res.data || res || []
  } catch (error) {
    ElMessage.error('加载历史版本失败')
  }
}

const viewVersionDetail = (version: any) => {
  if (!currentWorkflow.value) return
  router.push(`/workflow-design/${currentWorkflow.value.id}/version/${version.version}`)
}

const compareWithCurrent = async (version: any) => {
  if (!currentWorkflow.value) return
  
  try {
    const res = await workflowApi.compareWithCurrent(currentWorkflow.value.id!, version.version)
    versionDiff.value = res.data || res
    versionCompareVisible.value = true
  } catch (error) {
    ElMessage.error('版本对比失败')
  }
}

const rollbackToVersion = async (version: any) => {
  if (!currentWorkflow.value) return
  
  if (currentWorkflow.value.releaseState === 'ONLINE') {
    ElMessage.warning('已发布的工作流需要先下线才能回滚')
    return
  }
  
  try {
    await workflowApi.rollback(currentWorkflow.value.id!, version.version)
    ElMessage.success(`成功回滚到版本 ${version.version}`)
    versionDialogVisible.value = false
    loadWorkflows()
  } catch (error) {
    ElMessage.error('回滚失败')
  }
}

const getNodeName = (nodeId: string) => {
  if (!versionDiff.value?.targetNodes) return nodeId
  const node = versionDiff.value.targetNodes.find((n: any) => n.id === nodeId)
  return node ? node.name : nodeId
}

const getNodeName2 = (nodeId: string) => {
  if (!versionDiff.value?.currentNodes) return nodeId
  const node = versionDiff.value.currentNodes.find((n: any) => n.id === nodeId)
  return node ? node.name : nodeId
}

const getConnKey = (conn: any) => {
  const source = conn.fromNodeId || conn.sourceNodeId || conn.source
  const target = conn.toNodeId || conn.targetNodeId || conn.target
  return `${source}->${target}`
}

const isConnectionRemoved = (conn: any) => {
  if (!versionDiff.value?.currentConnections) return false
  const connStr = getConnKey(conn)
  return !versionDiff.value.currentConnections.some(
    (c: any) => getConnKey(c) === connStr
  )
}

const isConnectionAdded = (conn: any) => {
  if (!versionDiff.value?.targetConnections) return false
  const connStr = getConnKey(conn)
  return !versionDiff.value.targetConnections.some(
    (c: any) => getConnKey(c) === connStr
  )
}

onMounted(async () => {
  await loadProjects()
  loadWorkflows()
})
</script>

<style scoped>
.version-compare {
  max-height: 70vh;
  overflow-y: auto;
}

.compare-container {
  display: flex;
  gap: 20px;
  margin-top: 16px;
}

.compare-panel {
  flex: 1;
  min-width: 0;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 16px;
  background: #fafafa;
  max-height: 60vh;
  overflow-y: auto;
}

.compare-panel h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.compare-panel h5 {
  margin: 16px 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.node-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.node-card {
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background: #fff;
  padding: 12px;
}

.node-card.node-added {
  border-color: #67c23a;
  background: #f0f9eb;
}

.node-card.node-removed {
  border-color: #f56c6c;
  background: #fef0f0;
}

.node-card.node-modified {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.node-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.node-name {
  font-weight: bold;
  color: #303133;
}

.node-card-body {
  font-size: 13px;
}

.config-row {
  display: flex;
  margin-bottom: 4px;
}

.config-label {
  color: #909399;
  width: 70px;
  flex-shrink: 0;
}

.config-value {
  color: #606266;
  word-break: break-all;
}

.sql-content {
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 12px;
}

.connection-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.connection-item {
  padding: 6px 10px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.connection-item.connection-added {
  border-color: #67c23a;
  background: #f0f9eb;
}

.connection-item.connection-removed {
  border-color: #f56c6c;
  background: #fef0f0;
  opacity: 0.7;
}

.connection-status {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: bold;
}

.connection-status.added {
  background: #67c23a;
  color: #fff;
}

.connection-status.removed {
  background: #f56c6c;
  color: #fff;
}

.connection-arrow {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.node-label {
  padding: 2px 8px;
  background: #e8e8e8;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.connection-added .node-label {
  background: #c8e6c9;
  color: #237804;
}

.connection-removed .node-label {
  background: #ffcdd2;
  color: #c62828;
  text-decoration: line-through;
}

.arrow-icon {
  color: #409eff;
  font-size: 16px;
  font-weight: bold;
}

.connection-added .arrow-icon {
  color: #67c23a;
}

.connection-removed .arrow-icon {
  color: #f56c6c;
}

.empty-panel {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
