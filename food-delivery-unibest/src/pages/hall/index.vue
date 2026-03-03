<template>
  <view class="hall-page">
    <!-- Header -->
    <view class="hall-header">
      <view class="header-top">
        <view class="status-toggle" @click="toggleOnlineStatus">
          <view class="toggle-track" :class="{ active: isOnline }">
            <view class="toggle-thumb">
              <text class="fa fa-power-off"></text>
            </view>
          </view>
          <text class="status-text">{{ isOnline ? '听单中' : '休息中' }}</text>
        </view>
        <view class="online-time">
          <text class="fa fa-clock"></text> 在线 4h 32m
        </view>
      </view>

      <view class="stats-row">
        <view class="stat-item">
          <text class="stat-label">今日完成</text>
          <text class="stat-val">{{ todayOrders }} <text class="unit">单</text></text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-label">预计收入</text>
          <text class="stat-val income">¥{{ todayIncome }}</text>
        </view>
      </view>
      
      <!-- Searching Animation -->
      <view v-if="isOnline" class="searching-bar">
        <text class="fa fa-radar pulse"></text>
        <text>正在为您寻找附近的订单...</text>
      </view>
    </view>

    <!-- Order Pool -->
    <scroll-view scroll-y class="order-pool" @scrolltolower="loadMore">
      <view class="pool-header">
        <text class="pool-title">待抢订单 ({{ hallOrders.length }})</text>
        <view class="pool-filter">
          <text>距离最近</text>
          <text class="fa fa-chevron-down"></text>
        </view>
      </view>

      <view class="order-card" v-for="order in hallOrders" :key="order.id" @click="handleGrab(order)">
        <view class="card-top">
          <view class="tags">
            <view class="tag tag-blue">
              <text class="fa fa-location-arrow"></text> {{ (order.distance / 1000).toFixed(1) }}km
            </view>
            <view class="tag tag-orange">
              <text class="fa fa-clock"></text> 45分钟内
            </view>
          </view>
          <view class="price">
            <text class="currency">¥</text>
            <text class="amount">{{ order.deliveryFee || '5.0' }}</text>
          </view>
        </view>
        
        <view class="card-body">
          <view class="route-line">
            <view class="point">
              <view class="dot dot-yellow"></view>
              <view class="addr-info">
                <text class="addr-title">FlowMeal 商家 (#{{ order.merchantId || 1 }})</text>
                <text class="addr-desc">距离您 1.2km</text>
              </view>
            </view>
            
            <view class="point">
              <view class="dot dot-black"></view>
              <view class="addr-info">
                <text class="addr-title">{{ parseAddress(order.addressSnapshot) }}</text>
                <text class="addr-desc">包含 {{ order.items_count || 3 }} 件商品</text>
              </view>
            </view>
          </view>
        </view>

        <button class="grab-btn" @click.stop="handleGrab(order)">
          立即抢单 <text class="fa fa-bolt"></text>
        </button>
      </view>

      <view v-if="!loading && hallOrders.length === 0" class="empty-state">
         暂无待抢订单
      </view>
      <view v-if="loading" class="loading-state">加载中...</view>
    </scroll-view>

    <CustomTabBar :current="0" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRiderStore } from '@/stores/rider'
import { storeToRefs } from 'pinia'
import { getHallOrders, acceptOrder } from '@/api/order'
import { toggleOnline } from '@/api/rider'
import CustomTabBar from '@/components/CustomTabBar.vue'

const authStore = useAuthStore()
const riderStore = useRiderStore()
const { isOnline } = storeToRefs(riderStore)
const riderInfo = ref(authStore.riderInfo || {})
const hallOrders = ref([])
const loading = ref(false)
const page = ref(1)
const todayOrders = ref(12) 
const todayIncome = ref(186.5) 

const loadHall = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getHallOrders({ page: page.value, size: 10 })
    if (page.value === 1) hallOrders.value = res.records || []
    else hallOrders.value = [...hallOrders.value, ...res.records || []]
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  loadHall()
}

const toggleOnlineStatus = async () => {
  try {
    const nextStatus = isOnline.value ? 0 : 1
    await toggleOnline(nextStatus)
    riderStore.setOnline(!isOnline.value)
  } catch (e) {
    uni.showToast({ title: '切换状态失败', icon: 'none' })
  }
}

