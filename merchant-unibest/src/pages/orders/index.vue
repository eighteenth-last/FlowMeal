<template>
  <view class="orders-page">
    <!-- 顶部 Header -->
    <view class="page-header">
      <view class="header-left">
        <text class="shop-name font-bold">{{ shopName }}</text>
        <view class="status-dot" :class="isOpen ? 'dot-green' : 'dot-gray'">
          <text class="fa fa-circle" style="font-size:12rpx;margin-right:6rpx;"></text>
          <text>{{ isOpen ? '营业中' : '休息中' }}</text>
        </view>
      </view>
      <view class="header-right">
        <view class="new-order-badge" v-if="newCount > 0">
          <text class="fa fa-bell" style="font-size:32rpx;color:#ef4444;"></text>
          <text class="badge-num">{{ newCount }}</text>
        </view>
      </view>
    </view>

    <!-- 预警横幅 -->
    <view class="alert-banner" v-if="newCount > 0">
      <view class="alert-left">
        <text class="fa fa-triangle-exclamation" style="color:#ef4444;"></text>
        <text style="color:#ef4444;font-size:24rpx;font-weight:bold;">{{ newCount }} 笔新订单待处理!</text>
      </view>
      <text class="alert-btn">查看</text>
    </view>

    <!-- 状态标签 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs" :key="tab.value"
        :class="['tab-item', activeTab === tab.value ? 'tab-active' : '']"
        @click="switchTab(tab.value)"
      >
        <text>{{ tab.label }}</text>
        <text v-if="tab.count > 0" class="tab-count">{{ tab.count }}</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view scroll-y class="order-scroll" @scrolltolower="loadMore">
      <view class="order-card" v-for="order in filteredOrders" :key="order.id">
        <!-- 状态角标 -->
        <view class="order-corner" :class="'corner-' + order.status">
          {{ statusConfig[order.status]?.corner || '' }}
        </view>

        <!-- 订单号+时间 -->
        <view class="order-head">
          <text class="order-no font-bold">#{{ order.orderNo }}</text>
          <text class="order-time text-gray">{{ order.createdAt }}</text>
        </view>

        <!-- 商品列表 -->
        <view class="item-list">
          <view class="item-row" v-for="(item, idx) in (order.items || []).slice(0, 3)" :key="idx">
            <text class="item-name">{{ item.productName }}</text>
            <text class="text-gray">x{{ item.quantity }}</text>
          </view>
          <text v-if="(order.items || []).length > 3" class="text-gray" style="font-size:22rpx;">
            ...共{{ order.items.length }}件
          </text>
        </view>

        <!-- 备注 -->
        <view class="remark-row" v-if="order.remark">
          <text class="fa fa-comment-dots" style="color:#f59e0b;font-size:22rpx;"></text>
          <text style="font-size:24rpx;color:#f59e0b;">{{ order.remark }}</text>
        </view>

        <!-- 金额+操作 -->
        <view class="order-footer">
          <view class="order-price">
            <text class="text-gray" style="font-size:22rpx;">实付</text>
            <text class="price-value font-bold">¥{{ order.actualAmount }}</text>
          </view>
          <view class="order-actions">
            <view class="action-reject" v-if="order.status === 'WAIT_MERCHANT_CONFIRM'" @click="handleReject(order)">
              <text class="fa fa-xmark" style="font-size:24rpx;"></text> 拒单
            </view>
            <view class="action-accept" v-if="order.status === 'WAIT_MERCHANT_CONFIRM'" @click="handleAccept(order)">
              <text class="fa fa-check" style="font-size:24rpx;"></text> 接单
            </view>
          </view>
        </view>
      </view>

      <view v-if="!loading && filteredOrders.length === 0" class="empty-tip">
        <text class="fa fa-inbox" style="font-size:64rpx;color:#ddd;"></text>
        <text class="text-gray">暂无订单</text>
      </view>
      <view v-if="loading" class="loading-tip text-gray">加载中...</view>
    </scroll-view>

    <!-- 拒单弹窗 -->
    <view class="mask" v-if="showReject" @click="showReject = false">
      <view class="reject-modal" @click.stop>
        <text class="modal-title">拒单原因</text>
        <textarea v-model="rejectReason" placeholder="请输入拒单原因" class="reject-input" :maxlength="200" />
        <view class="reject-actions">
          <view class="reject-cancel" @click="showReject = false">取消</view>
          <view class="reject-confirm" @click="confirmReject">确认拒单</view>
        </view>
      </view>
    </view>

    <CustomTabBar :current="0" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMerchantOrders, acceptOrder, rejectOrder } from '@/api/order'
import { getMyShop } from '@/api/shop'
import { useAuthStore } from '@/stores/auth'
import CustomTabBar from '@/components/CustomTabBar.vue'

