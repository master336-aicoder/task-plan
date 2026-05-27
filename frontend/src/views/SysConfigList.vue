<template>
  <div class="sys-config-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="text-xl font-bold">系统配置</h2>
        <p class="text-gray-500 text-sm mt-1">管理系统运行所需的各项配置参数</p>
      </div>
      <el-button type="primary" @click="showForm()" class="header-btn">
        <el-icon><Plus /></el-icon>
        新增配置
      </el-button>
    </div>

    <el-card class="config-table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">配置列表</span>
          <span class="config-count">共 {{ configs.length }} 项配置</span>
        </div>
      </template>
      <el-table :data="configs" border v-loading="loading" stripe :empty-text="loading ? '' : '暂无配置数据'">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="configKey" label="配置键" width="200">
          <template #default="{ row }">
            <span class="config-key-tag">{{ row.configKey }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="configValue" label="配置值" min-width="300">
          <template #default="{ row }">
            <span class="config-value" :title="row.configValue">{{ row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" width="220" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="showForm(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-popconfirm title="确定要删除这个配置吗？" @confirm="deleteConfig(row)">
                <template #reference>
                  <el-button size="small" type="danger">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-row :gutter="20" class="config-sections">
      <el-col :span="12">
        <el-card class="section-card" shadow="hover">
          <template #header>
            <div class="section-header">
              <el-icon class="section-icon"><FolderOpened /></el-icon>
              <span class="section-title">路径配置</span>
            </div>
          </template>
          <div class="path-grid">
            <div class="path-item">
              <div class="path-label">
                <el-icon class="path-icon"><Upload /></el-icon>
                <span>上传路径</span>
                <span class="config-key-badge">upload.path</span>
              </div>
              <div class="path-value truncate" :title="paths.uploadPath">{{ paths.uploadPath || '-' }}</div>
            </div>
            <div class="path-item">
              <div class="path-label">
                <el-icon class="path-icon"><Clock /></el-icon>
                <span>调度日志路径</span>
                <span class="config-key-badge">schedule.log.path</span>
              </div>
              <div class="path-value truncate" :title="paths.scheduleLogPath">{{ paths.scheduleLogPath || '-' }}</div>
            </div>
            <div class="path-item">
              <div class="path-label">
                <el-icon class="path-icon"><List /></el-icon>
                <span>任务日志路径</span>
                <span class="config-key-badge">task.log.path</span>
              </div>
              <div class="path-value truncate" :title="paths.taskLogPath">{{ paths.taskLogPath || '-' }}</div>
            </div>
            <div class="path-item">
              <div class="path-label">
                <el-icon class="path-icon"><Document /></el-icon>
                <span>任务实例路径</span>
                <span class="config-key-badge">task.instance.path</span>
              </div>
              <div class="path-value truncate" :title="paths.taskInstancePath">{{ paths.taskInstancePath || '-' }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="section-card mt-4" shadow="hover">
          <template #header>
            <div class="section-header">
              <el-icon class="section-icon"><Document /></el-icon>
              <span class="section-title">文件上传限制</span>
            </div>
          </template>
          <div class="upload-grid">
            <div class="upload-item">
              <div class="upload-label">
                <span>允许文件类型</span>
                <span class="config-key-badge">upload.allowedTypes</span>
              </div>
              <div class="upload-value">{{ paths.uploadAllowedTypes || '-' }}</div>
            </div>
            <div class="upload-item">
              <div class="upload-label">
                <span>最大文件大小</span>
                <span class="config-key-badge">upload.maxSizeMB</span>
              </div>
              <div class="upload-value">{{ paths.uploadMaxSizeMB || '-' }} MB</div>
            </div>
            <div class="upload-item">
              <div class="upload-label">
                <span>最大分块数</span>
                <span class="config-key-badge">upload.maxChunks</span>
              </div>
              <div class="upload-value">{{ paths.uploadMaxChunks || '-' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="section-card mail-section" shadow="hover">
          <template #header>
            <div class="section-header">
              <el-icon class="section-icon"><Document /></el-icon>
              <span class="section-title">邮件服务器配置</span>
            </div>
          </template>
          <el-form :model="mailConfigForm" label-width="110px" class="mail-form">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="服务器地址">
                  <el-input v-model="mailConfigForm.host" placeholder="smtp.example.com" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="端口">
                  <el-input v-model="mailConfigForm.port" placeholder="587" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="用户名">
                  <el-input v-model="mailConfigForm.username" placeholder="邮箱账号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="密码">
                  <el-input v-model="mailConfigForm.password" type="password" placeholder="邮箱密码" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="发件人邮箱">
                  <el-input v-model="mailConfigForm.from" placeholder="sender@example.com" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="SSL加密">
                  <el-select v-model="mailConfigForm.ssl" placeholder="选择">
                    <el-option label="启用" value="true" />
                    <el-option label="禁用" value="false" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <el-divider class="mail-divider" />
          <div class="mail-actions">
            <el-button 
              type="primary" 
              @click="saveMailConfig"
              :loading="savingMailConfig"
              class="action-btn"
            >
              <el-icon><Check /></el-icon>
              保存配置
            </el-button>
            <div class="test-section">
              <el-input 
                v-model="testEmail" 
                placeholder="请输入测试邮箱" 
                class="test-input"
              />
              <el-button 
                type="success" 
                @click="testMail"
                :loading="testingMail"
              >
                <el-icon><ArrowRight /></el-icon>
                测试发送
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="formVisible" :title="editingConfig?.id ? '编辑配置' : '新增配置'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="dialog-form">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" :disabled="!!editingConfig?.id" placeholder="例如: mail.host" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="form.configValue" placeholder="请输入配置值" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, FolderOpened, Upload, Clock, List, Document, Check, ArrowRight, Edit, Delete } from '@element-plus/icons-vue'
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
  taskInstancePath?: string
  uploadAllowedTypes?: string
  uploadMaxSizeMB?: string
  uploadMaxChunks?: string
}

interface MailConfig {
  host?: string
  port?: string
  username?: string
  password?: string
  from?: string
  ssl?: string
}

const configs = ref<SysConfig[]>([])
const paths = ref<Paths>({})
const mailConfigForm = reactive<MailConfig>({
  host: '',
  port: '',
  username: '',
  password: '',
  from: '',
  ssl: ''
})
const testEmail = ref('')
const testingMail = ref(false)
const savingMailConfig = ref(false)
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

const loadMailConfigs = async () => {
  try {
    const response = await request.get('/sys-config/mail')
    if (response && typeof response === 'object') {
      const data = 'data' in response ? response.data : response
      mailConfigForm.host = data.host || ''
      mailConfigForm.port = data.port || ''
      mailConfigForm.username = data.username || ''
      mailConfigForm.password = data.password || ''
      mailConfigForm.from = data.from || ''
      mailConfigForm.ssl = data.ssl || ''
    }
  } catch (error) {
    console.error('加载邮件配置失败', error)
  }
}

const saveMailConfig = async () => {
  savingMailConfig.value = true
  try {
    await request.put('/sys-config/mail', {
      host: mailConfigForm.host,
      port: mailConfigForm.port,
      username: mailConfigForm.username,
      password: mailConfigForm.password,
      from: mailConfigForm.from,
      ssl: mailConfigForm.ssl
    })
    ElMessage.success('邮件配置保存成功')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    savingMailConfig.value = false
  }
}

const testMail = async () => {
  if (!testEmail.value) {
    ElMessage.warning('请输入测试邮箱地址')
    return
  }

  testingMail.value = true
  try {
    await request.post('/mail/test', { to: testEmail.value })
    ElMessage.success('测试邮件发送成功')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '发送失败')
  } finally {
    testingMail.value = false
  }
}

onMounted(() => {
  loadConfigs()
  loadPaths()
  loadMailConfigs()
})
</script>

<style scoped>
.sys-config-page {
  padding: 24px;
  min-height: 100vh;
  background: #f5f5f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.config-table-card {
  border-radius: 12px;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.config-count {
  font-size: 13px;
  color: #999;
}

.config-key-tag {
  background: #e8f4fd;
  color: #1e88e5;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.config-value {
  color: #333;
  font-size: 13px;
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}

.config-sections {
  margin-bottom: 20px;
}

.section-card {
  border-radius: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-icon {
  color: #409eff;
  font-size: 18px;
}

.section-title {
  font-weight: 600;
  font-size: 15px;
}

.path-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.path-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.path-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  flex-wrap: wrap;
}

.path-icon {
  font-size: 14px;
  color: #999;
}

.path-value {
  font-size: 14px;
  color: #333;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  word-break: break-all;
}

.config-key-badge {
  font-size: 11px;
  color: #909399;
  background: #f2f6fc;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: auto;
}

.upload-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  flex-wrap: wrap;
}

.upload-value {
  font-size: 14px;
  color: #333;
  padding: 10px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  word-break: break-all;
  max-width: 100%;
}

.mail-section {
  height: 100%;
}

.mail-form {
  padding: 12px 0;
}

.mail-divider {
  margin: 16px 0;
}

.mail-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.test-section {
  display: flex;
  gap: 12px;
}

.test-input {
  width: 200px;
}

.dialog-form {
  padding: 16px 0;
}

.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mt-4 {
  margin-top: 20px;
}
</style>