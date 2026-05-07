import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/pos',
      name: 'pos',
      component: () => import('../views/POSView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/inventory',
      name: 'inventory',
      component: () => import('../views/InventoryView.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    {
      path: '/reports',
      name: 'reports',
      component: () => import('../views/ReportsView.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    {
      path: '/cash',
      name: 'cash',
      component: () => import('../views/CashDeskView.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'VENDEDOR'] }
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('../views/UserManagementView.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
  } else if (to.meta.roles && !to.meta.roles.includes(authStore.user?.role)) {
    next('/')
  } else {
    next()
  }
})

export default router
