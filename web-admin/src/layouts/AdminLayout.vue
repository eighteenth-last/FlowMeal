<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'collapsed': collapsed }">
      <!-- Logo区 -->
      <div class="logo-area">
        <div class="logo-icon-wrap">
          <span class="logo-icon">🍜</span>
        </div>
        <span v-if="!collapsed" class="logo-text text-fade">智慧外卖中枢</span>
      </div>

      <!-- 用户信息展示 -->
      <div class="user-profile" v-if="!collapsed">
        <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=admin1" class="profile-avatar" />
        <div class="profile-info text-fade">
          <p class="profile-name">超级管理员</p>
          <p class="profile-role">👑 平台最高权限</p>
        </div>
      </div>

      <!-- 菜单列表 -->
      <nav class="nav-menu">
        <router-link
          v-for="item in menuOptions"
          :key="item.key"
          :to="{ name: item.key }"
          class="menu-item"
          :class="{ 'active': activeKey === item.key }"
        >
          <n-icon size="20" class="menu-icon"><component :is="item.iconComponent" /></n-icon>
          <span v-if="!collapsed" class="menu-text text-fade">{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- 底部 -->
      <div class="sidebar-footer" v-if="!collapsed">
        v2.5.0 FlowMeal System
      </div>
    </aside>

    <!-- 主体区域 -->
    <div class="main-container">
      <!-- 顶部导航条 -->
      <header class="header">
        <div class="header-left">
          <button class="toggle-btn" @click="collapsed = !collapsed">
            <n-icon size="24"><MenuOutline /></n-icon>
          </button>
          <div class="divider"></div>
          <span class="page-title">{{ currentPageTitle }}</span>
        </div>

        <div class="header-right">
          <div class="bell-btn cursor-pointer">
            <n-icon size="22"><NotificationsOutline /></n-icon>
            <span class="badge">3</span>
          </div>
          
          <n-dropdown :options="userDropdown" @select="handleUserAction">
            <div class="header-avatar cursor-pointer">
              <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=admin1" class="avatar-img" />
            </div>
          </n-dropdown>
        </div>
      </header>

      <!-- 页面内容路由容器 -->
      <main class="content-area">
        <div class="content-wrapper">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  GridOutline,
  PeopleOutline,
  StorefrontOutline,
  BicycleOutline,
  CartOutline,
  PricetagOutline,
  MenuOutline,
  NutritionOutline,
  NotificationsOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const collapsed = ref(false)

const menuOptions = [
  { label: '全局数据大盘', key: 'Dashboard',   iconComponent: GridOutline },
  { label: '原料集中采购', key: 'Procurement', iconComponent: CartOutline },
  { label: '供应商管理',   key: 'Suppliers',  iconComponent: StorefrontOutline },
  { label: '原料消耗上报', key: 'Materials', iconComponent: NutritionOutline },
  { label: '营销与优惠券', key: 'Coupons',     iconComponent: PricetagOutline },
  { label: 'C端用户管理',  key: 'Users',       iconComponent: PeopleOutline },
  { label: '商家审核与管理', key: 'Merchants', iconComponent: StorefrontOutline },
  { label: '骑手调度与管理', key: 'Riders',    iconComponent: BicycleOutline }
]

const titleMap = {
  Dashboard:   '平台数据大盘',
  Users:       'C端用户管理',
  Merchants:   '商家审核与管理',
  Riders:      '骑手调度管理',
  Procurement: '平台集中采购中心',
  Suppliers:   '供应商管理',
  Materials:   '原料消耗上报',
  Coupons:     '营销与优惠券管理'
}

const activeKey = computed(() => route.name)
const currentPageTitle = computed(() => titleMap[route.name] || '管理后台')

const userDropdown = [
  { label: '退出登录', key: 'logout' }
]

const handleUserAction = (key) => {
  if (key === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.layout-container {
  display: flex;
  height: 100vh;
  width: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  background-color: #f3f4f6;
  overflow: hidden;
  color: #1f2937;
}

/* 侧边栏 */
.sidebar {
  width: 256px;
  background-color: #111827;
  color: white;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  z-index: 20;
  flex-shrink: 0;
}
.sidebar.collapsed {
  width: 80px;
}

/* Logo区域 */
.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #1f2937;
  background-color: #030712;
  overflow: hidden;
}
.sidebar.collapsed .logo-area {
  justify-content: center;
  padding: 0;
}
.logo-icon-wrap {
  width: 32px;
  height: 32px;
  background-color: #FFD100;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: black;
  font-weight: bold;
  font-size: 20px;
  margin-right: 12px;
  box-shadow: 0 10px 15px -3px rgba(255, 209, 0, 0.2);
  flex-shrink: 0;
}
.sidebar.collapsed .logo-icon-wrap {
  margin-right: 0;
}
.logo-text {
  font-weight: 700;
  font-size: 18px;
  letter-spacing: 0.025em;
  white-space: nowrap;
}

/* 用户信息 */
.user-profile {
  padding: 24px;
  border-bottom: 1px solid #1f2937;
  display: flex;
  align-items: center;
  gap: 16px;
}
.profile-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #1f2937;
  border: 2px solid #FFD100;
  padding: 2px;
  object-fit: cover;
}
.profile-info {
  white-space: nowrap;
}
.profile-name {
  font-size: 14px;
  font-weight: 700;
  margin: 0;
}
.profile-role {
  font-size: 10px;
  color: #fbbf24;
  margin: 4px 0 0;
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
.nav-menu::-webkit-scrollbar { width: 4px; }
.nav-menu::-webkit-scrollbar-thumb { background: #374151; border-radius: 4px; }
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 12px;
  color: #9ca3af;
  text-decoration: none;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
}
.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: 12px 0;
}
.menu-item:hover {
  background-color: #1f2937;
  color: white;
}
.menu-item.active {
  background-color: #FFD100;
  color: black;
  font-weight: 700;
  box-shadow: 0 4px 6px -1px rgba(255, 209, 0, 0.2);
}
.menu-icon {
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}
.menu-text {
  margin-left: 12px;
  font-size: 14px;
}

/* 底部 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #1f2937;
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  white-space: nowrap;
}

/* 主体区域 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* 顶部导航 */
.header {
  height: 64px;
  background-color: white;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  z-index: 10;
  flex-shrink: 0;
}
.header-left, .header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.toggle-btn {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: color 0.2s;
}
.toggle-btn:hover { color: black; }
.divider {
  height: 24px;
  width: 1px;
  background-color: #e5e7eb;
}
.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

.bell-btn {
  position: relative;
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}
.bell-btn:hover { color: black; }
.badge {
  position: absolute;
  top: -4px;
  right: -6px;
  width: 16px;
  height: 16px;
  background-color: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  border: 2px solid white;
}
.header-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #e5e7eb;
  border: 1px solid #d1d5db;
  overflow: hidden;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 页面内容 */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
.content-area::-webkit-scrollbar { width: 6px; }
.content-area::-webkit-scrollbar-track { background: #f3f4f6; }
.content-area::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 4px; }
.content-wrapper {
  animation: fadeIn 0.3s ease-in-out;
  height: 100%;
}

.cursor-pointer { cursor: pointer; }
.text-fade {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
