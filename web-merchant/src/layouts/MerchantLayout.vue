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
      <div class="logo-area">
        <span class="logo-icon">🍜</span>
        <span v-if="!collapsed" class="logo-text">FlowMeal 商家</span>
      </div>

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

    <n-layout>
      <!-- 顶部 Header -->
      <n-layout-header bordered style="height:56px;padding:0 24px;display:flex;align-items:center;justify-content:space-between;background:#fff;">
        <span style="font-size:16px;font-weight:600;color:#333">
          {{ currentPageTitle }}
        </span>
        <n-space align="center">
          <!-- 新订单提示徽章 -->
          <n-badge :value="newOrderCount" :max="99">
            <n-button text @click="router.push({ name: 'Orders' })">
              <template #icon><n-icon><NotificationsOutline /></n-icon></template>
              新订单
            </n-button>
          </n-badge>
          <n-dropdown :options="userDropdown" @select="handleUserAction">
            <n-button text>
              <template #icon><n-icon><StorefrontOutline /></n-icon></template>
              {{ authStore.userInfo?.shopName || '我的店铺' }}
              <n-icon size="14" style="margin-left:4px"><ChevronDownOutline /></n-icon>
            </n-button>
          </n-dropdown>
        </n-space>
      </n-layout-header>

      <n-layout-content
        content-style="padding:24px;background:#f5f5f5;min-height:calc(100vh - 56px)"
      >
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
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
  ChevronDownOutline
} from '@vicons/ionicons5'
import { NIcon, useMessage } from 'naive-ui'
import { h } from 'vue'

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
  ws = new WebSocket(`ws://localhost:8080/api/ws/merchant/${merchantId}?token=${token}`)
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

const renderIcon = (icon) => () => h(NIcon, null, { default: () => h(icon) })

const menuOptions = [
  { label: '订单工作台', key: 'Orders',    icon: renderIcon(ReceiptOutline) },
  { label: 'POS 收银台', key: 'POS',       icon: renderIcon(CalculatorOutline) },
  { label: '餐品管理',   key: 'Products',  icon: renderIcon(RestaurantOutline) },
  { label: '原料上报',   key: 'Materials', icon: renderIcon(NutritionOutline) },
  { label: '营业数据',   key: 'ShopData',  icon: renderIcon(BarChartOutline) }
]

const titleMap = {
  Orders:    '订单工作台',
  POS:       'POS 收银台',
  Products:  '餐品管理',
  Materials: '原料消耗上报',
  ShopData:  '营业数据'
}

const activeKey = computed(() => route.name)
const currentPageTitle = computed(() => titleMap[route.name] || 'FlowMeal')

const userDropdown = [{ label: '退出登录', key: 'logout' }]

const handleMenuSelect = (key) => {
  if (key === 'Orders') newOrderCount.value = 0
  router.push({ name: key })
}

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
.logo-icon { font-size: 24px; line-height: 1; }
.logo-text { color: #FFD100; font-size: 15px; font-weight: 700; white-space: nowrap; }
</style>
