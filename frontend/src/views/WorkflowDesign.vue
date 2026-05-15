<template>
  <div class="workflow-design">
    <div class="page-header">
      <div class="flex justify-between items-center">
        <div>
          <h2 class="text-xl font-bold">工作流设计</h2>
          <div class="flex gap-4 mt-3">
            <el-select v-model="projectId" placeholder="选择所属项目" style="width: 200px">
              <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
            </el-select>
            <el-input v-model="workflowName" placeholder="请输入工作流名称" style="width: 300px" />
          </div>
        </div>
        <div class="flex gap-4">
          <el-button type="success" @click="validateAndSave">
            <el-icon class="mr-1"><DocumentChecked /></el-icon>
            保存
          </el-button>
          <el-button type="primary" @click="validateAndRelease">
            <el-icon class="mr-1"><Top /></el-icon>
            发布
          </el-button>
          <el-button v-if="workflowId" type="warning" @click="executeWorkflow">
            <el-icon class="mr-1"><VideoPlay /></el-icon>
            立即执行
          </el-button>
          <el-button @click="goBack">
            <el-icon class="mr-1"><Back /></el-icon>
            返回列表
          </el-button>
        </div>
      </div>
    </div>

    <div class="design-content">
      <div class="task-panel">
        <h3 class="font-bold mb-3">任务类型</h3>
        <div class="task-types">
          <div
            v-for="type in taskTypes"
            :key="type.value"
            class="task-type-item"
            draggable="true"
            @dragstart="handleDragStart($event, type)"
          >
            <div class="task-icon">{{ getTaskIcon(type.value) }}</div>
            <span>{{ type.label }}</span>
          </div>
        </div>
      </div>

      <div class="canvas-container">
        <div class="canvas-header">
          <span>工作流画布</span>
          <div class="canvas-actions">
            <el-button size="small" @click="clearCanvas">清空画布</el-button>
            <el-button size="small" @click="zoomIn">放大</el-button>
            <el-button size="small" @click="zoomOut">缩小</el-button>
          </div>
        </div>
        
        <div 
          class="canvas" 
          ref="canvasRef" 
          @click="handleCanvasClick"
          @dragover.prevent="handleDragOver"
          @drop="handleDrop"
        >
          <div
            v-for="node in nodes"
            :key="node.id"
            :data-node-id="node.id"
            class="canvas-node"
            :class="{ selected: selectedNodeId === node.id }"
            :style="{ position: 'absolute', left: node.x + 'px', top: node.y + 'px' }"
            @mousedown="startDrag($event, node)"
            @click="handleNodeClick(node)"
            @dblclick="openTaskDetail(node)"
          >
            <div class="node-header">
              <span class="node-type">{{ getTaskIcon(node.taskType) }}</span>
              <span class="node-name">{{ node.name }}</span>
              <button class="node-delete" @click.stop="deleteNode(node)">×</button>
            </div>
            <div class="node-body">
              <span class="node-task-type">{{ getTaskLabel(node.taskType) }}</span>
            </div>
            <div class="node-port node-port-top" @mousedown.stop="startConnect('top', node)">
              <span class="port-dot"></span>
            </div>
            <div class="node-port node-port-bottom" @mousedown.stop="startConnect('bottom', node)">
              <span class="port-dot"></span>
            </div>
            <div class="node-port node-port-left" @mousedown.stop="startConnect('left', node)">
              <span class="port-dot"></span>
            </div>
            <div class="node-port node-port-right" @mousedown.stop="startConnect('right', node)">
              <span class="port-dot"></span>
            </div>
          </div>

          <svg v-if="connections.length > 0" class="connections-layer">
            <line
              v-for="conn in connections"
              :key="conn.id"
              :x1="conn.x1"
              :y1="conn.y1"
              :x2="conn.x2"
              :y2="conn.y2"
              stroke="#409eff"
              stroke-width="2"
              class="connection-line"
              @click.stop="deleteConnection(conn)"
            />
          </svg>

          <svg v-if="connecting" class="temp-connection-layer">
            <line
              :x1="connecting.x1"
              :y1="connecting.y1"
              :x2="connecting.x2"
              :y2="connecting.y2"
              stroke="#409eff"
              stroke-width="2"
              stroke-dasharray="5,5"
            />
          </svg>
        </div>
      </div>

      <div v-if="detailNode" class="detail-panel">
        <div class="detail-header">
          <h3 class="font-bold">任务详情</h3>
          <button class="close-btn" @click="closeDetail">×</button>
        </div>
        <el-form :model="detailForm" label-width="100px" :rules="taskFormRules" ref="detailFormRef">
          <el-form-item label="任务名称" prop="name">
            <el-input v-model="detailForm.name" />
          </el-form-item>
          <el-form-item label="任务类型" prop="taskType">
            <el-select v-model="detailForm.taskType" class="w-full" @change="handleTaskTypeChange">
              <el-option v-for="type in taskTypes" :key="type.value" :label="type.label" :value="type.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="任务描述">
            <el-input v-model="detailForm.description" type="textarea" :rows="2" />
          </el-form-item>
          
          <template v-if="showDatasourceField">
            <el-form-item label="数据源">
              <el-select v-model="detailForm.datasourceId" class="w-full">
                <el-option label="请选择数据源" :value="''" />
                <el-option v-for="ds in datasources" :key="ds.id" :label="ds.name" :value="ds.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="SQL语句">
              <el-input v-model="detailForm.sql" type="textarea" :rows="4" />
            </el-form-item>
          </template>

          <template v-if="showServerField">
            <el-form-item label="执行服务器">
              <el-select v-model="detailForm.serverId" class="w-full">
                <el-option label="请选择服务器" :value="''" />
                <el-option v-for="server in servers" :key="server.id" :label="server.name" :value="server.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="脚本文件">
              <el-select v-model="detailForm.fileId" class="w-full">
                <el-option label="请选择文件" :value="''" />
                <el-option v-for="file in files" :key="file.id" :label="file.name" :value="file.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="脚本内容">
              <el-input v-model="detailForm.script" type="textarea" :rows="4" />
            </el-form-item>
            <el-form-item label="任务类型配置">
              <el-select v-model="detailForm.taskTypeId" class="w-full">
                <el-option label="使用默认" :value="''" />
                <el-option v-for="tt in taskTypesConfig" :key="tt.id" :label="tt.name" :value="tt.id" />
              </el-select>
            </el-form-item>
          </template>

          <template v-if="showClusterField">
            <el-form-item label="集群配置">
              <el-select v-model="detailForm.clusterConfigId" class="w-full">
                <el-option label="请选择集群" :value="''" />
                <el-option v-for="cluster in clusters" :key="cluster.id" :label="cluster.name" :value="cluster.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="主类/程序路径">
              <el-input v-model="detailForm.mainClass" />
            </el-form-item>
            <el-form-item label="程序参数">
              <el-input v-model="detailForm.programArgs" />
            </el-form-item>
            <el-form-item label="JAR包">
              <el-select v-model="detailForm.jarFileId" class="w-full">
                <el-option label="请选择JAR包" :value="''" />
                <el-option v-for="file in jarFiles" :key="file.id" :label="file.name" :value="file.id" />
              </el-select>
            </el-form-item>
          </template>

          <template v-if="showHttpField">
            <el-form-item label="请求URL">
              <el-input v-model="detailForm.httpUrl" />
            </el-form-item>
            <el-form-item label="请求方法">
              <el-select v-model="detailForm.httpMethod" class="w-full">
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
            <el-form-item label="请求体">
              <el-input v-model="detailForm.httpBody" type="textarea" :rows="3" />
            </el-form-item>
          </template>

          <el-form-item label="自定义参数">
            <el-input v-model="detailForm.params" type="textarea" :rows="2" placeholder="JSON格式参数" />
          </el-form-item>
          <el-form-item label="失败重试次数">
            <el-input-number v-model="detailForm.failRetryTimes" :min="0" :max="10" />
          </el-form-item>
          <el-form-item label="重试间隔(秒)">
            <el-input-number v-model="detailForm.failRetryInterval" :min="0" :max="300" />
          </el-form-item>
          <el-form-item label="超时时间(秒)">
            <el-input-number v-model="detailForm.timeout" :min="0" :max="86400" />
          </el-form-item>
          <el-form-item label="CPU核数">
            <el-input-number v-model="detailForm.cpuQuota" :min="1" :max="32" />
          </el-form-item>
          <el-form-item label="内存(MB)">
            <el-input-number v-model="detailForm.memoryMb" :min="128" :max="65536" />
          </el-form-item>
          <el-form-item label="负责人">
            <el-input v-model="detailForm.owner" />
          </el-form-item>
          <el-form-item label="环境变量">
            <el-select v-model="detailForm.envVariableIds" multiple placeholder="请选择环境变量">
              <el-option v-for="env in envVariables" :key="env.id" :label="env.name" :value="env.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="detail-footer">
          <el-button type="primary" @click="saveDetail">保存配置</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DocumentChecked, Top, Back, VideoPlay } from '@element-plus/icons-vue'