const authStore = useAuthStore()
const shopName = ref('我的店铺')
const isOpen = ref(true)
const orders = ref([])
const loading = ref(false)
const page = ref(1)
const activeTab = ref('WAIT_MERCHANT_CONFIRM')

// 拒单
const showReject = ref(false)
const rejectReason = ref('')
const rejectingOrder = ref(null)

const statusConfig = {
  WAIT_MERCHANT_CONFIRM: { corner: '等待接单', label: '新订单' },
  MERCHANT_CONFIRMED: { corner: '待出餐', label: '待出餐' },
  RIDER_ACCEPTED: { corner: '骑手已接', label: '配送中' },
  DELIVERING: { corner: '配送中', label: '配送中' },
  COMPLETED: { corner: '已完成', label: '已完成' },
  CANCELLED: { corner: '已取消', label: '已取消' }
}

const tabs = computed(() => [
  { value: 'WAIT_MERCHANT_CONFIRM', label: '新订单', count: orders.value.filter(o => o.status === 'WAIT_MERCHANT_CONFIRM').length },
  { value: 'MERCHANT_CONFIRMED', label: '待出餐', count: orders.value.filter(o => o.status === 'MERCHANT_CONFIRMED').length },
  { value: 'DELIVERING', label: '配送中', count: orders.value.filter(o => ['RIDER_ACCEPTED', 'DELIVERING'].includes(o.status)).length },
  { value: 'ALL', label: '全部', count: 0 }
])

const newCount = computed(() => orders.value.filter(o => o.status === 'WAIT_MERCHANT_CONFIRM').length)

const filteredOrders = computed(() => {
  if (activeTab.value === 'ALL') return orders.value
  if (activeTab.value === 'DELIVERING') return orders.value.filter(o => ['RIDER_ACCEPTED', 'DELIVERING'].includes(o.status))
  return orders.value.filter(o => o.status === activeTab.value)
})

const switchTab = (val) => { activeTab.value = val }

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getMerchantOrders({ page: page.value, size: 50 })
    orders.value = res?.records || res || []
  } catch (e) {} finally { loading.value = false }
}

const loadMore = () => { page.value++; loadOrders() }

const handleAccept = async (order) => {
  try {
    await acceptOrder(order.id)
    uni.showToast({ title: '已接单', icon: 'success' })
    loadOrders()
  } catch (e) {}
}

const handleReject = (order) => {
  rejectingOrder.value = order
  rejectReason.value = ''
  showReject.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    return uni.showToast({ title: '请填写拒单原因', icon: 'none' })
  }
  try {
    await rejectOrder(rejectingOrder.value.id, rejectReason.value)
    uni.showToast({ title: '已拒单', icon: 'success' })
    showReject.value = false
    loadOrders()
  } catch (e) {}
}

// WebSocket 实时推送
const connectWs = () => {
  const info = authStore.merchantInfo
  const merchantId = info?.id || info?.merchantId
  const token = uni.getStorageSync('fm_merchant_token')
  if (!merchantId || !token) return

  const wsUrl = `ws://localhost:8080/api/ws/merchant/${merchantId}?token=${token}`
  const ws = uni.connectSocket({ url: wsUrl, complete: () => {} })
  uni.onSocketMessage((res) => {
    try {
      const data = JSON.parse(res.data)
      if (data.type === 'NEW_ORDER') {
        uni.showToast({ title: '新订单来了!', icon: 'none' })
        loadOrders()
      }
    } catch (e) {}
  })
}

onMounted(async () => {
  const token = uni.getStorageSync('fm_merchant_token')
  if (!token) { uni.reLaunch({ url: '/pages/login/index' }); return }
  try {
    const shop = await getMyShop()
    if (shop) {
      shopName.value = shop.name || '我的店铺'
      isOpen.value = shop.status === 1
    }
  } catch (e) {}
  loadOrders()
  connectWs()
})
</script>

<style scoped>
.orders-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 130rpx; }

