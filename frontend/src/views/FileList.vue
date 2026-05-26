<template>
  <div class="file-list">
    <div class="page-header">
      <h2>文件管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDirDialog = true">
          <el-icon><Plus /></el-icon>新建目录
        </el-button>
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="{ parentId: currentParentId }"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
        >
          <el-button type="success">
            <el-icon><Upload /></el-icon>上传文件
          </el-button>
        </el-upload>
        <el-button @click="loadFiles">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="(item, index) in breadcrumbPath" :key="item.id || 'root'">
          <span class="breadcrumb-link" @click="navigateToPath(item, index)">
            {{ item.name || '根目录' }}
          </span>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-table :data="files" border v-loading="loading" stripe fit>
      <el-table-column label="图标" width="80">
        <template #default="{ row }">
          <el-icon v-if="row.isDirectory === 1" class="text-yellow-500" style="font-size: 20px;"><Folder /></el-icon>
          <el-icon v-else class="text-blue-500" style="font-size: 20px;"><Document /></el-icon>
        </template>
      </el-table-column>
      <el-table-column 
        prop="name" 
        label="名称" 
        min-width="120"
        show-overflow-tooltip
      >
        <template #default="{ row }">
          <span class="cursor-pointer hover:text-blue-500 truncate-text" @click="handleFileClick(row)">
            {{ row.name }}
            <el-icon v-if="row.isDirectory === 1" class="ml-1"><ArrowRight /></el-icon>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.type)">
            {{ getTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="size" label="大小" width="100">
        <template #default="{ row }">{{ formatSize(row.size) }}</template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="70" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="修改时间" width="180" />
      <el-table-column label="权限" width="100">
        <template #default="{ row }">
          <span class="text-gray-600">{{ row.isDirectory === 1 ? 'drwxr-xr-x' : '-rw-r--r--' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <template v-if="row.isDirectory === 1">
            <el-button size="small" type="primary" link @click="enterDirectory(row)">
              <el-icon><Promotion /></el-icon>
            </el-button>
          </template>
          <template v-else>
            <el-button size="small" type="success" link @click="previewFile(row)">
              <el-icon><View /></el-icon>
            </el-button>
            <el-button size="small" type="primary" link @click="editFile(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
          </template>
          <el-popconfirm title="确定要删除吗？" @confirm="deleteFile(row)">
            <template #reference>
              <el-button size="small" type="danger" link>
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showCreateDirDialog" title="新建目录" width="400px">
      <el-form label-width="80px">
        <el-form-item label="目录名">
          <el-input v-model="dirForm.name" placeholder="请输入目录名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDirDialog = false">取消</el-button>
        <el-button type="primary" @click="createDirectory">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="'预览 - ' + previewFileInfo.name" v-model="showPreviewDialog" width="900px" top="5vh">
      <div class="file-preview" style="display: flex; flex-direction: column; height: 450px;">
        <div class="preview-content-wrapper" style="flex: 1; overflow: hidden; border: 1px solid #e0e0e0; border-radius: 4px;">
          <codemirror
            v-model="previewContent"
            :style="{ height: '100%' }"
            :extensions="previewExtensions"
            :disabled="true"
          />
        </div>
        <div class="preview-footer" style="margin-top: 12px; padding: 10px 16px; background: #f5f5f5; border-radius: 8px;">
          <div class="file-info-row">
            <span class="info-label">文件名:</span>
            <span class="info-value">{{ previewFileInfo.name }}</span>
            <span class="info-label ml-4">类型:</span>
            <span class="info-value">{{ getTypeLabel(previewFileInfo.type) }}</span>
            <span class="info-label ml-4">大小:</span>
            <span class="info-value">{{ formatSize(previewFileInfo.size) }}</span>
          </div>
          <div class="file-info-row mt-2">
            <span class="info-label">创建时间:</span>
            <span class="info-value">{{ previewFileInfo.createTime || '-' }}</span>
            <span class="info-label ml-4">修改时间:</span>
            <span class="info-value">{{ previewFileInfo.updateTime || '-' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPreviewDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="'编辑 - ' + (editFileInfo?.name || '未知文件')" v-model="showEditDialog" width="900px" top="5vh">
      <div class="file-edit" style="display: flex; flex-direction: column; height: 450px;">
        <div style="flex: 1; overflow: hidden; border: 1px solid #e0e0e0; border-radius: 4px;">
          <codemirror
            v-model="editContent"
            :style="{ height: '100%' }"
            :extensions="editExtensions"
            placeholder="请输入文件内容"
          />
        </div>
        <div class="edit-footer" style="margin-top: 12px; padding: 10px 16px; background: #f5f5f5; border-radius: 8px;">
          <div class="file-info-row">
            <span class="info-label">文件名:</span>
            <span class="info-value">{{ editFileInfo?.name || '未知' }}</span>
            <span class="info-label ml-4">大小:</span>
            <span class="info-value">{{ editContent?.length || 0 }} 字符</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveFileEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Plus, Folder, Document, Refresh, ArrowRight, View, Edit, Delete, Promotion } from '@element-plus/icons-vue'
import { Codemirror } from 'vue-codemirror'
import { javascript } from '@codemirror/lang-javascript'
import { python } from '@codemirror/lang-python'
import { sql } from '@codemirror/lang-sql'
import { html } from '@codemirror/lang-html'
import { java } from '@codemirror/lang-java'
import { markdown } from '@codemirror/lang-markdown'
import { oneDark } from '@codemirror/theme-one-dark'
import { EditorView } from '@codemirror/view'
import request, { suppressErrors } from '@/utils/request'
import { fileApi, type FileResource } from '@/api'

interface FileResourceExt extends FileResource {
  isDirectory?: number
  parentId?: number | null
}

const files = ref<FileResourceExt[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const uploadFileList = ref<any[]>([])
const uploadRef = ref()
const showCreateDirDialog = ref(false)
const currentParentId = ref<number | null>(null)
const directoryTree = ref<any[]>([])
const breadcrumbPath = ref<any[]>([])

const showPreviewDialog = ref(false)
const showEditDialog = ref(false)
const previewFileInfo = ref<FileResourceExt>({} as FileResourceExt)
const editFileInfo = ref<FileResourceExt>({} as FileResourceExt)
const previewContent = ref('')
const editContent = ref('')

const dirForm = ref({
  name: ''
})

const treeProps = {
  children: 'children',
  label: 'name'
}

const uploadUrl = '/api/files/upload'
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

const getLanguageExtension = (filename: string) => {
  if (!filename) return []
  const ext = filename.split('.').pop()?.toLowerCase()
  const extensions: any[] = [oneDark]
  
  switch (ext) {
    case 'js':
    case 'ts':
    case 'jsx':
    case 'tsx':
    case 'json':
      extensions.push(javascript())
      break
    case 'py':
      extensions.push(python())
      break
    case 'sql':
      extensions.push(sql())
      break
    case 'html':
    case 'htm':
      extensions.push(html())
      break
    case 'java':
      extensions.push(java())
      break
    case 'md':
    case 'markdown':
      extensions.push(markdown())
      break
    case 'sh':
    case 'bash':
      extensions.push(bash())
      break
    default:
      break
  }
  
  return extensions
}

const bash = () => {
  return EditorView.lineWrapping
}

const previewExtensions = computed(() => {
  return getLanguageExtension(previewFileInfo.value.name || '')
})

const editExtensions = computed(() => {
  return getLanguageExtension(editFileInfo.value?.name || '')
})

const loadFiles = async () => {
  loading.value = true
  try {
    const response = await request.get('/files/directory', {
      params: { parentId: currentParentId.value }
    })
    const data = response?.data || response || {}
    files.value = [...(data.directories || []), ...(data.files || [])]
  } catch (error) {
    ElMessage.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

const loadDirectoryTree = async () => {
  try {
    const response = await request.get('/files/directories')
    directoryTree.value = response?.data || response || []
  } catch (error) {
    console.error('加载目录树失败', error)
  }
}

const beforeUpload = (file: File) => {
  return true
}

const handleUploadSuccess = (response: any) => {
  if (response.code === 200000) {
    ElMessage.success('上传成功')
    loadFiles()
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const handleUploadError = (error: any) => {
  ElMessage.error('上传失败: ' + (error.message || '未知错误'))
}

const createDirectory = async () => {
  if (!dirForm.value.name.trim()) {
    ElMessage.warning('请输入目录名')
    return
  }
  try {
    await request.post('/files/directory', {
      name: dirForm.value.name,
      parentId: currentParentId.value
    })
    ElMessage.success('创建成功')
    showCreateDirDialog.value = false
    dirForm.value.name = ''
    loadFiles()
  } catch (error: any) {
    ElMessage.error(error?.message || '创建失败')
  }
}

const enterDirectory = (row: FileResourceExt) => {
  currentParentId.value = row.id!
  loadFiles()
  updateBreadcrumb(row)
}

const updateBreadcrumb = (dir: FileResourceExt) => {
  const existingIndex = breadcrumbPath.value.findIndex(p => p.id === dir.id)
  if (existingIndex >= 0) {
    breadcrumbPath.value = breadcrumbPath.value.slice(0, existingIndex + 1)
  } else {
    breadcrumbPath.value.push({
      id: dir.id,
      name: dir.name,
      parentId: dir.parentId
    })
  }
}

const navigateToPath = (item: any, index: number) => {
  if (index === 0 && !item.id) {
    currentParentId.value = null
    breadcrumbPath.value = [{ id: null, name: '根目录', parentId: null }]
  } else {
    currentParentId.value = item.id
    breadcrumbPath.value = breadcrumbPath.value.slice(0, index + 1)
  }
  loadFiles()
}

const handleFileClick = (row: FileResourceExt) => {
  if (row.isDirectory === 1) {
    enterDirectory(row)
  }
}

const deleteFile = async (file: FileResourceExt) => {
  try {
    await request.delete(`/files/${file.id}`)
    ElMessage.success('删除成功')
    loadFiles()
  } catch (error: any) {
    ElMessage.error(error?.message || '删除失败')
  }
}

const previewFile = async (file: FileResourceExt) => {
    previewFileInfo.value = file
    showPreviewDialog.value = true
    previewContent.value = ''
    
    try {
      const response = await request.get(`/files/${file.id}/content`)
      if (response !== null && response !== undefined) {
        previewContent.value = String(response)
      } else {
        previewContent.value = ''
      }
    } catch (error) {
      ElMessage.error('读取文件内容失败')
      previewContent.value = '无法读取文件内容'
    }
  }

  const editFile = async (file: FileResourceExt) => {
    editFileInfo.value = file
    showEditDialog.value = true
    editContent.value = ''
    
    try {
      const response = await request.get(`/files/${file.id}/content`)
      if (response !== null && response !== undefined) {
        editContent.value = String(response)
      } else {
        editContent.value = ''
      }
    } catch (error: any) {
      ElMessage.error('读取文件内容失败: ' + (error?.message || '未知错误'))
      editContent.value = ''
    }
  }

const saveFileEdit = async () => {
  try {
    await request.put(`/files/${editFileInfo.value.id}/content`, {
      content: editContent.value
    })
    ElMessage.success('保存成功')
    showEditDialog.value = false
    loadFiles()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const formatSize = (bytes: number) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const getTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    'SCRIPT': '脚本',
    'DATA': '数据',
    'JAR': 'JAR包',
    'DIRECTORY': '目录',
    'OTHER': '其他'
  }
  return typeMap[type] || type || '未知'
}

const getTypeTagType = (type: string) => {
  const tagTypeMap: Record<string, string> = {
    'SCRIPT': 'success',
    'DATA': 'primary',
    'JAR': 'warning',
    'DIRECTORY': 'info',
    'OTHER': ''
  }
  return tagTypeMap[type] || ''
}

onMounted(() => {
  loadFiles()
  loadDirectoryTree()
  breadcrumbPath.value = [{ id: null, name: '根目录', parentId: null }]
})
</script>

<style scoped>
.file-list {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.breadcrumb-container {
  margin-bottom: 16px;
  padding: 10px 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.breadcrumb-link {
  cursor: pointer;
}

.breadcrumb-link:hover {
  color: #409eff;
}

.text-yellow-500 {
  color: #eab308;
}

.text-blue-500 {
  color: #3b82f6;
}

.text-gray-600 {
  color: #4b5563;
}

.cursor-pointer {
  cursor: pointer;
}

.hover\:text-blue-500:hover {
  color: #3b82f6;
}

.ml-1 {
  margin-left: 4px;
}

.ml-4 {
  margin-left: 16px;
}

.mt-2 {
  margin-top: 8px;
}

.file-info-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.info-label {
  color: #909399;
  font-size: 13px;
}

.info-value {
  color: #303133;
  font-size: 13px;
}

.truncate-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
  max-width: 100%;
}

:deep(.el-table .cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
