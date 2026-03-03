<template>
  <view class="delivery-list-page">
    <!-- Header -->
    <view class="list-header">
      <view class="header-title">配送中</view>
      <view class="header-stats">
        <text class="stat-text">在途 {{ activeOrders.length }} 单</text>
      </view>
    </view>

    <!-- Active Orders List -->
    <scroll-view scroll-y class="order-list" @scrolltolower="loadMore">
      <!-- Empty State -->
      <view v-if="!loading && activeOrders.length === 0" class="empty-state">
        <view class="empty-icon-wrap">
          <text class="fa fa-truck-fast empty-icon"></text>
        </view>
        <text class="empty-title">暂无配送订单</text>
        <text class="empty-desc">去大厅抢单后，订单将在这里显示</text>
        <view class="go-hall-btn" @click="goHall">前往抢单大厅</view>
      </view>

      <!-- Order Cards -->
      <view
        v-for="order in activeOrders"
        :key="order.id"
        class="order-card"
        @click="goDetail(order.id)"
      >
        <!-- Card Header -->
        <view class="card-header">
          <view class="order-no-wrap">
            <text class="order-no">订单 #{{ order.id }}</text>
            <view class="status-badge" :class="getStatusClass(order.status)">
              {{ statusLabel[order.status] || order.status }}
            </view>
          </view>
          <text class="order-fee">+{{ order.deliveryFee || '5.00' }}</text>
        </view>

        <!-- Route Info -->
        <view class="route-info">
          <view class="route-point">
            <view class="point-dot dot-blue"></view>
            <view class="point-content">
              <text class="point-label">取货</text>
              <text class="point-addr">FlowMeal 商家 (#{{ order.merchantId || '--' }})</text>
            </view>
          </view>
          <view class="route-connector"></view>
          <view class="route-point">
            <view class="point-dot dot-orange"></view>
            <view class="point-content">
              <text class="point-label">送达</text>
              <text class="point-addr">{{ parseAddress(order.addressSnapshot) }}</text>
            </view>
          </view>
        </view>

        <!-- Card Footer -->
        <view class="card-footer">
          <text class="footer-time">
            <text class="fa fa-clock"></text> 预计 30 分钟
          </text>
          <view class="detail-btn">
            查看详情 <text class="fa fa-chevron-right"></text>
          </view>
        </view>
      </view>

      <view v-if="loading" class="loading-row">
        <text class="fa fa-spinner fa-spin"></text> 加载中...
      </view>
    </scroll-view>

    <CustomTabBar :current="1" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getRiderOrders } from '@/api/order'
import CustomTabBar from '@/components/CustomTabBar.vue'

const activeOrders = ref([])
const loading = ref(false)
const page = ref(1)

const statusLabel = {
  MERCHANT_CONFIRMED: '待取货',
  RIDER_ACCEPTED:     '前往商家',
  DELIVERING:         '配送中',
  COMPLETED:          '已完成',
}

const getStatusClass = (status) => {
  if (status === 'DELIVERING') return 'badge-blue'
  if (status === 'RIDER_ACCEPTED') return 'badge-yellow'
  if (status === 'COMPLETED') return 'badge-green'
  return 'badge-gray'
}