const handleGrab = async (order) => {
  try {
    await acceptOrder(order.id)
    uni.showToast({ title: '抢单成功', icon: 'success' })
    hallOrders.value = hallOrders.value.filter(o => o.id !== order.id)
    
    // Navigate to delivery list, then auto-open detail
    setTimeout(() => {
      uni.switchTab({ url: '/pages/delivery/index' })
      setTimeout(() => {
        uni.navigateTo({ url: `/pages/delivery/detail?orderId=${order.id}` })
      }, 300)
    }, 800)
  } catch (e) {
    uni.showToast({ title: '抢单失败', icon: 'none' })
  }
}

const parseAddress = (addr) => {
  if (!addr) return '未知地址'
  try {
    const obj = JSON.parse(addr)
    return (obj.address || '') + ' ' + (obj.detail || '')
  } catch (e) {
    return addr
  }
}

onMounted(() => {
  loadHall()
})
</script>

<style>
.hall-page {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding-bottom: 200rpx;
}

.hall-header {
  background-color: #111827;
  color: white;
  padding: 40rpx 40rpx 80rpx;
  border-bottom-left-radius: 60rpx;
  border-bottom-right-radius: 60rpx;
  position: relative;
  z-index: 10;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.status-toggle {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background-color: #1f2937;
  padding: 8rpx 24rpx 8rpx 8rpx;
  border-radius: 999rpx;
}

.toggle-track {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #4b5563;
  transition: all 0.3s;
}

.toggle-track.active {
  background-color: #22c55e;
}

.toggle-thumb .fa {
  font-size: 24rpx;
  color: white;
}

.status-text {
  font-size: 28rpx;
  font-weight: bold;
}

.online-time {
  font-size: 24rpx;
  color: #9ca3af;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin-bottom: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}
.stat-label {
  font-size: 24rpx;
  color: #9ca3af;
}
.stat-val {
  font-size: 40rpx;
  font-weight: bold;
}
.stat-val.income {
  color: #facc15;
}
.unit {
  font-size: 24rpx;
  font-weight: normal;
}
.stat-divider {
  width: 2rpx;
  height: 60rpx;
  background-color: #374151;
}

.searching-bar {
  margin-top: 30rpx;
  background-color: rgba(31, 41, 55, 0.8);
  border-radius: 20rpx;
  padding: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  font-size: 24rpx;
  color: #4ade80;
  border: 1rpx solid #374151;
}

.order-pool {
  padding: 0 40rpx;
  margin-top: -60rpx;
  position: relative;
  z-index: 20;
  height: calc(100vh - 300rpx);
}

.pool-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  padding: 0 10rpx;
}
.pool-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #111827;
}
.pool-filter {
  font-size: 24rpx;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.order-card {
  background-color: white;
  border-radius: 40rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40rpx;
}

.tags {
  display: flex;
  gap: 16rpx;
}
.tag {
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.tag-blue { background-color: #eff6ff; color: #3b82f6; }
.tag-orange { background-color: #fff7ed; color: #f97316; }

.price .currency { font-size: 28rpx; font-weight: bold; }
.price .amount { font-size: 40rpx; font-weight: bold; color: #111827; }

.route-line {
  position: relative;
  padding-left: 30rpx;
  border-left: 2rpx dashed #e5e7eb;
  margin-left: 20rpx;
  margin-bottom: 40rpx;
}

.point {
  position: relative;
  margin-bottom: 40rpx;
}
.point:last-child { margin-bottom: 0; }

.dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  position: absolute;
  left: -40rpx;
  top: 10rpx;
  border: 4rpx solid white;
  box-shadow: 0 0 0 2rpx rgba(0,0,0,0.1);
}
.dot-yellow { background-color: #facc15; }
.dot-black { background-color: #111827; }

.addr-info {
  display: flex;
  flex-direction: column;
}
.addr-title { font-size: 28rpx; font-weight: bold; color: #111827; margin-bottom: 4rpx; display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 1; overflow: hidden; }
.addr-desc { font-size: 22rpx; color: #6b7280; }

.grab-btn {
  background-color: #111827;
  color: white;
  width: 100%;
  padding: 24rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
}
.grab-btn:active { opacity: 0.9; transform: scale(0.98); }

.empty-state, .loading-state {
  text-align: center;
  color: #9ca3af;
  padding: 40rpx;
  font-size: 26rpx;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}
.pulse { animation: pulse 2s infinite; }
</style>
