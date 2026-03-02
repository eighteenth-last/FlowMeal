<template>
  <view class="stats-page">
    <!-- 深色头部 -->
    <view class="stats-header">
      <text class="header-title font-bold">营业数据</text>
      <view class="header-stats">
        <view class="main-stat">
          <text class="main-num">¥{{ todayRevenue }}</text>
          <text class="main-label">今日营收</text>
        </view>
        <view class="sub-stats">
          <view class="sub-stat">
            <text class="sub-num">{{ todayOrders }}</text>
            <text class="sub-label">订单数</text>
          </view>
          <view class="sub-stat">
            <text class="sub-num">¥{{ avgPrice }}</text>
            <text class="sub-label">客单价</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 数据卡片 -->
    <scroll-view scroll-y class="stats-body">
      <!-- 核心指标 -->
      <view class="metric-grid">
        <view class="metric-card">
          <text class="fa fa-receipt metric-icon" style="color:#3b82f6;"></text>
          <text class="metric-num font-bold">{{ totalOrders }}</text>
          <text class="metric-label text-gray">总订单</text>
        </view>
        <view class="metric-card">
          <text class="fa fa-money-bill-wave metric-icon" style="color:#22c55e;"></text>
          <text class="metric-num font-bold">¥{{ totalRevenue }}</text>
          <text class="metric-label text-gray">总营收</text>
        </view>
        <view class="metric-card">
          <text class="fa fa-users metric-icon" style="color:#f59e0b;"></text>
          <text class="metric-num font-bold">{{ newCustomers }}</text>
          <text class="metric-label text-gray">新顾客</text>
        </view>
        <view class="metric-card">
          <text class="fa fa-rotate-left metric-icon" style="color:#8b5cf6;"></text>
          <text class="metric-num font-bold">{{ repeatRate }}%</text>
          <text class="metric-label text-gray">复购率</text>
        </view>
      </view>

      <!-- 营收趋势 -->
      <view class="chart-card">
        <view class="chart-head">
          <text class="font-bold">营收趋势</text>
          <view class="chart-tabs">
            <text :class="['chart-tab', chartTab === 'week' ? 'chart-tab-active' : '']" @click="chartTab = 'week'">本周</text>
            <text :class="['chart-tab', chartTab === 'month' ? 'chart-tab-active' : '']" @click="chartTab = 'month'">本月</text>
          </view>
        </view>
        <!-- 简易柱状图模拟 -->
        <view class="bar-chart">
          <view class="bar-item" v-for="(bar, idx) in chartBars" :key="idx">
            <view class="bar-fill" :style="{ height: bar.height + '%' }"></view>
            <text class="bar-label text-gray">{{ bar.label }}</text>
          </view>
        </view>
      </view>

      <!-- 转化漏斗 -->
      <view class="funnel-card">
        <text class="font-bold" style="display:block;margin-bottom:24rpx;">流量转化</text>
        <view class="funnel-item" v-for="(f, idx) in funnel" :key="idx">
          <view class="funnel-bar-wrap">
            <view class="funnel-bar" :style="{ width: f.percent + '%', background: f.color }"></view>
          </view>
          <view class="funnel-info">
            <text class="font-bold">{{ f.label }}</text>
            <text class="text-gray">{{ f.value }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <CustomTabBar :current="2" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import CustomTabBar from '@/components/CustomTabBar.vue'

const chartTab = ref('week')

// 模拟数据
const todayRevenue = ref('1,280.50')
const todayOrders = ref(42)
const avgPrice = ref('30.49')
const totalOrders = ref(1256)
const totalRevenue = ref('38,420')
const newCustomers = ref(86)
const repeatRate = ref(34)

const chartBars = ref([
  { label: '周一', height: 45 },
  { label: '周二', height: 62 },
  { label: '周三', height: 78 },
  { label: '周四', height: 55 },
  { label: '周五', height: 90 },
  { label: '周六', height: 100 },
  { label: '周日', height: 85 }
])

const funnel = ref([
  { label: '店铺展示', value: '2,450', percent: 100, color: '#FFD100' },
  { label: '进店浏览', value: '1,280', percent: 52, color: '#f59e0b' },
  { label: '加入购物车', value: '520', percent: 21, color: '#ef4444' },
  { label: '下单完成', value: '380', percent: 15, color: '#22c55e' }
])

onMounted(() => {
  const token = uni.getStorageSync('fm_merchant_token')
  if (!token) uni.reLaunch({ url: '/pages/login/index' })
})
</script>

<style scoped>
.stats-page { min-height: 100vh; background: #f0f0f0; padding-bottom: 130rpx; }

.stats-header {
  background: #1a1a1a; padding: 80rpx 30rpx 60rpx;
  border-radius: 0 0 40rpx 40rpx; color: #fff;
}
.header-title { font-size: 40rpx; display: block; text-align: center; margin-bottom: 40rpx; }
.header-stats { display: flex; align-items: flex-end; justify-content: space-between; }
.main-stat { display: flex; flex-direction: column; }
.main-num { font-size: 56rpx; font-weight: bold; color: #FFD100; }
.main-label { font-size: 24rpx; color: rgba(255,255,255,0.5); }
.sub-stats { display: flex; gap: 40rpx; }
.sub-stat { display: flex; flex-direction: column; align-items: center; }
.sub-num { font-size: 36rpx; font-weight: bold; }
.sub-label { font-size: 22rpx; color: rgba(255,255,255,0.5); }

.stats-body { padding: 20rpx 30rpx; }

.metric-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16rpx; margin-bottom: 20rpx; margin-top: -30rpx; position: relative; z-index: 10; }
.metric-card {
  background: #fff; border-radius: 20rpx; padding: 24rpx; text-align: center;
  display: flex; flex-direction: column; align-items: center; gap: 8rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.metric-icon { font-size: 36rpx; margin-bottom: 8rpx; }
.metric-num { font-size: 32rpx; }
.metric-label { font-size: 22rpx; }

.chart-card {
  background: #fff; border-radius: 20rpx; padding: 30rpx; margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.chart-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx; }
.chart-tabs { display: flex; gap: 12rpx; }
.chart-tab {
  font-size: 22rpx; padding: 8rpx 20rpx; border-radius: 8rpx;
  background: #f5f5f5; color: #999;
}
.chart-tab-active { background: #1a1a1a; color: #FFD100; font-weight: bold; }

.bar-chart { display: flex; align-items: flex-end; gap: 12rpx; height: 200rpx; }
.bar-item { flex: 1; display: flex; flex-direction: column; align-items: center; height: 100%; justify-content: flex-end; }
.bar-fill { width: 80%; background: linear-gradient(180deg, #FFD100 0%, #f59e0b 100%); border-radius: 8rpx 8rpx 0 0; min-height: 8rpx; transition: height 0.5s; }
.bar-label { font-size: 18rpx; margin-top: 8rpx; }

.funnel-card {
  background: #fff; border-radius: 20rpx; padding: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.funnel-item { margin-bottom: 20rpx; }
.funnel-bar-wrap { height: 16rpx; background: #f5f5f5; border-radius: 8rpx; margin-bottom: 8rpx; }
.funnel-bar { height: 100%; border-radius: 8rpx; transition: width 0.5s; }
.funnel-info { display: flex; justify-content: space-between; font-size: 24rpx; }
</style>
