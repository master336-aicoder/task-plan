<template>
  <div class="workflow-design">
    <div class="page-header">
      <div class="flex justify-between items-center">
        <div>
          <h2 class="text-xl font-bold">
            工作流设计
            <el-tag v-if="isReadonly" type="info" size="small">只读 - 版本 {{ viewVersion }}</el-tag>
          </h2>
          <div class="flex gap-4 mt-3">
            <el-select v-model="projectId" placeholder="选择所属项目" style="width: 200px" :disabled="isReadonly">
              <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
            </el-select>
            <el-input v-model="workflowName" placeholder="请输入工作流名称" style="width: 300px" :disabled="isReadonly" />
          </div>
        </div>
        <div class="flex gap-4">
          <template v-if="!isReadonly">
            <el-button type="success" @click="validateAndSave">
              <el-icon class="mr-1"><DocumentChecked /></el-icon>
              保存
            </el-button>
            <el-button type="primary" @click="validateAndRelease">
              <el-icon class="mr-1"><Top /></el-icon>
              发布
            </el-button>
            
          </template>
          <el-button @click="goBack">
            <el-icon class="mr-1"><Back /></el-icon>
            返回列表
          </el-button>
        </div>
      </div>
    </div>

    <div class="design-content">
      <div v-if="!isReadonly" class="task-panel">
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
            <el-button v-if="!isReadonly" size="small" @click="clearCanvas">清空画布</el-button>
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
              :class="['connection-line', { 'clickable': !isReadonly }]"
              @click.stop="!isReadonly && deleteConnection(conn)"
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
            <el-form-item label="脚本来源">
              <el-radio-group v-model="scriptSource" class="block">
                <el-radio :label="'file'">脚本文件</el-radio>
                <el-radio :label="'content'">脚本内容</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="scriptSource === 'file'" label="脚本文件">
              <el-input 
                :value="selectedFileName" 
                placeholder="请选择脚本文件" 
                readonly 
                class="w-full cursor-pointer"
                @click="showFileSelector = true"
              />
              <span class="file-select-hint">点击选择文件</span>
            </el-form-item>
            <el-form-item v-if="scriptSource === 'content'" label="SQL语句">
              <el-input 
                v-model="detailForm.sql" 
                type="textarea" 
                :rows="8" 
                :placeholder="sqlPlaceholder" 
                readonly 
                class="w-full cursor-pointer script-content-clickable"
                @click="openSqlEditor"
              />
              <span class="file-select-hint">点击编辑SQL</span>
            </el-form-item>
          </template>

          <template v-if="showServerField">
            <el-form-item label="执行服务器">
              <el-select v-model="detailForm.serverId" class="w-full">
                <el-option label="请选择服务器" :value="''" />
                <el-option v-for="server in servers" :key="server.id" :label="server.name" :value="server.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="脚本来源">
              <el-radio-group v-model="scriptSource" class="block">
                <el-radio :label="'file'">脚本文件</el-radio>
                <el-radio :label="'content'">脚本内容</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="scriptSource === 'file'" label="脚本文件">
              <el-input 
                :value="selectedFileName" 
                placeholder="请选择脚本文件" 
                readonly 
                class="w-full cursor-pointer"
                @click="showFileSelector = true"
              />
              <span class="file-select-hint">点击选择文件</span>
            </el-form-item>
            <el-form-item v-if="scriptSource === 'content'" label="脚本内容">
              <el-input 
                v-model="detailForm.script" 
                type="textarea" 
                :rows="6" 
                :placeholder="scriptPlaceholder"
                readonly 
                class="w-full cursor-pointer script-content-clickable"
                @click="openScriptEditor"
              />
              <span class="file-select-hint">点击编辑脚本</span>
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
              <el-input v-model="detailForm.mainClass" placeholder="例如：com.example.MainClass" />
            </el-form-item>
            <el-form-item label="程序参数">
              <el-input v-model="detailForm.programArgs" type="textarea" :rows="3" :placeholder="programArgsPlaceholder" />
            </el-form-item>
            <el-form-item label="JAR包">
              <div class="flex gap-2">
                <el-input v-model="selectedJarFileName" readonly placeholder="请选择JAR包" class="flex-1" />
                <el-button type="primary" @click="showJarSelector = true">选择文件</el-button>
              </div>
            </el-form-item>
          </template>

          <template v-if="showHttpField">
            <el-form-item label="请求URL">
              <el-input v-model="detailForm.httpUrl" placeholder="例如：http://api.example.com/data" />
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
              <el-input v-model="detailForm.httpBody" type="textarea" :rows="4" :placeholder="httpBodyPlaceholder" />
            </el-form-item>
          </template>

          <el-form-item label="自定义参数">
            <el-input v-model="detailForm.params" type="textarea" :rows="3" :placeholder="paramsPlaceholder" />
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
            <div class="flex gap-2">
              <el-input v-model="detailForm.owner" readonly placeholder="请选择负责人" class="flex-1" />
              <el-button type="primary" @click="showOwnerSelector = true">选择用户</el-button>
            </div>
          </el-form-item>
          <el-form-item label="环境变量配置">
            <el-select v-model="detailForm.envConfigGroupIds" multiple placeholder="请选择环境变量配置组" @change="loadAvailableEnvVariables">
              <el-option v-for="group in envConfigGroups" :key="group.id" :label="group.name" :value="group.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="availableEnvVariables.length > 0" label="可用环境变量">
            <el-table :data="availableEnvVariables" border size="small" style="width: 100%;">
              <el-table-column prop="name" label="变量名" width="150">
                <template #default="{ row }">
                  <code class="text-primary">{{ row.name }}</code>
                </template>
              </el-table-column>
              <el-table-column prop="value" label="变量值" min-width="200" />
              <el-table-column prop="description" label="描述" min-width="150" />
            </el-table>
          </el-form-item>
          <el-form-item label="变量替换说明">
            <div class="text-sm text-gray-600">
              在脚本或脚本文件中使用 ${变量名} 的方式引用环境变量，任务执行时会自动进行替换。
            </div>
          </el-form-item>
        </el-form>
        <div v-if="!isReadonly" class="detail-footer">
          <el-button type="primary" @click="saveDetail">保存配置</el-button>
        </div>
      </div>
    </div>

    <el-dialog title="选择脚本文件" v-model="showFileSelector" width="700px" top="5vh">
      <div class="file-selector">
        <div class="file-selector-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(item, index) in fileSelectorPath" :key="item.id || 'root'">
              <span class="breadcrumb-link" @click="navigateFileSelectorPath(item, index)">
                {{ item.name || '根目录' }}
              </span>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="file-selector-body">
          <div class="file-tree">
            <el-tree
              :data="fileSelectorTree"
              :props="{ label: 'name', children: 'children' }"
              :default-expand-all="true"
              @node-click="handleFileTreeNodeClick"
            >
              <template #default="{ node, data }">
                <span v-if="data.isDirectory === 1" class="tree-directory">
                  <el-icon class="mr-1"><Folder /></el-icon>
                  {{ node.label }}
                </span>
                <span v-else class="tree-file" :class="{ 'tree-file-selected': selectedFileId === data.id }">
                  <el-icon class="mr-1"><Document /></el-icon>
                  {{ node.label }}
                </span>
              </template>
            </el-tree>
          </div>
          <div class="file-list-panel">
            <div class="file-grid">
              <div
                v-for="file in fileSelectorFiles"
                :key="file.id"
                class="file-item"
                :class="{ selected: selectedFileId === file.id }"
                @click="selectFile(file)"
              >
                <el-icon v-if="file.isDirectory === 1" class="file-icon directory-icon"><Folder /></el-icon>
                <el-icon v-else class="file-icon file-icon-script"><Document /></el-icon>
                <span class="file-name">{{ file.name }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="file-selector-footer">
          <el-button @click="showFileSelector = false">取消</el-button>
          <el-button type="primary" @click="confirmFileSelection" :disabled="!selectedFileId">确认选择</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog title="选择JAR文件" v-model="showJarSelector" width="700px" top="5vh">
      <div class="file-selector">
        <div class="file-selector-header">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(item, index) in jarSelectorPath" :key="item.id || 'root'">
              <span class="breadcrumb-link" @click="navigateJarSelectorPath(item, index)">
                {{ item.name || '根目录' }}
              </span>
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="file-selector-body">
          <div class="file-tree">
            <el-tree
              :data="jarSelectorTree"
              :props="{ label: 'name', children: 'children' }"
              :default-expand-all="true"
              @node-click="handleJarTreeNodeClick"
            >
              <template #default="{ node, data }">
                <span v-if="data.isDirectory === 1" class="tree-directory">
                  <el-icon class="mr-1"><Folder /></el-icon>
                  {{ node.label }}
                </span>
                <span v-else class="tree-file" :class="{ 'tree-file-selected': selectedJarFileId === data.id }">
                  <el-icon class="mr-1"><Document /></el-icon>
                  {{ node.label }}
                </span>
              </template>
            </el-tree>
          </div>
          <div class="file-list-panel">
            <div class="file-grid">
              <div
                v-for="file in jarSelectorFiles"
                :key="file.id"
                class="file-item"
                :class="{ selected: selectedJarFileId === file.id }"
                @click="selectJarFile(file)"
              >
                <el-icon v-if="file.isDirectory === 1" class="file-icon directory-icon"><Folder /></el-icon>
                <el-icon v-else class="file-icon file-icon-jar"><Document /></el-icon>
                <span class="file-name">{{ file.name }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="file-selector-footer">
          <el-button @click="showJarSelector = false">取消</el-button>
          <el-button type="primary" @click="confirmJarSelection" :disabled="!selectedJarFileId">确认选择</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog title="SQL编辑器" v-model="showSqlEditor" width="900px" top="5vh">
      <div style="height: 400px;">
        <SqlEditor v-model="sqlEditorContent" placeholder="请输入SQL语句..." />
      </div>
      <template #footer>
        <el-button @click="showSqlEditor = false">取消</el-button>
        <el-button type="primary" @click="saveSqlEditor">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="currentScriptEditorTitle" v-model="showScriptEditor" width="900px" top="5vh">
      <div style="height: 400px;">
        <PythonEditor v-if="currentTaskType === 'PYTHON'" v-model="scriptEditorContent" placeholder="请输入Python脚本..." />
        <PerlEditor v-else-if="currentTaskType === 'PERL'" v-model="scriptEditorContent" placeholder="请输入Perl脚本..." />
        <ShellEditor v-else v-model="scriptEditorContent" placeholder="请输入Shell脚本..." />
      </div>
      <template #footer>
        <el-button @click="showScriptEditor = false">取消</el-button>
        <el-button type="primary" @click="saveScriptEditor">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog title="选择负责人" v-model="showOwnerSelector" width="500px">
      <div class="owner-selector">
        <el-input
          v-model="ownerSearchKeyword"
          placeholder="搜索用户名或姓名"
          class="mb-4"
          clearable
        />
        <el-table :data="filteredUsers" border>
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="selectOwner(row)">选择</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DocumentChecked, Top, Back, VideoPlay, Folder, Document } from '@element-plus/icons-vue'
import SqlEditor from '@/components/SqlEditor.vue'
import PythonEditor from '@/components/PythonEditor.vue'
import PerlEditor from '@/components/PerlEditor.vue'
import ShellEditor from '@/components/ShellEditor.vue'
import { taskTypes, workflowApi, projectApi, datasourceApi, serverApi, fileApi, clusterConfigApi, taskTypeApi, envVariableApi, envConfigGroupApi, userApi, type TaskDefinition, type Project, type Datasource, type ServerNode, type FileResource, type ClusterConfig, type TaskTypeConfig, type EnvVariable, type EnvConfigGroup, type SysUser } from '@/api'

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
  envConfigGroupIds: number[]
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
const scriptSource = ref<'file' | 'content'>('content')
const isReadonly = ref(false)
const viewVersion = ref<number | null>(null)

const sqlPlaceholder = '请输入SQL语句，支持多行，例如：\nSELECT * FROM users\nWHERE status = 1\nORDER BY create_time DESC\nLIMIT 100'
const scriptPlaceholder = '请输入脚本内容，例如：\n#!/bin/bash\necho "Hello World"\ncd /data\n./process.sh'
const programArgsPlaceholder = '多个参数用空格或换行分隔，例如：\n--input /data/input\n--output /data/output\n--mode batch'
const httpBodyPlaceholder = 'JSON格式请求体，例如：\n{\n  "name": "test",\n  "value": 123\n}'
const paramsPlaceholder = 'JSON格式参数，例如：\n{\n  "key1": "value1",\n  "key2": 123\n}'

const projects = ref<Project[]>([])
const datasources = ref<Datasource[]>([])
const servers = ref<ServerNode[]>([])
const files = ref<FileResource[]>([])
const clusters = ref<ClusterConfig[]>([])
const taskTypesConfig = ref<TaskTypeConfig[]>([])
const envVariables = ref<EnvVariable[]>([])
const envConfigGroups = ref<EnvConfigGroup[]>([])
const availableEnvVariables = ref<EnvVariable[]>([])

const jarFiles = computed(() => files.value.filter(f => f.type === 'JAR'))

const sqlLines = computed(() => {
  return sqlEditorContent.value.split('\n')
})

const currentTaskType = computed(() => {
  return detailForm.taskType || 'SHELL'
})

const currentScriptEditorTitle = computed(() => {
  const titles: Record<string, string> = {
    SHELL: 'Shell脚本编辑器',
    PYTHON: 'Python脚本编辑器',
    PERL: 'Perl脚本编辑器'
  }
  return titles[currentTaskType.value] || '脚本编辑器'
})

const showFileSelector = ref(false)
const selectedFileId = ref<number | null>(null)
const selectedFileName = ref('')
const fileSelectorParentId = ref<number | null>(null)
const fileSelectorPath = ref<any[]>([{ id: null, name: '根目录', parentId: null }])
const fileSelectorFiles = ref<FileResource[]>([])
const fileSelectorTree = ref<any[]>([])

const showJarSelector = ref(false)
const selectedJarFileId = ref<number | null>(null)
const selectedJarFileName = ref('')
const jarSelectorParentId = ref<number | null>(null)
const jarSelectorPath = ref<any[]>([{ id: null, name: '根目录', parentId: null }])
const jarSelectorFiles = ref<FileResource[]>([])
const jarSelectorTree = ref<any[]>([])

const showSqlEditor = ref(false)
const sqlEditorContent = ref('')

const showScriptEditor = ref(false)
const scriptEditorContent = ref('')

const showOwnerSelector = ref(false)
const users = ref<SysUser[]>([])
const selectedOwner = ref('')

const ownerSearchKeyword = ref('')

const loadFileSelectorFiles = async () => {
  try {
    const response = await fileApi.getDirectory(fileSelectorParentId.value)
    const data = response?.data || response || {}
    fileSelectorFiles.value = [...(data.directories || []), ...(data.files || [])]
  } catch (error) {
    console.error('加载文件列表失败', error)
  }
}

const loadFileSelectorTree = async () => {
  try {
    const response = await fileApi.listAll()
    const allFiles = response.data || response || []
    fileSelectorTree.value = buildFileTree(allFiles, null)
  } catch (error) {
    console.error('加载文件树失败', error)
  }
}

const loadJarSelectorFiles = async () => {
  try {
    const response = await fileApi.getDirectory(jarSelectorParentId.value)
    const data = response?.data || response || {}
    jarSelectorFiles.value = [...(data.directories || []), ...(data.files || [])]
  } catch (error) {
    console.error('加载文件列表失败', error)
  }
}

const loadJarSelectorTree = async () => {
  try {
    const response = await fileApi.listAll()
    const allFiles = response.data || response || []
    jarSelectorTree.value = buildFileTree(allFiles, null)
  } catch (error) {
    console.error('加载文件树失败', error)
  }
}

const selectJarFile = (file: FileResource) => {
  if (file.isDirectory === 1) {
    jarSelectorParentId.value = file.id
    loadJarSelectorFiles()
    jarSelectorPath.value.push({
      id: file.id,
      name: file.name,
      parentId: file.parentId
    })
  } else {
    selectedJarFileId.value = file.id
    selectedJarFileName.value = file.name
  }
}

const handleJarTreeNodeClick = (data: any) => {
  if (data.isDirectory === 1) {
    jarSelectorParentId.value = data.id
    loadJarSelectorFiles()
    jarSelectorPath.value = buildPathFromTree(data)
  } else {
    selectedJarFileId.value = data.id
    selectedJarFileName.value = data.name
  }
}

const navigateJarSelectorPath = (item: any, index: number) => {
  jarSelectorParentId.value = item.id
  jarSelectorPath.value = jarSelectorPath.value.slice(0, index + 1)
  loadJarSelectorFiles()
}

const confirmJarSelection = () => {
  if (selectedJarFileId.value) {
    detailForm.jarFileId = selectedJarFileId.value
    showJarSelector.value = false
  }
}

const openSqlEditor = () => {
  sqlEditorContent.value = detailForm.sql || ''
  showSqlEditor.value = true
}

const saveSqlEditor = () => {
  detailForm.sql = sqlEditorContent.value
  showSqlEditor.value = false
}

const openScriptEditor = () => {
  scriptEditorContent.value = detailForm.script || ''
  showScriptEditor.value = true
}

const saveScriptEditor = () => {
  detailForm.script = scriptEditorContent.value
  showScriptEditor.value = false
}

const loadUsers = async () => {
  try {
    const response = await userApi.list()
    users.value = response.data || response || []
  } catch (error) {
    console.error('加载用户列表失败', error)
  }
}

const selectOwner = (user: SysUser) => {
  selectedOwner.value = user.username
  detailForm.owner = user.username
  showOwnerSelector.value = false
}

const filteredUsers = computed(() => {
  if (!ownerSearchKeyword.value) return users.value
  return users.value.filter(u => 
    u.username.toLowerCase().includes(ownerSearchKeyword.value.toLowerCase()) ||
    u.name.toLowerCase().includes(ownerSearchKeyword.value.toLowerCase())
  )
})

const buildFileTree = (files: FileResource[], parentId: number | null): any[] => {
  return files
    .filter(f => f.parentId === parentId)
    .map(f => ({
      id: f.id,
      name: f.name,
      isDirectory: f.isDirectory,
      parentId: f.parentId,
      children: f.isDirectory === 1 ? buildFileTree(files, f.id!) : []
    }))
    .sort((a, b) => {
      if (a.isDirectory !== b.isDirectory) {
        return a.isDirectory === 1 ? -1 : 1
      }
      return a.name.localeCompare(b.name)
    })
}

const selectFile = (file: FileResource) => {
  if (file.isDirectory === 1) {
    fileSelectorParentId.value = file.id
    loadFileSelectorFiles()
    fileSelectorPath.value.push({
      id: file.id,
      name: file.name,
      parentId: file.parentId
    })
  } else {
    selectedFileId.value = file.id
    selectedFileName.value = file.name
  }
}

const handleFileTreeNodeClick = (data: any) => {
  if (data.isDirectory === 1) {
    fileSelectorParentId.value = data.id
    loadFileSelectorFiles()
    fileSelectorPath.value = buildPathFromTree(data)
  } else {
    selectedFileId.value = data.id
    selectedFileName.value = data.name
  }
}

const buildPathFromTree = (data: any): any[] => {
  const path: any[] = []
  let current = data
  while (current) {
    path.unshift({ id: current.id, name: current.name, parentId: current.parentId })
    if (current.parentId !== null) {
      current = findParentNode(fileSelectorTree.value, current.parentId)
    } else {
      current = null
    }
  }
  path.unshift({ id: null, name: '根目录', parentId: null })
  return path
}

const findParentNode = (tree: any[], parentId: number): any => {
  for (const node of tree) {
    if (node.id === parentId) {
      return node
    }
    if (node.children && node.children.length > 0) {
      const found = findParentNode(node.children, parentId)
      if (found) return found
    }
  }
  return null
}

const navigateFileSelectorPath = (item: any, index: number) => {
  fileSelectorParentId.value = item.id
  fileSelectorPath.value = fileSelectorPath.value.slice(0, index + 1)
  loadFileSelectorFiles()
}

const confirmFileSelection = () => {
  if (selectedFileId.value) {
    detailForm.fileId = selectedFileId.value
    showFileSelector.value = false
  }
}

watch(showFileSelector, (val) => {
  if (val) {
    selectedFileId.value = detailForm.fileId || null
    if (selectedFileId.value) {
      const file = files.value.find(f => f.id === selectedFileId.value)
      selectedFileName.value = file?.name || ''
    } else {
      selectedFileName.value = ''
    }
    fileSelectorParentId.value = null
    fileSelectorPath.value = [{ id: null, name: '根目录', parentId: null }]
    loadFileSelectorFiles()
    loadFileSelectorTree()
  }
})

watch(showJarSelector, (val) => {
  if (val) {
    selectedJarFileId.value = detailForm.jarFileId || null
    if (selectedJarFileId.value) {
      const file = files.value.find(f => f.id === selectedJarFileId.value)
      selectedJarFileName.value = file?.name || ''
    } else {
      selectedJarFileName.value = ''
    }
    jarSelectorParentId.value = null
    jarSelectorPath.value = [{ id: null, name: '根目录', parentId: null }]
    loadJarSelectorFiles()
    loadJarSelectorTree()
  }
})

watch(showOwnerSelector, (val) => {
  if (val) {
    loadUsers()
  }
})

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
  envVariableIds: [] as number[],
  envConfigGroupIds: [] as number[]
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
    const [projectRes, dsRes, serverRes, fileRes, clusterRes, ttRes, envRes, envNamesRes] = await Promise.all([
      projectApi.list(),
      datasourceApi.list({ page: 1, size: 100 }),
      serverApi.list({ page: 1, size: 100 }),
      fileApi.list({ page: 1, size: 100 }),
      clusterConfigApi.list({ page: 1, size: 100 }),
      taskTypeApi.list({ page: 1, size: 100 }),
      envVariableApi.list({ page: 1, size: 100 }),
      envConfigGroupApi.list()
    ])
    projects.value = projectRes.data || projectRes || []
    datasources.value = dsRes.data || []
    servers.value = serverRes.data || []
    files.value = fileRes.data || []
    clusters.value = clusterRes.data || []
    taskTypesConfig.value = ttRes.data || []
    envVariables.value = envRes.data || []
    envConfigGroups.value = (envNamesRes as any).data || envNamesRes || []
    
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
    envVariableIds: [],
    envConfigNames: []
  }
  
  nodes.value.push(newNode)
  ElMessage.success('任务已添加到画布')
}

