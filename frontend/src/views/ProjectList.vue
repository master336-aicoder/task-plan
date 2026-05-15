<template>
  <div class="project-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">项目管理</h2>
      <div class="flex gap-4">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索项目名称"
          style="width: 300px"
          @keyup.enter="loadProjects"
          clearable
        >
          <template #append>
            <el-button @click="loadProjects">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="showForm()">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建项目
        </el-button>
      </div>
    </div>

    <el-table :data="projects" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="项目名称" min-width="200" />
      <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showForm(row)">编辑</el-button>
          <el-button size="small" type="success" @click="goToTasks(row)">任务</el-button>
          <el-button size="small" type="primary" @click="goToWorkflows(row)">工作流</el-button>
          <el-popconfirm title="确定要删除这个项目吗？" @confirm="deleteProject(row)">
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
      @size-change="loadProjects"
      @current-change="loadProjects"
    />

    <el-dialog v-model="formVisible" :title="editingProject ? '编辑项目' : '新建项目'" width="600px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { projectApi, type Project } from '@/api'

const projects = ref<Project[]>([])
const loading = ref(false)
const submitting = ref(false)
const formVisible = ref(false)
const editingProject = ref<Project | null>(null)
const formRef = ref()

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const router = useRouter()

const form = ref<Project>({
  name: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

const loadProjects = async () => {
  loading.value = true
  try {
    const res = await projectApi.list({ keyword: searchKeyword.value })
    projects.value = res.data || res || []
    total.value = projects.value.length
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  } finally {
    loading.value = false
  }
}

const showForm = (project?: Project) => {
  editingProject.value = project || null
  if (project) {
    form.value = { ...project }
  } else {
    form.value = { name: '', description: '' }
  }
  formVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingProject.value?.id) {
      await projectApi.update(editingProject.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await projectApi.create(form.value)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    loadProjects()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

const deleteProject = async (project: Project) => {
  try {
    await projectApi.delete(project.id!)
    ElMessage.success('删除成功')
    loadProjects()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const goToTasks = (project: Project) => {
  sessionStorage.setItem('currentProject', JSON.stringify(project))
  router.push('/tasks')
}

const goToWorkflows = (project: Project) => {
  sessionStorage.setItem('currentProject', JSON.stringify(project))
  router.push('/workflows')
}

onMounted(() => loadProjects())
</script>
