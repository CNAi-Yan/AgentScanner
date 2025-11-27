import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    children: [
      {
        path: '/agent',
        name: 'AgentManagement',
        component: () => import('../views/AgentManagement.vue')
      },
      {
        path: '/policy',
        name: 'PolicyManagement',
        component: () => import('../views/PolicyManagement.vue')
      },
      {
        path: '/log',
        name: 'LogManagement',
        component: () => import('../views/LogManagement.vue')
      },
      {
        path: '/system',
        name: 'SystemManagement',
        component: () => import('../views/SystemManagement.vue')
      },
      {
        path: '/nginx',
        name: 'NginxConfig',
        component: () => import('../views/NginxConfig.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router