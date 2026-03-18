<template>
  <view class="home-page">
    <view class="home-header">
      <view class="header-top">
        <view class="location-area">
          <text class="location-label">送至</text>
          <view class="location-row" @click="showMapPopup = true">
            <text class="location-text">{{ currentAddress || currentCity || '定位中...' }}</text>
            <image src="/static/chevron_down.png" class="chevron-icon" mode="aspectFit" />
          </view>
        </view>
        <view class="header-actions">
          <image src="/static/bell.png" class="action-icon" mode="aspectFit" />
          <image src="/static/search-magnifying-glass_32.png" class="action-icon" mode="aspectFit" @click="focusSearch" />
        </view>
      </view>
      <view class="banner-card">
        <view class="banner-left">
          <text class="banner-title">新用户立减20元</text>
          <text class="banner-sub">首单特惠，满30可用</text>
          <view class="banner-btn" @click="loadShops">立即领取</view>
        </view>
        <image src="/static/bowl-food.png" class="banner-img" mode="aspectFit" />
      </view>
    </view>

    <view class="category-grid">
      <view class="cat-item" v-for="cat in categories" :key="cat.name" @click="filterByCategory(cat)">
        <view class="cat-icon-wrap" :style="{ background: cat.bg }">
          <image :src="'/static/' + cat.icon + '.png'" class="cat-icon" mode="aspectFit" />
        </view>
        <text class="cat-name">{{ cat.name }}</text>
      </view>
    </view>

    <view class="shop-section">
      <view class="section-header">
        <text class="section-title">附近商家</text>
        <text class="section-sort text-gray">距离最近 </text>
        <image src="/static/chevron_down.png" style="width:18rpx;height:18rpx;vertical-align:middle;" mode="aspectFit" />
      </view>

      <view v-if="loading" class="loading-tip">
        <text>加载中...</text>
      </view>

      <view class="shop-card" v-for="shop in shops" :key="shop.id" @click="goShop(shop.id)">
        <image :src="shop.avatar || '/static/store.png'" class="shop-img" mode="aspectFill" />
        <view class="shop-info">
          <text class="shop-name">{{ shop.shopName }}</text>
          <view class="shop-meta">
            <text class="shop-rating" v-if="shop.rating">⭐ {{ shop.rating }}</text>
            <text class="shop-sales text-gray" v-if="shop.sales">月售 {{ shop.sales }}+</text>
          </view>
          <view class="shop-extra">
            <text class="shop-time text-gray" v-if="shop.deliveryTime">{{ shop.deliveryTime }}分钟<text v-if="shop.distance"> | {{ shop.distance }}</text></text>
            <text class="shop-promo" v-if="shop.promo">{{ shop.promo }}</text>
          </view>
        </view>
      </view>

      <view v-if="!loading && shops.length === 0" class="empty-tip">
        <text class="empty-text text-gray">暂无商家</text>
      </view>
    </view>

    <view class="map-overlay" v-if="showMapPopup" @click.self="showMapPopup = false">
      <view class="map-popup">
        <view class="map-popup-header">
          <text class="map-popup-title">当前位置</text>
          <view @click="showMapPopup = false">
            <image src="/static/close.png" style="width:32rpx;height:32rpx;" mode="aspectFit" />
          </view>
        </view>
        <view class="map-popup-addr">
          <image src="/static/dingwei.png" style="width:24rpx;height:24rpx;margin-right:10rpx;" mode="aspectFit" />
          <text>{{ currentAddress || currentCity || '定位中...' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getShopList } from '@/api/shop'

const keyword = ref('')
const shops = ref([])
const loading = ref(false)
const currentCity = ref('')
const currentAddress = ref('')
const showMapPopup = ref(false)

const categories = [
  { name: '美食外卖', icon: 'bowl-food', bg: '#fff3e0' },
  { name: '超市便利', icon: 'store', bg: '#e3f2fd' },
  { name: '甜品饮品', icon: 'Receipt', bg: '#fce4ec' },
  { name: '蔬菜水果', icon: 'Smile', bg: '#e8f5e9' },
  { name: '医疗健康', icon: 'circle-info', bg: '#ffebee' }
]

const loadShops = async () => {
  loading.value = true
  try {
    const params = { page: 1, size: 20 }
    if (keyword.value) params.keyword = keyword.value
    const res = await getShopList(params)
    shops.value = res?.records || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goShop = (id) => {
  uni.navigateTo({ url: `/pages/shop/index?id=${id}` })
}

const focusSearch = () => {}
const filterByCategory = (cat) => {}

const doLocate = async () => {
  try {
    uni.getLocation({
      type: 'gcj02',
      success: (res) => {
        currentCity.value = '当前位置'
        currentAddress.value = `${res.latitude.toFixed(4)}, ${res.longitude.toFixed(4)}`
      },
      fail: () => { currentCity.value = '未知位置' }
    })
  } catch (e) {
    currentCity.value = '未知位置'
  }
}

onMounted(() => {
  const token = uni.getStorageSync('fm_user_token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  doLocate()
  loadShops()
})
</script>

<style scoped>
.home-page { min-height: 100vh; background: #f8f9fa; padding-bottom: 160rpx; }

.home-header {
  background: rgb(255, 209, 0);
  padding: 0 30rpx 40rpx;
  padding-top: calc(var(--status-bar-height) + 40rpx);
  border-radius: 0 0 50rpx 50rpx;
  box-shadow: 0 4rpx 20rpx rgba(255,209,0,0.3);
}
.header-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 30rpx; }
.location-area { display: flex; flex-direction: column; gap: 4rpx; }
.location-label { font-size: 22rpx; opacity: 0.7; }
.location-row { display: flex; align-items: center; gap: 8rpx; }
.location-text { font-size: 30rpx; font-weight: bold; max-width: 400rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chevron-icon { width: 20rpx; height: 20rpx; }
.header-actions { display: flex; gap: 30rpx; padding-top: 20rpx; }
.action-icon { width: 40rpx; height: 40rpx; }

.banner-card {
  background: rgba(255,255,255,0.35); backdrop-filter: blur(10px);
  border-radius: 24rpx; padding: 30rpx;
  display: flex; justify-content: space-between; align-items: center;
}
.banner-left { display: flex; flex-direction: column; gap: 8rpx; }
.banner-title { font-size: 34rpx; font-weight: bold; }
.banner-sub { font-size: 22rpx; opacity: 0.8; }
.banner-btn {
  margin-top: 10rpx; background: #1a1a1a; color: #fff;
  font-size: 22rpx; font-weight: bold; padding: 10rpx 28rpx;
  border-radius: 999rpx; display: inline-block; width: fit-content;
}
.banner-img { width: 140rpx; height: 140rpx; }

.category-grid { display: flex; justify-content: space-around; padding: 40rpx 20rpx 20rpx; }
.cat-item { display: flex; flex-direction: column; align-items: center; gap: 12rpx; }
.cat-icon-wrap { width: 96rpx; height: 96rpx; border-radius: 28rpx; display: flex; align-items: center; justify-content: center; }
.cat-icon { width: 48rpx; height: 48rpx; }
.cat-name { font-size: 22rpx; color: #333; font-weight: 500; text-align: center; }

.shop-section { padding: 0 24rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title { font-size: 36rpx; font-weight: bold; }
.section-sort { font-size: 24rpx; color: #999; }

.shop-card {
  display: flex; gap: 24rpx; background: #fff; padding: 24rpx;
  border-radius: 30rpx; margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); border: 2rpx solid #f0f0f0;
}
.shop-img { width: 160rpx; height: 160rpx; border-radius: 24rpx; flex-shrink: 0; background: #f0f0f0; }
.shop-info { flex: 1; display: flex; flex-direction: column; gap: 10rpx; overflow: hidden; }
.shop-name { font-size: 30rpx; font-weight: bold; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.shop-meta { display: flex; align-items: center; gap: 12rpx; }
.shop-rating { font-size: 24rpx; font-weight: bold; color: #f59e0b; }
.shop-sales { font-size: 22rpx; }
.shop-extra { display: flex; justify-content: space-between; align-items: center; }
.shop-time { font-size: 22rpx; }
.shop-promo { font-size: 20rpx; background: #fffbeb; color: #d97706; padding: 4rpx 14rpx; border-radius: 8rpx; font-weight: 500; }

.loading-tip { text-align: center; padding: 60rpx; color: #999; font-size: 28rpx; }
.empty-tip { text-align: center; padding: 100rpx 0; }
.empty-text { font-size: 28rpx; }
.text-gray { color: #999; }

.map-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: flex-end;
}
.map-popup { width: 100%; background: #fff; border-radius: 30rpx 30rpx 0 0; padding: 40rpx; }
.map-popup-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.map-popup-title { font-size: 32rpx; font-weight: bold; }
.map-popup-addr { display: flex; align-items: center; font-size: 26rpx; color: #666; }
</style>
