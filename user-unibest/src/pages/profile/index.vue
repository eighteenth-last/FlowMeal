<template>
  <view class="profile-page">
    <view class="profile-header">
      <view class="header-actions">
        <image src="/static/gear.png" class="action-icon" mode="aspectFit" />
        <image src="/static/bell.png" class="action-icon" mode="aspectFit" />
      </view>
      <view class="profile-info">
        <view class="avatar-wrap">
          <image src="/static/User.png" class="avatar-img" mode="aspectFit" />
        </view>
        <view class="user-detail">
          <text class="user-name">{{ userInfo.nickname }}</text>
          <text class="user-realname" v-if="userInfo.username">{{ userInfo.username }}</text>
          <text class="user-phone" v-if="userInfo.phone">{{ maskPhone(userInfo.phone) }}
            <image src="/static/chevronright.png" style="width:18rpx;height:18rpx;vertical-align:middle;" mode="aspectFit" />
          </text>
        </view>
      </view>
    </view>

    <view class="menu-card">
      <view class="menu-item" @click="goAddress">
        <view class="menu-icon-wrap blue">
          <image src="/static/dingwei.png" class="menu-icon" mode="aspectFit" />
        </view>
        <text class="menu-text">收货地址</text>
        <image src="/static/chevronright.png" class="menu-arrow" mode="aspectFit" />
      </view>
      <view class="menu-item">
        <view class="menu-icon-wrap orange">
          <image src="/static/star.png" class="menu-icon" mode="aspectFit" />
        </view>
        <text class="menu-text">我的评价</text>
        <image src="/static/chevronright.png" class="menu-arrow" mode="aspectFit" />
      </view>
      <view class="menu-item" style="border-bottom: none;">
        <view class="menu-icon-wrap green">
          <image src="/static/headset.png" class="menu-icon" mode="aspectFit" />
        </view>
        <text class="menu-text">我的客服</text>
        <image src="/static/chevronright.png" class="menu-arrow" mode="aspectFit" />
      </view>
    </view>

    <view class="business-entry">
      <view class="business-left">
        <view class="business-icon">
          <image src="/static/store.png" class="menu-icon" mode="aspectFit" />
        </view>
        <view class="business-info">
          <text class="business-title">商家入驻</text>
          <text class="business-sub">零门槛开店，快速盈利</text>
        </view>
      </view>
      <view class="business-btn">去申请</view>
    </view>

    <view class="logout-btn" @click="handleLogout">退出登录</view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getUserInfo } from '@/api/user'
import { logout } from '@/api/auth'

const authStore = useAuthStore()
const userInfo = ref(authStore.userInfo || {})

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
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
.profile-page { min-height: 100vh; background: #f8f9fa; padding-bottom: 120rpx; }

.profile-header {
  background: rgb(255, 209, 0);
  padding: 0 30rpx 60rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
  border-radius: 0 0 50rpx 50rpx;
}
.header-actions { display: flex; justify-content: flex-end; gap: 30rpx; padding-top: 20rpx; margin-bottom: 40rpx; }
.action-icon { width: 40rpx; height: 40rpx; }
.profile-info { display: flex; align-items: center; gap: 24rpx; }
.avatar-wrap {
  width: 120rpx; height: 120rpx; border-radius: 50%;
  border: 4rpx solid #1a1a1a; background: #fff; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
}
.avatar-img { width: 80rpx; height: 80rpx; }
.user-detail { display: flex; flex-direction: column; gap: 6rpx; }
.user-name { font-size: 40rpx; font-weight: bold; }
.user-realname { font-size: 26rpx; opacity: 0.75; }
.user-phone { font-size: 26rpx; opacity: 0.8; display: flex; align-items: center; gap: 6rpx; }

.menu-card {
  margin: 30rpx; background: #fff; border-radius: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); overflow: hidden; padding: 8rpx;
}
.menu-item { display: flex; align-items: center; padding: 28rpx 20rpx; border-bottom: 2rpx solid #f8f8f8; }
.menu-icon-wrap {
  width: 64rpx; height: 64rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; margin-right: 24rpx;
}
.menu-icon-wrap.blue { background: #eff6ff; }
.menu-icon-wrap.orange { background: #fff7ed; }
.menu-icon-wrap.green { background: #f0fdf4; }
.menu-icon { width: 32rpx; height: 32rpx; }
.menu-text { flex: 1; font-size: 28rpx; font-weight: 500; }
.menu-arrow { width: 24rpx; height: 24rpx; opacity: 0.3; }

.business-entry {
  margin: 0 30rpx 30rpx; background: #fff; border-radius: 30rpx;
  padding: 30rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  display: flex; align-items: center; justify-content: space-between;
}
.business-left { display: flex; align-items: center; gap: 20rpx; }
.business-icon {
  width: 80rpx; height: 80rpx; background: rgb(255,209,0);
  border-radius: 20rpx; display: flex; align-items: center; justify-content: center;
}
.business-info { display: flex; flex-direction: column; gap: 6rpx; }
.business-title { font-size: 28rpx; font-weight: bold; }
.business-sub { font-size: 22rpx; color: #999; }
.business-btn {
  background: #1a1a1a; color: #fff; font-size: 24rpx; font-weight: bold;
  padding: 14rpx 30rpx; border-radius: 999rpx;
}

.logout-btn {
  margin: 0 30rpx 40rpx; background: #fff; text-align: center; padding: 30rpx;
  border-radius: 24rpx; color: #ef4444; font-weight: bold; font-size: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
</style>
