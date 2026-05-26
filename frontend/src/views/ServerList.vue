<template>
  <div class="server-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">服务器管理</h2>
      <div class="flex gap-4 flex-wrap">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索服务器名称"
          style="width: 300px"
          @keyup.enter="loadServers"
          clearable
        >
          <template #append>
            <el-button @click="loadServers">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="showForm = true">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建服务器
        </el-button>
      </div>
    </div>

    <el-table :data="servers" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="服务器名称" min-width="150" />
      <el-table-column prop="hostname" label="主机地址" min-width="150" />
      <el-table-column prop="port" label="SSH端口" width="100" />
      <el-table-column prop="username" label="用户名" width="100" />
      <el-table-column prop="groupName" label="分组" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ONLINE' ? 'success' : 'info'">
            {{ row.status === 'ONLINE' ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="350" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editServer(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="editEnvScript(row)">编辑脚本</el-button>
          <el-button size="small" type="success" @click="pingServer(row)">测试连接</el-button>
          <el-popconfirm title="确定要删除这个服务器吗？" @confirm="deleteServer(row)">
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
      @size-change="loadServers"
      @current-change="loadServers"
    />

    <el-dialog v-model="showForm" title="服务器配置" width="500px">
      <el-form :model="form" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="服务器名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入服务器名称" />
        </el-form-item>
        <el-form-item label="主机地址" prop="hostname">
          <el-input v-model="form.hostname" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item label="SSH端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" class="w-full" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="分组名称">
          <el-input v-model="form.groupName" placeholder="请输入分组名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="success" @click="testServerConnection">测试连接</el-button>
        <el-button type="primary" @click="saveServer" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="编辑环境变量脚本" v-model="showScriptEditor" width="800px" top="5vh">
      <div style="height: 350px;">
        <ShellEditor v-model="currentScript" placeholder="在此输入环境变量配置脚本..." />
      </div>
      <div class="mt-3 text-sm text-gray-600">
        <p>该脚本会在每次连接服务器执行任务前自动执行，用于配置环境变量等初始化操作。</p>
      </div>
      <template #footer>
        <el-button @click="showScriptEditor = false">取消</el-button>
        <el-button type="primary" @click="saveEnvScript" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { serverApi, type ServerNode } from '@/api'
import ShellEditor from '@/components/ShellEditor.vue'

const servers = ref<ServerNode[]>([])
const loading = ref(false)
const saving = ref(false)
const showForm = ref(false)
const showScriptEditor = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref()

const form = ref<ServerNode>({
  name: '',
  hostname: '',
  port: 22,
  username: '',
  password: '',
  groupName: ''
})

const currentServerId = ref<number | null>(null)
const currentScript = ref('')

const editEnvScript = (server: ServerNode) => {
  currentServerId.value = server.id
  currentScript.value = server.envScript || ''
  showScriptEditor.value = true
}

const saveEnvScript = async () => {
  if (!currentServerId.value) return
  
  saving.value = true
  try {
    await serverApi.updateEnvScript(currentServerId.value, { envScript: currentScript.value })
    ElMessage.success('保存成功')
    showScriptEditor.value = false
    loadServers()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const formRules = {
  name: [{ required: true, message: '请输入服务器名称', trigger: 'blur' }],
  hostname: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const loadServers = async () => {
  loading.value = true
  try {
    const res = await serverApi.list({
      name: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    servers.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载服务器列表失败')
  } finally {
    loading.value = false
  }
}

const editServer = (server: ServerNode) => {
  form.value = { ...server }
  showForm.value = true
}

const saveServer = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    if (form.value.id) {
      await serverApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await serverApi.create(form.value)
      ElMessage.success('创建成功')
    }
    showForm.value = false
    loadServers()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const testServerConnection = async () => {
  try {
    const res = await serverApi.testConnection(form.value)
    if (res.success) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.message || '连接测试失败')
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

const pingServer = async (server: ServerNode) => {
  try {
    const res = await serverApi.ping(server.id!)
    if (res) {
      ElMessage.success('连接测试成功')
      loadServers()
    } else {
      ElMessage.error('连接测试失败')
      loadServers()
    }
  } catch (error) {
    ElMessage.error('连接测试失败')
  }
}

const deleteServer = async (server: ServerNode) => {
  try {
    await serverApi.delete(server.id!)
    ElMessage.success('删除成功')
    loadServers()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadServers()
})
</script>
