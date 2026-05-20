<template>
  <div class="role-list">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="showForm()">新增角色</el-button>
    </div>

    <el-table :data="roles" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roleCode" label="角色代码" width="150" />
      <el-table-column prop="roleName" label="角色名称" width="150" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showForm(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="showMenus(row)">分配菜单</el-button>
          <el-popconfirm title="确定要删除这个角色吗？" @confirm="deleteRole(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="editingRole?.id ? '编辑角色' : '新增角色'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色代码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="!!editingRole?.id" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" />
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

    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="600px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        node-key="id"
        :check-strictly="false"
        show-checkbox
        default-expand-all
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMenus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { roleApi, menuApi, type SysRole, type SysMenu } from '@/api'

const roles = ref<SysRole[]>([])
const menuTree = ref<SysMenu[]>([])
const loading = ref(false)
const formVisible = ref(false)
const menuDialogVisible = ref(false)
const submitting = ref(false)
const editingRole = ref<SysRole | null>(null)
const currentRoleId = ref<number>()
const menuTreeRef = ref()
const formRef = ref()

const form = reactive<SysRole>({
  roleCode: '',
  roleName: '',
  description: '',
  status: 1
})

const rules = {
  roleCode: [{ required: true, message: '请输入角色代码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const loadRoles = async () => {
  loading.value = true
  try {
    roles.value = await roleApi.list()
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

const loadMenuTree = async () => {
  try {
    menuTree.value = await menuApi.getTree()
  } catch (error) {
    console.error('Load menu tree failed:', error)
  }
}

const showForm = (role?: SysRole) => {
  editingRole.value = role || null
  if (role) {
    Object.assign(form, role)
  } else {
    Object.assign(form, {
      roleCode: '',
      roleName: '',
      description: '',
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
    if (editingRole.value?.id) {
      await roleApi.update(editingRole.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await roleApi.create(form)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadRoles()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const showMenus = async (role: SysRole) => {
  currentRoleId.value = role.id
  try {
    const menus = await roleApi.getMenus(role.id!)
    const checkedKeys = menus.map((m: SysMenu) => m.id!)
    menuTreeRef.value?.setCheckedKeys(checkedKeys)
    menuDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载菜单权限失败')
  }
}

const submitMenus = async () => {
  const checkedNodes = menuTreeRef.value?.getCheckedNodes() || []
  const menuIds = checkedNodes.map((n: SysMenu) => n.id!)
  try {
    await roleApi.assignMenus(currentRoleId.value!, menuIds)
    ElMessage.success('分配成功')
    menuDialogVisible.value = false
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

const deleteRole = async (role: SysRole) => {
  try {
    await roleApi.delete(role.id!)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadRoles()
  loadMenuTree()
})
</script>

<style scoped>
.role-list {
  padding: 20px;
}
</style>
