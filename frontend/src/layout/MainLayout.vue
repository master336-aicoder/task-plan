<template>
  <div class="main-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h1 class="logo">任务计划系统</h1>
      </div>
      <el-menu :default-active="activeMenu" mode="vertical" class="sidebar-menu" :router="true">
        <template v-for="menu in menus" :key="menu.id">
          <el-menu-item v-if="getChildren(menu.id).length === 0" :index="menu.path">
            <component v-if="getIcon(menu.icon)" :is="getIcon(menu.icon)" class="el-icon" />
            <span>{{ menu.menuName }}</span>
          </el-menu-item>
          <el-sub-menu v-else :index="menu.path">
            <template #title>
              <component v-if="getIcon(menu.icon)" :is="getIcon(menu.icon)" class="el-icon" />
              <span>{{ menu.menuName }}</span>
            </template>
            <el-menu-item v-for="child in getChildren(menu.id)" :key="child.id" :index="child.path">
              {{ child.menuName }}
            </el-menu-item>
          </el-sub-menu>
        </template>
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
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Odometer, FolderOpened, Connection, Clock, List, Coin, Cpu, Folder, Grid, Briefcase, Setting, Tools, User, Document } from '@element-plus/icons-vue'
import { authApi, menuApi, type SysMenu } from '@/api'

const router = useRouter()
const activeMenu = ref('/dashboard')
const menuList = ref<SysMenu[]>([])

const menus = computed(() => {
  return menuList.value.filter(m => m.parentId === null || m.parentId === 0)
})

const userInfo = reactive({
  userId: 0,
  username: '',
  realName: '',
  roles: [] as number[]
})

const getChildren = (parentId: number) => {
  return menuList.value.filter(m => m.parentId === parentId)
}

const iconMap: Record<string, any> = {
  Odometer,
  FolderOpened,
  Connection,
  Clock,
  List,
  Coin,
  Cpu,
  Folder,
  Grid,
  Briefcase,
  Setting,
  Tools,
  Document,
  FileText: Document
}

const getIcon = (iconName?: string) => {
  if (!iconName) return null
  return iconMap[iconName] || null
}

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

const loadMenus = async () => {
  try {
    const res = await menuApi.list()
    menuList.value = res.data || res || []
  } catch (error) {
    console.error('加载菜单失败:', error)
  }
}

onMounted(() => {
  updateActiveMenu()
  window.addEventListener('hashchange', updateActiveMenu)
  
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    Object.assign(userInfo, JSON.parse(stored))
  }
  
  loadMenus()
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
