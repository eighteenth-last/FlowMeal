<template>
  <view class="hall-page">
    <!-- 骑手头部 -->
    <view class="hall-header">
      <view class="header-top">
        <view class="rider-info">
          <view class="rider-avatar-sm">
            <text class="fa fa-motorcycle" style="font-size:28rpx;color:#FFD100;"></text>
          </view>
          <view>
            <text class="rider-name">{{ riderInfo.nickname || riderInfo.username || '骑手' }}</text>
            <text :class="['online-tag', isOnline ? 'tag-online' : 'tag-offline']">
              <text class="fa fa-circle" style="font-size:14rpx;margin-right:6rpx;"></text>
              {{ isOnline ? '在线接单' : '离线中' }}
            </text>
          </view>
        </view>
        <view class="online-switch" @click="toggleOnlineStatus">
          <text :class="'fa ' + (isOnline ? 'fa-toggle-on' : 'fa-toggle-off')" :style="{ fontSize: '52rpx', color: isOnline ? '#22c55e' : '#999' }"></text>
        </view>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stat-card">
      <view class="stat-item">
        <text class="stat-num">{{ todayOrders }}</text>
        <text class="stat-label text-gray">今日配送</text>
      </view>
      <view class="stat-sep"></view>
      <view class="stat-item">
        <text class="stat-num">{{ todayIncome }}</text>
        <text class="stat-label text-gray">今日收入</text>
      </view>
      <view class="stat-sep"></view>
      <view class="stat-item">
        <text class="stat-num text-primary">{{ hallOrders.length }}</text>
        <text class="stat-label text-gray">待抢订单</text>
      </view>
    </view>

    <!-- 抢单列表 -->
    <view class="section-header">
      <text class="section-title font-bold">待抢订单</text>
      <text class="text-gray" style="font-size:24rpx;" @click="loadHall">
        <text class="fa fa-rotate" style="font-size:20rpx;margin-right:6rpx;"></text>刷新
      </text>
    </view>

    <scroll-view scroll-y class="order-scroll" @scrolltolower="loadMore">
      <view class="grab-card" v-for="order in hallOrders" :key="order.id" @click="goDetail(order)">
        <view class="grab-left">
          <view class="grab-badge">
            <text class="fa fa-utensils" style="color:#FFD100;font-size:24rpx;"></text>
          </view>
        </view>
        <view class="grab-info">
          <text class="grab-no font-bold">订单 {{ order.orderNo }}</text>
          <text class="grab-addr text-gray">
            <text class="fa fa-location-dot" style="font-size:20rpx;margin-right:6rpx;"></text>
            {{ order.addressSnapshot || '配送地址' }}
          </text>
          <view class="grab-meta">
            <text class="grab-price">¥{{ order.actualAmount }}</text>
            <text class="grab-time text-gray">{{ order.createdAt }}</text>
          </view>
        </view>
        <view class="grab-action" @click.stop="handleGrab(order)">
          <text class="fa fa-hand-pointer" style="font-size:28rpx;color:#fff;"></text>
          <text style="font-size:20rpx;color:#fff;margin-top:4rpx;">抢单</text>
        </view>
      </view>

      <view v-if="!loading && hallOrders.length === 0" class="empty-tip">
        <text class="fa fa-coffee" style="font-size:64rpx;color:#ddd;"></text>
        <text class="text-gray">暂无待抢订单</text>
      </view>

      <view v-if="loading" class="loading-tip text-gray">加载中...</view>
    </scroll-view>

    <CustomTabBar :current="0" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getHallOrders, acceptOrder } from '@/api/order'
import { toggleOnline } from '@/api/rider'
import CustomTabBar from '@/components/CustomTabBar.vue'

const authStore = useAuthStore()
const riderInfo = ref(authStore.riderInfo || {})
const isOnline = ref(true)
const hallOrders = ref([])
const loading = ref(false)
const page = ref(1)
const todayOrders = ref(0)
const todayIncome = ref('0.00')

const loadHall = async () => {
  loading.value = true
  try {
    const res = await getHallOrders({ page: page.value, size: 20 })
    hallOrders.value = res?.records || []
  } catch (e) {} finally { loading.value = false }
}

const loadMore = () => { page.value++; loadHall() }

const toggleOnlineStatus = async () => {
  const newStatus = isOnline.value ? 0 : 1
  try {
    await toggleOnline(newStatus)
    isOnline.value = !isOnline.value
    uni.showToast({ title: isOnline.value ? '已上线' : '已离线', icon: 'none' })
  } catch (e) {}
}

const handleGrab = async (order) => {
  try {
    await acceptOrder(order.id)
    uni.showToast({ title: '抢单成功!', icon: 'success' })
    uni.navigateTo({ url: `/pages/delivery/index?orderId=${order.id}` })
  } catch (e) {}
}

const goDetail = (order) => {
  uni.navigateTo({ url: `/pages/delivery/index?orderId=${order.id}` })
}

onMounted(() => {
  const token = uni.getStorageSync('fm_rider_token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  loadHall()
})
</script>

<style scoped>
.hall-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 160rpx; }

.hall-header {
  background: #1a1a1a; padding: 80rpx 30rpx 40rpx;
  border-radius: 0 0 40rpx 40rpx;
}
.header-top { display: flex; align-items: center; justify-content: space-between; }
.rider-info { display: flex; align-items: center; gap: 16rpx; }
.rider-avatar-sm {
  width: 70rpx; height: 70rpx; background: #333; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.rider-name { font-size: 30rpx; font-weight: bold; color: #fff; display: block; }
.online-tag { font-size: 22rpx; display: flex; align-items: center; }
.tag-online { color: #22c55e; }
.tag-offline { color: #999; }

.stat-card {
  margin: -20rpx 30rpx 0; background: #fff; border-radius: 20rpx;
  padding: 30rpx; display: flex; align-items: center; justify-content: space-around;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06); position: relative; z-index: 10;
}
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 8rpx; }
.stat-num { font-size: 40rpx; font-weight: bold; }
.stat-label { font-size: 22rpx; }
.stat-sep { width: 2rpx; height: 60rpx; background: #f0f0f0; }

.section-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 30rpx 30rpx 16rpx;
}
.section-title { font-size: 32rpx; }

.order-scroll { padding: 0 30rpx; height: calc(100vh - 450rpx); }

.grab-card {
  display: flex; align-items: center; gap: 20rpx; background: #fff;
  border-radius: 24rpx; padding: 24rpx; margin-bottom: 20rpx;
  border-left: 8rpx solid #FFD100; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.grab-left { flex-shrink: 0; }
.grab-badge {
  width: 60rpx; height: 60rpx; background: #fffbeb; border-radius: 16rpx;
  display: flex; align-items: center; justify-content: center;
}
.grab-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; overflow: hidden; }
.grab-no { font-size: 28rpx; }
.grab-addr { font-size: 24rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.grab-meta { display: flex; align-items: center; gap: 16rpx; }
.grab-price { font-size: 30rpx; font-weight: bold; color: #ff4400; }
.grab-time { font-size: 22rpx; }

.grab-action {
  width: 90rpx; height: 90rpx; background: #1a1a1a; border-radius: 20rpx;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.empty-tip { text-align: center; padding: 120rpx 0; display: flex; flex-direction: column; align-items: center; gap: 20rpx; }
.loading-tip { text-align: center; padding: 40rpx; font-size: 26rpx; }
</style>
