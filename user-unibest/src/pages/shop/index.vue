<template>
  <view class="shop-page">
    <!-- 顶部导航栏 -->
    <view class="shop-nav">
      <view class="nav-back" @click="goBack">
        <text class="fa fa-chevron-left"></text>
      </view>
      <text class="nav-title">{{ merchant.shopName || '店铺详情' }}</text>
      <view style="width:60rpx;"></view>
    </view>

    <!-- 店铺信息 -->
    <view class="shop-banner">
      <image :src="merchant.avatar || '/static/default-shop.png'" class="banner-img" mode="aspectFill" />
      <view class="shop-header-card">
        <text class="shop-name-lg">{{ merchant.shopName }}</text>
        <view class="shop-tags">
          <text class="fa fa-star" style="color:#FFD100;font-size:22rpx;"></text>
          <text class="tag-text">4.8</text>
          <text class="tag-sep">|</text>
          <text class="tag-text text-gray">
            <text class="fa fa-clock" style="font-size:20rpx;"></text>
            {{ merchant.businessHours || '09:00-22:00' }}
          </text>
        </view>
        <view class="shop-notice" v-if="merchant.notice">
          <text class="fa fa-bullhorn" style="color:#FFD100;font-size:20rpx;margin-right:10rpx;"></text>
          <text class="notice-text">{{ merchant.notice }}</text>
        </view>
      </view>
    </view>

    <!-- 分类 + 商品列表 -->
    <view class="menu-area">
      <!-- 左侧分类 -->
      <scroll-view scroll-y class="cat-sidebar">
        <view
          v-for="(cat, idx) in categories" :key="cat.id"
          :class="['cat-item', activeCat === idx ? 'cat-active' : '']"
          @click="activeCat = idx"
        >
          <text>{{ cat.name }}</text>
        </view>
      </scroll-view>

      <!-- 右侧商品 -->
      <scroll-view scroll-y class="product-list">
        <view v-for="p in filteredProducts" :key="p.id" class="product-card">
          <image :src="p.image || '/static/default-food.png'" class="product-img" mode="aspectFill" />
          <view class="product-info">
            <text class="product-name">{{ p.name }}</text>
            <text class="product-desc text-gray" v-if="p.description">{{ p.description }}</text>
            <view class="product-bottom">
              <view class="price-row">
                <text class="product-price">¥{{ p.discountPrice || p.price }}</text>
                <text class="product-origin" v-if="p.discountPrice">¥{{ p.price }}</text>
              </view>
              <view class="qty-controls">
                <view class="qty-btn" v-if="getQty(p.id) > 0" @click="minusItem(p)">
                  <text class="fa fa-minus" style="font-size:20rpx;"></text>
                </view>
                <text class="qty-num" v-if="getQty(p.id) > 0">{{ getQty(p.id) }}</text>
                <view class="qty-btn qty-add" @click="addItem(p)">
                  <text class="fa fa-plus" style="font-size:20rpx;color:#fff;"></text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view v-if="filteredProducts.length === 0" class="empty-tip">
          <text class="text-gray">暂无商品</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部购物车条 -->
    <view class="cart-bar safe-bottom" v-if="cartStore.totalCount > 0">
      <view class="cart-icon-wrap">
        <text class="fa fa-shopping-basket" style="font-size:40rpx;color:#1a1a1a;"></text>
        <view class="cart-badge">{{ cartStore.totalCount }}</view>
      </view>
      <view class="cart-price-area">
        <text class="cart-total">¥{{ cartStore.totalPrice.toFixed(2) }}</text>
        <text class="cart-fee text-gray">另需配送费 ¥{{ merchant.deliveryFee || 3 }}</text>
      </view>
      <view class="cart-submit-btn" @click="goCheckout">去结算</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getShopDetail } from '@/api/shop'
import { getCategories, getProducts } from '@/api/product'
import { useCartStore } from '@/stores/cart'

const cartStore = useCartStore()
const merchant = ref({})
const categories = ref([])
const products = ref([])
const activeCat = ref(0)
const merchantId = ref(null)

const filteredProducts = computed(() => {
  if (categories.value.length === 0) return products.value
  const cat = categories.value[activeCat.value]
  if (!cat) return products.value
  return products.value.filter(p => p.categoryId === cat.id)
})

const getQty = (productId) => {
  const item = cartStore.items.find(i => i.productId === productId)
  return item ? item.quantity : 0
}

const addItem = (p) => {
  cartStore.addItem(p, merchantId.value, merchant.value.shopName)
}

const minusItem = (p) => {
  const qty = getQty(p.id)
  cartStore.updateQty(p.id, qty - 1)
}

