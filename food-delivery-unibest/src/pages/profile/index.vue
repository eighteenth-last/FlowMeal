<template>
  <view class="profile-page">
    <!-- Status Toggle Header (核心业务：在线状态控制) -->
    <view class="status-header">
      <view class="header-top">
        <view class="bell-icon-wrap">
          <text class="fa fa-bell bell-icon"></text>
          <view class="red-dot"></view>
        </view>
      </view>
      
      <!-- Online Switch UI -->
      <view class="online-switch" @click="toggleStatus" :class="{ active: isOnline }">
        <text class="fa fa-power-off switch-icon"></text>
        <text class="switch-text">{{ isOnline ? '接单中' : '休息中' }}</text>
      </view>
      
      <text class="status-desc">{{ isOnline ? '点击切换为"休息"状态，将停止派单' : '点击切换为"接单"状态，开始接收新订单' }}</text>
    </view>

    <!-- Rider Info -->
    <view class="rider-card">
      <view class="rider-info-left">
        <view class="avatar-wrap">
           <image class="avatar-img" :src="riderInfo.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=rider'" mode="aspectFill"></image>
        </view>
        <view class="rider-detail">
          <text class="rider-name">{{ riderInfo.nickname || '张三师傅' }}</text>
          <view class="rider-badges">
            <view class="level-badge">王者骑手</view>
            <text class="rider-id">ID: {{ riderInfo.id || 'R88902' }}</text>
          </view>
        </view>
      </view>
      <text class="fa fa-qrcode qr-icon"></text>
    </view>

    <!-- Content Area -->
    <view class="content-area">
      <!-- Feature Grid -->
      <view class="feature-grid">
        <view class="grid-item relative" @click="goWallet">
          <view class="item-icon-blue"><text class="fa fa-wallet text-xl text-blue-500"></text></view>
          <text class="grid-label">我的钱包</text>
        </view>
        <view class="grid-item">
          <view class="item-icon-green"><text class="fa fa-map-location-dot text-xl text-green-500"></text></view>
          <text class="grid-label">常跑商圈</text>
        </view>
        <view class="grid-item">
          <view class="item-icon-orange"><text class="fa fa-motorcycle text-xl text-orange-500"></text></view>
          <text class="grid-label">车辆管理</text>
        </view>
         <view class="grid-item">
          <view class="item-icon-red"><text class="fa fa-bullhorn text-xl text-red-500"></text></view>
          <text class="grid-label">站长公告</text>
        </view>
      </view>

      <!-- Menu List -->
      <view class="menu-list">
        <view class="menu-item border-bottom">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="fa fa-clock-rotate-left text-gray"></text>
            </view>
            <text class="menu-text">配送考核</text>
          </view>
          <view class="menu-right">
             <view class="status-tag tag-green">达标</view>
             <text class="fa fa-chevron-right arrow"></text>
          </view>
        </view>

        <view class="menu-item border-bottom">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="fa fa-file-contract text-gray"></text>
            </view>
            <text class="menu-text">用户评价</text>
          </view>
          <view class="menu-right">
             <text class="right-text">好评率 99%</text>
             <text class="fa fa-chevron-right arrow"></text>
          </view>
        </view>

        <view class="menu-item" @click="handleLogout">
          <view class="menu-left">
            <view class="menu-icon-wrap">
              <text class="fa fa-gear text-gray"></text>
            </view>
            <text class="menu-text">系统设置</text>
          </view>
          <view class="menu-right">
             <text class="fa fa-chevron-right arrow"></text>
          </view>
        </view>
      </view>
    </view>

    <CustomTabBar :current="3" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRiderStore } from '@/stores/rider'
import { storeToRefs } from 'pinia'
import { toggleOnline } from '@/api/rider'
import { logout } from '@/api/auth'
import CustomTabBar from '@/components/CustomTabBar.vue'

const authStore = useAuthStore()
const riderStore = useRiderStore()
const { isOnline } = storeToRefs(riderStore)
const riderInfo = ref(authStore.riderInfo || {})

