<template>
  <div class="workflow-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">工作流定义管理</h2>
      <div class="flex gap-4 flex-wrap">
        <el-select v-model="selectedProjectId" placeholder="全部项目" @change="loadWorkflows" style="width: 200px">
          <el-option label="全部项目" :value="0" />
          <el-option v-for="project in projects" :key="project.id" :label="project.name" :value="project.id" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索工作流名称"
          style="width: 300px"
          @keyup.enter="loadWorkflows"
          clearable
        >
          <template #append>
            <el-button @click="loadWorkflows">搜索</el-button>
          </template>
        </el-input>
        <el-button type="primary" @click="goToDesign()">
          <el-icon class="mr-1"><Plus /></el-icon>
          新建工作流
        </el-button>
      </div>
    </div>

    <el-table :data="workflows" border v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="工作流名称" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="releaseState" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.releaseState === 'ONLINE' ? 'success' : 'info'">
            {{ row.releaseState === 'ONLINE' ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column prop="owner" label="负责人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="350" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="goToDesign(row)">设计</el-button>
          <el-button size="small" @click="goToSchedule(row)">调度</el-button>
          <el-button v-if="row.releaseState === 'OFFLINE'" size="small" type="success" @click="releaseWorkflow(row)">发布</el-button>
          <el-button v-else size="small" type="warning" @click="offlineWorkflow(row)">下线</el-button>
          <el-popconfirm title="确定要删除这个工作流吗？" @confirm="deleteWorkflow(row)">
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
      @size-change="loadWorkflows"
      @current-change="loadWorkflows"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { projectApi, workflowApi, type Project, type ProcessDefinition } from '@/api'

const workflows = ref<ProcessDefinition[]>([])
const projects = ref<Project[]>([])
const loading = ref(false)

const selectedProjectId = ref<number>(0)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const router = useRouter()

const loadProjects = async () => {
  try {
    const res = await projectApi.list()
    projects.value = res.data || res || []
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadWorkflows = async () => {
  loading.value = true
  try {
    const res = await workflowApi.list({
      projectId: selectedProjectId.value && selectedProjectId.value !== 0 ? selectedProjectId.value : undefined,
      name: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    workflows.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载工作流列表失败')
  } finally {
    loading.value = false
  }
}

const goToDesign = (workflow?: ProcessDefinition) => {
  if (workflow) {
    sessionStorage.setItem('currentWorkflow', JSON.stringify(workflow))
    router.push(`/workflow-design/${workflow.id}`)
  } else {
    sessionStorage.setItem('currentProjectId', String(selectedProjectId.value))
    router.push('/workflow-design')
  }
}

const goToSchedule = (workflow: ProcessDefinition) => {
  sessionStorage.setItem('currentWorkflow', JSON.stringify(workflow))
  router.push('/schedules')
}

const releaseWorkflow = async (workflow: ProcessDefinition) => {
  try {
    await workflowApi.release(workflow.id!)
    ElMessage.success('发布成功')
    loadWorkflows()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

const offlineWorkflow = async (workflow: ProcessDefinition) => {
  try {
    await workflowApi.offline(workflow.id!)
    ElMessage.success('已下线')
    loadWorkflows()
  } catch (error) {
    ElMessage.error('下线失败')
  }
}

const deleteWorkflow = async (workflow: ProcessDefinition) => {
  try {
    await workflowApi.delete(workflow.id!)
    ElMessage.success('删除成功')
    loadWorkflows()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(async () => {
  await loadProjects()
  loadWorkflows()
})
</script>
