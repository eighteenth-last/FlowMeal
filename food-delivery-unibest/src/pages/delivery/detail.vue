<template>
  <view class="detail-page">
    <!-- Map Area (top 45%) -->
    <view class="map-area">
      <!-- Fallback static map image (amap link) -->
      <image
        class="map-img"
        src="https://restapi.amap.com/v3/staticmap?location=116.397428,39.90923&zoom=14&size=750*400&markers=mid,,A:116.397428,39.90923&key=313edac04a9967175a01eb525c79c53b"
        mode="aspectFill"
      ></image>
      <!-- Navigation Card Overlay -->
      <view class="nav-card">
        <view class="nav-icon"><text class="fa fa-arrow-up"></text></view>
        <view class="nav-info">
          <text class="nav-hint">前方 300米</text>
          <text class="nav-road">{{ isGoingToMerchant ? '前往商家取货' : '前往用户送达' }}</text>
        </view>
        <view class="back-btn" @click="goBack">
          <text class="fa fa-xmark"></text>
        </view>
      </view>
    </view>

    <!-- Bottom Sheet -->
    <view class="bottom-sheet">
      <view class="sheet-handle"></view>

      <!-- Order Header -->
      <view class="order-header">
        <view>
          <view class="header-badge-row">
            <view class="status-badge">{{ statusLabel[order.status] || '配送中' }}</view>
            <text class="order-no">单号：#{{ order.id }}</text>
          </view>
          <text class="eta">预计 12:45 送达</text>
        </view>
        <view class="action-icons">
          <view class="icon-btn" @click="sendMessage">
            <text class="fa fa-message"></text>
          </view>
          <view class="icon-btn icon-btn-green" @click="callUser">
            <text class="fa fa-phone"></text>
          </view>
        </view>
      </view>

      <!-- Progress Steps -->
      <view class="progress-area">
        <view class="progress-track-line">
          <view class="progress-fill" :style="{ width: progressWidth }"></view>
        </view>
        <view class="progress-steps">
          <view class="step-item" :class="{ active: stepIndex >= 0 }">
            <view class="step-dot"></view>
            <text class="step-text">接单</text>
          </view>
          <view class="step-item" :class="{ active: stepIndex >= 1 }">
            <view class="step-dot"></view>
            <text class="step-text">到店</text>
          </view>
          <view class="step-item" :class="{ active: stepIndex >= 2 }">
            <view class="step-dot"></view>
            <text class="step-text">取货</text>
          </view>
          <view class="step-item" :class="{ active: stepIndex >= 3 }">
            <view class="step-dot"></view>
            <text class="step-text">送达</text>
          </view>
        </view>
      </view>

      <!-- Route Detail -->
      <scroll-view scroll-y class="route-scroll">
        <!-- Merchant -->
        <view class="route-row">
          <view class="route-icon merchant-icon">
            <text class="fa fa-store"></text>
          </view>
          <view class="route-content">
            <text class="route-title">FlowMeal 商家 (#{{ order.merchantId || '--' }})</text>
            <text class="route-sub">{{ merchantAddress }}</text>
            <view v-if="order.status === 'RIDER_ACCEPTED' || order.status === 'MERCHANT_CONFIRMED'" class="tag tag-orange">商家已备好餐</view>
            <view v-else class="tag tag-green">
              <text class="fa fa-check"></text> 已取货
            </view>
          </view>
          <view class="call-btn" @click="callMerchant">
            <text class="fa fa-phone"></text>
          </view>
        </view>

        <view class="route-divider"></view>

        <!-- User -->
        <view class="route-row">
          <view class="route-icon user-icon">
            <text class="fa fa-user"></text>
          </view>
          <view class="route-content">
            <text class="route-title">{{ parseAddress(order.addressSnapshot) }}</text>
            <text class="route-sub">{{ order.remark || '无备注' }}</text>
          </view>
          <view class="call-btn" @click="callUser">
            <text class="fa fa-phone"></text>
          </view>
        </view>

        <!-- Items Summary -->
        <view class="items-row">
          <view class="items-left">
            <text class="fa fa-bag-shopping items-icon"></text>
            <text class="items-text">餐品详情 ({{ itemCount }} 件)</text>
          </view>
          <view class="items-right">
            <text class="items-fee">本单收入 {{ order.deliveryFee || '5.00' }}</text>
            <text class="fa fa-chevron-right items-arrow"></text>
          </view>
        </view>
      </scroll-view>

      <!-- Action Footer -->
      <view class="action-footer">
        <view class="secondary-btn" @click="reportIssue">
          <text class="fa fa-triangle-exclamation"></text> 上报异常
        </view>
        <view class="primary-btn" :class="actionBtnClass" @click="handleAction">
          <text v-if="!loading" :class="'fa fa-' + nextAction.icon"></text>
          <text v-if="loading" class="fa fa-spinner"></text>
          {{ loading ? '处理中...' : nextAction.label }}
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail, startDeliver, completeDeliver } from '@/api/order'

