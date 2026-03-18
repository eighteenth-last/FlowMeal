<template>
  <view class="order-page">
    <view class="page-header">
      <text class="header-title">我的订单</text>
      <scroll-view scroll-x class="tab-scroll">
        <view class="tab-bar">
          <view
            v-for="tab in tabs" :key="tab.value"
            :class="['tab-item', activeTab === tab.value ? 'tab-active' : '']"
            @click="switchTab(tab.value)"
          >
            <text>{{ tab.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <scroll-view scroll-y class="order-list" @scrolltolower="loadMore">
      <view class="order-card" v-for="order in orders" :key="order.id" @click="goTrack(order)">
        <view class="delivering-bar" v-if="order.status === 'DELIVERING'"></view>
        <view class="order-head">
          <view class="shop-row">
            <view class="shop-avatar-wrap">
              <image :src="order.shopAvatar || '/static/store.png'" class="shop-avatar-img" mode="aspectFill" />
            </view>
            <text class="order-shop-name">{{ order.shopName || '商家' }}</text>
            <image src="/static/chevronright.png" style="width:20rpx;height:20rpx;opacity:0.3;" mode="aspectFit" />
          </view>
          <text :class="['order-status', 'status-' + order.status]">{{ statusLabel[order.status] }}</text>
        </view>
        <view class="order-body">
          <image :src="order.firstItemImage || '/static/bowl-food.png'" class="order-item-img" mode="aspectFill" />
          <view class="order-body-info">
            <text class="order-item-name">{{ order.firstItemName }}</text>
            <text class="order-time text-gray">{{ order.createdAt }}</text>
          </view>
          <view class="order-price-wrap">
            <text class="order-price">¥{{ order.actualAmount }}</text>
          </view>
        </view>
        <view class="order-actions">
          <template v-if="order.status === 'WAIT_PAY'">
            <view class="action-btn cancel-btn" @click.stop="handleCancel(order)">取消订单</view>
            <view class="action-btn pay-btn" @click.stop="handlePay(order)">去支付</view>
          </template>
          <template v-if="order.status === 'DELIVERING'">
            <view class="action-btn cancel-btn" @click.stop="">催单</view>
            <view class="action-btn map-btn" @click.stop="goTrack(order)">查看地图</view>
          </template>
          <template v-if="order.status === 'COMPLETED'">
            <view class="action-btn cancel-btn" @click.stop="">评价</view>
            <view class="action-btn reorder-btn" @click.stop="">再来一单</view>
          </template>
        </view>
      </view>

      <view v-if="!loading && orders.length === 0" class="empty-tip">
        <text class="empty-text text-gray">暂无订单</text>
      </view>
      <view v-if="loading" class="loading-tip text-gray">加载中...</view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders, cancelOrder } from '@/api/order'

const tabs = [
  { label: '全部', value: '' },
  { label: '待支付', value: 'WAIT_PAY' },
  { label: '配送中', value: 'DELIVERING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

const statusLabel = {
  WAIT_PAY: '等待支付',
  WAIT_MERCHANT_CONFIRM: '待商家确认',
  MERCHANT_CONFIRMED: '商家已确认',
  RIDER_ACCEPTED: '骑手已接单',
  DELIVERING: '骑手配送中',
  COMPLETED: '订单已完成',
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
  } catch (e) {}
  finally { loading.value = false }
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
.text-gray { color: #999; }

.page-header {
  background: #fff; padding: 0 30rpx 0;
  padding-top: calc(var(--status-bar-height) + 20rpx);
  position: sticky; top: 0; z-index: 20;
}
.header-title { font-size: 44rpx; font-weight: bold; display: block; margin-bottom: 20rpx; margin-top: 10rpx; }

.tab-scroll { width: 100%; }
.tab-bar { display: flex; padding-bottom: 0; white-space: nowrap; }
.tab-item {
  padding: 16rpx 30rpx; font-size: 26rpx; color: #999;
  border-bottom: 4rpx solid transparent; flex-shrink: 0;
}
.tab-active { color: #1a1a1a; font-weight: bold; border-bottom-color: rgb(255,209,0); }

.order-list { padding: 20rpx 0; height: calc(100vh - 240rpx); box-sizing: border-box; }

.order-card {
  background: #fff; border-radius: 30rpx; padding: 30rpx;
  margin: 0 24rpx 20rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  border: 2rpx solid #f0f0f0; position: relative; overflow: hidden;
  width: calc(100% - 48rpx); box-sizing: border-box;
}
.delivering-bar {
  position: absolute; top: 0; left: 0; width: 8rpx; height: 100%;
  background: rgb(255,209,0);
}

.order-head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24rpx; padding-bottom: 20rpx; border-bottom: 2rpx solid #f8f8f8;
}
.shop-row { display: flex; align-items: center; gap: 14rpx; }
.shop-avatar-wrap { width: 56rpx; height: 56rpx; border-radius: 50%; overflow: hidden; background: #f0f0f0; }
.shop-avatar-img { width: 100%; height: 100%; }
.order-shop-name { font-size: 28rpx; font-weight: bold; }
.order-status { font-size: 24rpx; font-weight: bold; }
.status-WAIT_PAY { color: #ef4444; }
.status-DELIVERING { color: #3b82f6; }
.status-COMPLETED { color: #999; }
.status-CANCELLED { color: #999; }

.order-body { display: flex; align-items: center; gap: 20rpx; margin-bottom: 20rpx; }
.order-item-img { width: 120rpx; height: 120rpx; border-radius: 16rpx; flex-shrink: 0; background: #f0f0f0; }
.order-body-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
.order-item-name { font-size: 28rpx; font-weight: 500; color: #333; }
.order-time { font-size: 22rpx; }
.order-price-wrap { text-align: right; }
.order-price { font-size: 34rpx; font-weight: bold; }

.order-actions { display: flex; justify-content: flex-end; gap: 16rpx; padding-top: 20rpx; border-top: 2rpx solid #f8f8f8; }
.action-btn { padding: 14rpx 30rpx; border-radius: 999rpx; font-size: 24rpx; font-weight: 500; }
.cancel-btn { border: 2rpx solid #e0e0e0; color: #666; }
.pay-btn { background: #ef4444; color: #fff; box-shadow: 0 4rpx 12rpx rgba(239,68,68,0.3); }
.map-btn { background: rgb(255,209,0); color: #1a1a1a; }
.reorder-btn { border: 2rpx solid rgb(255,209,0); color: #d97706; }

.empty-tip { text-align: center; padding: 120rpx 0; }
.empty-text { font-size: 28rpx; }
.loading-tip { text-align: center; padding: 40rpx; font-size: 26rpx; }
</style>