import { taskTypes, workflowApi, projectApi, datasourceApi, serverApi, fileApi, clusterConfigApi, taskTypeApi, envVariableApi, type TaskDefinition, type Project, type Datasource, type ServerNode, type FileResource, type ClusterConfig, type TaskTypeConfig, type EnvVariable } from '@/api'

interface CanvasNode {
  id: string
  name: string
  taskType: string
  x: number
  y: number
  description: string
  params: string
  failRetryTimes: number
  failRetryInterval: number
  timeout: number
  cpuQuota: number
  memoryMb: number
  owner: string
  datasourceId: number | null
  sql: string
  serverId: number | null
  fileId: number | null
  script: string
  taskTypeId: number | null
  clusterConfigId: number | null
  mainClass: string
  programArgs: string
  jarFileId: number | null
  httpUrl: string
  httpMethod: string
  httpBody: string
  envVariableIds: number[]
}

interface Connection {
  id: string
  fromNodeId: string
  fromPort: string
  toNodeId: string
  toPort: string
  x1: number
  y1: number
  x2: number
  y2: number
}

interface ConnectingState {
  fromNode: CanvasNode | null
  fromPort: string
  x1: number
  y1: number
  x2: number
  y2: number
}

const canvasRef = ref()
const workflowName = ref('')
const selectedNodeId = ref<string | null>(null)
const detailNode = ref<CanvasNode | null>(null)
const router = useRouter()
const route = useRoute()

