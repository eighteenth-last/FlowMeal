<template>
  <view class="profile-page">
    <!-- 个人头部 -->
    <view class="profile-header">
      <view class="profile-top-actions">
        <text class="fa fa-gear" style="font-size:36rpx;"></text>
        <text class="fa fa-bell" style="font-size:36rpx;"></text>
      </view>
      <view class="profile-info">
        <view class="avatar-wrap">
          <text class="fa fa-user" style="font-size:48rpx;color:#FFD100;"></text>
        </view>
        <view class="user-info">
          <text class="user-name">{{ userInfo.nickname || userInfo.username || '用户' }}</text>
          <text class="user-phone text-gray">{{ userInfo.phone || '' }}</text>
        </view>
        <text class="fa fa-chevron-right text-gray"></text>
      </view>
    </view>

    <!-- 订单快捷入口 -->
    <view class="order-shortcut">
      <view class="shortcut-title">
        <text class="font-bold">我的订单</text>
        <text class="text-gray" style="font-size:24rpx;" @click="goOrders('')">查看全部 <text class="fa fa-chevron-right" style="font-size:18rpx;"></text></text>
      </view>
      <view class="shortcut-grid">
        <view class="shortcut-item" @click="goOrders('WAIT_PAY')">
          <text class="fa fa-credit-card shortcut-icon"></text>
          <text class="shortcut-text">待支付</text>
        </view>
        <view class="shortcut-item" @click="goOrders('WAIT_MERCHANT_CONFIRM')">
          <text class="fa fa-hourglass-half shortcut-icon"></text>
          <text class="shortcut-text">待接单</text>
        </view>
        <view class="shortcut-item" @click="goOrders('DELIVERING')">
          <text class="fa fa-truck shortcut-icon"></text>
          <text class="shortcut-text">配送中</text>
        </view>
        <view class="shortcut-item" @click="goOrders('COMPLETED')">
          <text class="fa fa-circle-check shortcut-icon"></text>
          <text class="shortcut-text">已完成</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-card">
      <view class="menu-item" @click="goAddress">
        <text class="fa fa-location-dot menu-icon" style="color:#3b82f6;"></text>
        <text class="menu-text">收货地址</text>
        <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
      </view>
      <view class="menu-item">
        <text class="fa fa-ticket menu-icon" style="color:#f59e0b;"></text>
        <text class="menu-text">优惠券</text>
        <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
      </view>
      <view class="menu-item">
        <text class="fa fa-headset menu-icon" style="color:#22c55e;"></text>
        <text class="menu-text">客服中心</text>
        <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
      </view>
      <view class="menu-item">
        <text class="fa fa-circle-info menu-icon" style="color:#8b5cf6;"></text>
        <text class="menu-text">关于我们</text>
        <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-btn" @click="handleLogout">退出登录</view>

    <CustomTabBar :current="2" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getUserInfo } from '@/api/user'
import { logout } from '@/api/auth'
import CustomTabBar from '@/components/CustomTabBar.vue'

const authStore = useAuthStore()
const userInfo = ref(authStore.userInfo || {})

const goOrders = (status) => {
  uni.switchTab({ url: '/pages/order-list/index' })
}

const goAddress = () => {
  uni.navigateTo({ url: '/pages/address-list/index' })
}

const handleLogout = async () => {
  try { await logout() } catch (e) {}
  authStore.clearAuth()
  uni.reLaunch({ url: '/pages/login/index' })
}

onMounted(async () => {
  try {
    const info = await getUserInfo()
    if (info) userInfo.value = info
  } catch (e) {}
})
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #f8f9fa; padding-bottom: 100rpx; }

.profile-header {
  background: linear-gradient(135deg, #FFD100 0%, #FFE550 100%);
  padding: 60rpx 30rpx 80rpx;
  border-radius: 0 0 60rpx 60rpx;
}
.profile-top-actions {
  display: flex; justify-content: flex-end; gap: 30rpx; padding-top: 40rpx; margin-bottom: 30rpx;
}
.profile-info { display: flex; align-items: center; gap: 24rpx; }
.avatar-wrap {
  width: 110rpx; height: 110rpx; background: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
}
.user-info { flex: 1; display: flex; flex-direction: column; gap: 6rpx; }
.user-name { font-size: 36rpx; font-weight: bold; }
.user-phone { font-size: 24rpx; }

.order-shortcut {
  margin: -40rpx 30rpx 0; background: #fff; border-radius: 24rpx;
  padding: 30rpx; box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06); position: relative; z-index: 10;
}
.shortcut-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.shortcut-grid { display: flex; justify-content: space-around; }
.shortcut-item { display: flex; flex-direction: column; align-items: center; gap: 10rpx; }
.shortcut-icon { font-size: 40rpx; color: #1a1a1a; }
.shortcut-text { font-size: 24rpx; color: #666; }

.menu-card {
  margin: 30rpx; background: #fff; border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); overflow: hidden;
}
.menu-item {
  display: flex; align-items: center; padding: 30rpx; border-bottom: 2rpx solid #f8f8f8;
}
.menu-item:last-child { border-bottom: none; }
.menu-icon { font-size: 32rpx; width: 50rpx; text-align: center; margin-right: 20rpx; }
.menu-text { flex: 1; font-size: 28rpx; }

.logout-btn {
  margin: 40rpx 30rpx; background: #fff; text-align: center; padding: 28rpx;
  border-radius: 16rpx; color: #ef4444; font-weight: bold; font-size: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
</style>
