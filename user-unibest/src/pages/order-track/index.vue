<template>
  <view class="track-page">
    <view class="track-header">
      <view class="header-nav">
        <view class="nav-back" @click="goBack">
          <image src="/static/chevron-left.png" style="width:28rpx;height:28rpx;" mode="aspectFit" />
        </view>
        <text class="nav-title">订单详情</text>
        <view class="nav-right">
          <image src="/static/headset.png" style="width:36rpx;height:36rpx;" mode="aspectFit" />
        </view>
      </view>
      <view class="status-icon-wrap">
        <image src="/static/truck.png" style="width:64rpx;height:64rpx;" mode="aspectFit" />
      </view>
      <text class="status-text">{{ statusLabel[order.status] }}</text>
    </view>

    <view class="content-area">
      <view class="card rider-card" v-if="order.status === 'DELIVERING' || order.status === 'RIDER_ACCEPTED'">
        <view class="rider-row">
          <image src="/static/User.png" class="rider-avatar" mode="aspectFit" />
          <view class="rider-info">
            <view class="rider-name-row">
              <text class="rider-name">{{ order.riderName }}</text>
              <view class="rider-badge">金牌骑手</view>
            </view>
            <text class="rider-hint">正在为您配送，请保持电话畅通</text>
          </view>
          <view class="rider-actions">
            <view class="action-circle green">
              <image src="/static/phone.png" style="width:28rpx;height:28rpx;" mode="aspectFit" />
            </view>
            <view class="action-circle blue">
              <image src="/static/bell.png" style="width:28rpx;height:28rpx;" mode="aspectFit" />
            </view>
          </view>
        </view>
        <view class="progress-bar">
          <view class="progress-step done">
            <view class="step-dot-lg"></view>
            <text class="step-label">已接单</text>
          </view>
          <view class="progress-line done"></view>
          <view class="progress-step done">
            <view class="step-dot-lg"></view>
            <text class="step-label">已取货</text>
          </view>
          <view class="progress-line"></view>
          <view class="progress-step">
            <view class="step-dot-lg gray"></view>
            <text class="step-label gray">送达</text>
          </view>
        </view>
      </view>

      <view class="card">
        <view class="card-shop-header">
          <text class="card-shop-name">{{ order.shopName }}</text>
          <image src="/static/chevronright.png" style="width:20rpx;height:20rpx;opacity:0.3;" mode="aspectFit" />
        </view>
        <view class="order-item-row" v-for="item in (order.items || [])" :key="item.id">
          <image :src="item.productImage || '/static/bowl-food.png'" class="item-img" mode="aspectFill" />
          <text class="item-name">{{ item.productName }}</text>
          <text class="item-qty text-gray">x {{ item.quantity }}</text>
          <text class="item-price">¥{{ item.unitPrice }}</text>
        </view>
        <view class="fee-row" v-if="order.deliveryFee">
          <text class="text-gray">配送费</text>
          <text>¥{{ order.deliveryFee }}</text>
        </view>
        <view class="total-row">
          <text class="order-no text-gray">订单编号：{{ order.orderNo }}</text>
          <view class="total-right">
            <text class="text-gray" style="font-size:22rpx;">实付</text>
            <text class="total-price">¥{{ order.actualAmount }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="bottom-bar" v-if="order.status === 'DELIVERING'">
      <view class="bar-btn bar-secondary">联系客服</view>
      <view class="bar-btn bar-primary">确认收货</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderDetail } from '@/api/order'

const order = ref({})

const statusLabel = {
  WAIT_PAY: '待支付',
  WAIT_MERCHANT_CONFIRM: '等待商家接单',
  MERCHANT_CONFIRMED: '商家已接单',
  RIDER_ACCEPTED: '骑手已接单',
  DELIVERING: '美食正在配送中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

const goBack = () => uni.navigateBack()

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const orderId = currentPage.options?.orderId || currentPage.$page?.options?.orderId
  if (!orderId) return
  try {
    order.value = await getOrderDetail(orderId) || {}
  } catch (e) {}
})
</script>

