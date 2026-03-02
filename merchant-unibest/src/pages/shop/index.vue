<template>
  <view class="shop-page">
    <!-- 店铺头部 -->
    <view class="shop-header">
      <text class="header-title font-bold">我的店铺</text>
    </view>

    <!-- 店铺信息卡 -->
    <view class="shop-card">
      <view class="shop-avatar">
        <text class="fa fa-store" style="font-size:48rpx;color:#FFD100;"></text>
      </view>
      <view class="shop-info">
        <text class="shop-name font-bold">{{ shopInfo.name || '我的店铺' }}</text>
        <view class="shop-rating">
          <text class="fa fa-star" style="color:#FFD100;font-size:22rpx;"></text>
          <text style="font-size:24rpx;font-weight:bold;">{{ shopInfo.rating || '5.0' }}</text>
          <text class="text-gray" style="font-size:22rpx;">月售 {{ shopInfo.monthlySales || 0 }}</text>
        </view>
      </view>
      <view class="edit-shop" @click="goEditShop">
        <text class="fa fa-pen" style="font-size:24rpx;color:#3b82f6;"></text>
      </view>
    </view>

    <scroll-view scroll-y class="shop-body">
      <!-- 核心营业状态 -->
      <view class="setting-card">
        <view class="setting-row">
          <view class="setting-left">
            <text class="fa fa-circle-dot setting-icon" style="color:#22c55e;"></text>
            <view>
              <text class="setting-title font-bold">营业状态</text>
              <text class="text-gray" style="font-size:22rpx;">{{ isOpen ? '当前营业中' : '当前已打烊' }}</text>
            </view>
          </view>
          <switch :checked="isOpen" color="#22c55e" @change="handleToggleStatus" />
        </view>

        <view class="setting-row">
          <view class="setting-left">
            <text class="fa fa-clock setting-icon" style="color:#3b82f6;"></text>
            <view>
              <text class="setting-title font-bold">营业时间</text>
              <text class="text-gray" style="font-size:22rpx;">{{ shopInfo.businessHours || '08:00 - 22:00' }}</text>
            </view>
          </view>
          <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
        </view>

        <view class="setting-row">
          <view class="setting-left">
            <text class="fa fa-truck setting-icon" style="color:#f59e0b;"></text>
            <view>
              <text class="setting-title font-bold">配送费</text>
              <text class="text-gray" style="font-size:22rpx;">¥{{ shopInfo.deliveryFee || '3.00' }}</text>
            </view>
          </view>
          <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
        </view>
      </view>

      <!-- 店铺公告 -->
      <view class="setting-card">
        <view class="setting-row">
          <view class="setting-left">
            <text class="fa fa-bullhorn setting-icon" style="color:#8b5cf6;"></text>
            <view>
              <text class="setting-title font-bold">店铺公告</text>
              <text class="text-gray announcement" style="font-size:22rpx;">
                {{ shopInfo.announcement || '点击编辑店铺公告...' }}
              </text>
            </view>
          </view>
          <text class="fa fa-chevron-right text-gray" style="font-size:24rpx;"></text>
        </view>
      </view>

      <!-- 自动设置 -->
      <view class="setting-card">
        <view class="setting-row">
          <view class="setting-left">
            <text class="fa fa-clock-rotate-left setting-icon" style="color:#ef4444;"></text>
            <view>
              <text class="setting-title font-bold">自动接单</text>
              <text class="text-gray" style="font-size:22rpx;">新订单自动确认</text>
            </view>
          </view>
          <switch :checked="autoAccept" color="#FFD100" @change="autoAccept = $event.detail.value" />
        </view>

        <view class="setting-row">
          <view class="setting-left">
            <text class="fa fa-print setting-icon" style="color:#6b7280;"></text>
            <view>
              <text class="setting-title font-bold">自动打印</text>
              <text class="text-gray" style="font-size:22rpx;">新订单自动打印小票</text>
            </view>
          </view>
          <switch :checked="autoPrint" color="#FFD100" @change="autoPrint = $event.detail.value" />
        </view>

        <view class="setting-row">
          <view class="setting-left">
            <text class="fa fa-bell setting-icon" style="color:#3b82f6;"></text>
            <view>
              <text class="setting-title font-bold">声音提醒</text>
              <text class="text-gray" style="font-size:22rpx;">新订单提示音</text>
            </view>
          </view>
          <switch :checked="soundAlert" color="#FFD100" @change="soundAlert = $event.detail.value" />
        </view>
      </view>

      <!-- 店铺位置地图 -->
      <view class="setting-card">
        <view class="setting-row" style="border-bottom:none;">
          <view class="setting-left">
            <text class="fa fa-map-location-dot setting-icon" style="color:#ef4444;"></text>
            <view>
              <text class="setting-title font-bold">店铺位置</text>
              <text class="text-gray" style="font-size:22rpx;">{{ shopAddress || '点击地图定位店铺' }}</text>
            </view>
          </view>
        </view>
        <view id="shopMapContainer" class="shop-map-container"></view>
      </view>

      <!-- 退出 -->
      <view class="logout-btn" @click="handleLogout">退出商家账号</view>
    </scroll-view>

    <CustomTabBar :current="3" />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyShop, toggleShopStatus } from '@/api/shop'
