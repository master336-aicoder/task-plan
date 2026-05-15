<template>
  <div class="task-type-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">任务类型配置</h2>
      <div class="flex gap-4 flex-wrap">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索配置名称"
          style="width: 300px"
          @keyup.enter="loadConfigs"
          clearable
        >
          <template #append>
            <el-button @click="loadConfigs">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="showForm = true">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建配置
        </el-button>
      </div>
    </div>

    <el-table :data="configs" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="配置名称" min-width="150" />
      <el-table-column prop="taskType" label="基础类型" width="120">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.taskType)">
            {{ getTypeLabel(row.taskType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="programPath" label="程序路径" min-width="200" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editConfig(row)">编辑</el-button>
          <el-popconfirm title="确定要删除这个配置吗？" @confirm="deleteConfig(row)">
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
      @size-change="loadConfigs"
      @current-change="loadConfigs"
    />

    <el-dialog v-model="showForm" title="任务类型配置" width="600px">
      <el-form :model="form" label-width="120px" :rules="formRules" ref="formRef">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="如: Python2、Python3、Java8" />
        </el-form-item>
        <el-form-item label="基础任务类型" prop="taskType">
          <el-select v-model="form.taskType" class="w-full">
            <el-option label="Shell脚本" value="SHELL" />
            <el-option label="Python脚本" value="PYTHON" />
            <el-option label="Perl脚本" value="PERL" />
            <el-option label="Java程序" value="JAVA" />
          </el-select>
        </el-form-item>
        <el-form-item label="程序路径">
          <el-input v-model="form.programPath" placeholder="如: /usr/bin/python3" />
        </el-form-item>
        <el-form-item label="默认参数">
          <el-input v-model="form.defaultParams" placeholder="JSON格式默认参数" />
        </el-form-item>
        <el-form-item label="环境变量">
          <el-input v-model="form.envParams" placeholder="JSON格式环境变量" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入配置描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" @click="saveConfig" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { taskTypeApi, type TaskTypeConfig } from '@/api'

const configs = ref<TaskTypeConfig[]>([])
const loading = ref(false)
const saving = ref(false)
const showForm = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref()

const form = ref<TaskTypeConfig>({
  name: '',
  taskType: 'PYTHON',
  programPath: '',
  defaultParams: '',
  envParams: '',
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择基础任务类型', trigger: 'change' }]
}

const typeLabels: Record<string, string> = {
  SHELL: 'Shell脚本',
  PYTHON: 'Python脚本',
  PERL: 'Perl脚本',
  JAVA: 'Java程序'
}

const getTypeLabel = (type: string) => typeLabels[type] || type
const getTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    SHELL: 'primary',
    PYTHON: 'success',
    PERL: 'warning',
    JAVA: 'info'
  }
  return types[type] || 'info'
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const res = await taskTypeApi.list({
      name: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    configs.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载配置列表失败')
  } finally {
    loading.value = false
  }
}

const editConfig = (config: TaskTypeConfig) => {
  form.value = { ...config }
  showForm.value = true
}

const saveConfig = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    if (form.value.id) {
      await taskTypeApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await taskTypeApi.create(form.value)
      ElMessage.success('创建成功')
    }
    showForm.value = false
    loadConfigs()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const deleteConfig = async (config: TaskTypeConfig) => {
  try {
    await taskTypeApi.delete(config.id!)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadConfigs()
})
</script>
