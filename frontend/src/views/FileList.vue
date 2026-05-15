<template>
  <div class="file-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">文件管理</h2>
      <div class="flex gap-4 flex-wrap">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文件名"
          style="width: 300px"
          @keyup.enter="loadFiles"
          clearable
        >
          <template #append>
            <el-button @click="loadFiles">搜索</el-button>
          </template>
        </el-input>
        <el-upload
          class="upload-demo"
          :http-request="handleHttpUpload"
          :on-success="handleUploadSuccess"
          :before-upload="beforeUpload"
          :file-list="uploadFileList"
          :auto-upload="true"
          ref="uploadRef"
        >
          <el-button type="primary">
            <el-icon class="mr-1"><Upload /></el-icon>
            上传文件
          </el-button>
        </el-upload>
      </div>
    </div>

    <el-table :data="files" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="文件名" min-width="200" />
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
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column prop="projectId" label="所属项目" width="100" />
      <el-table-column prop="createTime" label="上传时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="downloadFile(row)">下载</el-button>
          <el-popconfirm title="确定要删除这个文件吗？" @confirm="deleteFile(row)">
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
      @size-change="loadFiles"
      @current-change="loadFiles"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import request, { suppressErrors } from '@/utils/request'
import { fileApi, type FileResource } from '@/api'

const files = ref<FileResource[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const uploadFileList = ref<any[]>([])
const uploadRef = ref()

const uploadUrl = computed(() => '/api/files/upload')

const typeLabels: Record<string, string> = {
  SCRIPT: '脚本文件',
  JAR: 'JAR包',
  OTHER: '其他'
}

const getTypeLabel = (type?: string) => typeLabels[type || 'OTHER'] || '其他'
const getTypeTagType = (type?: string) => {
  const types: Record<string, string> = {
    SCRIPT: 'primary',
    JAR: 'success',
    OTHER: 'info'
  }
  return types[type || 'OTHER'] || 'info'
}

const formatSize = (size?: number) => {
  if (!size) return '0 B'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}

const loadFiles = async () => {
  loading.value = true
  try {
    const res = await fileApi.list({
      name: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    files.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

const beforeUpload = (file: File) => {
  const allowedTypes = ['.sh', '.py', '.pl', '.jar']
  const ext = file.name.substring(file.name.lastIndexOf('.'))
  if (!allowedTypes.includes(ext.toLowerCase())) {
    ElMessage.error('只允许上传脚本文件(.sh/.py/.pl)和JAR包(.jar)')
    return false
  }
  return true
}

const handleHttpUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)
  
  suppressErrors()
  try {
    await request.post('/api/files/upload', formData)
    ElMessage.success('文件上传成功')
    uploadFileList.value = []
    loadFiles()
    onSuccess()
  } catch (error: any) {
    ElMessage.error(error?.message || '文件上传失败')
    uploadFileList.value = []
    loadFiles()
    onError(error)
  }
}

const handleUploadSuccess = () => {
  loadFiles()
}

const downloadFile = (file: FileResource) => {
  ElMessage.info('下载功能开发中')
}

const deleteFile = async (file: FileResource) => {
  try {
    await fileApi.delete(file.id!)
    ElMessage.success('删除成功')
    loadFiles()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadFiles()
})
</script>
