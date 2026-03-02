<template>
  <view class="home-page">
    <!-- 顶部搜索栏 -->
    <view class="home-header">
      <view class="location-row" @click="showMapPopup = true">
        <text class="fa fa-location-dot" :style="{ color: locating ? '#ccc' : '#1a1a1a' }"></text>
        <text class="location-text">{{ currentCity || '定位中...' }}</text>
        <text class="fa fa-chevron-down" style="font-size:20rpx;margin-left:6rpx;"></text>
      </view>
      <view class="search-bar" @click="focusSearch">
        <text class="fa fa-magnifying-glass search-icon"></text>
        <input v-model="keyword" placeholder="搜索商家或美食" class="search-input" confirm-type="search" @confirm="loadShops" />
      </view>
      <!-- Banner -->
      <view class="banner">
        <text class="banner-title">新用户首单立减</text>
        <text class="banner-tag">最高减15元</text>
      </view>
    </view>

    <!-- 地图弹窗 -->
    <view class="map-overlay" v-if="showMapPopup" @click.self="showMapPopup = false">
      <view class="map-popup">
        <view class="map-popup-header">
          <text class="font-bold" style="font-size:32rpx;">当前位置</text>
          <view @click="showMapPopup = false">
            <text class="fa fa-xmark" style="font-size:32rpx;color:#999;"></text>
          </view>
        </view>
        <view class="map-popup-addr">
          <text class="fa fa-location-dot" style="color:#FFD100;margin-right:10rpx;"></text>
          <text style="font-size:26rpx;">{{ currentAddress || currentCity || '定位中...' }}</text>
        </view>
        <view id="homeMapContainer" class="map-container"></view>
      </view>
    </view>

    <!-- 分类 -->
    <view class="category-grid">
      <view class="cat-item" v-for="cat in categories" :key="cat.icon">
        <view class="cat-icon-wrap">
          <text :class="'fa fa-' + cat.icon" class="cat-icon"></text>
        </view>
        <text class="cat-name">{{ cat.name }}</text>
      </view>
    </view>

    <!-- 商家列表 -->
    <view class="shop-section">
      <view class="section-header">
        <text class="section-title">附近商家</text>
        <text class="section-more text-gray">更多 <text class="fa fa-chevron-right" style="font-size:20rpx;"></text></text>
      </view>

      <view v-if="loading" class="loading-tip">
        <text class="fa fa-spinner fa-spin"></text>
        <text> 加载中...</text>
      </view>

      <view class="shop-card" v-for="shop in shops" :key="shop.id" @click="goShop(shop.id)">
        <image :src="shop.avatar || '/static/default-shop.png'" class="shop-img" mode="aspectFill" />
        <view class="shop-info">
          <text class="shop-name">{{ shop.shopName }}</text>
          <view class="shop-meta">
            <text class="fa fa-star" style="color:#FFD100;font-size:22rpx;"></text>
            <text class="shop-rating">4.8</text>
            <text class="shop-sales text-gray">月售 {{ shop.sales || 999 }}+</text>
          </view>
          <view class="shop-extra">
            <text class="shop-fee text-gray">
              <text class="fa fa-truck" style="font-size:20rpx;"></text>
              ¥{{ shop.deliveryFee || 3 }} · {{ shop.businessHours || '09:00-22:00' }}
            </text>
          </view>
          <text class="shop-desc text-gray" v-if="shop.notice">{{ shop.notice }}</text>
        </view>
      </view>

      <view v-if="!loading && shops.length === 0" class="empty-tip">
        <text class="fa fa-box-open" style="font-size:64rpx;color:#ddd;"></text>
        <text class="empty-text">暂无商家</text>
      </view>
    </view>

    <CustomTabBar :current="0" />
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { getShopList } from '@/api/shop'
import { getIPLocation, getBrowserLocation, initMap, addMarker } from '@/utils/amap'
import CustomTabBar from '@/components/CustomTabBar.vue'

const keyword = ref('')
const shops = ref([])
const loading = ref(false)
const page = ref(1)
const currentCity = ref('')
const currentAddress = ref('')
const currentLng = ref(0)
const currentLat = ref(0)
const locating = ref(true)
const showMapPopup = ref(false)
let mapInstance = null

const categories = [
  { name: '美食', icon: 'utensils' },
  { name: '快餐', icon: 'burger' },
  { name: '奶茶', icon: 'mug-hot' },
  { name: '甜品', icon: 'ice-cream' },
  { name: '更多', icon: 'ellipsis' }
]