.page-header {
  background: #1a1a1a; padding: 80rpx 30rpx 30rpx;
  display: flex; justify-content: space-between; align-items: center;
}
.shop-name { font-size: 36rpx; color: #fff; display: block; }
.status-dot { font-size: 22rpx; display: flex; align-items: center; margin-top: 6rpx; }
.dot-green { color: #22c55e; }
.dot-gray { color: #999; }
.new-order-badge { position: relative; }
.badge-num {
  position: absolute; top: -12rpx; right: -16rpx;
  background: #ef4444; color: #fff; font-size: 18rpx;
  min-width: 30rpx; height: 30rpx; line-height: 30rpx;
  text-align: center; border-radius: 50%; padding: 0 6rpx;
}

.alert-banner {
  margin: 16rpx 30rpx 0; background: #fef2f2; border: 2rpx solid #fecaca;
  border-radius: 16rpx; padding: 20rpx 24rpx;
  display: flex; justify-content: space-between; align-items: center;
}
.alert-left { display: flex; align-items: center; gap: 10rpx; }
.alert-btn {
  background: #ef4444; color: #fff; font-size: 20rpx;
  padding: 6rpx 16rpx; border-radius: 8rpx; font-weight: bold;
}

.tab-bar {
  display: flex; gap: 0; padding: 0 30rpx; margin-top: 20rpx;
  background: #fff; border-radius: 16rpx 16rpx 0 0; margin-left: 30rpx; margin-right: 30rpx;
}
.tab-item {
  flex: 1; text-align: center; padding: 24rpx 0; font-size: 26rpx; color: #999;
  border-bottom: 4rpx solid transparent; position: relative;
  display: flex; align-items: center; justify-content: center; gap: 6rpx;
}
.tab-active { color: #1a1a1a; font-weight: bold; border-bottom-color: #FFD100; }
.tab-count {
  background: #ef4444; color: #fff; font-size: 18rpx;
  min-width: 28rpx; height: 28rpx; line-height: 28rpx;
  text-align: center; border-radius: 50%; padding: 0 6rpx;
}

.order-scroll { padding: 0 30rpx; height: calc(100vh - 380rpx); }

.order-card {
  background: #fff; border-radius: 20rpx; padding: 24rpx;
  margin-bottom: 16rpx; position: relative; overflow: hidden;
  border-left: 8rpx solid #FFD100;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.order-corner {
  position: absolute; top: 0; right: 0;
  font-size: 20rpx; padding: 6rpx 16rpx;
  border-radius: 0 0 0 12rpx; font-weight: bold;
}
.corner-WAIT_MERCHANT_CONFIRM { background: #fef2f2; color: #ef4444; }
.corner-MERCHANT_CONFIRMED { background: #fffbeb; color: #f59e0b; }
.corner-RIDER_ACCEPTED, .corner-DELIVERING { background: #eff6ff; color: #3b82f6; }
.corner-COMPLETED { background: #f0fdf4; color: #22c55e; }
.corner-CANCELLED { background: #f5f5f5; color: #999; }

.order-head {
  display: flex; justify-content: space-between; align-items: center;
  padding-bottom: 16rpx; border-bottom: 2rpx solid #f5f5f5; margin-bottom: 12rpx;
}
.order-no { font-size: 28rpx; }
.order-time { font-size: 22rpx; }

.item-list { margin-bottom: 12rpx; }
.item-row {
  display: flex; justify-content: space-between; font-size: 26rpx;
  padding: 6rpx 0;
}
.item-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.remark-row {
  display: flex; align-items: center; gap: 8rpx;
  background: #fffbeb; padding: 12rpx 16rpx; border-radius: 10rpx;
  margin-bottom: 12rpx;
}

.order-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 12rpx; border-top: 2rpx solid #f5f5f5;
}
.price-value { font-size: 32rpx; color: #1a1a1a; margin-left: 8rpx; }
.order-actions { display: flex; gap: 16rpx; }
.action-reject {
  padding: 14rpx 28rpx; border-radius: 12rpx; font-size: 24rpx;
  background: #f5f5f5; color: #666; font-weight: bold;
}
.action-accept {
  padding: 14rpx 28rpx; border-radius: 12rpx; font-size: 24rpx;
  background: #FFD100; color: #1a1a1a; font-weight: bold;
}

.empty-tip { text-align: center; padding: 120rpx 0; display: flex; flex-direction: column; align-items: center; gap: 20rpx; }
.loading-tip { text-align: center; padding: 40rpx; font-size: 26rpx; }

.mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999;
}
.reject-modal { width: 85%; background: #fff; border-radius: 24rpx; padding: 40rpx; }
.modal-title { font-size: 34rpx; font-weight: bold; text-align: center; margin-bottom: 30rpx; display: block; }
.reject-input {
  width: 100%; height: 200rpx; background: #f5f5f5; border-radius: 12rpx;
  padding: 20rpx; font-size: 28rpx; margin-bottom: 30rpx; box-sizing: border-box;
}
.reject-actions { display: flex; gap: 20rpx; }
.reject-cancel {
  flex: 1; text-align: center; padding: 22rpx 0; background: #f5f5f5;
  border-radius: 12rpx; color: #666; font-weight: bold;
}
.reject-confirm {
  flex: 1; text-align: center; padding: 22rpx 0; background: #ef4444;
  border-radius: 12rpx; color: #fff; font-weight: bold;
}
</style>
