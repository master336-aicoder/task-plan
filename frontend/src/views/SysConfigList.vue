<template>
  <div class="sys-config-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">系统配置</h2>
      <el-button type="primary" @click="showForm()">新增配置</el-button>
    </div>

    <el-table :data="configs" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="configKey" label="配置键" width="180" />
      <el-table-column prop="configValue" label="配置值" min-width="300">
        <template #default="{ row }">
          <span class="truncate" :title="row.configValue">{{ row.configValue }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" width="200" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showForm(row)">编辑</el-button>
          <el-popconfirm title="确定要删除这个配置吗？" @confirm="deleteConfig(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="editingConfig?.id ? '编辑配置' : '新增配置'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" :disabled="!!editingConfig?.id" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="form.configValue" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入配置描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <div class="config-section">
      <h3 class="text-lg font-semibold mb-4">路径配置</h3>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="config-card">
            <template #header>
              <span class="font-medium">上传路径</span>
            </template>
            <p class="text-gray-600 text-sm mt-2">{{ paths.uploadPath || '-' }}</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="config-card">
            <template #header>
              <span class="font-medium">调度日志路径</span>
            </template>
            <p class="text-gray-600 text-sm mt-2">{{ paths.scheduleLogPath || '-' }}</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="config-card">
            <template #header>
              <span class="font-medium">任务日志路径</span>
            </template>
            <p class="text-gray-600 text-sm mt-2">{{ paths.taskLogPath || '-' }}</p>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="config-section mt-4">
      <h3 class="text-lg font-semibold mb-4">文件上传限制</h3>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="config-card">
            <template #header>
              <span class="font-medium">允许文件类型</span>
            </template>
            <p class="text-gray-600 text-sm mt-2">{{ paths.uploadAllowedTypes || '-' }}</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="config-card">
            <template #header>
              <span class="font-medium">最大文件大小</span>
            </template>
            <p class="text-gray-600 text-sm mt-2">{{ paths.uploadMaxSizeMB || '-' }} MB</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="config-card">
            <template #header>
              <span class="font-medium">最大分块数</span>
            </template>
            <p class="text-gray-600 text-sm mt-2">{{ paths.uploadMaxChunks || '-' }}</p>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

interface SysConfig {
  id?: number
  configKey: string
  configValue: string
  description?: string
  createTime?: string
  updateTime?: string
}

interface Paths {
  uploadPath?: string
  scheduleLogPath?: string
  taskLogPath?: string
  uploadAllowedTypes?: string
  uploadMaxSizeMB?: string
  uploadMaxChunks?: string
}

const configs = ref<SysConfig[]>([])
const paths = ref<Paths>({})
const loading = ref(false)
const formVisible = ref(false)
const submitting = ref(false)
const editingConfig = ref<SysConfig | null>(null)
const formRef = ref()

const form = reactive<SysConfig>({
  configKey: '',
  configValue: '',
  description: ''
})

const rules = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const response = await request.get('/sys-config/list')
    if (Array.isArray(response)) {
      configs.value = response
    } else if (response && typeof response === 'object' && 'data' in response) {
      configs.value = Array.isArray(response.data) ? response.data : []
    } else {
      configs.value = []
    }
  } catch (error) {
    ElMessage.error('加载配置列表失败')
  } finally {
    loading.value = false
  }
}

const loadPaths = async () => {
  try {
    const response = await request.get('/sys-config/paths')
    if (response && typeof response === 'object' && !Array.isArray(response)) {
      paths.value = response
    } else if (response && typeof response === 'object' && 'data' in response) {
      paths.value = response.data || {}
    } else {
      paths.value = {}
    }
  } catch (error) {
    console.error('加载路径配置失败', error)
  }
}

const showForm = (config?: SysConfig) => {
  editingConfig.value = config || null
  if (config) {
    form.configKey = config.configKey || ''
    form.configValue = config.configValue || ''
    form.description = config.description || ''
  } else {
    form.configKey = ''
    form.configValue = ''
    form.description = ''
  }
  formVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingConfig.value?.id) {
      await request.put(`/sys-config/${editingConfig.value.id}`, {
        configKey: form.configKey,
        configValue: form.configValue,
        description: form.description
      })
      ElMessage.success('更新成功')
    } else {
      await request.post('/sys-config', {
        configKey: form.configKey,
        configValue: form.configValue,
        description: form.description
      })
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadConfigs()
    loadPaths()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const deleteConfig = async (config: SysConfig) => {
  try {
    await request.delete(`/sys-config/${config.id}`)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadConfigs()
  loadPaths()
})
</script>

<style scoped>
.sys-config-list {
  padding: 20px;
}

.config-section {
  margin-top: 20px;
}

.config-card {
  height: 100%;
}

.truncate {
  display: inline-block;
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>