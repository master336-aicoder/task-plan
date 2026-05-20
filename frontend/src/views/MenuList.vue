<template>
  <div class="menu-list">
    <div class="page-header">
      <h2>菜单管理</h2>
      <el-button type="primary" @click="showForm()">新增菜单</el-button>
    </div>

    <el-table :data="menus" border v-loading="loading" stripe row-key="id" default-expand-all>
      <el-table-column prop="menuName" label="菜单名称" width="150" />
      <el-table-column prop="menuCode" label="菜单编码" width="150" />
      <el-table-column prop="menuType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.menuType === 'M'">目录</el-tag>
          <el-tag v-else-if="row.menuType === 'C'" type="success">菜单</el-tag>
          <el-tag v-else type="warning">按钮</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由路径" width="200" />
      <el-table-column prop="component" label="组件" />
      <el-table-column prop="icon" label="图标" width="120" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="permission" label="权限标识" width="150" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showForm(row)">编辑</el-button>
          <el-popconfirm title="确定要删除这个菜单吗？" @confirm="deleteMenu(row)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="formVisible" :title="editingMenu?.id ? '编辑菜单' : '新增菜单'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio label="M">目录</el-radio>
            <el-radio label="C">菜单</el-radio>
            <el-radio label="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="菜单编码" prop="menuCode">
          <el-input v-model="form.menuCode" />
        </el-form-item>
        <el-form-item label="上级菜单">
          <el-select v-model="form.parentId" placeholder="请选择上级菜单" clearable style="width: 100%">
            <el-option v-for="m in flatMenus" :key="m.id" :label="m.menuName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.menuType !== 'F'">
          <el-input v-model="form.component" placeholder="views/xxx.vue" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.menuType === 'F'">
          <el-input v-model="form.permission" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { menuApi, type SysMenu } from '@/api'

const menus = ref<SysMenu[]>([])
const loading = ref(false)
const formVisible = ref(false)
const submitting = ref(false)
const editingMenu = ref<SysMenu | null>(null)
const formRef = ref()

const form = reactive<SysMenu>({
  menuName: '',
  menuCode: '',
  menuType: 'C',
  path: '',
  component: '',
  parentId: undefined,
  icon: '',
  sortOrder: 0,
  status: 1,
  permission: ''
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuCode: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const flatMenus = computed(() => {
  const result: SysMenu[] = []
  const flatten = (items: SysMenu[]) => {
    items.forEach(item => {
      result.push(item)
      if (item.children) {
        flatten(item.children)
      }
    })
  }
  flatten(menus.value)
  return result
})

const loadMenus = async () => {
  loading.value = true
  try {
    menus.value = await menuApi.getTree()
  } catch (error) {
    ElMessage.error('加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

const showForm = (menu?: SysMenu) => {
  editingMenu.value = menu || null
  if (menu) {
    Object.assign(form, menu)
  } else {
    Object.assign(form, {
      menuName: '',
      menuCode: '',
      menuType: 'C',
      path: '',
      component: '',
      parentId: undefined,
      icon: '',
      sortOrder: 0,
      status: 1,
      permission: ''
    })
  }
  formVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingMenu.value?.id) {
      await menuApi.update(editingMenu.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await menuApi.create(form)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadMenus()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const deleteMenu = async (menu: SysMenu) => {
  try {
    await menuApi.delete(menu.id!)
    ElMessage.success('删除成功')
    loadMenus()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadMenus()
})
</script>

<style scoped>
.menu-list {
  padding: 20px;
}
</style>