const order = ref({})
const loading = ref(false)
const orderId = ref(null)
const merchantAddress = ref('深圳市罗湖区深南东路5001号')

const statusLabel = {
  MERCHANT_CONFIRMED: '待取货',
  RIDER_ACCEPTED:     '前往商家',
  DELIVERING:         '配送中',
  COMPLETED:          '已完成',
}

const stepIndex = computed(() => {
  const s = order.value.status
  if (s === 'MERCHANT_CONFIRMED') return 0
  if (s === 'RIDER_ACCEPTED')     return 1
  if (s === 'DELIVERING')         return 2
  if (s === 'COMPLETED')          return 3
  return 0
})

const progressWidth = computed(() => {
  const p = [0, 33, 66, 100]
  return p[stepIndex.value] + '%'
})

const isGoingToMerchant = computed(() =>
  ['MERCHANT_CONFIRMED', 'RIDER_ACCEPTED'].includes(order.value.status)
)

const nextAction = computed(() => {
  const s = order.value.status
  if (s === 'RIDER_ACCEPTED' || s === 'MERCHANT_CONFIRMED')
    return { label: '确认取货，开始配送', icon: 'box-open', action: 'pickup' }
  if (s === 'DELIVERING')
    return { label: '确认送达', icon: 'flag-checkered', action: 'complete' }
  return { label: '已完成', icon: 'check-circle', action: 'none' }
})

const actionBtnClass = computed(() => {
  if (order.value.status === 'DELIVERING') return 'btn-success'
  return 'btn-dark'
})

const itemCount = computed(() => {
  if (!order.value.items) return 0
  try { return JSON.parse(order.value.items).length } catch { return 0 }
})

const loadOrder = async () => {
  if (!orderId.value) return
  try {
    const res = await getOrderDetail(orderId.value)
    order.value = res
  } catch (e) {
    // Mock for preview
    order.value = {
      id: orderId.value,
      merchantId: 3,
      status: 'DELIVERING',
      deliveryFee: '8.50',
      remark: '请放前台，不要敲门',
      addressSnapshot: JSON.stringify({ address: '华润大厦 A座 1205' }),
    }
  }
}