const getFullFilePath = (fileId: number): string => {
  const file = files.value.find(f => f.id === fileId)
  if (!file) return ''
  
  const pathParts: string[] = [file.name || '']
  let parentId = file.parentId
  
  while (parentId !== null && parentId !== undefined) {
    const parent = files.value.find(f => f.id === parentId)
    if (!parent) break
    pathParts.unshift(parent.name || '')
    parentId = parent.parentId
  }
  
  return pathParts.join('/')
}

const openTaskDetail = (node: CanvasNode) => {
  detailNode.value = node
  Object.assign(detailForm, node)
  scriptSource.value = node.fileId ? 'file' : 'content'
  if (node.taskType === 'SQL' && node.script) {
    detailForm.sql = node.script
  }
  if (node.fileId) {
    selectedFileId.value = node.fileId
    selectedFileName.value = getFullFilePath(node.fileId)
  } else {
    selectedFileId.value = null
    selectedFileName.value = ''
  }
  loadAvailableEnvVariables()
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
    scriptSource.value = 'content'
  } else {
    scriptSource.value = detailForm.fileId ? 'file' : 'content'
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

const loadAvailableEnvVariables = async () => {
  const groupIds = detailForm.envConfigGroupIds || []
  if (groupIds.length === 0) {
    availableEnvVariables.value = []
    return
  }
  
  try {
    const variables: EnvVariable[] = []
    for (const groupId of groupIds) {
      const response = await envVariableApi.listByGroupId(groupId)
      const data = response.data || response || []
      variables.push(...data)
    }
    availableEnvVariables.value = variables
  } catch (error) {
    console.error('加载环境变量失败', error)
    availableEnvVariables.value = []
  }
}

const saveDetail = async () => {
  const valid = await detailFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (detailNode.value) {
    Object.assign(detailNode.value, detailForm)
    if (detailForm.taskType === 'SQL') {
      detailNode.value.script = detailForm.sql
    }
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
  nodes.value.forEach(node => {
    if (node.taskType === 'SQL' && node.sql) {
      node.script = node.sql
    }
    if (['SHELL', 'PYTHON', 'PERL'].includes(node.taskType) && node.fileId) {
      const file = files.value.find(f => f.id === node.fileId)
      if (file) {
        node.filePath = file.path
        node.fileName = file.name
      }
    }
  })
  
  const workflowData = {
    id: workflowId.value || undefined,
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

const loadWorkflowVersion = async (id: number, version: number) => {
  try {
    const res = await workflowApi.getHistoryVersion(id, version)
    const data = res.data || res
    
    workflowId.value = data.processDefinitionId
    workflowName.value = data.name
    projectId.value = data.projectId
    viewVersion.value = version
    
    const definition = JSON.parse(data.definitionJson || '{}')
    nodes.value = definition.nodes || []
    
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
    console.error('加载历史版本失败', error)
    ElMessage.error('加载历史版本失败')
  }
}

onMounted(() => {
  loadResources()
  
  const workflowIdParam = route.params.id
  const versionParam = route.params.version
  console.log('Route params:', route.params)
  console.log('workflowIdParam:', workflowIdParam)
  console.log('versionParam:', versionParam)
  
  if (workflowIdParam) {
    const id = parseInt(workflowIdParam as string)
    console.log('Parsed workflow id:', id)
    
    if (versionParam) {
      isReadonly.value = true
      loadWorkflowVersion(id, parseInt(versionParam as string))
    } else {
      loadWorkflow(id)
    }
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

.connection-line:not(.clickable) {
  cursor: default;
}

.connection-line:not(.clickable):hover {
  stroke: #409eff;
  stroke-width: 2;
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

.file-select-hint {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.file-selector {
  height: 450px;
  display: flex;
  flex-direction: column;
}

.file-selector-header {
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e8e8e8;
}

.file-selector-header .breadcrumb-link {
  cursor: pointer;
  color: #409eff;
}

.file-selector-header .breadcrumb-link:hover {
  text-decoration: underline;
}

.file-selector-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.file-tree {
  width: 220px;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
  padding: 8px;
}

.tree-directory {
  display: flex;
  align-items: center;
  color: #eab308;
}

.tree-file {
  display: flex;
  align-items: center;
  color: #3b82f6;
}

.tree-file-selected {
  background: #e8f4fd;
  padding: 2px 4px;
  border-radius: 4px;
}

.file-list-panel {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.file-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.file-item:hover {
  background: #f5f7fa;
  border-color: #e8e8e8;
}

.file-item.selected {
  background: #e8f4fd;
  border-color: #409eff;
}

.file-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.directory-icon {
  color: #eab308;
}

.file-icon-script {
  color: #3b82f6;
}

.file-name {
  font-size: 12px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.file-selector-footer {
  padding: 12px 16px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.file-icon-jar {
  color: #8b5cf6;
}

.sql-editor {
  display: flex;
  flex-direction: column;
  height: 400px;
}

.sql-editor-toolbar {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e8e8e8;
}

.sql-editor-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.line-numbers {
  width: 50px;
  background: #f5f7fa;
  padding: 12px 0;
  text-align: right;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
}

.line-number {
  font-size: 13px;
  line-height: 20px;
  color: #909399;
  padding-right: 8px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.sql-textarea,
.script-textarea {
  flex: 1;
  border: none;
  outline: none;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 20px;
  resize: none;
  background: #fff;
}

.sql-textarea::placeholder,
.script-textarea::placeholder {
  color: #c0c4cc;
}

.script-content-clickable:hover {
  background: #f5f7fa;
  border-color: #409eff;
}

.env-variable-list {
  max-height: 200px;
  overflow-y: auto;
  background: #f9fafb;
  border-radius: 6px;
  padding: 8px;
}

.env-variable-item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 6px 8px;
  margin-bottom: 4px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.env-variable-item:last-child {
  margin-bottom: 0;
}

.var-name {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #667eea;
  font-weight: 500;
  margin-right: 12px;
}

.var-value {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #10b981;
  flex: 1;
}

.var-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  width: 100%;
}

.owner-selector {
  max-height: 400px;
  overflow-y: auto;
}
</style>
