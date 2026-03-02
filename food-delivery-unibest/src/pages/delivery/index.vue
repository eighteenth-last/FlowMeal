<template>
  <view class="delivery-page">
    <!-- 地图导航区域 -->
    <view class="map-area">
      <view id="deliveryMapContainer" class="delivery-map"></view>
      <view class="map-back" @click="goBack">
        <text class="fa fa-chevron-left" style="font-size:28rpx;"></text>
      </view>
      <!-- 重新定位按钮 -->
      <view class="map-relocate" @click="relocate">
        <text class="fa fa-location-crosshairs" style="font-size:28rpx;color:#333;"></text>
      </view>
      <!-- 地图图例 -->
      <view class="map-legend">
        <view class="legend-item">
          <view class="legend-dot" style="background:#f59e0b;"></view>
          <text style="font-size:22rpx;">取餐</text>
        </view>
        <view class="legend-item">
          <view class="legend-dot" style="background:#22c55e;"></view>
          <text style="font-size:22rpx;">送达</text>
        </view>
      </view>
    </view>

    <!-- 订单详情卡片 -->
    <view class="detail-panel">
      <!-- 地址信息 -->
      <view class="addr-section">
        <view class="addr-row">
          <view class="dot dot-orange"></view>
          <view class="addr-info">
            <text class="addr-label text-gray">取餐地址</text>
            <text class="addr-text font-bold">商家地址</text>
          </view>
        </view>
        <view class="addr-line"></view>
        <view class="addr-row">
          <view class="dot dot-green"></view>
          <view class="addr-info">
            <text class="addr-label text-gray">送餐地址</text>
            <text class="addr-text font-bold">{{ order.addressSnapshot || '收货地址' }}</text>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="order-info-card">
        <view class="info-top">
          <text class="info-orderNo">订单 {{ order.orderNo }}</text>
          <text class="info-amount">¥{{ order.actualAmount }}</text>
        </view>
        <view class="info-rows">
          <view class="info-row">
            <text class="text-gray">下单时间</text>
            <text>{{ order.createdAt }}</text>
          </view>
          <view class="info-row" v-if="order.remark">
            <text class="text-gray">备注</text>
            <text>{{ order.remark }}</text>
          </view>
          <view class="info-row">
            <text class="text-gray">状态</text>
            <text class="font-bold text-primary">{{ statusLabel[order.status] || order.status }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-area">
        <view class="action-btn-secondary" @click="callUser">
          <text class="fa fa-phone" style="margin-right:8rpx;"></text>联系用户
        </view>
        <view class="action-btn-primary" @click="handleAction" v-if="nextAction">
          <text :class="'fa fa-' + nextAction.icon" style="margin-right:8rpx;"></text>
          {{ nextAction.label }}
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { getOrderDetail, acceptOrder, startDeliver, completeDeliver } from '@/api/order'
import { initMap, addIconMarker, addPolyline, drivingRoute, fitView, getBrowserLocation } from '@/utils/amap'

const order = ref({})
let mapInstance = null

const statusLabel = {
  MERCHANT_CONFIRMED: '待取餐',
  RIDER_ACCEPTED: '骑手已接单',
  DELIVERING: '配送中',
  COMPLETED: '已完成'
}

const nextAction = computed(() => {
  const s = order.value.status
  if (s === 'MERCHANT_CONFIRMED') return { label: '接下这单', icon: 'hand-pointer', action: 'accept' }
  if (s === 'RIDER_ACCEPTED') return { label: '已取餐，开始配送', icon: 'truck-fast', action: 'deliver' }
  if (s === 'DELIVERING') return { label: '已送达', icon: 'circle-check', action: 'complete' }
  return null
})

const handleAction = async () => {
  const action = nextAction.value?.action
  if (!action) return
  try {
    if (action === 'accept') await acceptOrder(order.value.id)
    else if (action === 'deliver') await startDeliver(order.value.id)
    else if (action === 'complete') await completeDeliver(order.value.id)
    uni.showToast({ title: '操作成功', icon: 'success' })
    await loadOrder()
  } catch (e) {}
}

const callUser = () => {
  uni.showToast({ title: '联系用户功能', icon: 'none' })
}

const goBack = () => uni.navigateBack()

// 初始化配送地图
const initDeliveryMap = async () => {
  await nextTick()
  setTimeout(async () => {
    mapInstance = initMap('deliveryMapContainer', { zoom: 13 })
    if (!mapInstance) return

    // 商家坐标（实际从订单数据获取）
    const shopLng = order.value.shopLng || 116.397428
    const shopLat = order.value.shopLat || 39.90923
    // 收货地址坐标
    const addrLng = order.value.addressLng || 116.407428
    const addrLat = order.value.addressLat || 39.91923

    // 添加取餐标记（橙色）
    addIconMarker(mapInstance, shopLng, shopLat,
      '<i class="fa fa-store" style="color:#f59e0b;font-size:18px;"></i>',
      { title: '取餐地址' })

    // 添加送餐标记（绿色）
    addIconMarker(mapInstance, addrLng, addrLat,
      '<i class="fa fa-flag-checkered" style="color:#22c55e;font-size:18px;"></i>',
      { title: '送餐地址' })

    // 尝试绘制路线
    try {
      const route = await drivingRoute(shopLng, shopLat, addrLng, addrLat)
      if (route.points.length > 0) {
        addPolyline(mapInstance, route.points, { color: '#FFD100', weight: 6 })
      }
    } catch (e) {
      // 路线规划失败，画直线
      addPolyline(mapInstance, [[shopLng, shopLat], [addrLng, addrLat]], { color: '#FFD100', weight: 4 })
    }
    fitView(mapInstance)
  }, 200)
}

// 重新定位
const relocate = async () => {
  try {
    const loc = await getBrowserLocation()
    if (mapInstance) {
      mapInstance.setCenter([loc.lng, loc.lat])
    }
  } catch (e) {
    uni.showToast({ title: '定位失败', icon: 'none' })
  }
}

const loadOrder = async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const orderId = currentPage.options?.orderId || currentPage.$page?.options?.orderId
  if (!orderId) return
  try {
    order.value = await getOrderDetail(orderId) || {}
    initDeliveryMap()
  } catch (e) {}
}

