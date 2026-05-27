<template>
  <div class="operation-log-list">
    <div class="page-header">
      <h2 class="text-xl font-bold mb-3">操作日志</h2>
      <div class="flex gap-4 flex-wrap justify-between">
        <el-form :inline="true" :model="queryForm" class="query-form">
          <el-form-item label="用户名">
            <el-input v-model="queryForm.username" placeholder="用户名" style="width: 150px" />
          </el-form-item>
          <el-form-item label="请求方法">
            <el-select v-model="queryForm.requestMethod" placeholder="选择方法" clearable style="width: 120px">
              <el-option label="全部" value="" />
              <el-option label="GET" value="GET" />
              <el-option label="POST" value="POST" />
              <el-option label="PUT" value="PUT" />
              <el-option label="DELETE" value="DELETE" />
            </el-select>
          </el-form-item>
          <el-form-item label="请求URL">
            <el-input v-model="queryForm.requestUrl" placeholder="请求URL" style="width: 200px" />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker v-model="queryForm.startTime" type="datetime" placeholder="开始时间" style="width: 200px" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="queryForm.endTime" type="datetime" placeholder="结束时间" style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex gap-4">
          <el-button type="success" @click="exportAllLogs">全部导出</el-button>
          <el-popconfirm title="确定要删除全部日志吗？" @confirm="deleteAllLogs">
            <template #reference>
              <el-button type="danger">全部删除</el-button>
            </template>
          </el-popconfirm>
          <el-button v-if="selectedIds.length > 0" type="success" @click="exportLogs">导出选中</el-button>
          <el-popconfirm v-if="selectedIds.length > 0" title="确定要批量删除选中的日志吗？" @confirm="batchDelete">
            <template #reference>
              <el-button type="danger">删除选中</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <el-table :data="logs" border v-loading="loading" stripe style="width: 100%;" @select-all="handleSelectAll" @select="handleSelect">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="requestMethod" label="请求方法" width="100">
        <template #default="{ row }">
          <el-tag :type="getMethodTagType(row.requestMethod)">{{ row.requestMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
      <el-table-column prop="requestParams" label="请求参数" min-width="150" show-overflow-tooltip />
      <el-table-column prop="clientIp" label="客户端IP" width="150" />
      <el-table-column prop="browserInfo" label="浏览器" width="150" />
      <el-table-column prop="userAgent" label="User-Agent" min-width="200" show-overflow-tooltip />
      <el-table-column prop="statusCode" label="状态码" width="100" />
      <el-table-column prop="requestTime" label="耗时(ms)" width="100" />
      <el-table-column prop="operationTime" label="操作时间" width="180" />
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
import { operationLogApi } from '@/api'

const logs = ref<any[]>([])
const loading = ref(false)
const selectedIds = ref<number[]>([])

const queryForm = reactive({
  username: '',
  requestMethod: '',
  requestUrl: '',
  startTime: '',
  endTime: ''
})

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const getMethodTagType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger'
  }
  return types[method] || 'info'
}

const handleQuery = () => {
  currentPage.value = 1
  loadLogs()
}

const handleReset = () => {
  queryForm.username = ''
  queryForm.requestMethod = ''
  queryForm.requestUrl = ''
  queryForm.startTime = ''
  queryForm.endTime = ''
  selectedIds.value = []
  handleQuery()
}

const handleSelectAll = (val: any[]) => {
  if (val.length === 0) {
    selectedIds.value = []
  } else {
    selectedIds.value = val.map(item => item.id)
  }
}

const handleSelect = (val: any[], row: any) => {
  if (val.length === 0) {
    selectedIds.value = selectedIds.value.filter(id => id !== row.id)
  } else {
    selectedIds.value.push(row.id)
  }
}

const exportLogs = async () => {
  try {
    const res = await operationLogApi.export({ ids: selectedIds.value })
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `operation-logs-${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const batchDelete = async () => {
  try {
    await operationLogApi.batchDelete(selectedIds.value)
    ElMessage.success('删除成功')
    selectedIds.value = []
    loadLogs()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const exportAllLogs = async () => {
  try {
    const params = {
      username: queryForm.username || undefined,
      requestMethod: queryForm.requestMethod || undefined,
      requestUrl: queryForm.requestUrl || undefined,
      startTime: queryForm.startTime || undefined,
      endTime: queryForm.endTime || undefined
    }
    const res = await operationLogApi.exportAll(params)
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `operation-logs-all-${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const deleteAllLogs = async () => {
  try {
    const params = {
      username: queryForm.username || undefined,
      requestMethod: queryForm.requestMethod || undefined,
      requestUrl: queryForm.requestUrl || undefined,
      startTime: queryForm.startTime || undefined,
      endTime: queryForm.endTime || undefined
    }
    await operationLogApi.deleteAll(params)
    ElMessage.success('全部删除成功')
    selectedIds.value = []
    loadLogs()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await operationLogApi.list({
      username: queryForm.username || undefined,
      requestMethod: queryForm.requestMethod || undefined,
      requestUrl: queryForm.requestUrl || undefined,
      startTime: queryForm.startTime || undefined,
      endTime: queryForm.endTime || undefined,
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