const loadOrders = async (reset = false) => {
  if (loading.value) return
  if (reset) page.value = 1
  loading.value = true
  try {
    const res = await getRiderOrders({ page: page.value, size: 10 })
    const records = res.records || res || []
    if (reset) activeOrders.value = records
    else activeOrders.value = [...activeOrders.value, ...records]
  } catch (e) {
    // Mock data for UI preview
    if (reset) {
      activeOrders.value = [
        {
          id: 1501,
          merchantId: 3,
          status: 'DELIVERING',
          deliveryFee: '8.50',
          addressSnapshot: JSON.stringify({ address: '华润大厦 A座 1205' }),
        },
        {
          id: 1500,
          merchantId: 2,
          status: 'RIDER_ACCEPTED',
          deliveryFee: '6.00',
          addressSnapshot: JSON.stringify({ address: '科技园南区 B栋 305' }),
        },
      ]
    }
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  loadOrders()
}

const parseAddress = (snap) => {
  if (!snap) return '收货地址'
  try {
    const obj = JSON.parse(snap)
    return obj.address || snap
  } catch { return snap }
}

const goDetail = (orderId) => {
  uni.navigateTo({ url: `/pages/delivery/detail?orderId=${orderId}` })
}

const goHall = () => {
  uni.switchTab({ url: '/pages/hall/index' })
}

onShow(() => loadOrders(true))
</script>

<style>
.delivery-list-page {
  min-height: 100vh;
  background-color: #f9fafb;
  padding-bottom: 200rpx;
}

/* Header */
.list-header {
  background: #1a1a2e;
  padding: 100rpx 48rpx 48rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  font-size: 48rpx;
  font-weight: bold;
  color: white;
}
.stat-text {
  font-size: 24rpx;
  color: rgba(255,255,255,0.6);
  background: rgba(255,255,255,0.1);
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

/* Order List */
.order-list {
  padding: 32rpx 32rpx;
  height: calc(100vh - 260rpx);
  box-sizing: border-box;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}
.empty-icon-wrap {
  width: 160rpx;
  height: 160rpx;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.06);
}
.empty-icon { font-size: 64rpx; color: #d1d5db; }
.empty-title { font-size: 34rpx; font-weight: bold; color: #374151; margin-bottom: 12rpx; }
.empty-desc { font-size: 26rpx; color: #9ca3af; margin-bottom: 48rpx; text-align: center; }
.go-hall-btn {
  background: #1a1a2e;
  color: #FFD100;
  font-size: 28rpx;
  font-weight: bold;
  padding: 24rpx 64rpx;
  border-radius: 48rpx;
}

/* Order Card */
.order-card {
  background: white;
  border-radius: 32rpx;
  padding: 36rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.05);
  border: 1rpx solid #f3f4f6;
}
.order-card:active { transform: scale(0.99); }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}
.order-no-wrap { display: flex; align-items: center; gap: 16rpx; }
.order-no { font-size: 30rpx; font-weight: bold; color: #111827; }
.status-badge {
  font-size: 20rpx;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  font-weight: 500;
}
.badge-blue { background: #eff6ff; color: #3b82f6; }
.badge-yellow { background: #fffbeb; color: #d97706; }
.badge-green { background: #f0fdf4; color: #22c55e; }
.badge-gray { background: #f9fafb; color: #9ca3af; }
.order-fee { font-size: 34rpx; font-weight: bold; color: #ef4444; }

/* Route Info */
.route-info {
  background: #f9fafb;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 28rpx;
}
.route-point {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}
.point-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  margin-top: 8rpx;
  flex-shrink: 0;
}
.dot-blue { background: #3b82f6; box-shadow: 0 0 0 6rpx rgba(59,130,246,0.1); }
.dot-orange { background: #f97316; box-shadow: 0 0 0 6rpx rgba(249,115,22,0.1); }
.point-content { flex: 1; }
.point-label { font-size: 20rpx; color: #9ca3af; display: block; margin-bottom: 4rpx; }
.point-addr { font-size: 26rpx; font-weight: 500; color: #1f2937; }
.route-connector {
  width: 2rpx;
  height: 24rpx;
  background: #e5e7eb;
  margin: 8rpx 0 8rpx 8rpx;
}

/* Footer */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.footer-time {
  font-size: 24rpx;
  color: #9ca3af;
}
.footer-time .fa { margin-right: 6rpx; }
.detail-btn {
  font-size: 24rpx;
  color: #3b82f6;
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.detail-btn .fa { font-size: 20rpx; }

.loading-row {
  text-align: center;
  padding: 40rpx;
  color: #9ca3af;
  font-size: 26rpx;
}
.loading-row .fa { margin-right: 8rpx; }
</style>