const nodes = ref<CanvasNode[]>([])
const connections = ref<Connection[]>([])
const connecting = ref<ConnectingState | null>(null)
const dragging = ref(false)
const dragNode = ref<CanvasNode | null>(null)
const dragOffset = reactive({ x: 0, y: 0 })

const workflowId = ref<number | null>(null)
const projectId = ref<number>(0)

const projects = ref<Project[]>([])
const datasources = ref<Datasource[]>([])
const servers = ref<ServerNode[]>([])
const files = ref<FileResource[]>([])
const clusters = ref<ClusterConfig[]>([])
const taskTypesConfig = ref<TaskTypeConfig[]>([])
const envVariables = ref<EnvVariable[]>([])

const jarFiles = computed(() => files.value.filter(f => f.type === 'JAR'))

const detailForm = reactive({
  name: '',
  taskType: 'SHELL',
  description: '',
  params: '',
  failRetryTimes: 0,
  failRetryInterval: 1,
  timeout: 0,
  cpuQuota: 1,
  memoryMb: 512,
  owner: '',
  datasourceId: null as number | null,
  sql: '',
  serverId: null as number | null,
  fileId: null as number | null,
  script: '',
  taskTypeId: null as number | null,
  clusterConfigId: null as number | null,
  mainClass: '',
  programArgs: '',
  jarFileId: null as number | null,
  httpUrl: '',
  httpMethod: 'GET',
  httpBody: '',
  envVariableIds: [] as number[]
})

const taskFormRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }]
}

const detailFormRef = ref()

const showDatasourceField = computed(() => ['SQL'].includes(detailForm.taskType))
const showServerField = computed(() => ['SHELL', 'PYTHON', 'PERL'].includes(detailForm.taskType))
const showClusterField = computed(() => ['SPARK', 'FLINK', 'MAPREDUCE', 'JAVA'].includes(detailForm.taskType))
const showHttpField = computed(() => ['HTTP'].includes(detailForm.taskType))

const getTaskIcon = (type: string) => {
  const icons: Record<string, string> = {
    SHELL: '🖥️',
    PYTHON: '🐍',
    PERL: '🐪',
    JAVA: '☕',
    HTTP: '🌐',
    SQL: '📊',
    SPARK: '⚡',
    FLINK: '🔥',
    MAPREDUCE: '📦'
  }
  return icons[type] || '📋'
}

const getTaskLabel = (type: string) => {
  const found = taskTypes.find(t => t.value === type)
  return found ? found.label : type
}

