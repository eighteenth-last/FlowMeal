<template>
  <view class="records-page">
    <!-- 头部统计 -->
    <view class="records-header">
      <text class="header-title">配送记录</text>
      <view class="stats-row">
        <view class="stat-box">
          <text class="stat-num">{{ totalOrders }}</text>
          <text class="stat-label">总配送单</text>
        </view>
        <view class="stat-box">
          <text class="stat-num">{{ avgDuration }}</text>
          <text class="stat-label">平均时长(分)</text>
        </view>
        <view class="stat-box">
          <text class="stat-num text-primary">{{ totalDistance }}</text>
          <text class="stat-label">总里程(km)</text>
        </view>
      </view>
    </view>

    <!-- 记录列表 -->
    <scroll-view scroll-y class="record-list" @scrolltolower="loadMore">
      <view class="record-card" v-for="record in records" :key="record.id">
        <view class="record-top">
          <view class="record-badge">
            <text class="fa fa-route" style="color:#FFD100;font-size:22rpx;"></text>
          </view>
          <view class="record-info">
            <text class="record-order-no font-bold">订单 #{{ record.orderId }}</text>
            <text class="record-time text-gray">{{ record.createdAt }}</text>
          </view>
        </view>
        <view class="record-stats">
          <view class="record-stat">
            <text class="fa fa-clock text-gray" style="font-size:22rpx;"></text>
            <text>{{ record.durationMin || '--' }} 分钟</text>
          </view>
          <view class="record-stat">
            <text class="fa fa-road text-gray" style="font-size:22rpx;"></text>
            <text>{{ record.distanceKm || '--' }} km</text>
          </view>
        </view>
        <view class="record-timeline text-gray" style="font-size:22rpx;">
          <text v-if="record.acceptTime">接单 {{ record.acceptTime }}</text>
          <text v-if="record.completeTime"> → 完成 {{ record.completeTime }}</text>
        </view>
      </view>

      <view v-if="!loading && records.length === 0" class="empty-tip">
        <text class="fa fa-clipboard-list" style="font-size:64rpx;color:#ddd;"></text>
        <text class="text-gray">暂无配送记录</text>
      </view>

      <view v-if="loading" class="loading-tip text-gray">加载中...</view>
    </scroll-view>

    <CustomTabBar :current="1" />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDeliveryRecords } from '@/api/rider'
import CustomTabBar from '@/components/CustomTabBar.vue'

const records = ref([])
const loading = ref(false)
const page = ref(1)

const totalOrders = computed(() => records.value.length)
const avgDuration = computed(() => {
  const valid = records.value.filter(r => r.durationMin)
  if (valid.length === 0) return '--'
  return Math.round(valid.reduce((s, r) => s + r.durationMin, 0) / valid.length)
})
const totalDistance = computed(() => {
  return records.value.reduce((s, r) => s + (parseFloat(r.distanceKm) || 0), 0).toFixed(1)
})

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getDeliveryRecords({ page: page.value, size: 50 })
    records.value = res?.records || []
  } catch (e) {} finally { loading.value = false }
}

const loadMore = () => { page.value++; loadRecords() }

onMounted(() => {
  const token = uni.getStorageSync('fm_rider_token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  loadRecords()
})
</script>

<style scoped>
.records-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 160rpx; }

.records-header {
  background: #1a1a1a; padding: 80rpx 30rpx 50rpx; color: #fff;
  border-radius: 0 0 40rpx 40rpx;
}
.header-title { font-size: 40rpx; font-weight: bold; display: block; margin-bottom: 30rpx; padding-top: 20rpx; }
.stats-row { display: flex; justify-content: space-around; }
.stat-box { display: flex; flex-direction: column; align-items: center; gap: 8rpx; }
.stat-num { font-size: 44rpx; font-weight: bold; }
.stat-label { font-size: 22rpx; color: rgba(255,255,255,0.5); }

.record-list { padding: 20rpx 30rpx; height: calc(100vh - 350rpx); }

.record-card {
  background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.record-top { display: flex; align-items: center; gap: 16rpx; margin-bottom: 16rpx; }
.record-badge {
  width: 50rpx; height: 50rpx; background: #fffbeb; border-radius: 12rpx;
  display: flex; align-items: center; justify-content: center;
}
.record-info { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.record-order-no { font-size: 28rpx; }
.record-time { font-size: 22rpx; }

.record-stats { display: flex; gap: 40rpx; margin-bottom: 10rpx; }
.record-stat { display: flex; align-items: center; gap: 8rpx; font-size: 26rpx; }

.record-timeline { padding-top: 10rpx; border-top: 2rpx solid #f8f8f8; }

.empty-tip { text-align: center; padding: 120rpx 0; display: flex; flex-direction: column; align-items: center; gap: 20rpx; }
.loading-tip { text-align: center; padding: 40rpx; font-size: 26rpx; }
</style>
