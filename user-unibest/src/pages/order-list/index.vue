<template>
  <view class="order-page">
    <!-- 顶部 -->
    <view class="page-header">
      <text class="header-title">我的订单</text>
    </view>

    <!-- 状态标签 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs" :key="tab.value"
        :class="['tab-item', activeTab === tab.value ? 'tab-active' : '']"
        @click="switchTab(tab.value)"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view scroll-y class="order-list" @scrolltolower="loadMore">
      <view class="order-card" v-for="order in orders" :key="order.id" @click="goTrack(order)">
        <view class="order-top">
          <text class="order-shop font-bold">订单 {{ order.orderNo }}</text>
          <text :class="['order-status', 'status-' + order.status]">{{ statusLabel[order.status] || order.status }}</text>
        </view>
        <view class="order-amount">
          <text class="text-gray">实付</text>
          <text class="amount-value">¥{{ order.actualAmount }}</text>
        </view>
        <view class="order-time text-gray">
          <text class="fa fa-clock" style="font-size:20rpx;margin-right:6rpx;"></text>
          {{ order.createdAt }}
        </view>
        <view class="order-actions" v-if="order.status === 'WAIT_PAY'">
          <view class="action-btn cancel-btn" @click.stop="handleCancel(order)">取消</view>
          <view class="action-btn pay-btn" @click.stop="handlePay(order)">去支付</view>
        </view>
      </view>

      <view v-if="!loading && orders.length === 0" class="empty-tip">
        <text class="fa fa-box-open" style="font-size:64rpx;color:#ddd;"></text>
        <text class="empty-text text-gray">暂无订单</text>
      </view>

      <view v-if="loading" class="loading-tip text-gray">加载中...</view>
    </scroll-view>

    <CustomTabBar :current="1" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders, cancelOrder } from '@/api/order'
import CustomTabBar from '@/components/CustomTabBar.vue'

const tabs = [
  { label: '全部', value: '' },
  { label: '待支付', value: 'WAIT_PAY' },
  { label: '进行中', value: 'ACTIVE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const statusLabel = {
  WAIT_PAY: '待支付',
  WAIT_MERCHANT_CONFIRM: '待商家确认',
  MERCHANT_CONFIRMED: '商家已确认',
  RIDER_ACCEPTED: '骑手已接单',
  DELIVERING: '配送中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const activeTab = ref('')
const orders = ref([])
const loading = ref(false)
const page = ref(1)

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (activeTab.value) params.status = activeTab.value
    const res = await getMyOrders(params)
    orders.value = res?.records || []
  } catch (e) {} finally { loading.value = false }
}

const switchTab = (val) => {
  activeTab.value = val
  page.value = 1
  loadOrders()
}

const loadMore = () => {
  page.value++
  loadOrders()
}

const goTrack = (order) => {
  uni.navigateTo({ url: `/pages/order-track/index?orderId=${order.id}` })
}

const handleCancel = async (order) => {
  try {
    await cancelOrder(order.id, '用户取消')
    uni.showToast({ title: '已取消', icon: 'success' })
    loadOrders()
  } catch (e) {}
}

const handlePay = (order) => {
  uni.showToast({ title: '跳转支付...', icon: 'none' })
}

onMounted(() => {
  const token = uni.getStorageSync('fm_user_token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  loadOrders()
})
</script>

<style scoped>
.order-page { min-height: 100vh; background: #f8f9fa; padding-bottom: 160rpx; }
.page-header {
  padding: 80rpx 30rpx 20rpx; background: #fff;
}
.header-title { font-size: 40rpx; font-weight: bold; }

.tab-bar {
  display: flex; background: #fff; padding: 0 20rpx 20rpx;
  gap: 10rpx; overflow-x: auto;
}
.tab-item {
  padding: 14rpx 28rpx; border-radius: 999rpx; font-size: 26rpx;
  color: #666; background: #f5f5f5; flex-shrink: 0;
}
.tab-active { background: #FFD100; color: #1a1a1a; font-weight: bold; }

.order-list { padding: 20rpx 30rpx; height: calc(100vh - 260rpx); }
.order-card {
  background: #fff; border-radius: 24rpx; padding: 30rpx;
  margin-bottom: 20rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.order-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.order-shop { font-size: 28rpx; }
.order-status { font-size: 24rpx; font-weight: bold; }
.status-WAIT_PAY { color: #ff4400; }
.status-DELIVERING { color: #3b82f6; }
.status-COMPLETED { color: #22c55e; }
.status-CANCELLED { color: #999; }

.order-amount { display: flex; align-items: center; gap: 10rpx; margin-bottom: 8rpx; font-size: 26rpx; }
.amount-value { font-weight: bold; font-size: 32rpx; }
.order-time { font-size: 22rpx; margin-bottom: 16rpx; }

.order-actions { display: flex; justify-content: flex-end; gap: 16rpx; margin-top: 16rpx; border-top: 2rpx solid #f5f5f5; padding-top: 20rpx; }
.action-btn { padding: 12rpx 36rpx; border-radius: 999rpx; font-size: 24rpx; font-weight: bold; }
.cancel-btn { border: 2rpx solid #ddd; color: #666; }
.pay-btn { background: #FFD100; color: #1a1a1a; }

.empty-tip { text-align: center; padding: 120rpx 0; display: flex; flex-direction: column; align-items: center; gap: 20rpx; }
.empty-text { font-size: 28rpx; }
.loading-tip { text-align: center; padding: 40rpx; font-size: 26rpx; }
</style>
