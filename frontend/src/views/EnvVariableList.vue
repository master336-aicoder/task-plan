<template>
  <div class="env-variable-list">
    <div class="page-header">
      <div class="flex items-center gap-4">
        <el-button @click="goBack" type="text">
          <el-icon class="mr-1"><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div>
          <h2 class="text-xl font-bold">{{ groupName }}</h2>
          <p class="text-sm text-gray-500">{{ groupDescription }}</p>
        </div>
      </div>
    </div>

    <div class="content-area">
      <div class="action-bar">
        <el-button type="primary" @click="addVariable">
          <el-icon class="mr-1"><Plus /></el-icon>
          添加变量
        </el-button>
      </div>

      <div class="table-container">
        <el-table :data="variables" border v-loading="loading">
          <el-table-column prop="name" label="变量名" min-width="150" />
          <el-table-column prop="value" label="变量值" min-width="200">
            <template #default="{ row }">
              <span class="value-text">{{ row.value || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="updateTime" label="更新时间" width="180" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="editVariable(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteVariable(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="variables.length === 0 && !loading" class="empty-state">
          <el-icon class="empty-icon"><Document /></el-icon>
          <p>暂无环境变量</p>
          <el-button type="primary" @click="addVariable">添加第一个变量</el-button>
        </div>
      </div>
    </div>

    <el-dialog title="编辑变量" v-model="showVariableDialog" width="500px">
      <el-form :model="variableForm" label-width="100px">
        <el-form-item label="变量名" prop="name">
          <el-input v-model="variableForm.name" placeholder="请输入变量名" />
        </el-form-item>
        <el-form-item label="变量值" prop="value">
          <el-input v-model="variableForm.value" type="textarea" :rows="3" placeholder="请输入变量值" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="variableForm.description" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVariableDialog = false">取消</el-button>
        <el-button type="primary" @click="saveVariable">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft, Document } from '@element-plus/icons-vue'
import { envVariableApi, envConfigGroupApi, type EnvVariable, type EnvConfigGroup } from '@/api'

const route = useRoute()
const router = useRouter()

const variables = ref<EnvVariable[]>([])
const loading = ref(false)
const showVariableDialog = ref(false)
const variableForm = ref<EnvVariable>({
  name: '',
  value: '',
  description: ''
})

const groupName = ref('')
const groupDescription = ref('')
let currentGroupId: number | null = null

const goBack = () => {
  router.push('/env-variables')
}

const loadVariables = async () => {
  const groupId = parseInt(route.params.groupId as string)
  currentGroupId = groupId
  
  loading.value = true
  try {
    const groupResponse = await envConfigGroupApi.get(groupId)
    const group = groupResponse.data || groupResponse
    groupName.value = group.name
    groupDescription.value = group.description || ''
    
    const response = await envVariableApi.listByGroupId(groupId)
    variables.value = response.data || response || []
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const addVariable = () => {
  variableForm.value = {
    name: '',
    value: '',
    description: '',
    groupId: currentGroupId
  }
  showVariableDialog.value = true
}

const editVariable = (variable: EnvVariable) => {
  variableForm.value = { ...variable }
  showVariableDialog.value = true
}

const saveVariable = async () => {
  if (!variableForm.value.name.trim()) {
    ElMessage.warning('请输入变量名')
    return
  }
  
  variableForm.value.groupId = currentGroupId
  
  try {
    if (variableForm.value.id) {
      await envVariableApi.update(variableForm.value.id, variableForm.value)
      ElMessage.success('更新成功')
    } else {
      await envVariableApi.create(variableForm.value)
      ElMessage.success('创建成功')
    }
    showVariableDialog.value = false
    loadVariables()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '操作失败')
  }
}

const deleteVariable = async (variable: EnvVariable) => {
  if (!variable.id) return
  
  ElMessageBox.confirm('确定要删除这个变量吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await envVariableApi.delete(variable.id!)
      ElMessage.success('删除成功')
      loadVariables()
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.msg || '删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

onMounted(() => {
  loadVariables()
})

watch(() => route.params.groupId, () => {
  loadVariables()
})
</script>

<style scoped>
.env-variable-list {
  padding: 30px;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.content-area {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.action-bar {
  margin-bottom: 20px;
}

.table-container {
  min-height: 300px;
}

.value-text {
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #67c23a;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #fafafa;
  border-radius: 8px;
}

.empty-icon {
  font-size: 48px;
  color: #ccc;
  margin-bottom: 16px;
}

.empty-state p {
  color: #909399;
  margin-bottom: 20px;
}
</style>