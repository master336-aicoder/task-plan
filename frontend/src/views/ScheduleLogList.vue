<template>
  <div class="schedule-log-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">调度日志</h2>
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="日志级别">
          <el-select v-model="queryForm.logLevel" placeholder="选择级别" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="INFO" value="INFO" />
            <el-option label="WARN" value="WARN" />
            <el-option label="ERROR" value="ERROR" />
          </el-select>
        </el-form-item>
        <el-form-item label="调度ID">
          <el-input v-model="queryForm.scheduleId" placeholder="调度ID" style="width: 100px" />
        </el-form-item>
        <el-form-item label="工作流ID">
          <el-input v-model="queryForm.processDefinitionId" placeholder="工作流ID" style="width: 100px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="logs" border v-loading="loading" stripe style="width: 100%;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="scheduleId" label="调度ID" width="100" />
      <el-table-column prop="processDefinitionId" label="工作流ID" width="120" />
      <el-table-column prop="processInstanceId" label="实例ID" width="120" />
      <el-table-column prop="taskNodeId" label="任务节点ID" width="120" />
      <el-table-column prop="taskName" label="任务名称" width="150" />
      <el-table-column prop="logLevel" label="级别" width="80">
        <template #default="{ row }">
          <el-tag :type="getLevelTagType(row.logLevel)">{{ row.logLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="日志内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="logTime" label="时间" width="180" />
    </el-table>

    <el-pagination
      class="mt-4"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadLogs"
      @current-change="loadLogs"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { scheduleLogApi } from '@/api'

const logs = ref<any[]>([])
const loading = ref(false)

const queryForm = reactive({
  logLevel: '',
  scheduleId: '',
  processDefinitionId: ''
})

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const getLevelTagType = (level: string) => {
  const types: Record<string, string> = {
    INFO: 'success',
    WARN: 'warning',
    ERROR: 'danger'
  }
  return types[level] || 'info'
}

const handleQuery = () => {
  currentPage.value = 1
  loadLogs()
}

const handleReset = () => {
  queryForm.logLevel = ''
  queryForm.scheduleId = ''
  queryForm.processDefinitionId = ''
  handleQuery()
}

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await scheduleLogApi.list({
      scheduleId: queryForm.scheduleId ? parseInt(queryForm.scheduleId) : undefined,
      processDefinitionId: queryForm.processDefinitionId ? parseInt(queryForm.processDefinitionId) : undefined,
      logLevel: queryForm.logLevel || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    logs.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}
</style>