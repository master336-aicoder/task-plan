<template>
  <div class="main-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h1 class="logo">任务计划系统</h1>
      </div>
      <el-menu :default-active="activeMenu" mode="vertical" class="sidebar-menu" :router="true">
        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/workflows">
          <el-icon><Share /></el-icon>
          <span>工作流管理</span>
        </el-menu-item>
        <el-menu-item index="/schedules">
          <el-icon><Timer /></el-icon>
          <span>调度管理</span>
        </el-menu-item>
        <el-menu-item index="/executions">
          <el-icon><VideoPlay /></el-icon>
          <span>执行记录</span>
        </el-menu-item>
        <el-menu-item index="/datasources">
          <el-icon><DataBoard /></el-icon>
          <span>数据源管理</span>
        </el-menu-item>
        <el-menu-item index="/servers">
          <el-icon><Connection /></el-icon>
          <span>服务器管理</span>
        </el-menu-item>
        <el-menu-item index="/files">
          <el-icon><Document /></el-icon>
          <span>文件管理</span>
        </el-menu-item>
        <el-menu-item index="/cluster-configs">
          <el-icon><Cpu /></el-icon>
          <span>集群配置</span>
        </el-menu-item>
        <el-menu-item index="/task-types">
          <el-icon><EditPen /></el-icon>
          <span>任务类型</span>
        </el-menu-item>
        <el-menu-item index="/env-variables">
          <el-icon><Setting /></el-icon>
          <span>环境变量</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <main class="main-content">
      <header class="top-header">
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon class="mr-2"><User /></el-icon>
              管理员
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <div class="content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Folder, Share, Timer, VideoPlay, User, DataBoard, Connection, Document, Cpu, EditPen, Setting } from '@element-plus/icons-vue'

const activeMenu = ref('/projects')

const updateActiveMenu = () => {
  const path = window.location.hash.replace('#', '') || '/projects'
  activeMenu.value = path.split('/')[1] ? `/${path.split('/')[1]}` : '/projects'
}

onMounted(() => {
  updateActiveMenu()
  window.addEventListener('hashchange', updateActiveMenu)
})

onUnmounted(() => {
  window.removeEventListener('hashchange', updateActiveMenu)
})
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 200px;
  background: #2f4050;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #1f2d3d;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  color: #fff;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #a7b1c2;
  height: 48px;
  line-height: 48px;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: #1f2d3d;
  color: #fff;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #1ab394;
  color: #fff;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f3f3f4;
}

.top-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e7eaec;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.content-wrapper {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
