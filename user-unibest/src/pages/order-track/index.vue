<template>
  <view class="track-page">
    <!-- 顶部地图区域 -->
    <view class="map-section" v-if="order.status === 'DELIVERING' || order.status === 'RIDER_ACCEPTED'">
      <view id="trackMapContainer" class="track-map"></view>
      <view class="map-nav-back" @click="goBack">
        <text class="fa fa-chevron-left" style="font-size:28rpx;"></text>
      </view>
      <view class="map-legend">
        <view class="legend-item">
          <view class="legend-dot" style="background:#f59e0b;"></view>
          <text style="font-size:22rpx;">商家</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot" style="background:#22c55e;"></view>
          <text style="font-size:22rpx;">收货地址</text>
        </view>
      </view>
    </view>

    <!-- 顶部状态（无地图时） -->
    <view class="track-header" v-if="order.status !== 'DELIVERING' && order.status !== 'RIDER_ACCEPTED'">
      <view class="header-nav">
        <view class="nav-back" @click="goBack"><text class="fa fa-chevron-left" style="color:#000;"></text></view>
        <text class="nav-title">订单详情</text>
        <view style="width:60rpx;"></view>
      </view>
      <view class="status-icon-wrap">
        <text :class="'fa fa-' + statusIcon" style="font-size:52rpx;color:#FFD100;"></text>
      </view>
      <text class="status-text">{{ statusLabel[order.status] || '加载中' }}</text>
      <text class="status-hint text-gray" v-if="order.status === 'DELIVERING'">预计 30 分钟送达</text>
    </view>

    <!-- 配送信息 -->
    <view class="card" v-if="order.status === 'DELIVERING' || order.status === 'RIDER_ACCEPTED'">
      <view class="delivery-info">
        <view class="rider-avatar">
          <text class="fa fa-motorcycle" style="font-size:36rpx;color:#FFD100;"></text>
        </view>
        <view class="rider-detail">
          <text class="rider-name font-bold">骑手配送中</text>
          <text class="text-gray" style="font-size:24rpx;">正在火速赶往</text>
        </view>
        <view class="rider-actions">
          <view class="action-circle">
            <text class="fa fa-phone" style="font-size:28rpx;color:#22c55e;"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 订单进度 -->
    <view class="card">
      <view class="card-title">
        <text class="fa fa-timeline" style="color:#FFD100;margin-right:10rpx;"></text>
        订单进度
      </view>
      <view class="timeline">
        <view class="timeline-item" v-for="(step, idx) in timeline" :key="idx" :class="{ 'step-done': step.done }">
          <view class="step-dot"></view>
          <view class="step-content">
            <text class="step-text">{{ step.label }}</text>
            <text class="step-time text-gray" v-if="step.time">{{ step.time }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="card">
      <view class="card-title">
        <text class="fa fa-receipt" style="color:#FFD100;margin-right:10rpx;"></text>
        订单信息
      </view>
      <view class="info-row">
        <text class="text-gray">订单编号</text>
        <text>{{ order.orderNo }}</text>
      </view>
      <view class="info-row">
        <text class="text-gray">下单时间</text>
        <text>{{ order.createdAt }}</text>
      </view>
      <view class="info-row">
        <text class="text-gray">实付金额</text>
        <text class="font-bold" style="color:#ff4400;">¥{{ order.actualAmount }}</text>
      </view>
      <view class="info-row" v-if="order.remark">
        <text class="text-gray">备注</text>
        <text>{{ order.remark }}</text>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar safe-bottom" v-if="order.status === 'DELIVERING'">
      <view class="bar-btn bar-secondary">联系客服</view>
      <view class="bar-btn bar-primary">确认收货</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { getOrderDetail } from '@/api/order'
import { initMap, addMarker, fitView } from '@/utils/amap'

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

const statusIcon = computed(() => {
  const map = {
    WAIT_PAY: 'credit-card',
    WAIT_MERCHANT_CONFIRM: 'hourglass-half',
    MERCHANT_CONFIRMED: 'check-circle',
    RIDER_ACCEPTED: 'motorcycle',
    DELIVERING: 'truck-fast',
    COMPLETED: 'circle-check',
    CANCELLED: 'circle-xmark'
  }
  return map[order.value.status] || 'spinner'
})

const timeline = computed(() => {
  const o = order.value
  return [
    { label: '订单已提交', time: o.createdAt, done: true },
    { label: '商家已接单', time: o.acceptTime, done: !!o.acceptTime },
    { label: '骑手已接单', time: o.pickUpTime, done: !!o.pickUpTime },
    { label: '配送中', time: o.deliverTime, done: !!o.deliverTime },
    { label: '已送达', time: o.completeTime, done: !!o.completeTime }
  ]
})

const goBack = () => uni.navigateBack()

// 初始化配送地图
const initTrackMap = () => {
  setTimeout(() => {
    const map = initMap('trackMapContainer', { zoom: 13 })
    if (!map) return
    // 模拟商家位置和收货位置（实际应从订单数据获取坐标）
    // 默认使用示例坐标，实际数据可从 order.shopLng/shopLat, order.addressLng/addressLat 获取
    const shopLng = order.value.shopLng || 116.397428
    const shopLat = order.value.shopLat || 39.90923
    const addrLng = order.value.addressLng || 116.407428
    const addrLat = order.value.addressLat || 39.91923
    addMarker(map, shopLng, shopLat, { title: '商家' })
    addMarker(map, addrLng, addrLat, { title: '收货地址' })
    fitView(map)
  }, 200)
}

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const orderId = currentPage.options?.orderId || currentPage.$page?.options?.orderId
  if (!orderId) return
  try {
    order.value = await getOrderDetail(orderId) || {}
    // 配送中 / 骑手已接单 则显示地图
    if (order.value.status === 'DELIVERING' || order.value.status === 'RIDER_ACCEPTED') {
      await nextTick()
      initTrackMap()
    }
  } catch (e) {}
})
</script>