const loadResources = async () => {
  try {
    const [projectRes, dsRes, serverRes, fileRes, clusterRes, ttRes, envRes] = await Promise.all([
      projectApi.list(),
      datasourceApi.list({ page: 1, size: 100 }),
      serverApi.list({ page: 1, size: 100 }),
      fileApi.list({ page: 1, size: 100 }),
      clusterConfigApi.list({ page: 1, size: 100 }),
      taskTypeApi.list({ page: 1, size: 100 }),
      envVariableApi.list({ page: 1, size: 100 })
    ])
    projects.value = projectRes.data || projectRes || []
    datasources.value = dsRes.data || []
    servers.value = serverRes.data || []
    files.value = fileRes.data || []
    clusters.value = clusterRes.data || []
    taskTypesConfig.value = ttRes.data || []
    envVariables.value = envRes.data || []
    
    if (projects.value.length > 0 && projectId.value === 0) {
      projectId.value = projects.value[0].id
    }
  } catch (error) {
    console.error('加载资源失败', error)
  }
}

const handleDragStart = (event: DragEvent, type: { value: string; label: string }) => {
  if (event.dataTransfer) {
    event.dataTransfer.setData('taskType', JSON.stringify(type))
  }
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  const canvas = canvasRef.value
  if (!canvas || !event.dataTransfer) return

  const data = event.dataTransfer.getData('taskType')
  if (!data) return

  const type = JSON.parse(data)
  const rect = canvas.getBoundingClientRect()
  
  const x = event.clientX - rect.left - 60
  const y = event.clientY - rect.top - 30

  const newNode: CanvasNode = {
    id: `node-${Date.now()}`,
    name: `${type.label}任务`,
    taskType: type.value,
    x: Math.max(0, x),
    y: Math.max(0, y),
    description: '',
    params: '',
    failRetryTimes: 0,
    failRetryInterval: 1,
    timeout: 0,
    cpuQuota: 1,
    memoryMb: 512,
    owner: '',
    datasourceId: null,
    sql: '',
    serverId: null,
    fileId: null,
    script: '',
    taskTypeId: null,
    clusterConfigId: null,
    mainClass: '',
    programArgs: '',
    jarFileId: null,
    httpUrl: '',
    httpMethod: 'GET',
    httpBody: '',
    envVariableIds: []
  }
  
  nodes.value.push(newNode)
  ElMessage.success('任务已添加到画布')
}

const openTaskDetail = (node: CanvasNode) => {
  detailNode.value = node
  Object.assign(detailForm, node)
}

const closeDetail = () => {
  detailNode.value = null
}

const handleTaskTypeChange = () => {
  if (detailForm.taskType !== 'SQL') {
    detailForm.datasourceId = null
    detailForm.sql = ''
  }
  if (!['SHELL', 'PYTHON', 'PERL'].includes(detailForm.taskType)) {
    detailForm.serverId = null
    detailForm.fileId = null
    detailForm.script = ''
    detailForm.taskTypeId = null
  }
  if (!['SPARK', 'FLINK', 'MAPREDUCE', 'JAVA'].includes(detailForm.taskType)) {
    detailForm.clusterConfigId = null
    detailForm.mainClass = ''
    detailForm.programArgs = ''
    detailForm.jarFileId = null
  }
  if (detailForm.taskType !== 'HTTP') {
    detailForm.httpUrl = ''
    detailForm.httpMethod = 'GET'
    detailForm.httpBody = ''
  }
}

const saveDetail = async () => {
  const valid = await detailFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (detailNode.value) {
    Object.assign(detailNode.value, detailForm)
    ElMessage.success('配置已保存')
  }
}

const selectNode = (node: CanvasNode) => {
  selectedNodeId.value = node.id
}

const handleNodeClick = (node: CanvasNode) => {
  if (!dragging.value) {
    selectNode(node)
  }
}

const deleteNode = (node: CanvasNode) => {
  connections.value = connections.value.filter(
    c => c.fromNodeId !== node.id && c.toNodeId !== node.id
  )
  nodes.value = nodes.value.filter(n => n.id !== node.id)
  if (selectedNodeId.value === node.id) {
    selectedNodeId.value = null
  }
  if (detailNode.value?.id === node.id) {
    closeDetail()
  }
}

const deleteConnection = (conn: Connection) => {
  connections.value = connections.value.filter(c => c.id !== conn.id)
}

