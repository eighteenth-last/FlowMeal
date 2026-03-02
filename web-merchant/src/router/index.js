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
    component: () => import('@/layouts/MerchantLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/orders',
    children: [
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/Orders.vue'),
        meta: { title: '线上订单工作台', icon: 'NotificationsOutline' }
      },
      {
        path: 'pos',
        name: 'POS',
        component: () => import('@/views/POS.vue'),
        meta: { title: '线下POS收银台', icon: 'DesktopOutline' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/Products.vue'),
        meta: { title: '餐品管理', icon: 'FastFoodOutline' }
      },
      {
        path: 'materials',
        name: 'Materials',
        component: () => import('@/views/Materials.vue'),
        meta: { title: '原料消耗与上报', icon: 'ClipboardOutline' }
      },
      {
        path: 'shopdata',
        name: 'ShopData',
        component: () => import('@/views/ShopData.vue'),
        meta: { title: '店铺营业数据', icon: 'TrendingUpOutline' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth !== false && !auth.token) {
    return '/login'
  }
})

export default router