import { logout } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { initMap, addMarker, getBrowserLocation, getIPLocation, reverseGeocode } from '@/utils/amap'
import CustomTabBar from '@/components/CustomTabBar.vue'

const authStore = useAuthStore()
const shopInfo = ref({})
const isOpen = ref(false)
const autoAccept = ref(false)
const autoPrint = ref(true)
const soundAlert = ref(true)
const shopAddress = ref('')
let mapInstance = null

const handleToggleStatus = async (e) => {
  const newStatus = e.detail.value ? 1 : 0
  try {
    await toggleShopStatus(newStatus)
    isOpen.value = e.detail.value
    uni.showToast({ title: isOpen.value ? '已开启营业' : '已打烊', icon: 'none' })
  } catch (err) {
    isOpen.value = !e.detail.value
  }
}

const goEditShop = () => {
  uni.showToast({ title: '编辑店铺信息', icon: 'none' })
}

const handleLogout = async () => {
  try { await logout() } catch (e) {}
  authStore.clearAuth()
  uni.reLaunch({ url: '/pages/login/index' })
}

// 初始化店铺位置地图
const initShopMap = async () => {
  setTimeout(async () => {
    let center
    // 从店铺数据获取坐标
    if (shopInfo.value.lng && shopInfo.value.lat) {
      center = [shopInfo.value.lng, shopInfo.value.lat]
    }

    mapInstance = initMap('shopMapContainer', { zoom: 16, center })
    if (!mapInstance) return

    if (center) {
      addMarker(mapInstance, center[0], center[1], { title: shopInfo.value.name || '我的店铺' })
      try {
        const geo = await reverseGeocode(center[0], center[1])
        shopAddress.value = geo.address || ''
      } catch (e) {}
    } else {
      // 没有店铺坐标，定位当前位置
      try {
        const loc = await getBrowserLocation()
        mapInstance.setCenter([loc.lng, loc.lat])
        addMarker(mapInstance, loc.lng, loc.lat, { title: '当前位置' })
        shopAddress.value = loc.address || loc.district || loc.city || ''
      } catch (e) {
        try {
          const ip = await getIPLocation()
          shopAddress.value = ip.city || ''
          if (ip.rectangle) {
            const parts = ip.rectangle.split(';')
            if (parts.length === 2) {
              const [lng1, lat1] = parts[0].split(',').map(Number)
              const [lng2, lat2] = parts[1].split(',').map(Number)
              const cLng = (lng1 + lng2) / 2
              const cLat = (lat1 + lat2) / 2
              mapInstance.setCenter([cLng, cLat])
              addMarker(mapInstance, cLng, cLat, { title: ip.city })
            }
          }
        } catch (e2) {}
      }
    }
  }, 300)
}

onMounted(async () => {
  const token = uni.getStorageSync('fm_merchant_token')
  if (!token) { uni.reLaunch({ url: '/pages/login/index' }); return }
  try {
    const shop = await getMyShop()
    if (shop) {
      shopInfo.value = shop
      isOpen.value = shop.status === 1
    }
  } catch (e) {}
  initShopMap()
})
</script>

<style scoped>
.shop-page { min-height: 100vh; background: #f5f5f5; padding-bottom: 130rpx; }

.shop-header {
  background: #FFD100; padding: 80rpx 30rpx 50rpx; text-align: center;
}
.header-title { font-size: 36rpx; }

.shop-card {
  margin: -30rpx 30rpx 0; background: #fff; border-radius: 24rpx;
  padding: 30rpx; display: flex; align-items: center; gap: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08); position: relative; z-index: 10;
  border: 2rpx solid #fff3b0;
}
.shop-avatar {
  width: 100rpx; height: 100rpx; background: #1a1a1a; border-radius: 20rpx;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.shop-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
.shop-name { font-size: 32rpx; }
.shop-rating { display: flex; align-items: center; gap: 8rpx; }
.edit-shop {
  width: 60rpx; height: 60rpx; background: #eff6ff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}

.shop-body { padding: 30rpx; }

.setting-card {
  background: #fff; border-radius: 24rpx; margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04); overflow: hidden;
}
.setting-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 28rpx 24rpx; border-bottom: 2rpx solid #f8f8f8;
}
.setting-row:last-child { border-bottom: none; }
.setting-left { display: flex; align-items: center; gap: 20rpx; flex: 1; }
.setting-icon { font-size: 32rpx; width: 44rpx; text-align: center; }
.setting-title { font-size: 28rpx; display: block; }
.announcement {
  display: block; max-width: 400rpx;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.logout-btn {
  background: #fff; text-align: center; padding: 28rpx; border-radius: 20rpx;
  color: #ef4444; font-weight: bold; font-size: 28rpx; margin-top: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}

.shop-map-container {
  width: calc(100% - 48rpx); height: 360rpx; margin: 0 24rpx 24rpx;
  border-radius: 16rpx; overflow: hidden; background: #e8eef3;
}
</style>