const clearCanvas = () => {
  nodes.value = []
  connections.value = []
  selectedNodeId.value = null
  closeDetail()
}

const startDrag = (event: MouseEvent, node: CanvasNode) => {
  event.preventDefault()
  dragging.value = true
  dragNode.value = node
  const canvas = canvasRef.value
  if (canvas) {
    const rect = canvas.getBoundingClientRect()
    dragOffset.x = event.clientX - rect.left - node.x
    dragOffset.y = event.clientY - rect.top - node.y
  } else {
    dragOffset.x = event.clientX - node.x
    dragOffset.y = event.clientY - node.y
  }
  
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
}

const handleDrag = (event: MouseEvent) => {
  if (!dragging.value || !dragNode.value) return
  
  const canvas = canvasRef.value
  if (!canvas) return
  
  const rect = canvas.getBoundingClientRect()
  const newX = event.clientX - rect.left - dragOffset.x
  const newY = event.clientY - rect.top - dragOffset.y
  
  dragNode.value.x = Math.max(0, newX)
  dragNode.value.y = Math.max(0, newY)
  
  updateConnections()
}

const stopDrag = () => {
  dragging.value = false
  dragNode.value = null
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
}

const startConnect = (port: string, node: CanvasNode) => {
  const canvas = canvasRef.value
  if (!canvas) return
  
  const nodeRect = canvas.querySelector(`[data-node-id="${node.id}"]`)?.getBoundingClientRect()
  if (!nodeRect) return
  
  let x1 = node.x + 120 / 2
  let y1 = node.y + 60 / 2
  
  const portOffset = 8
  
  if (port === 'top') y1 = node.y - portOffset
  if (port === 'bottom') y1 = node.y + 60 + portOffset
  if (port === 'left') x1 = node.x - portOffset
  if (port === 'right') x1 = node.x + 120 + portOffset
  
  connecting.value = {
    fromNode: node,
    fromPort: port,
    x1: x1,
    y1: y1,
    x2: event.clientX - (canvas.getBoundingClientRect().left || 0),
    y2: event.clientY - (canvas.getBoundingClientRect().top || 0)
  }
  
  document.addEventListener('mousemove', handleConnect)
  document.addEventListener('mouseup', stopConnect)
}

const handleConnect = (event: MouseEvent) => {
  if (!connecting.value) return
  
  const canvas = canvasRef.value
  if (!canvas) return
  
  const rect = canvas.getBoundingClientRect()
  connecting.value.x2 = event.clientX - rect.left
  connecting.value.y2 = event.clientY - rect.top
}

const stopConnect = () => {
  if (!connecting.value) {
    document.removeEventListener('mousemove', handleConnect)
    document.removeEventListener('mouseup', stopConnect)
    return
  }
  
  const canvas = canvasRef.value
  if (!canvas) {
    connecting.value = null
    document.removeEventListener('mousemove', handleConnect)
    document.removeEventListener('mouseup', stopConnect)
    return
  }
  
  const x = connecting.value.x2
  const y = connecting.value.y2
  
  let targetNode: CanvasNode | null = null
  for (const node of nodes.value) {
    if (x >= node.x && x <= node.x + 120 && y >= node.y && y <= node.y + 60) {
      targetNode = node
      break
    }
  }
  
  if (targetNode && targetNode.id !== connecting.value.fromNode?.id) {
    const existingConn = connections.value.find(
      c => c.fromNodeId === connecting.value.fromNode?.id && c.toNodeId === targetNode.id
    )
    
    if (!existingConn) {
      let toPort = 'top'
      const fromNode = connecting.value.fromNode
      
      if (fromNode.y > targetNode.y) toPort = 'top'
      else if (fromNode.y < targetNode.y) toPort = 'bottom'
      else if (fromNode.x > targetNode.x) toPort = 'left'
      else toPort = 'right'
      
      addConnection(connecting.value.fromNode, connecting.value.fromPort, targetNode, toPort)
      ElMessage.success('连线成功')
    }
  }
  
  connecting.value = null
  document.removeEventListener('mousemove', handleConnect)
  document.removeEventListener('mouseup', stopConnect)
}

