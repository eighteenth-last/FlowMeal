<template>
  <view class="records-page">
    <view class="records-header">
      <view class="header-title">历史统计</view>
      <view class="stats-overview">
        <view class="stat-main">
          <text class="label">本周收入 (元)</text>
          <text class="value">1,280.50</text>
        </view>
        <view class="stat-chart">
           <!-- CSS Bar Chart Simulation -->
           <view class="bar-col" v-for="(h, i) in [40, 60, 30, 80, 50, 90, 70]" :key="i">
             <view class="bar" :style="{ height: h + '%' }"></view>
             <text class="day">{{ ['S','M','T','W','T','F','S'][i] }}</text>
           </view>
        </view>
      </view>

      <view class="mini-stats">
        <view class="mini-stat">
          <text class="num">{{ totalOrders }}</text>
          <text class="desc">总订单</text>
        </view>
        <view class="mini-stat">
          <text class="num">{{ avgDuration }}</text>
          <text class="desc">平均耗时(分)</text>
        </view>
        <view class="mini-stat">
          <text class="num">{{ totalDistance }}</text>
          <text class="desc">总里程(km)</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="record-list" @scrolltolower="loadMore">
      <view class="list-title">最近配送</view>
      
      <view class="record-item" v-for="record in records" :key="record.id">
        <view class="item-left">
          <view class="date-box">
             <text class="day">{{ record.createdAt ? record.createdAt.substring(8,10) : '01' }}</text>
             <text class="month">{{ record.createdAt ? record.createdAt.substring(5,7) : '01' }}月</text>
          </view>
        </view>
        <view class="item-body">
          <view class="order-no">订单 #{{ record.orderId }}</view>
          <view class="order-meta">
            <text class="meta-tag">{{ record.distanceKm }}km</text>
            <text class="meta-tag">{{ record.durationMin }}min</text>
            <text class="time">{{ record.completeTime ? record.completeTime.substring(11,16) : '--:--' }}</text>
          </view>
        </view>
        <view class="item-right">
          <text class="income">+{{ record.income || '5.0' }}</text>
        </view>
      </view>

      <view v-if="!loading && records.length === 0" class="empty-tip">
        <text class="fa fa-clipboard-list"></text>
        <text>暂无配送记录</text>
      </view>
      <view v-if="loading" class="loading-tip">加载中...</view>
    </scroll-view>

    <CustomTabBar :current="2" />
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
    const res = await getDeliveryRecords({ page: page.value, size: 20 })
    if (page.value === 1) records.value = res.records || []
    else records.value = [...records.value, ...res.records || []]
  } catch (e) {} finally { loading.value = false }
}

const loadMore = () => { page.value++; loadRecords() }

onMounted(() => {
  loadRecords()
})
</script>

<style>
.records-page {
  min-height: 100vh;
  background-color: #f3f4f6;
  padding-bottom: 200rpx;
}

.records-header {
  background-color: #111827;
  color: white;
  padding: 40rpx 40rpx 80rpx;
  border-bottom-left-radius: 60rpx;
  border-bottom-right-radius: 60rpx;
  position: relative;
  z-index: 10;
}

.header-title {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 40rpx;
  text-align: center;
}

.stats-overview {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40rpx;
}

.stat-main {
  display: flex;
  flex-direction: column;
}
.stat-main .label { font-size: 24rpx; color: #9ca3af; margin-bottom: 8rpx; }
.stat-main .value { font-size: 60rpx; font-weight: bold; color: #facc15; line-height: 1; }

.stat-chart {
  display: flex;
  align-items: flex-end;
  gap: 12rpx;
  height: 80rpx;
}
.bar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}
.bar {
  width: 8rpx;
  background-color: #4b5563;
  border-radius: 4rpx;
}
.bar-col:nth-child(even) .bar { background-color: #6b7280; }
.bar-col:last-child .bar { background-color: #facc15; }
.day { font-size: 16rpx; color: #6b7280; }

.mini-stats {
  display: flex;
  justify-content: space-between;
  background-color: rgba(255,255,255,0.1);
  padding: 30rpx;
  border-radius: 30rpx;
}
.mini-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}
.mini-stat .num { font-size: 32rpx; font-weight: bold; }
.mini-stat .desc { font-size: 20rpx; color: #9ca3af; }

.record-list {
  padding: 0 40rpx;
  margin-top: -60rpx;
  position: relative;
  z-index: 20;
  height: calc(100vh - 450rpx);
}

.list-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #111827;
  margin-bottom: 24rpx;
  padding-left: 10rpx;
}

.record-item {
  background-color: white;
  border-radius: 30rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03);
}

.item-left {
  margin-right: 24rpx;
}
.date-box {
  background-color: #f3f4f6;
  border-radius: 20rpx;
  width: 100rpx;
  height: 100rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.date-box .day { font-size: 36rpx; font-weight: bold; color: #111827; line-height: 1; }
.date-box .month { font-size: 20rpx; color: #6b7280; margin-top: 4rpx; }

.item-body {
  flex: 1;
}
.order-no { font-size: 28rpx; font-weight: bold; color: #111827; margin-bottom: 8rpx; }
.order-meta { display: flex; align-items: center; gap: 12rpx; }
.meta-tag {
  font-size: 20rpx;
  background-color: #eff6ff;
  color: #3b82f6;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.time { font-size: 20rpx; color: #9ca3af; margin-left: auto; }

.item-right {
  display: flex;
  align-items: center;
}
.income { font-size: 32rpx; font-weight: bold; color: #facc15; }

.empty-tip, .loading-tip {
  text-align: center; color: #9ca3af; padding: 40rpx; font-size: 26rpx; display: flex; flex-direction: column; align-items: center; gap: 16rpx;
}
.empty-tip .fa { font-size: 48rpx; opacity: 0.5; }
</style>
