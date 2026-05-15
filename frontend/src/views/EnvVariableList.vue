<template>
  <div class="env-variable-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">环境变量管理</h2>
      <div class="flex gap-4 flex-wrap">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索环境变量名称"
          style="width: 300px"
          @keyup.enter="loadEnvVariables"
          clearable
        >
          <template #append>
            <el-button @click="loadEnvVariables">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="showForm = true">
          <el-icon class="mr-1"><Plus /></el-icon>
          新增环境变量
        </el-button>
      </div>
    </div>

    <el-table :data="envVariables" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="变量名称" min-width="200" />
      <el-table-column prop="value" label="变量值" min-width="300" />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{ row }">
          <el-tag :type="getCategoryTagType(row.category)">
            {{ getCategoryLabel(row.category) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editEnvVariable(row)">编辑</el-button>
          <el-popconfirm title="确定要删除这个环境变量吗？" @confirm="deleteEnvVariable(row)">
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
      @size-change="loadEnvVariables"
      @current-change="loadEnvVariables"
    />

    <el-dialog v-model="showForm" title="环境变量配置" width="500px">
      <el-form :model="form" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="变量名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入变量名称" />
        </el-form-item>
        <el-form-item label="变量值" prop="value">
          <el-input v-model="form.value" type="textarea" :rows="3" placeholder="请输入变量值" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" class="w-full">
            <el-option label="系统变量" value="SYSTEM" />
            <el-option label="数据库" value="DATABASE" />
            <el-option label="服务器" value="SERVER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入描述信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" @click="saveEnvVariable" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { envVariableApi, type EnvVariable } from '@/api'

const envVariables = ref<EnvVariable[]>([])
const loading = ref(false)
const saving = ref(false)
const showForm = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref()

const form = ref<EnvVariable>({
  name: '',
  value: '',
  description: '',
  category: 'OTHER'
})

const formRules = {
  name: [{ required: true, message: '请输入变量名称', trigger: 'blur' }],
  value: [{ required: true, message: '请输入变量值', trigger: 'blur' }]
}

const categoryLabels: Record<string, string> = {
  SYSTEM: '系统变量',
  DATABASE: '数据库',
  SERVER: '服务器',
  OTHER: '其他'
}

const getCategoryLabel = (category?: string) => categoryLabels[category || 'OTHER'] || '其他'
const getCategoryTagType = (category?: string) => {
  const types: Record<string, string> = {
    SYSTEM: 'primary',
    DATABASE: 'success',
    SERVER: 'warning',
    OTHER: 'info'
  }
  return types[category || 'OTHER'] || 'info'
}

const loadEnvVariables = async () => {
  loading.value = true
  try {
    const res = await envVariableApi.list({
      name: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    envVariables.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载环境变量列表失败')
  } finally {
    loading.value = false
  }
}

const editEnvVariable = (envVariable: EnvVariable) => {
  form.value = { ...envVariable }
  showForm.value = true
}

const saveEnvVariable = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    if (form.value.id) {
      await envVariableApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await envVariableApi.create(form.value)
      ElMessage.success('创建成功')
    }
    showForm.value = false
    loadEnvVariables()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const deleteEnvVariable = async (envVariable: EnvVariable) => {
  try {
    await envVariableApi.delete(envVariable.id!)
    ElMessage.success('删除成功')
    loadEnvVariables()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const resetForm = () => {
  form.value = {
    name: '',
    value: '',
    description: '',
    category: 'OTHER'
  }
}

onMounted(() => {
  loadEnvVariables()
  showForm.value = false
})
</script>

<style scoped>
.env-variable-list {
  padding: 20px;
}
</style>