<style scoped>
.track-page { min-height: 100vh; background: #f8f9fa; padding-bottom: 180rpx; }
.text-gray { color: #999; }

.track-header {
  background: rgb(255, 209, 0);
  padding: 0 30rpx 60rpx;
  padding-top: calc(var(--status-bar-height) + 10rpx);
  border-radius: 0 0 50rpx 50rpx;
  display: flex; flex-direction: column; align-items: center;
  box-shadow: 0 4rpx 20rpx rgba(255,209,0,0.3);
}
.header-nav {
  width: 100%; display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 40rpx; padding-top: 20rpx;
}
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 32rpx; font-weight: bold; }
.nav-right { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.status-icon-wrap {
  width: 140rpx; height: 140rpx; background: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.12); margin-bottom: 24rpx;
}
.status-text { font-size: 40rpx; font-weight: bold; margin-bottom: 8rpx; }

.content-area { padding: 0 24rpx; margin-top: -30rpx; }

.card {
  background: #fff; border-radius: 30rpx; padding: 30rpx;
  margin-bottom: 20rpx; box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.04);
  border: 2rpx solid #f0f0f0;
}

.rider-row { display: flex; align-items: center; gap: 20rpx; margin-bottom: 30rpx; padding-bottom: 30rpx; border-bottom: 2rpx solid #f8f8f8; }
.rider-avatar { width: 90rpx; height: 90rpx; border-radius: 50%; background: #f0f0f0; border: 2rpx solid #e0e0e0; }
.rider-info { flex: 1; display: flex; flex-direction: column; gap: 6rpx; }
.rider-name-row { display: flex; align-items: center; gap: 12rpx; }
.rider-name { font-size: 30rpx; font-weight: bold; }
.rider-badge { background: #fffbeb; color: #d97706; font-size: 20rpx; padding: 4rpx 14rpx; border-radius: 8rpx; }
.rider-hint { font-size: 22rpx; color: #999; }
.rider-actions { display: flex; gap: 14rpx; }
.action-circle { width: 72rpx; height: 72rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.action-circle.green { background: #f0fdf4; }
.action-circle.blue { background: #eff6ff; }

.progress-bar { display: flex; align-items: center; justify-content: center; padding: 0 20rpx; }
.progress-step { display: flex; flex-direction: column; align-items: center; gap: 10rpx; }
.step-dot-lg { width: 28rpx; height: 28rpx; border-radius: 50%; background: rgb(255,209,0); box-shadow: 0 0 0 8rpx rgba(255,209,0,0.2); }
.step-dot-lg.gray { background: #e0e0e0; box-shadow: none; }
.step-label { font-size: 20rpx; font-weight: bold; color: #333; }
.step-label.gray { color: #999; }
.progress-line { flex: 1; height: 4rpx; background: rgb(255,209,0); margin: 0 8rpx; margin-bottom: 20rpx; }
.progress-line:not(.done) { background: #e0e0e0; }

.card-shop-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24rpx; padding-bottom: 20rpx; border-bottom: 2rpx solid #f8f8f8; }
.card-shop-name { font-size: 28rpx; font-weight: bold; }
.order-item-row { display: flex; align-items: center; gap: 20rpx; margin-bottom: 20rpx; }
.item-img { width: 80rpx; height: 80rpx; border-radius: 12rpx; background: #f0f0f0; }
.item-name { flex: 1; font-size: 26rpx; color: #333; }
.item-qty { font-size: 22rpx; margin-right: 10rpx; }
.item-price { font-size: 28rpx; font-weight: bold; }
.fee-row { display: flex; justify-content: space-between; padding: 12rpx 0; font-size: 26rpx; border-bottom: 2rpx solid #fafafa; }
.total-row { display: flex; justify-content: space-between; align-items: center; padding-top: 20rpx; }
.order-no { font-size: 20rpx; }
.total-right { display: flex; align-items: baseline; gap: 6rpx; }
.total-price { font-size: 40rpx; font-weight: bold; }

.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background: rgba(255,255,255,0.95);
  padding: 20rpx 30rpx 40rpx; display: flex; gap: 20rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06); backdrop-filter: blur(10px);
}
.bar-btn { flex: 1; text-align: center; padding: 28rpx 0; border-radius: 24rpx; font-weight: bold; font-size: 28rpx; }
.bar-secondary { background: #f5f5f5; color: #666; }
.bar-primary { background: rgb(255,209,0); color: #1a1a1a; box-shadow: 0 4rpx 16rpx rgba(255,209,0,0.4); }
</style>
