import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'
import ProjectList from '@/views/ProjectList.vue'
import WorkflowList from '@/views/WorkflowList.vue'
import WorkflowDesign from '@/views/WorkflowDesign.vue'
import ScheduleList from '@/views/ScheduleList.vue'
import ExecutionList from '@/views/ExecutionList.vue'
import DatasourceList from '@/views/DatasourceList.vue'
import ServerList from '@/views/ServerList.vue'
import FileList from '@/views/FileList.vue'
import ClusterConfigList from '@/views/ClusterConfigList.vue'
import TaskTypeList from '@/views/TaskTypeList.vue'
import EnvVariableList from '@/views/EnvVariableList.vue'
import Login from '@/views/Login.vue'
import Dashboard from '@/views/Dashboard.vue'
import UserList from '@/views/UserList.vue'
import RoleList from '@/views/RoleList.vue'
import MenuList from '@/views/MenuList.vue'
import SysConfigList from '@/views/SysConfigList.vue'
import ScheduleLogList from '@/views/ScheduleLogList.vue'
import TaskExecutionLogList from '@/views/TaskExecutionLogList.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', component: Dashboard, name: 'Dashboard' },
      { path: '/projects', component: ProjectList, name: 'ProjectList' },
      { path: '/workflows', component: WorkflowList, name: 'WorkflowList' },
      { path: '/workflow-design', component: WorkflowDesign, name: 'WorkflowDesign' },
      { path: '/workflow-design/:id', component: WorkflowDesign, name: 'WorkflowDesignEdit' },
      { path: '/workflow-design/:id/version/:version', component: WorkflowDesign, name: 'WorkflowDesignVersion' },
      { path: '/schedules', component: ScheduleList, name: 'ScheduleList' },
      { path: '/executions', component: ExecutionList, name: 'ExecutionList' },
      { path: '/datasources', component: DatasourceList, name: 'DatasourceList' },
      { path: '/servers', component: ServerList, name: 'ServerList' },
      { path: '/files', component: FileList, name: 'FileList' },
      { path: '/cluster-configs', component: ClusterConfigList, name: 'ClusterConfigList' },
      { path: '/task-types', component: TaskTypeList, name: 'TaskTypeList' },
      { path: '/env-variables', component: EnvVariableList, name: 'EnvVariableList' },
      { path: '/system/users', component: UserList, name: 'UserList' },
      { path: '/system/roles', component: RoleList, name: 'RoleList' },
      { path: '/system/menus', component: MenuList, name: 'MenuList' },
      { path: '/system/config', component: SysConfigList, name: 'SysConfigList' },
      { path: '/logs/schedule', component: ScheduleLogList, name: 'ScheduleLogList' },
      { path: '/logs/task-execution', component: TaskExecutionLogList, name: 'TaskExecutionLogList' }
      ]
    }
  ]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
  
  if (requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