const goCheckout = () => {
  uni.navigateTo({ url: '/pages/order-confirm/index' })
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  merchantId.value = currentPage.options?.id || currentPage.$page?.options?.id

  if (!merchantId.value) {
    uni.showToast({ title: '参数错误', icon: 'none' })
    return
  }

  try {
    const [shopData, catData, prodData] = await Promise.all([
      getShopDetail(merchantId.value),
      getCategories(merchantId.value),
      getProducts(merchantId.value)
    ])
    merchant.value = shopData || {}
    categories.value = catData || []
    products.value = prodData || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.shop-page { min-height: 100vh; background: #f8f9fa; padding-bottom: 160rpx; }

.shop-nav {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  display: flex; align-items: center; justify-content: space-between;
  padding: 80rpx 30rpx 20rpx; background: rgba(255,255,255,0.95); backdrop-filter: blur(10px);
}
.nav-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 32rpx; font-weight: bold; }

.shop-banner { padding-top: 160rpx; }
.banner-img { width: 100%; height: 300rpx; }
.shop-header-card {
  margin: -60rpx 30rpx 0; position: relative; z-index: 10;
  background: #fff; border-radius: 30rpx; padding: 36rpx; box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
}
.shop-name-lg { font-size: 38rpx; font-weight: bold; display: block; margin-bottom: 12rpx; }
.shop-tags { display: flex; align-items: center; gap: 10rpx; font-size: 24rpx; margin-bottom: 12rpx; }
.tag-text { font-size: 24rpx; }
.tag-sep { color: #e0e0e0; margin: 0 4rpx; }
.shop-notice { display: flex; align-items: center; background: #fffbeb; border-radius: 12rpx; padding: 14rpx 20rpx; }
.notice-text { font-size: 24rpx; color: #666; flex: 1; }

.menu-area {
  display: flex; margin-top: 20rpx; height: calc(100vh - 600rpx);
}
.cat-sidebar {
  width: 180rpx; background: #f0f0f0; height: 100%;
}
.cat-item {
  padding: 28rpx 20rpx; font-size: 26rpx; text-align: center; color: #666;
  border-left: 6rpx solid transparent;
}
.cat-active {
  background: #fff; color: #1a1a1a; font-weight: bold;
  border-left-color: #FFD100;
}
.product-list { flex: 1; height: 100%; padding: 0 20rpx; }
.product-card {
  display: flex; gap: 20rpx; padding: 24rpx 0; border-bottom: 2rpx solid #f5f5f5;
}
.product-img { width: 140rpx; height: 140rpx; border-radius: 16rpx; flex-shrink: 0; background: #f0f0f0; }
.product-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.product-name { font-size: 28rpx; font-weight: bold; }
.product-desc { font-size: 22rpx; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.product-bottom { display: flex; align-items: center; justify-content: space-between; }
.price-row { display: flex; align-items: baseline; gap: 10rpx; }
.product-price { font-size: 32rpx; font-weight: bold; color: #ff4400; }
.product-origin { font-size: 22rpx; color: #ccc; text-decoration: line-through; }

.qty-controls { display: flex; align-items: center; gap: 16rpx; }
.qty-btn {
  width: 44rpx; height: 44rpx; border-radius: 50%; border: 2rpx solid #ddd;
  display: flex; align-items: center; justify-content: center;
}
.qty-add { background: #FFD100; border-color: #FFD100; }
.qty-num { font-size: 28rpx; font-weight: bold; min-width: 36rpx; text-align: center; }

.cart-bar {
  position: fixed; bottom: 30rpx; left: 30rpx; right: 30rpx;
  height: 110rpx; background: #1a1a1a; border-radius: 999rpx;
  display: flex; align-items: center; padding: 0 20rpx; z-index: 100;
}
.cart-icon-wrap {
  width: 100rpx; height: 100rpx; background: #FFD100; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin-top: -30rpx; border: 8rpx solid #1a1a1a; position: relative;
}
.cart-badge {
  position: absolute; top: -10rpx; right: -10rpx;
  min-width: 36rpx; height: 36rpx; background: #ff4400;
  color: #fff; font-size: 20rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  border: 4rpx solid #1a1a1a;
}
.cart-price-area { flex: 1; margin-left: 20rpx; }
.cart-total { color: #fff; font-size: 36rpx; font-weight: bold; display: block; }
.cart-fee { font-size: 22rpx; }
.cart-submit-btn {
  background: #FFD100; color: #1a1a1a; font-weight: bold; font-size: 28rpx;
  padding: 18rpx 50rpx; border-radius: 999rpx;
}

.empty-tip { padding: 60rpx; text-align: center; font-size: 26rpx; }
</style>
