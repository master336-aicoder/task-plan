<template>
  <div class="main-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h1 class="logo">任务计划系统</h1>
      </div>
      <el-menu :default-active="activeMenu" mode="vertical" class="sidebar-menu" :router="true">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/projects">
          <el-icon><FolderOpened /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/workflows">
          <el-icon><Connection /></el-icon>
          <span>工作流管理</span>
        </el-menu-item>
        <el-menu-item index="/schedules">
          <el-icon><Clock /></el-icon>
          <span>调度管理</span>
        </el-menu-item>
        <el-menu-item index="/executions">
          <el-icon><List /></el-icon>
          <span>执行记录</span>
        </el-menu-item>
        <el-menu-item index="/datasources">
          <el-icon><Coin /></el-icon>
          <span>数据源管理</span>
        </el-menu-item>
        <el-menu-item index="/servers">
          <el-icon><Cpu /></el-icon>
          <span>服务器管理</span>
        </el-menu-item>
        <el-menu-item index="/files">
          <el-icon><Folder /></el-icon>
          <span>文件管理</span>
        </el-menu-item>
        <el-menu-item index="/cluster-configs">
          <el-icon><Grid /></el-icon>
          <span>集群配置</span>
        </el-menu-item>
        <el-menu-item index="/task-types">
          <el-icon><Briefcase /></el-icon>
          <span>任务类型</span>
        </el-menu-item>
        <el-menu-item index="/env-variables">
          <el-icon><Setting /></el-icon>
          <span>环境变量</span>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users">用户管理</el-menu-item>
          <el-menu-item index="/system/roles">角色管理</el-menu-item>
          <el-menu-item index="/system/menus">菜单管理</el-menu-item>
          <el-menu-item index="/system/config">系统配置</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>
    <main class="main-content">
      <header class="top-header">
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon class="mr-2"><User /></el-icon>
              {{ userInfo.realName || userInfo.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Odometer, FolderOpened, Connection, Clock, List, Coin, Cpu, Folder, Grid, Briefcase, Setting, Tools, User } from '@element-plus/icons-vue'
import { authApi } from '@/api'

const router = useRouter()
const activeMenu = ref('/dashboard')

const userInfo = reactive({
  userId: 0,
  username: '',
  realName: '',
  roles: [] as number[]
})

const updateActiveMenu = () => {
  const path = window.location.hash.replace('#', '') || '/dashboard'
  activeMenu.value = path.split('/').slice(0, 2).join('/') || '/dashboard'
}

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('Logout failed:', error)
    }
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'profile') {
    ElMessage.info('个人设置功能开发中')
  }
}

onMounted(() => {
  updateActiveMenu()
  window.addEventListener('hashchange', updateActiveMenu)
  
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    Object.assign(userInfo, JSON.parse(stored))
  }
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

.sidebar-menu :deep(.el-sub-menu__title) {
  color: #a7b1c2;
}

.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: #1f2d3d;
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
