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
        <el-button type="primary" @click="addRow">
          <el-icon class="mr-1"><Plus /></el-icon>
          添加一行
        </el-button>
        <el-button type="success" @click="saveAll" :loading="saving">
          <el-icon class="mr-1"><Download /></el-icon>
          保存所有
        </el-button>
      </div>
    </div>

    <el-table :data="editableRows" border v-loading="loading" stripe fit>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="configName" label="配置名称" min-width="150">
        <template #default="{ row }">
          <el-input 
            v-model="row.configName" 
            placeholder="请输入配置名称"
            class="w-full"
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="变量名称" min-width="150">
        <template #default="{ row }">
          <el-input 
            v-model="row.name" 
            placeholder="请输入变量名称"
            class="w-full"
          />
        </template>
      </el-table-column>
      <el-table-column prop="value" label="变量值" min-width="200">
        <template #default="{ row }">
          <el-input 
            v-model="row.value" 
            placeholder="请输入变量值"
            class="w-full"
          />
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{ row }">
          <el-select v-model="row.category" class="w-full">
            <el-option label="系统变量" value="SYSTEM" />
            <el-option label="数据库" value="DATABASE" />
            <el-option label="服务器" value="SERVER" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="150">
        <template #default="{ row }">
          <el-input 
            v-model="row.description" 
            placeholder="请输入描述"
            class="w-full"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <template v-if="!row.id">
            <el-button size="small" type="danger" @click="removeRow(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
          <template v-else>
            <el-popconfirm title="确定要删除这个环境变量吗？" @confirm="deleteEnvVariable(row)">
              <template #reference>
                <el-button size="small" type="danger">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-popconfirm>
          </template>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Download } from '@element-plus/icons-vue'
import { envVariableApi, type EnvVariable } from '@/api'

interface EditableEnvVariable extends EnvVariable {
  _isNew?: boolean
}

const editableRows = ref<EditableEnvVariable[]>([])
const loading = ref(false)
const saving = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadEnvVariables = async () => {
  loading.value = true
  try {
    const res = await envVariableApi.list({
      name: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    editableRows.value = (res.data || []).map((item: EnvVariable) => ({ ...item }))
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载环境变量列表失败')
  } finally {
    loading.value = false
  }
}

const addRow = () => {
  editableRows.value.unshift({
    name: '',
    value: '',
    description: '',
    category: 'OTHER',
    _isNew: true
  })
}

const removeRow = (row: EditableEnvVariable) => {
  const index = editableRows.value.indexOf(row)
  if (index > -1) {
    editableRows.value.splice(index, 1)
  }
}

const saveAll = async () => {
  const newRows = editableRows.value.filter(row => row._isNew && row.name.trim())
  const updateRows = editableRows.value.filter(row => row.id && (row._dirty || row._isNew))

  if (newRows.length === 0 && updateRows.length === 0) {
    ElMessage.info('没有需要保存的修改')
    return
  }

  saving.value = true
  try {
    for (const row of newRows) {
      await envVariableApi.create(row)
    }

    for (const row of updateRows) {
      if (row.id) {
        await envVariableApi.update(row.id, row)
      }
    }

    ElMessage.success('保存成功')
    loadEnvVariables()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const deleteEnvVariable = async (row: EditableEnvVariable) => {
  if (!row.id) return
  
  try {
    await envVariableApi.delete(row.id)
    ElMessage.success('删除成功')
    loadEnvVariables()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadEnvVariables()
})
</script>

<style scoped>
.env-variable-list {
  padding: 20px;
}

:deep(.el-table .cell) {
  padding: 8px 12px;
}

:deep(.el-input) {
  width: 100%;
}
</style>
