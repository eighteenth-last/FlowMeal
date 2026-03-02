<template>
  <n-layout has-sider style="height: 100vh">
    <!-- 侧边栏 -->
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      style="background:#18181c"
    >
      <!-- Logo区域 -->
      <div class="logo-area">
        <span class="logo-icon">🍜</span>
        <span v-if="!collapsed" class="logo-text">FlowMeal 管理</span>
      </div>

      <!-- 导航菜单 -->
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuSelect"
        :indent="18"
        style="background:#18181c"
      />
    </n-layout-sider>

    <!-- 右侧内容区 -->
    <n-layout>
      <!-- 顶部 Header -->
      <n-layout-header bordered style="height:56px;padding:0 24px;display:flex;align-items:center;justify-content:space-between;background:#fff;">
        <span style="font-size:16px;font-weight:600;color:#333">
          {{ currentPageTitle }}
        </span>
        <n-dropdown :options="userDropdown" @select="handleUserAction">
          <n-button text>
            <template #icon>
              <n-icon><PersonCircleOutline /></n-icon>
            </template>
            {{ authStore.userInfo?.username || '管理员' }}
            <n-icon size="14" style="margin-left:4px"><ChevronDownOutline /></n-icon>
          </n-button>
        </n-dropdown>
      </n-layout-header>

      <!-- 页面内容 -->
      <n-layout-content
        content-style="padding:24px;background:#f5f5f5;min-height:calc(100vh - 56px)"
      >
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
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
  PersonCircleOutline,
  ChevronDownOutline
} from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'
import { h } from 'vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const collapsed = ref(false)

const renderIcon = (icon) => () => h(NIcon, null, { default: () => h(icon) })

const menuOptions = [
  { label: '数据大盘', key: 'Dashboard',   icon: renderIcon(GridOutline) },
  { label: '用户管理', key: 'Users',       icon: renderIcon(PeopleOutline) },
  { label: '商家管理', key: 'Merchants',   icon: renderIcon(StorefrontOutline) },
  { label: '骑手管理', key: 'Riders',      icon: renderIcon(BicycleOutline) },
  { label: '原料采购', key: 'Procurement', icon: renderIcon(CartOutline) },
  { label: '营销优惠券', key: 'Coupons',  icon: renderIcon(PricetagOutline) }
]

const titleMap = {
  Dashboard:   '数据大盘',
  Users:       '用户管理',
  Merchants:   '商家管理',
  Riders:      '骑手管理',
  Procurement: '原料采购',
  Coupons:     '营销优惠券'
}

const activeKey = computed(() => route.name)
const currentPageTitle = computed(() => titleMap[route.name] || 'FlowMeal')

const userDropdown = [
  { label: '退出登录', key: 'logout' }
]

const handleMenuSelect = (key) => router.push({ name: key })

const handleUserAction = (key) => {
  if (key === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.logo-area {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 10px;
  border-bottom: 1px solid #2a2a2e;
}
.logo-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
}
.logo-text {
  color: #FFD100;
  font-size: 15px;
  font-weight: 700;
  white-space: nowrap;
}
</style>