const loadShops = async () => {
  loading.value = true
  try {
    const res = await getShopList({ page: page.value, size: 20, keyword: keyword.value || undefined })
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

// 定位
const doLocate = async () => {
  locating.value = true
  try {
    // 优先浏览器精确定位
    const loc = await getBrowserLocation()
    currentCity.value = loc.district || loc.city || '未知城市'
    currentAddress.value = loc.address || ''
    currentLng.value = loc.lng
    currentLat.value = loc.lat
  } catch (e) {
    // 回退到IP定位
    try {
      const ip = await getIPLocation()
      currentCity.value = ip.city || '未知城市'
      // 从 rectangle 解析中心坐标
      if (ip.rectangle) {
        const parts = ip.rectangle.split(';')
        if (parts.length === 2) {
          const [lng1, lat1] = parts[0].split(',').map(Number)
          const [lng2, lat2] = parts[1].split(',').map(Number)
          currentLng.value = (lng1 + lng2) / 2
          currentLat.value = (lat1 + lat2) / 2
        }
      }
    } catch (e2) {
      currentCity.value = '定位失败'
    }
  } finally {
    locating.value = false
  }
}

// 初始化地图弹窗
watch(showMapPopup, async (val) => {
  if (val) {
    await nextTick()
    setTimeout(() => {
      if (mapInstance) {
        mapInstance.destroy()
        mapInstance = null
      }
      const center = currentLng.value && currentLat.value
        ? [currentLng.value, currentLat.value]
        : undefined
      mapInstance = initMap('homeMapContainer', { zoom: 15, center })
      if (mapInstance && center) {
        addMarker(mapInstance, center[0], center[1], { title: '我的位置' })
      }
    }, 100)
  }
})

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
  background: linear-gradient(135deg, #FFD100 0%, #FFE550 100%);
  padding: 80rpx 30rpx 40rpx;
  border-radius: 0 0 50rpx 50rpx;
}
.location-row { display: flex; align-items: center; margin-bottom: 24rpx; padding: 0 10rpx; }
.location-text { font-size: 28rpx; font-weight: bold; margin-left: 10rpx; }
.search-bar {
  display: flex; align-items: center; background: rgba(255,255,255,0.7);
  border-radius: 50rpx; padding: 0 30rpx; height: 76rpx; backdrop-filter: blur(10px);
}
.search-icon { font-size: 28rpx; color: #999; margin-right: 16rpx; }
.search-input { flex: 1; font-size: 28rpx; }
.banner {
  margin-top: 24rpx; background: rgba(255,255,255,0.3); border-radius: 20rpx;
  padding: 24rpx 30rpx; display: flex; align-items: center; justify-content: space-between;
}
.banner-title { font-size: 28rpx; font-weight: bold; }
.banner-tag {
  background: #1a1a1a; color: #FFD100; font-size: 22rpx; font-weight: bold;
  padding: 6rpx 20rpx; border-radius: 20rpx;
}

.category-grid {
  display: flex; justify-content: space-around; padding: 30rpx 20rpx;
}
.cat-item { display: flex; flex-direction: column; align-items: center; gap: 10rpx; }
.cat-icon-wrap {
  width: 88rpx; height: 88rpx; border-radius: 24rpx; background: #fff;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.06);
}
.cat-icon { font-size: 36rpx; color: #FFD100; }
.cat-name { font-size: 24rpx; color: #666; }

.shop-section { padding: 0 30rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title { font-size: 34rpx; font-weight: bold; }
.section-more { font-size: 24rpx; }

.shop-card {
  display: flex; gap: 24rpx; background: #fff; padding: 24rpx;
  border-radius: 24rpx; margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); border: 2rpx solid #f5f5f5;
}
.shop-img { width: 160rpx; height: 160rpx; border-radius: 20rpx; flex-shrink: 0; background: #f0f0f0; }
.shop-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; overflow: hidden; }
.shop-name { font-size: 30rpx; font-weight: bold; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.shop-meta { display: flex; align-items: center; gap: 8rpx; font-size: 24rpx; }
.shop-rating { font-weight: bold; font-size: 24rpx; }
.shop-sales { font-size: 22rpx; margin-left: 12rpx; }
.shop-extra { font-size: 22rpx; }
.shop-fee { display: flex; align-items: center; gap: 6rpx; }
.shop-desc { font-size: 22rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.loading-tip { text-align: center; padding: 60rpx; color: #999; font-size: 28rpx; }
.empty-tip { text-align: center; padding: 100rpx 0; display: flex; flex-direction: column; align-items: center; gap: 20rpx; }
.empty-text { color: #ccc; font-size: 28rpx; }

/* 地图弹窗 */
.map-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: flex-end;
}
.map-popup {
  width: 100%; background: #fff; border-radius: 30rpx 30rpx 0 0;
  padding: 30rpx; animation: slideUp 0.3s ease;
}
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.map-popup-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx;
}
.map-popup-addr { margin-bottom: 20rpx; display: flex; align-items: center; }
.map-container { width: 100%; height: 500rpx; border-radius: 20rpx; overflow: hidden; background: #e8eef3; }
</style>
