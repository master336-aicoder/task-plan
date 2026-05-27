<template>
  <div class="env-config-group-list">
    <div class="page-header">
      <div class="flex justify-between items-center">
        <div>
          <h2 class="text-xl font-bold mb-1">环境变量配置组</h2>
          <p class="text-sm text-gray-500">管理环境变量配置组，每个配置组可包含多个环境变量</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建配置组
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索配置组名称"
        style="width: 300px"
        clearable
        @keyup.enter="loadGroups"
      />
    </div>

    <div class="groups-grid">
      <div
        v-for="group in filteredGroups"
        :key="group.id"
        class="group-card"
        @click="goToVariables(group)"
      >
        <div class="card-header">
          <div class="card-icon">
            <el-icon :size="32"><FolderOpened /></el-icon>
          </div>
          <div class="card-title">
            <h3>{{ group.name }}</h3>
            <span class="variable-count">{{ group.variableCount || 0 }} 个变量</span>
          </div>
        </div>
        
        <div class="card-body">
          <p class="description">{{ group.description || '暂无描述' }}</p>
        </div>
        
        <div class="card-footer">
          <div class="meta-item">
            <el-icon class="small-icon"><User /></el-icon>
            <span>{{ group.owner || '未设置' }}</span>
          </div>
          <div class="meta-item">
            <el-icon class="small-icon"><Clock /></el-icon>
            <span>{{ formatTime(group.createTime) }}</span>
          </div>
        </div>
        
        <div class="card-actions">
          <el-button size="small" @click.stop="editGroup(group)">编辑</el-button>
          <el-button size="small" type="danger" @click.stop="deleteGroup(group)">删除</el-button>
        </div>
      </div>

      <div v-if="filteredGroups.length === 0" class="empty-state">
        <el-icon class="empty-icon"><FolderOpen /></el-icon>
        <p>暂无配置组</p>
        <el-button type="primary" @click="showCreateDialog = true">创建第一个配置组</el-button>
      </div>
    </div>

    <el-dialog title="新建配置组" v-model="showCreateDialog" width="450px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="配置组名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入配置组名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入配置组描述" />
        </el-form-item>
        <el-form-item label="负责人">
          <div class="flex gap-2">
            <el-input v-model="form.owner" readonly placeholder="请选择负责人" class="flex-1" />
            <el-button type="primary" @click="showOwnerSelector = true">选择</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveGroup">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="选择负责人" v-model="showOwnerSelector" width="500px">
      <div class="owner-selector">
        <el-input
          v-model="ownerSearchKeyword"
          placeholder="搜索用户名或姓名"
          class="mb-4"
          clearable
        />
        <el-table :data="filteredUsers" border>
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="realName" label="姓名" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="selectOwner(row)">选择</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, FolderOpened, User, Clock } from '@element-plus/icons-vue'
import { envConfigGroupApi, userApi, type EnvConfigGroup, type SysUser, envVariableApi, type EnvVariable } from '@/api'

const router = useRouter()

const groups = ref<EnvConfigGroup[]>([])
const searchKeyword = ref('')
const showCreateDialog = ref(false)
const showOwnerSelector = ref(false)
const ownerSearchKeyword = ref('')
const users = ref<SysUser[]>([])
const form = ref<EnvConfigGroup>({
  name: '',
  description: '',
  owner: ''
})

const resetForm = () => {
  form.value = {
    name: '',
    description: '',
    owner: ''
  }
}

const filteredUsers = computed(() => {
  if (!ownerSearchKeyword.value) return users.value
  return users.value.filter(u => 
    u.username.toLowerCase().includes(ownerSearchKeyword.value.toLowerCase()) ||
    (u.realName && u.realName.toLowerCase().includes(ownerSearchKeyword.value.toLowerCase()))
  )
})

const loadUsers = async () => {
  try {
    const response = await userApi.list()
    users.value = response.data || response || []
  } catch (error) {
    console.error('加载用户列表失败', error)
  }
}

const selectOwner = (user: SysUser) => {
  form.value.owner = user.username
  showOwnerSelector.value = false
}

watch(showOwnerSelector, (val) => {
  if (val) {
    loadUsers()
  }
})

watch(showCreateDialog, (val) => {
  if (!val) {
    resetForm()
  }
})

const filteredGroups = computed(() => {
  if (!searchKeyword.value) return groups.value
  return groups.value.filter(g => 
    g.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const loadGroups = async () => {
  try {
    const response = await envConfigGroupApi.list()
    const responseData = response.data || response
    
    if (Array.isArray(responseData)) {
      groups.value = responseData
    } else if (responseData && typeof responseData === 'object' && 'data' in responseData) {
      groups.value = responseData.data || []
    } else {
      groups.value = []
    }
  } catch (error) {
    console.error('Load groups failed:', error)
    ElMessage.error('加载配置组失败')
  }
}

const formatTime = (time: string | undefined): string => {
  if (!time) return '未知'
  return time.split('T')[0] || time
}

const goToVariables = (group: EnvConfigGroup) => {
  if (group.id) {
    router.push(`/env-variables/group/${group.id}`)
  }
}

const openCreateDialog = () => {
  resetForm()
  showCreateDialog.value = true
}

const saveGroup = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入配置组名称')
    return
  }
  
  try {
    if (form.value.id) {
      await envConfigGroupApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await envConfigGroupApi.create(form.value)
      ElMessage.success('创建成功')
    }
    showCreateDialog.value = false
    form.value = { name: '', description: '', owner: '' }
    loadGroups()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '操作失败')
  }
}

const editGroup = (group: EnvConfigGroup) => {
  form.value = { ...group }
  showCreateDialog.value = true
}

const deleteGroup = async (group: EnvConfigGroup) => {
  if (!group.id) return
  
  ElMessageBox.confirm('确定要删除这个配置组吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await envConfigGroupApi.delete(group.id!)
      ElMessage.success('删除成功')
      loadGroups()
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.msg || '删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

onMounted(() => {
  loadGroups()
})
</script>

<style scoped>
.env-config-group-list {
  padding: 30px;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.search-bar {
  margin-bottom: 24px;
}

.groups-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.group-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.group-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.card-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.card-title {
  flex: 1;
}

.card-title h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.variable-count {
  font-size: 13px;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 10px;
}

.card-body {
  margin-bottom: 16px;
}

.description {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.small-icon {
  font-size: 14px;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  justify-content: flex-end;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  background: #fafafa;
  border-radius: 12px;
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