<style scoped>
.track-page { min-height: 100vh; background: #f8f9fa; padding-bottom: 180rpx; }

.track-header {
  background: linear-gradient(135deg, #FFD100 0%, #FFE550 100%);
  padding: 20rpx 30rpx 60rpx;
  border-radius: 0 0 60rpx 60rpx;
  display: flex; flex-direction: column; align-items: center;
}
.header-nav {
  width: 100%; display: flex; align-items: center; justify-content: space-between;
  padding-top: 60rpx; margin-bottom: 30rpx;
}
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 32rpx; font-weight: bold; }
.status-icon-wrap {
  width: 120rpx; height: 120rpx; background: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.1); margin-bottom: 20rpx;
}
.status-text { font-size: 36rpx; font-weight: bold; margin-bottom: 8rpx; }
.status-hint { font-size: 26rpx; }

.card { margin: 20rpx 30rpx; background: #fff; border-radius: 24rpx; padding: 30rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); }
.card-title { font-size: 30rpx; font-weight: bold; margin-bottom: 24rpx; display: flex; align-items: center; }

.delivery-info { display: flex; align-items: center; gap: 20rpx; }
.rider-avatar {
  width: 80rpx; height: 80rpx; background: #fffbeb; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.rider-detail { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.rider-name { font-size: 28rpx; }
.action-circle {
  width: 70rpx; height: 70rpx; border: 2rpx solid #e0e0e0; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}

.timeline { padding-left: 20rpx; }
.timeline-item {
  display: flex; align-items: flex-start; gap: 20rpx; padding-bottom: 30rpx;
  position: relative; padding-left: 30rpx;
  border-left: 4rpx solid #e8e8e8;
}
.timeline-item:last-child { border-left-color: transparent; padding-bottom: 0; }
.step-done { border-left-color: #FFD100; }
.step-dot {
  width: 20rpx; height: 20rpx; border-radius: 50%; background: #e8e8e8;
  position: absolute; left: -12rpx; top: 6rpx;
}
.step-done .step-dot { background: #FFD100; }
.step-content { display: flex; flex-direction: column; gap: 4rpx; }
.step-text { font-size: 26rpx; }
.step-time { font-size: 22rpx; }

.info-row { display: flex; justify-content: space-between; padding: 14rpx 0; font-size: 26rpx; border-bottom: 2rpx solid #fafafa; }
.info-row:last-child { border-bottom: none; }

.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0; background: #fff;
  padding: 20rpx 30rpx; display: flex; gap: 20rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
}
.bar-btn { flex: 1; text-align: center; padding: 24rpx 0; border-radius: 16rpx; font-weight: bold; font-size: 28rpx; }
.bar-secondary { background: #f5f5f5; color: #666; }
.bar-primary { background: #FFD100; color: #1a1a1a; }

/* 地图区域 */
.map-section { position: relative; height: 45vh; overflow: hidden; }
.track-map { width: 100%; height: 100%; }
.map-nav-back {
  position: absolute; top: 80rpx; left: 30rpx;
  width: 70rpx; height: 70rpx; background: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.12); z-index: 10;
}
.map-legend {
  position: absolute; bottom: 20rpx; right: 20rpx; background: rgba(255,255,255,0.95);
  border-radius: 12rpx; padding: 12rpx 16rpx; display: flex; gap: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
}
.legend-item { display: flex; align-items: center; gap: 6rpx; }
.legend-dot { width: 16rpx; height: 16rpx; border-radius: 50%; }
</style>
