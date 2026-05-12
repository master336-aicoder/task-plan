<template>
  <div>
    <h2 class="text-2xl font-bold mb-4">欢迎使用任务计划工具箱</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span>任务总数</span>
              <el-icon class="text-blue-500"><Document /></el-icon>
            </div>
          </template>
          <div class="text-3xl font-bold">{{ stats.totalTasks }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span>运行中</span>
              <el-icon class="text-green-500"><Loading /></el-icon>
            </div>
          </template>
          <div class="text-3xl font-bold">{{ stats.runningTasks }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span>今日执行</span>
              <el-icon class="text-orange-500"><Calendar /></el-icon>
            </div>
          </template>
          <div class="text-3xl font-bold">{{ stats.todayExecutions }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span>成功率</span>
              <el-icon class="text-purple-500"><CircleCheck /></el-icon>
            </div>
          </template>
          <div class="text-3xl font-bold">{{ stats.successRate }}%</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const stats = ref({
  totalTasks: 0,
  runningTasks: 0,
  todayExecutions: 0,
  successRate: 0
})

onMounted(() => {
  stats.value = {
    totalTasks: 12,
    runningTasks: 3,
    todayExecutions: 45,
    successRate: 98.5
  }
})
</script>
