import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: '/tasks', name: 'Tasks', component: () => import('@/views/task/TaskList.vue') },
      { path: '/schedules', name: 'Schedules', component: () => import('@/views/schedule/ScheduleList.vue') },
      { path: '/instances', name: 'Instances', component: () => import('@/views/instance/InstanceList.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
