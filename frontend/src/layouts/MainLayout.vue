<template>
  <el-container class="h-screen">
    <el-aside width="200px" class="bg-gray-800">
      <div class="p-4 text-white text-xl font-bold">任务计划</div>
      <el-menu
        :default-active="$route.name as string"
        class="bg-gray-800 border-0"
        text-color="#fff"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        <el-menu-item index="/schedules">
          <el-icon><Clock /></el-icon>
          <span>调度管理</span>
        </el-menu-item>
        <el-menu-item index="/instances">
          <el-icon><Monitor /></el-icon>
          <span>执行记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="bg-white border-b flex-between px-4">
        <span class="text-lg font-medium">任务计划工具箱</span>
        <el-button text>帮助文档</el-button>
      </el-header>
      <el-main class="bg-gray-50 p-4">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
</script>
