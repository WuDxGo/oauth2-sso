import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '首页', requiresAuth: true }
        },
        {
          path: 'users',
          name: 'UserManage',
          component: () => import('@/views/UserManage.vue'),
          meta: { title: '用户管理', requiresAuth: true }
        },
        {
          path: 'clients',
          name: 'ClientManage',
          component: () => import('@/views/ClientManage.vue'),
          meta: { title: '客户端管理', requiresAuth: true }
        },
        {
          path: 'orders',
          name: 'OrderManage',
          component: () => import('@/views/OrderManage.vue'),
          meta: { title: '订单管理', requiresAuth: true }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.title) {
    document.title = `${to.meta.title} - OAuth2 SSO 系统`
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