const toggleStatus = async () => {
  try {
    const nextStatus = isOnline.value ? 0 : 1
    await toggleOnline(nextStatus)
    riderStore.setOnline(!isOnline.value)
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const handleLogout = async () => {
  uni.showToast({ title: '点击了系统设置', icon: 'none' })
}

const goWallet = () => uni.showToast({ title: '功能开发中', icon: 'none' })

</script>

<style>
.profile-page {
  min-height: 100vh;
  background-color: #f9fafb;
  padding-bottom: 200rpx;
}

/* Header Area */
.status-header {
  background-color: white;
  padding: 80rpx 40rpx 120rpx; /* Increased bottom padding for overlap */
  border-bottom-left-radius: 48rpx;
  border-bottom-right-radius: 48rpx;
  border-bottom: 1rpx solid #f3f4f6;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 10;
}

.header-top {
  width: 100%;
  padding-top: 40rpx;
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20rpx;
}

.bell-icon-wrap {
  position: relative;
}
.bell-icon { font-size: 36rpx; color: #9ca3af; }
.red-dot {
  position: absolute; top: -4rpx; right: -4rpx;
  width: 16rpx; height: 16rpx; background-color: #ef4444; border-radius: 50%;
}

.online-switch {
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  background-color: #f0fdf4;
  border: 16rpx solid #ecfdf5; 
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 20rpx 50rpx rgba(34,197,94,0.1);
  transition: all 0.3s;
  margin-bottom: 30rpx;
}
/* Inactive */
.online-switch {
  background-color: white;
  border-color: #f3f4f6;
  color: #9ca3af;
  box-shadow: inset 0 4rpx 10rpx rgba(0,0,0,0.02);
}
.switch-icon { font-size: 60rpx; margin-bottom: 8rpx; }
.switch-text { font-size: 32rpx; font-weight: bold; }

/* Active State */
.online-switch.active {
  background-color: #22c55e;
  border-color: #dcfce7;
  color: white;
  box-shadow: 0 20rpx 40rpx rgba(34,197,94,0.3);
}

.status-desc { font-size: 24rpx; color: #9ca3af; margin-top: 16rpx; }

/* Rider Card */
.rider-card {
  margin: -60rpx 48rpx 0; /* Negative margin to pull up */
  background-color: #111827; /* gray-900 */
  border-radius: 32rpx;
  padding: 32rpx 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 20;
  color: white;
  box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.1);
}

.rider-info-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.avatar-wrap {
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
  background-color: white;
  padding: 4rpx;
}
.avatar-img {
  width: 100%; height: 100%; border-radius: 50%; background-color: #f3f4f6;
}

.rider-detail { display: flex; flex-direction: column; }
.rider-name { font-size: 30rpx; font-weight: bold; color: white; margin-bottom: 8rpx; }
.rider-badges { display: flex; align-items: center; gap: 12rpx; }

.level-badge {
  background: linear-gradient(to right, #facc15, #ca8a04);
  color: black;
  font-size: 18rpx;
  font-weight: bold;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
  text-transform: uppercase;
}
.rider-id { font-size: 20rpx; color: #9ca3af; }

.qr-icon { font-size: 40rpx; color: #6b7280; }

/* Content Area */
.content-area {
  padding: 50rpx 48rpx;
}

/* Grid */
.feature-grid {
  background-color: white;
  border-radius: 32rpx;
  padding: 40rpx 20rpx;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.02);
  margin-bottom: 40rpx;
  border: 1rpx solid #f9fafb;
}

.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.item-icon-blue .fa { color: #3b82f6; font-size: 40rpx; }
.item-icon-green .fa { color: #22c55e; font-size: 40rpx; }
.item-icon-orange .fa { color: #f97316; font-size: 40rpx; }
.item-icon-red .fa { color: #ef4444; font-size: 40rpx; }

.grid-label { font-size: 20rpx; color: #4b5563; }

/* Menu List */
.menu-list {
  background-color: white;
  border-radius: 32rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.02);
  border: 1rpx solid #f9fafb;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx;
}
.menu-item:active { background-color: #f9fafb; }
.border-bottom { border-bottom: 1rpx solid #f3f4f6; }

.menu-left { display: flex; align-items: center; gap: 24rpx; }
.menu-icon-wrap { width: 40rpx; display: flex; justify-content: center; }
.menu-icon-wrap .text-gray { color: #9ca3af; font-size: 32rpx; }
.menu-text { font-size: 28rpx; font-weight: 500; color: #1f2937; }

.menu-right { display: flex; align-items: center; gap: 16rpx; }
.status-tag {
  font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 8rpx;
}
.tag-green { background-color: #f0fdf4; color: #22c55e; }
.right-text { font-size: 22rpx; color: #9ca3af; }
.arrow { font-size: 20rpx; color: #e5e7eb; }

</style>
