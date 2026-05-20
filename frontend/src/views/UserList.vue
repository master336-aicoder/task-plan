<template>
  <div class="user-list">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showForm()">新增用户</el-button>
    </div>

    <el-table :data="usersWithRoles" border v-loading="loading" stripe fit>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="roleNames" label="角色清单" min-width="180">
        <template #default="{ row }">
          <el-tag v-for="roleName in row.roleNames" :key="roleName" size="small" type="info">
            {{ roleName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link title="编辑" @click="showForm({ ...row, roles: undefined })">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button size="small" type="warning" link title="分配角色" @click="showRoles({ ...row, roles: undefined })">
            <el-icon><User /></el-icon>
          </el-button>
          <el-popconfirm title="确定要删除这个用户吗？" @confirm="deleteUser({ ...row, roles: undefined })">
            <template #reference>
              <el-button size="small" type="danger" link title="删除">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="editingUser?.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!editingUser?.id" />
        </el-form-item>
        <el-form-item label="密码" :prop="editingUser?.id ? '' : 'password'">
          <el-input v-model="form.password" type="password" show-password :placeholder="editingUser?.id ? '不修改请留空' : ''" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in roles" :key="role.id" :label="role.id">
          {{ role.roleName }} ({{ role.roleCode }})
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRoles">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, User, Delete } from '@element-plus/icons-vue'
import { userApi, roleApi, type SysUser, type SysRole } from '@/api'

const users = ref<(SysUser & { roleNames: string[] })[]>([])
const roles = ref<SysRole[]>([])
const loading = ref(false)

const usersWithRoles = computed(() => users.value)
const formVisible = ref(false)
const roleDialogVisible = ref(false)
const submitting = ref(false)
const editingUser = ref<SysUser | null>(null)
const selectedRoleIds = ref<number[]>([])
const formRef = ref()
const currentUserId = ref<number>()

const form = reactive<SysUser>({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const loadUsers = async () => {
  loading.value = true
  try {
    const userList = await userApi.list()
    const usersWithRoleInfo = await Promise.all(
      userList.map(async (user) => {
        try {
          const userRoles = await userApi.getRoles(user.id!)
          const roleNames = Array.isArray(userRoles) 
            ? userRoles.map((r: SysRole) => r.roleName || r.roleCode || '')
            : []
          return { ...user, roleNames }
        } catch {
          return { ...user, roleNames: [] }
        }
      })
    )
    users.value = usersWithRoleInfo
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    roles.value = await roleApi.listActive()
  } catch (error) {
    console.error('Load roles failed:', error)
  }
}

const showForm = (user?: SysUser) => {
  editingUser.value = user || null
  if (user) {
    Object.assign(form, user)
    form.password = ''
  } else {
    Object.assign(form, {
      username: '',
      password: '',
      realName: '',
      email: '',
      phone: '',
      status: 1
    })
  }
  formVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingUser.value?.id) {
      await userApi.update(editingUser.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await userApi.create(form)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const showRoles = async (user: SysUser) => {
  currentUserId.value = user.id
  selectedRoleIds.value = []
  try {
    const userRoles = await userApi.getRoles(user.id!)
    if (Array.isArray(userRoles)) {
      selectedRoleIds.value = userRoles.map((r: SysRole) => r.id!)
    } else {
      console.warn('getRoles returned non-array:', userRoles)
    }
    roleDialogVisible.value = true
  } catch (error: any) {
    console.error('加载用户角色失败:', error)
    ElMessage.error('加载用户角色失败: ' + (error?.message || '未知错误'))
  }
}

const submitRoles = async () => {
  try {
    await userApi.assignRoles(currentUserId.value!, selectedRoleIds.value)
    ElMessage.success('分配成功')
    roleDialogVisible.value = false
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

const deleteUser = async (user: SysUser) => {
  try {
    await userApi.delete(user.id!)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadUsers()
  loadRoles()
})
</script>

<style scoped>
.user-list {
  padding: 20px;
  width: 100%;
  min-height: 100vh;
  box-sizing: border-box;
}

.user-list :deep(.el-table) {
  width: 100%;
}
</style>
