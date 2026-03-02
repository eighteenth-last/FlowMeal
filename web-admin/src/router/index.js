import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '平台数据大盘', icon: 'BarChartOutline' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: 'C端用户管理', icon: 'PeopleOutline' }
      },
      {
        path: 'merchants',
        name: 'Merchants',
        component: () => import('@/views/Merchants.vue'),
        meta: { title: '商家审核与管理', icon: 'StorefrontOutline' }
      },
      {
        path: 'riders',
        name: 'Riders',
        component: () => import('@/views/Riders.vue'),
        meta: { title: '骑手调度管理', icon: 'BicycleOutline' }
      },
      {
        path: 'procurement',
        name: 'Procurement',
        component: () => import('@/views/Procurement.vue'),
        meta: { title: '原料集中采购', icon: 'CubeOutline' }
      },
      {
        path: 'coupons',
        name: 'Coupons',
        component: () => import('@/views/Coupons.vue'),
        meta: { title: '营销与优惠券', icon: 'TicketOutline' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth !== false && !auth.token) {
    return '/login'
  }
})

export default router
