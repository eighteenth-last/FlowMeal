<template>
  <view class="profile-page">
    <!-- 在线状态开关 -->
    <view class="profile-header">
      <view class="avatar-lg">
        <text class="fa fa-motorcycle" style="font-size:56rpx;color:#FFD100;"></text>
      </view>
      <text class="rider-name-lg">{{ riderInfo.realName || riderInfo.username || '骑手' }}</text>
      <view class="online-toggle" @click="toggleOnlineStatus">
        <text :class="'fa ' + (isOnline ? 'fa-toggle-on' : 'fa-toggle-off')" :style="{ fontSize: '60rpx', color: isOnline ? '#22c55e' : '#999' }"></text>
        <text :style="{ color: isOnline ? '#22c55e' : '#999', fontSize: '24rpx', marginTop: '6rpx' }">
          {{ isOnline ? '在线接单中' : '点击上线' }}
        </text>
      </view>
    </view>

    <!-- 骑手信息卡 -->
    <view class="info-card">
      <view class="info-item">
        <text class="fa fa-phone info-icon" style="color:#3b82f6;"></text>
        <text class="info-label">手机号</text>
        <text class="info-value">{{ riderInfo.phone || '--' }}</text>
      </view>
      <view class="info-item">
        <text class="fa fa-id-card info-icon" style="color:#f59e0b;"></text>
        <text class="info-label">身份证</text>
        <text class="info-value">{{ riderInfo.idCard || '未填写' }}</text>
      </view>
      <view class="info-item">
        <text class="fa fa-box info-icon" style="color:#22c55e;"></text>
        <text class="info-label">总配送单数</text>
        <text class="info-value font-bold">{{ riderInfo.totalOrders || 0 }}</text>
      </view>
      <view class="info-item">
        <text class="fa fa-shield info-icon" style="color:#8b5cf6;"></text>
        <text class="info-label">审核状态</text>
        <text :class="'info-value font-bold ' + auditClass">{{ auditLabel }}</text>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-card">
      <view class="menu-item" @click="goRecords">
        <text class="fa fa-clipboard-list menu-icon" style="color:#3b82f6;"></text>
        <text class="menu-text">配送记录</text>
        <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
      </view>
      <view class="menu-item">
        <text class="fa fa-wallet menu-icon" style="color:#f59e0b;"></text>
        <text class="menu-text">我的收入</text>
        <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
      </view>
      <view class="menu-item">
        <text class="fa fa-headset menu-icon" style="color:#22c55e;"></text>
        <text class="menu-text">客服帮助</text>
        <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
      </view>
      <view class="menu-item">
        <text class="fa fa-gear menu-icon" style="color:#999;"></text>
        <text class="menu-text">设置</text>
        <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
      </view>
    </view>

    <!-- 退出 -->
    <view class="logout-btn" @click="handleLogout">退出登录</view>

    <CustomTabBar :current="2" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getRiderInfo, toggleOnline } from '@/api/rider'
import { logout } from '@/api/auth'
import CustomTabBar from '@/components/CustomTabBar.vue'

const authStore = useAuthStore()
const riderInfo = ref(authStore.riderInfo || {})
const isOnline = ref(false)

const auditLabel = computed(() => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[riderInfo.value.auditStatus] || '未知'
})
const auditClass = computed(() => {
  const map = { 0: 'text-gray', 1: 'text-primary', 2: '' }
  return map[riderInfo.value.auditStatus] || ''
})

const toggleOnlineStatus = async () => {
  const newStatus = isOnline.value ? 0 : 1
  try {
    await toggleOnline(newStatus)
    isOnline.value = !isOnline.value
    riderInfo.value.onlineStatus = newStatus
  } catch (e) {}
}

const goRecords = () => {
  uni.switchTab({ url: '/pages/records/index' })
}

const handleLogout = async () => {
  try { await logout() } catch (e) {}
  authStore.clearAuth()
  uni.reLaunch({ url: '/pages/login/index' })
}

onMounted(async () => {
  try {
    const info = await getRiderInfo()
    if (info) {
      riderInfo.value = info
      isOnline.value = info.onlineStatus === 1
    }
  } catch (e) {}
})
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 100rpx; }

.profile-header {
  background: #fff; padding: 80rpx 30rpx 50rpx;
  display: flex; flex-direction: column; align-items: center;
  border-bottom: 2rpx solid #f0f0f0;
}
.avatar-lg {
  width: 140rpx; height: 140rpx; background: #1a1a1a; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 16rpx; margin-top: 30rpx;
}
.rider-name-lg { font-size: 36rpx; font-weight: bold; margin-bottom: 20rpx; }
.online-toggle { display: flex; flex-direction: column; align-items: center; }

.info-card {
  margin: -20rpx 30rpx 0; background: #1a1a1a; border-radius: 20rpx;
  padding: 24rpx; color: #fff; box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.1);
  position: relative; z-index: 10;
}
.info-item {
  display: flex; align-items: center; padding: 18rpx 0;
  border-bottom: 2rpx solid rgba(255,255,255,0.1);
}
.info-item:last-child { border-bottom: none; }
.info-icon { font-size: 28rpx; width: 50rpx; text-align: center; margin-right: 16rpx; }
.info-label { flex: 1; font-size: 26rpx; color: rgba(255,255,255,0.6); }
.info-value { font-size: 26rpx; }

.menu-card {
  margin: 30rpx; background: #fff; border-radius: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); overflow: hidden;
}
.menu-item {
  display: flex; align-items: center; padding: 30rpx;
  border-bottom: 2rpx solid #f8f8f8;
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