onMounted(loadOrder)
</script>

<style scoped>
.delivery-page { min-height: 100vh; background: #f5f5f5; display: flex; flex-direction: column; }

.map-area {
  height: 45vh; background: #e8eef3; position: relative;
}
.delivery-map { width: 100%; height: 100%; }
.map-placeholder { display: flex; flex-direction: column; align-items: center; }
.map-back {
  position: absolute; top: 80rpx; left: 30rpx;
  width: 70rpx; height: 70rpx; background: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1); z-index: 10;
}
.map-relocate {
  position: absolute; bottom: 20rpx; left: 30rpx;
  width: 70rpx; height: 70rpx; background: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1); z-index: 10;
}
.map-legend {
  position: absolute; bottom: 20rpx; right: 20rpx; background: rgba(255,255,255,0.95);
  border-radius: 12rpx; padding: 12rpx 16rpx; display: flex; gap: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1); z-index: 10;
}
.legend-item { display: flex; align-items: center; gap: 6rpx; }
.legend-dot { width: 16rpx; height: 16rpx; border-radius: 50%; }

.detail-panel {
  flex: 1; margin-top: -40rpx; background: #fff;
  border-radius: 40rpx 40rpx 0 0; padding: 40rpx 30rpx;
  box-shadow: 0 -8rpx 30rpx rgba(0,0,0,0.06);
}

.addr-section { margin-bottom: 30rpx; }
.addr-row { display: flex; align-items: flex-start; gap: 20rpx; }
.dot { width: 20rpx; height: 20rpx; border-radius: 50%; margin-top: 8rpx; flex-shrink: 0; }
.dot-orange { background: #f59e0b; }
.dot-green { background: #22c55e; }
.addr-info { display: flex; flex-direction: column; gap: 4rpx; }
.addr-label { font-size: 22rpx; }
.addr-text { font-size: 28rpx; }
.addr-line { width: 4rpx; height: 40rpx; background: #e8e8e8; margin-left: 8rpx; }

.order-info-card {
  background: #f8f9fa; border-radius: 20rpx; padding: 24rpx; margin-bottom: 30rpx;
}
.info-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.info-orderNo { font-size: 26rpx; font-weight: bold; }
.info-amount { font-size: 32rpx; font-weight: bold; color: #ff4400; }
.info-rows { display: flex; flex-direction: column; gap: 12rpx; }
.info-row { display: flex; justify-content: space-between; font-size: 26rpx; }

.action-area { display: flex; gap: 20rpx; }
.action-btn-secondary {
  flex: 1; text-align: center; padding: 26rpx 0; border-radius: 16rpx;
  background: #f5f5f5; color: #666; font-weight: bold; font-size: 28rpx;
}
.action-btn-primary {
  flex: 1.5; text-align: center; padding: 26rpx 0; border-radius: 16rpx;
  background: #1a1a1a; color: #FFD100; font-weight: bold; font-size: 28rpx;
}
</style>