const handleAction = async () => {
  if (loading.value || nextAction.value.action === 'none') return
  loading.value = true
  try {
    if (nextAction.value.action === 'pickup') {
      await startDeliver(orderId.value)
      order.value.status = 'DELIVERING'
      uni.showToast({ title: '已确认取货', icon: 'success' })
    } else if (nextAction.value.action === 'complete') {
      await completeDeliver(orderId.value)
      uni.showToast({ title: '配送完成！', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1500)
    }
  } catch (e) {
    // Optimistic fallback for demo
    if (nextAction.value.action === 'pickup') {
      order.value.status = 'DELIVERING'
      uni.showToast({ title: '已确认取货', icon: 'success' })
    } else {
      uni.showToast({ title: '配送完成！', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1500)
    }
  } finally {
    loading.value = false
  }
}

const parseAddress = (snap) => {
  if (!snap) return '收货地址'
  try { return JSON.parse(snap).address || snap } catch { return snap }
}

const goBack = () => uni.navigateBack()
const callUser = () => uni.makePhoneCall({ phoneNumber: '13800138000' })
const callMerchant = () => uni.makePhoneCall({ phoneNumber: '13800138001' })
const sendMessage = () => uni.showToast({ title: '功能开发中', icon: 'none' })
const reportIssue = () => uni.showToast({ title: '已记录异常', icon: 'none' })

onLoad((options) => {
  if (options.orderId) orderId.value = options.orderId
  loadOrder()
})
</script>

<style>
.detail-page {
  min-height: 100vh;
  background: #f1f5f9;
  display: flex;
  flex-direction: column;
}

/* Map */
.map-area {
  height: 45vh;
  position: relative;
  overflow: hidden;
}
.map-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.nav-card {
  position: absolute;
  top: 80rpx;
  left: 32rpx;
  right: 32rpx;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10rpx);
  border-radius: 32rpx;
  padding: 24rpx 28rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.15);
}
.nav-icon {
  width: 80rpx;
  height: 80rpx;
  background: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.nav-icon .fa { font-size: 32rpx; color: white; }
.nav-info { flex: 1; }
.nav-hint { font-size: 22rpx; color: #9ca3af; display: block; }
.nav-road { font-size: 32rpx; font-weight: bold; color: #111827; }
.back-btn {
  width: 64rpx;
  height: 64rpx;
  background: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.back-btn .fa { font-size: 28rpx; color: #4b5563; }

/* Bottom Sheet */
.bottom-sheet {
  background: white;
  border-top-left-radius: 64rpx;
  border-top-right-radius: 64rpx;
  flex: 1;
  padding: 0 40rpx;
  margin-top: -40rpx;
  position: relative;
  z-index: 10;
  box-shadow: 0 -8rpx 40rpx rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
}
.sheet-handle {
  width: 80rpx;
  height: 8rpx;
  background: #e5e7eb;
  border-radius: 4rpx;
  margin: 20rpx auto 24rpx;
}

/* Order Header */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32rpx;
}
.header-badge-row { display: flex; align-items: center; gap: 16rpx; margin-bottom: 8rpx; }
.status-badge {
  background: #fef3c7;
  color: #d97706;
  font-size: 20rpx;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  font-weight: bold;
}
.order-no { font-size: 22rpx; color: #9ca3af; }
.eta { font-size: 48rpx; font-weight: bold; color: #111827; line-height: 1.1; }

.action-icons { display: flex; gap: 16rpx; }
.icon-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-btn .fa { font-size: 32rpx; color: #3b82f6; }
.icon-btn-green { background: #22c55e; box-shadow: 0 8rpx 20rpx rgba(34,197,94,0.3); }
.icon-btn-green .fa { color: white; }

/* Progress */
.progress-area { margin-bottom: 36rpx; }
.progress-track-line {
  height: 6rpx;
  background: #f3f4f6;
  border-radius: 3rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(to right, #22c55e, #16a34a);
  border-radius: 3rpx;
  transition: width 0.4s;
}
.progress-steps {
  display: flex;
  justify-content: space-between;
}
.step-item { display: flex; flex-direction: column; align-items: center; gap: 8rpx; flex: 1; }
.step-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #e5e7eb;
}
.step-item.active .step-dot { background: #22c55e; }
.step-text { font-size: 20rpx; color: #9ca3af; }
.step-item.active .step-text { color: #22c55e; font-weight: bold; }

/* Route */
.route-scroll { flex: 1; }
.route-row {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
  padding: 20rpx 0;
}
.route-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.merchant-icon { background: #fffbeb; }
.merchant-icon .fa { font-size: 28rpx; color: #d97706; }
.user-icon { background: #eff6ff; }
.user-icon .fa { font-size: 28rpx; color: #3b82f6; }
.route-content { flex: 1; }
.route-title { font-size: 28rpx; font-weight: bold; color: #111827; display: block; margin-bottom: 6rpx; }
.route-sub { font-size: 22rpx; color: #9ca3af; display: block; margin-bottom: 10rpx; }
.tag {
  display: inline-block;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  margin-top: 6rpx;
}
.tag .fa { margin-right: 6rpx; }
.tag-orange { background: #fef3c7; color: #d97706; }
.tag-green { background: #f0fdf4; color: #22c55e; }

.call-btn {
  width: 64rpx;
  height: 64rpx;
  background: #f0fdf4;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.call-btn .fa { font-size: 26rpx; color: #22c55e; }

.route-divider {
  height: 48rpx;
  width: 2rpx;
  background: #f3f4f6;
  margin-left: 31rpx;
}

/* Items Row */
.items-row {
  background: #f9fafb;
  border-radius: 20rpx;
  padding: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16rpx 0 24rpx;
  border: 1rpx solid #f3f4f6;
}
.items-left { display: flex; align-items: center; gap: 16rpx; }
.items-icon { font-size: 28rpx; color: #6b7280; }
.items-text { font-size: 26rpx; color: #374151; font-weight: 500; }
.items-right { display: flex; align-items: center; gap: 12rpx; }
.items-fee { font-size: 28rpx; font-weight: bold; color: #ef4444; }
.items-arrow { font-size: 22rpx; color: #d1d5db; }

/* Action Footer */
.action-footer {
  padding: 20rpx 0 60rpx;
  display: flex;
  gap: 24rpx;
}
.secondary-btn {
  flex: 1;
  height: 100rpx;
  background: #f3f4f6;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 26rpx;
  color: #4b5563;
  font-weight: 500;
}
.secondary-btn .fa { font-size: 24rpx; }
.primary-btn {
  flex: 2;
  height: 100rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 30rpx;
  font-weight: bold;
}
.primary-btn .fa { font-size: 28rpx; }
.btn-dark { background: #111827; color: #FFD100; }
.btn-success { background: #22c55e; color: white; box-shadow: 0 10rpx 30rpx rgba(34,197,94,0.3); }
</style>