const addConnection = (fromNode: CanvasNode, fromPort: string, toNode: CanvasNode, toPort: string) => {
  const getPortPosition = (node: CanvasNode, port: string) => {
    let x = node.x + 120 / 2
    let y = node.y + 60 / 2
    
    const portOffset = 8
    
    if (port === 'top') y = node.y - portOffset
    if (port === 'bottom') y = node.y + 60 + portOffset
    if (port === 'left') x = node.x - portOffset
    if (port === 'right') x = node.x + 120 + portOffset
    
    return { x, y }
  }
  
  const fromPos = getPortPosition(fromNode, fromPort)
  const toPos = getPortPosition(toNode, toPort)
  
  connections.value.push({
    id: `${fromNode.id}-${toNode.id}-${Date.now()}`,
    fromNodeId: fromNode.id,
    fromPort,
    toNodeId: toNode.id,
    toPort,
    x1: fromPos.x,
    y1: fromPos.y,
    x2: toPos.x,
    y2: toPos.y
  })
}

const updateConnections = () => {
  const getPortPosition = (node: CanvasNode, port: string) => {
    let x = node.x + 120 / 2
    let y = node.y + 60 / 2
    
    const portOffset = 8
    
    if (port === 'top') y = node.y - portOffset
    if (port === 'bottom') y = node.y + 60 + portOffset
    if (port === 'left') x = node.x - portOffset
    if (port === 'right') x = node.x + 120 + portOffset
    
    return { x, y }
  }
  
  connections.value.forEach(conn => {
    const fromNode = nodes.value.find(n => n.id === conn.fromNodeId)
    const toNode = nodes.value.find(n => n.id === conn.toNodeId)
    
    if (fromNode && toNode) {
      const fromPos = getPortPosition(fromNode, conn.fromPort)
      const toPos = getPortPosition(toNode, conn.toPort)
      
      conn.x1 = fromPos.x
      conn.y1 = fromPos.y
      conn.x2 = toPos.x
      conn.y2 = toPos.y
    }
  })
}

const handleCanvasClick = () => {
  selectedNodeId.value = null
}

const validateAndSave = async () => {
  if (!workflowName.value.trim()) {
    ElMessage.error('请输入工作流名称')
    return
  }
  
  if (nodes.value.length === 0) {
    ElMessage.error('画布上没有任务，请至少添加一个任务')
    return
  }
  
  await saveWorkflow(false)
}

const validateAndRelease = async () => {
  if (!workflowName.value.trim()) {
    ElMessage.error('请输入工作流名称')
    return
  }
  
  if (nodes.value.length === 0) {
    ElMessage.error('画布上没有任务，请至少添加一个任务')
    return
  }
  
  await saveWorkflow(true)
}

const saveWorkflow = async (release: boolean) => {
  const workflowData = {
    name: workflowName.value,
    projectId: projectId.value,
    releaseState: release ? 'ONLINE' : 'OFFLINE',
    taskNodes: nodes.value,
    connections: connections.value.map(c => ({
      fromNodeId: c.fromNodeId,
      fromPort: c.fromPort,
      toNodeId: c.toNodeId,
      toPort: c.toPort
    }))
  }
  
  try {
    const res = await workflowApi.saveDesign(workflowData)
    workflowId.value = res.id || workflowId.value
    
    ElMessage.success(release ? '发布成功' : '保存成功')
  } catch (error) {
    ElMessage.error(release ? '发布失败' : '保存失败')
  }
}

const goBack = () => {
  router.push('/workflows')
}

const executeWorkflow = async () => {
  try {
    await workflowApi.execute(workflowId.value!)
    ElMessage.success('工作流已启动')
    router.push('/executions')
  } catch (error) {
    ElMessage.error('启动失败')
  }
}

const zoomIn = () => {
  const canvas = canvasRef.value
  if (canvas) {
    const currentScale = parseFloat(canvas.style.transform?.replace('scale(', '').replace(')', '') || '1')
    canvas.style.transform = `scale(${Math.min(currentScale + 0.1, 2)})`
  }
}

const zoomOut = () => {
  const canvas = canvasRef.value
  if (canvas) {
    const currentScale = parseFloat(canvas.style.transform?.replace('scale(', '').replace(')', '') || '1')
    canvas.style.transform = `scale(${Math.max(currentScale - 0.1, 0.5)})`
  }
}

