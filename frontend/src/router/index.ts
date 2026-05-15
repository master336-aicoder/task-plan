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

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', redirect: '/projects' },
      { path: '/projects', component: ProjectList },
      { path: '/workflows', component: WorkflowList },
      { path: '/workflow-design', component: WorkflowDesign },
      { path: '/workflow-design/:id', component: WorkflowDesign },
      { path: '/schedules', component: ScheduleList },
      { path: '/executions', component: ExecutionList },
      { path: '/datasources', component: DatasourceList },
      { path: '/servers', component: ServerList },
      { path: '/files', component: FileList },
      { path: '/cluster-configs', component: ClusterConfigList },
      { path: '/task-types', component: TaskTypeList },
      { path: '/env-variables', component: EnvVariableList }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router