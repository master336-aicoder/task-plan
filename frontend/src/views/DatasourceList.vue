<template>
  <div class="datasource-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">数据源管理</h2>
      <div class="flex gap-4 flex-wrap">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索数据源名称"
          style="width: 300px"
          @keyup.enter="loadDatasources"
          clearable
        >
          <template #append>
            <el-button @click="loadDatasources">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="showForm = true">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建数据源
        </el-button>
      </div>
    </div>

    <el-table :data="datasources" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="数据源名称" min-width="150" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.type)">
            {{ getTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="host" label="主机地址" min-width="150" />
      <el-table-column prop="port" label="端口" width="80" />
      <el-table-column prop="database" label="数据库" width="120" />
      <el-table-column prop="username" label="用户名" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editDatasource(row)">编辑</el-button>
          <el-button size="small" type="success" @click="testConnection(row)">测试连接</el-button>
          <el-popconfirm title="确定要删除这个数据源吗？" @confirm="deleteDatasource(row)">
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
      @size-change="loadDatasources"
      @current-change="loadDatasources"
    />

    <el-dialog v-model="showForm" title="数据源配置" width="500px">
      <el-form :model="form" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="数据库类型" prop="type">
          <el-select v-model="form.type" class="w-full">
            <el-option label="MySQL" value="MYSQL" />
            <el-option label="PostgreSQL" value="POSTGRESQL" />
            <el-option label="Oracle" value="ORACLE" />
            <el-option label="SQLite" value="SQLITE" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="form.host" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" class="w-full" />
        </el-form-item>
        <el-form-item label="数据库名称">
          <el-input v-model="form.databaseName" placeholder="请输入数据库名称（非必填）" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="连接参数">
          <el-input v-model="form.params" placeholder="JSON格式参数" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="success" @click="testDatasourceConnection">测试连接</el-button>
        <el-button type="primary" @click="saveDatasource" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { datasourceApi, type Datasource } from '@/api'

const datasources = ref<Datasource[]>([])
const loading = ref(false)
const saving = ref(false)
const showForm = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const router = useRouter()
const formRef = ref()

const form = ref<Datasource>({
  name: '',
  type: 'MYSQL',
  host: '',
  port: 3306,
  databaseName: '',
  username: '',
  password: '',
  params: ''
})

const resetForm = () => {
  form.value = {
    name: '',
    type: 'MYSQL',
    host: '',
    port: 3306,
    databaseName: '',
    username: '',
    password: '',
    params: ''
  }
}

watch(showForm, (val) => {
  if (!val) {
    resetForm()
  }
})

const formRules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  databaseName: [{ required: true, message: '请输入数据库名称', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const typeLabels: Record<string, string> = {
  MYSQL: 'MySQL',
  POSTGRESQL: 'PostgreSQL',
  ORACLE: 'Oracle',
  SQLITE: 'SQLite'
}

const getTypeLabel = (type: string) => typeLabels[type] || type
const getTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    MYSQL: 'success',
    POSTGRESQL: 'primary',
    ORACLE: 'warning',
    SQLITE: 'info'
  }
  return types[type] || 'info'
}

const loadDatasources = async () => {
  loading.value = true
  try {
    const res = await datasourceApi.list({
      name: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    datasources.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载数据源列表失败')
  } finally {
    loading.value = false
  }
}

const editDatasource = (datasource: Datasource) => {
  form.value = { ...datasource }
  showForm.value = true
}

const saveDatasource = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    if (form.value.id) {
      await datasourceApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await datasourceApi.create(form.value)
      ElMessage.success('创建成功')
    }
    showForm.value = false
    loadDatasources()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const testDatasourceConnection = async () => {
  try {
    const res = await datasourceApi.testConnection(form.value)
    if (res.success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.message || '连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

const testConnection = async (datasource: Datasource) => {
  try {
    const res = await datasourceApi.test(datasource.id!)
    if (res) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error('连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

const deleteDatasource = async (datasource: Datasource) => {
  try {
    await datasourceApi.delete(datasource.id!)
    ElMessage.success('删除成功')
    loadDatasources()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadDatasources()
})
</script>
