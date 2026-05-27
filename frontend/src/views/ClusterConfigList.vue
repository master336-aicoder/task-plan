<template>
  <div class="cluster-config-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">大数据集群配置</h2>
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
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.type)">
            {{ getTypeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
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

    <el-dialog v-model="showForm" title="集群配置" width="500px">
      <el-form :model="form" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置类型" prop="type">
          <el-select v-model="form.type" class="w-full">
            <el-option label="Flink集群" value="FLINK" />
            <el-option label="YARN环境" value="YARN" />
            <el-option label="Spark配置" value="SPARK" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置内容">
          <el-input v-model="form.configJson" type="textarea" :rows="4" placeholder="JSON格式配置内容" />
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
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { clusterConfigApi, type ClusterConfig } from '@/api'

const configs = ref<ClusterConfig[]>([])
const loading = ref(false)
const saving = ref(false)
const showForm = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref()

const form = ref<ClusterConfig>({
  name: '',
  type: 'FLINK',
  configJson: '',
  description: ''
})

const resetForm = () => {
  form.value = {
    name: '',
    type: 'FLINK',
    configJson: '',
    description: ''
  }
}

watch(showForm, (val) => {
  if (!val) {
    resetForm()
  }
})

const formRules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择配置类型', trigger: 'change' }]
}

const typeLabels: Record<string, string> = {
  FLINK: 'Flink集群',
  YARN: 'YARN环境',
  SPARK: 'Spark配置'
}

const getTypeLabel = (type: string) => typeLabels[type] || type
const getTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    FLINK: 'warning',
    YARN: 'primary',
    SPARK: 'success'
  }
  return types[type] || 'info'
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const res = await clusterConfigApi.list({
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

const editConfig = (config: ClusterConfig) => {
  form.value = { ...config }
  showForm.value = true
}

const saveConfig = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    if (form.value.id) {
      await clusterConfigApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await clusterConfigApi.create(form.value)
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

const deleteConfig = async (config: ClusterConfig) => {
  try {
    await clusterConfigApi.delete(config.id!)
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
