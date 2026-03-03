<template>
  <div class="layout-container">
    <!-- 左侧边栏 -->
    <aside class="sidebar" :class="{ 'collapsed': collapsed }">
      <!-- Logo -->
      <div class="logo-area">
        <div class="logo-icon">
          <n-icon><RestaurantOutline /></n-icon>
        </div>
        <span v-show="!collapsed" class="logo-text">FlowMeal 商家</span>
      </div>

      <!-- 用户信息展示 -->
      <div class="user-profile" v-show="!collapsed">
        <div class="avatar-wrap">
          <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=merchant" class="avatar" />
        </div>
        <div class="user-info">
          <p class="user-name">{{ authStore.userInfo?.shopName || '我的店铺' }}</p>
          <p class="user-role"><n-icon><StorefrontOutline /></n-icon> 商铺掌柜</p>
        </div>
      </div>

      <!-- 菜单 -->
      <nav class="nav-menu">
        <router-link
          v-for="item in menuOptions"
          :key="item.key"
          class="menu-item"
          :class="{ 'active': activeKey === item.key }"
          :to="{ name: item.key }"
          @click="item.key === 'Orders' ? newOrderCount = 0 : null"
        >
          <n-icon class="menu-icon"><component :is="item.iconComponent" /></n-icon>
          <span v-show="!collapsed" class="menu-text">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer" v-show="!collapsed">
        v2.5.0 FlowMeal System
      </div>
    </aside>

    <!-- 右侧容器 -->
    <div class="main-container">
      <!-- 顶部Header -->
      <header class="header">
        <div class="header-left">
          <button class="collapse-btn" @click="collapsed = !collapsed">
            <n-icon size="20"><MenuOutline /></n-icon>
          </button>
          <div class="divider"></div>
          <span class="page-title">{{ currentPageTitle }}</span>
        </div>

        <div class="header-right">
          <n-badge :value="newOrderCount" :max="99" style="margin-right: 20px;">
            <n-button text @click="router.push({ name: 'Orders' }); newOrderCount = 0">
              <template #icon><n-icon size="20"><NotificationsOutline /></n-icon></template>
              新订单
            </n-button>
          </n-badge>

          <n-dropdown :options="userDropdown" @select="handleUserAction">
            <div class="header-user">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=merchant" class="header-avatar" />
              <n-icon size="14" class="ml-1"><ChevronDownOutline /></n-icon>
            </div>
          </n-dropdown>
        </div>
      </header>

      <!-- 主要内容区域 -->
      <main class="content-area">
        <div class="fade-in wrapper">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  ReceiptOutline,
  CalculatorOutline,
  RestaurantOutline,
  NutritionOutline,
  BarChartOutline,
  StorefrontOutline,
  NotificationsOutline,
  ChevronDownOutline,
  MenuOutline
} from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const message = useMessage()
const collapsed = ref(false)
const newOrderCount = ref(0)

// WebSocket 连听新订单
let ws = null
onMounted(() => {
  const merchantId = authStore.merchantId
  if (!merchantId) return
  const token = localStorage.getItem('fm_merchant_token')
  ws = new WebSocket(`ws://localhost:8012/api/ws/merchant/${merchantId}?token=${token}`)
  ws.onmessage = (e) => {
    try {
      const data = JSON.parse(e.data)
      if (data.type === 'NEW_ORDER') {
        newOrderCount.value++
        message.info(`新订单来啦！订单号：${data.orderNo}`, { duration: 5000 })
      }
    } catch {}
  }
})
onUnmounted(() => ws?.close())

const menuOptions = [
  { label: '订单工作台', key: 'Orders',    iconComponent: ReceiptOutline },
  { label: 'POS 收银台', key: 'POS',       iconComponent: CalculatorOutline },
  { label: '餐品管理',   key: 'Products',  iconComponent: RestaurantOutline },
  { label: '原料上报',   key: 'Materials', iconComponent: NutritionOutline },
  { label: '营业数据',   key: 'ShopData',  iconComponent: BarChartOutline }
]

const titleMap = {
  Orders:    '订单工作台',
  POS:       'POS 收银台',
  Products:  '餐品管理',
  Materials: '原料消耗上报',
  ShopData:  '营业数据'
}

const activeKey = computed(() => route.name)
const currentPageTitle = computed(() => titleMap[route.name] || 'FlowMeal商家后台')

const userDropdown = [{ label: '退出登录', key: 'logout' }]

const handleUserAction = (key) => {
  if (key === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  width: 100%;
  background-color: #f3f4f6;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 侧边栏 */
.sidebar {
  width: 256px;
  background-color: #111827;
  color: white;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  box-shadow: 4px 0 24px rgba(0,0,0,0.1);
  z-index: 20;
  flex-shrink: 0;
}
.sidebar.collapsed {
  width: 72px;
}

/* Logo */
.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #1f2937;
  background-color: #030712;
  flex-shrink: 0;
}
.sidebar.collapsed .logo-area {
  padding: 0;
  justify-content: center;
}
.logo-icon {
  width: 32px;
  height: 32px;
  background-color: #FFD100;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #000;
  font-size: 20px;
  margin-right: 12px;
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.2);
}
.sidebar.collapsed .logo-icon {
  margin-right: 0;
}
.logo-text {
  font-weight: 700;
  font-size: 18px;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

/* 用户信息 */
.user-profile {
  padding: 24px;
  border-bottom: 1px solid #1f2937;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}
.avatar-wrap {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #1f2937;
  border: 2px solid #FFD100;
  padding: 2px;
}
.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}
.user-name {
  font-size: 14px;
  font-weight: 700;
  margin: 0 0 4px 0;
  white-space: nowrap;
}
.user-role {
  font-size: 10px;
  color: #FFD100;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  border-radius: 8px;
  color: #9ca3af;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.sidebar.collapsed .menu-item {
  padding: 12px;
  justify-content: center;
}

.menu-item:hover {
  color: white;
  background-color: rgba(255, 255, 255, 0.05);
}

.menu-item.active {
  background-color: #FFD100;
  color: black;
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.2);
}

.menu-icon {
  font-size: 20px;
  margin-right: 12px;
}

.sidebar.collapsed .menu-icon {
  margin-right: 0;
  font-size: 24px;
}

.menu-text {
  white-space: nowrap;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #1f2937;
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  flex-shrink: 0;
}

/* 右侧容器 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* 顶部 Header */
.header {
  height: 64px;
  background-color: white;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s;
}
.collapse-btn:hover { color: #111827; }

.divider {
  height: 24px;
  width: 1px;
  background-color: #e5e7eb;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-user {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px;
  border-radius: 20px;
  transition: background 0.2s;
}
.header-user:hover { background: #f3f4f6; }

.header-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
}

/* 内容区域 */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #f9fafb;
}

.wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 进入动画 */
.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