const loadWorkflow = async (id: number) => {
  try {
    console.log('Loading workflow with id:', id)
    const res = await workflowApi.get(id)
    console.log('Workflow API response:', res)
    
    const data = res
    
    workflowId.value = data.id
    workflowName.value = data.name
    projectId.value = data.projectId
    
    console.log('definitionJson:', data.definitionJson)
    const definition = JSON.parse(data.definitionJson || '{}')
    console.log('Parsed definition:', definition)
    nodes.value = definition.nodes || []
    console.log('Loaded nodes:', nodes.value)
    
    const loadedConnections = definition.connections || []
    connections.value = loadedConnections.map((conn: any) => ({
      ...conn,
      x1: 0,
      y1: 0,
      x2: 0,
      y2: 0
    }))
    
    setTimeout(() => {
      updateConnections()
    }, 100)
  } catch (error) {
    console.error('加载工作流失败', error)
  }
}

onMounted(() => {
  loadResources()
  
  const workflowIdParam = route.params.id
  console.log('Route params:', route.params)
  console.log('workflowIdParam:', workflowIdParam)
  
  if (workflowIdParam) {
    const id = parseInt(workflowIdParam as string)
    console.log('Parsed workflow id:', id)
    loadWorkflow(id)
  } else {
    const projectIdStr = sessionStorage.getItem('currentProjectId')
    if (projectIdStr) {
      projectId.value = parseInt(projectIdStr)
    }
  }
})
</script>

<style scoped>
.workflow-design {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

.page-header {
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.design-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.task-panel {
  width: 180px;
  background: #f5f5f5;
  padding: 16px;
  overflow-y: auto;
}

.task-types {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-type-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  background: #fff;
  border-radius: 6px;
  cursor: grab;
  transition: all 0.2s;
  border: 1px solid #e8e8e8;
}

.task-type-item:hover {
  background: #e8f4fd;
  border-color: #409eff;
}

.task-type-item:active {
  cursor: grabbing;
}

.task-icon {
  font-size: 20px;
  margin-right: 8px;
}

.canvas-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-left: 1px solid #e8e8e8;
}

.canvas-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
}

.canvas-actions {
  display: flex;
  gap: 8px;
}

.canvas {
  flex: 1;
  position: relative;
  overflow: auto;
  background: #fafafa;
  background-image: 
    linear-gradient(#e8e8e8 1px, transparent 1px),
    linear-gradient(90deg, #e8e8e8 1px, transparent 1px);
  background-size: 20px 20px;
}

.canvas-node {
  position: absolute;
  width: 120px;
  background: #fff;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: move;
  transition: all 0.3s;
  z-index: 10;
}

.canvas-node:hover {
  border-color: #409eff;
}

.canvas-node.selected {
  border-color: #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.node-header {
  display: flex;
  align-items: center;
  padding: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 6px 6px 0 0;
}

.node-type {
  margin-right: 4px;
}

.node-name {
  flex: 1;
  font-size: 12px;
  font-weight: bold;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-delete {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: #fff;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.node-delete:hover {
  background: rgba(255, 255, 255, 0.4);
  transform: scale(1.1);
}

.node-body {
  padding: 8px;
}

.node-task-type {
  font-size: 11px;
  color: #666;
}

.node-port {
  position: absolute;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 20;
  transition: all 0.2s ease;
}

.node-port:hover {
  transform: scale(1.3);
}

.node-port-top {
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
}

.node-port-bottom {
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
}

.node-port-left {
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
}

.node-port-right {
  right: -10px;
  top: 50%;
  transform: translateY(-50%);
}

.port-dot {
  width: 12px;
  height: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 
    0 0 0 2px rgba(102, 126, 234, 0.4),
    0 2px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease;
}

.port-dot:hover {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  transform: scale(1.5);
  box-shadow: 
    0 0 0 3px rgba(245, 87, 108, 0.5),
    0 4px 14px rgba(0, 0, 0, 0.3);
}

.canvas-node.selected .port-dot {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  box-shadow: 
    0 0 0 2px rgba(17, 153, 142, 0.5),
    0 2px 10px rgba(0, 0, 0, 0.2);
}

.connections-layer, .temp-connection-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: stroke;
}

.connection-line {
  cursor: pointer;
  transition: all 0.2s;
}

.connection-line:hover {
  stroke: #67c23a;
  stroke-width: 3;
}

.detail-panel {
  width: 350px;
  background: #fff;
  border-left: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  padding: 0 8px;
}

.close-btn:hover {
  color: #666;
}

.detail-panel > .el-form {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.detail-footer {
  padding: 12px 16px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
}
</style>
