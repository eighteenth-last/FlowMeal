<template>
  <view class="shop-page">
    <view class="shop-nav">
      <view class="nav-back" @click="goBack">
        <image src="/static/chevron-left.png" style="width:28rpx;height:28rpx;" mode="aspectFit" />
      </view>
      <view class="nav-right-btns">
        <view class="nav-icon-btn">
          <image src="/static/star.png" style="width:28rpx;height:28rpx;" mode="aspectFit" />
        </view>
        <view class="nav-icon-btn">
          <image src="/static/chevronright.png" style="width:28rpx;height:28rpx;" mode="aspectFit" />
        </view>
      </view>
    </view>

    <view class="shop-banner">
      <image :src="merchant.avatar || '/static/store.png'" class="banner-img" mode="aspectFill" />
    </view>

    <view class="merchant-card">
      <text class="merchant-name">{{ merchant.shopName }}</text>
      <view class="merchant-meta">
        <text class="meta-rating">⭐ 4.8</text>
        <text class="meta-sep">|</text>
        <text class="meta-text">准时达</text>
        <text class="meta-sep">|</text>
        <text class="meta-text" v-if="merchant.deliveryTime">约{{ merchant.deliveryTime }}分钟</text>
      </view>
      <view class="merchant-notice" v-if="merchant.notice">
        <view class="notice-tag">公告</view>
        <text class="notice-text">{{ merchant.notice }}</text>
      </view>
    </view>

    <view class="menu-tabs">
      <view :class="['menu-tab', activeMenuTab === 0 ? 'menu-tab-active' : '']" @click="activeMenuTab = 0">点菜</view>
      <view :class="['menu-tab', activeMenuTab === 1 ? 'menu-tab-active' : '']" @click="activeMenuTab = 1">评价 <text style="font-size:20rpx;color:#999;">4.8k</text></view>
      <view :class="['menu-tab', activeMenuTab === 2 ? 'menu-tab-active' : '']" @click="activeMenuTab = 2">商家</view>
    </view>

    <view class="menu-area">
      <scroll-view scroll-y class="cat-sidebar">
        <view
          v-for="(cat, idx) in categories" :key="cat.id"
          :class="['cat-item', activeCat === idx ? 'cat-active' : '']"
          @click="activeCat = idx"
        >
          <text>{{ cat.name }}</text>
        </view>
      </scroll-view>

      <scroll-view scroll-y class="product-list">
        <text class="product-section-title">{{ categories[activeCat]?.name }}</text>
        <view v-for="p in filteredProducts" :key="p.id" class="product-card">
          <image :src="p.image || '/static/bowl-food.png'" class="product-img" mode="aspectFill" />
          <view class="product-info">
            <text class="product-name">{{ p.name }}</text>
            <text class="product-desc" v-if="p.description">{{ p.description }}</text>
            <view class="product-bottom">
              <view class="price-row">
                <text class="product-price">¥{{ p.discountPrice || p.price }}</text>
                <text class="product-origin" v-if="p.discountPrice">¥{{ p.price }}</text>
              </view>
              <view class="qty-controls">
                <view class="qty-btn qty-minus" v-if="getQty(p.id) > 0" @click="minusItem(p)">
                  <image src="/static/minus.png" style="width:20rpx;height:20rpx;" mode="aspectFit" />
                </view>
                <text class="qty-num" v-if="getQty(p.id) > 0">{{ getQty(p.id) }}</text>
                <view class="qty-btn qty-add" @click="addItem(p)">
                  <image src="/static/plus.png" style="width:20rpx;height:20rpx;" mode="aspectFit" />
                </view>
              </view>
            </view>
          </view>
        </view>
        <view v-if="filteredProducts.length === 0" class="empty-tip">
          <text style="color:#999;">暂无商品</text>
        </view>
      </scroll-view>
    </view>

    <view class="cart-bar" v-if="cartStore.totalCount > 0">
      <view class="cart-icon-wrap">
        <image src="/static/gouwuche.png" style="width:44rpx;height:44rpx;" mode="aspectFit" />
        <view class="cart-badge">{{ cartStore.totalCount }}</view>
      </view>
      <view class="cart-price-area">
        <text class="cart-total">¥{{ cartStore.totalPrice.toFixed(2) }}</text>
        <text class="cart-fee" v-if="merchant.deliveryFee">另需配送费 ¥{{ merchant.deliveryFee }}</text>
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
import { addToCart, updateCartQty } from '@/api/cart'

const cartStore = useCartStore()
const merchant = ref({})
const categories = ref([])
const products = ref([])
const activeCat = ref(0)
const activeMenuTab = ref(0)
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
  addToCart(merchantId.value, p.id, 1).catch(() => {})
}

const minusItem = (p) => {
  const newQty = getQty(p.id) - 1
  cartStore.updateQty(p.id, newQty)
  if (newQty <= 0) {
    updateCartQty(p.id, 0).catch(() => {})
  } else {
    updateCartQty(p.id, newQty).catch(() => {})
  }
}

const goCheckout = () => {
  uni.navigateTo({ url: '/pages/order-confirm/index' })
}

const goBack = () => uni.navigateBack()

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  merchantId.value = currentPage.options?.id || currentPage.$page?.options?.id
  if (!merchantId.value) return
  try {
    const [shopData, catData, prodData] = await Promise.all([
      getShopDetail(merchantId.value),
      getCategories(merchantId.value),
      getProducts(merchantId.value)
    ])
    merchant.value = shopData || {}
    categories.value = (catData || []).map(c => ({ ...c, id: Number(c.id) }))
    products.value = (prodData || []).map(p => ({ ...p, id: Number(p.id), categoryId: Number(p.categoryId) }))
  } catch (e) { console.error(e) }
})
</script>

<style scoped>
.shop-page { min-height: 100vh; background: #f8f9fa; padding-bottom: 160rpx; }

.shop-nav {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  display: flex; align-items: center; justify-content: space-between;
  padding: calc(var(--status-bar-height) + 20rpx) 24rpx 20rpx;
}
.nav-back {
  width: 72rpx; height: 72rpx; background: rgba(255,255,255,0.6);
  backdrop-filter: blur(10px); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.nav-right-btns { display: flex; gap: 16rpx; }
.nav-icon-btn {
  width: 72rpx; height: 72rpx; background: rgba(255,255,255,0.6);
  backdrop-filter: blur(10px); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}

.shop-banner { padding-top: calc(var(--status-bar-height) + 80rpx); }
.banner-img { width: 100%; height: 360rpx; display: block; }

.merchant-card {
  background: #fff; border-radius: 50rpx 50rpx 0 0;
  margin-top: -50rpx; padding: 40rpx 30rpx 20rpx;
  position: relative; z-index: 10;
}
.merchant-name { font-size: 40rpx; font-weight: bold; display: block; margin-bottom: 16rpx; }
.merchant-meta { display: flex; align-items: center; gap: 12rpx; font-size: 24rpx; color: #666; margin-bottom: 16rpx; }
.meta-rating { color: #f59e0b; font-weight: bold; }
.meta-sep { color: #e0e0e0; }
.meta-text { color: #666; }
.merchant-notice {
  background: #f8f8f8; border-radius: 16rpx; padding: 16rpx 20rpx;
  display: flex; align-items: center; gap: 14rpx;
}
.notice-tag { background: #ef4444; color: #fff; font-size: 20rpx; font-weight: bold; padding: 4rpx 12rpx; border-radius: 8rpx; flex-shrink: 0; }
.notice-text { font-size: 22rpx; color: #666; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.menu-tabs {
  display: flex; background: #fff; padding: 0 30rpx;
  border-bottom: 2rpx solid #f0f0f0; position: sticky; top: 0; z-index: 10;
}
.menu-tab { padding: 24rpx 30rpx; font-size: 26rpx; color: #999; border-bottom: 6rpx solid transparent; margin-right: 10rpx; }
.menu-tab-active { color: #1a1a1a; font-weight: bold; border-bottom-color: rgb(255,209,0); }

.menu-area { display: flex; height: 800rpx; background: #fff; }
.cat-sidebar { width: 180rpx; background: #f8f8f8; height: 100%; }
.cat-item { padding: 30rpx 20rpx; font-size: 26rpx; color: #666; border-left: 6rpx solid transparent; text-align: center; }
.cat-active { background: #fff; color: #1a1a1a; font-weight: bold; border-left-color: rgb(255,209,0); }

.product-list { flex: 1; height: 100%; padding: 0 20rpx; }
.product-section-title { font-size: 26rpx; font-weight: bold; color: #333; display: block; padding: 20rpx 0 10rpx; }
.product-card { display: flex; gap: 20rpx; padding: 24rpx 0; border-bottom: 2rpx solid #f5f5f5; }
.product-img { width: 160rpx; height: 160rpx; border-radius: 20rpx; flex-shrink: 0; background: #f0f0f0; }
.product-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.product-name { font-size: 28rpx; font-weight: bold; }
.product-desc { font-size: 22rpx; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-bottom { display: flex; align-items: center; justify-content: space-between; }
.price-row { display: flex; align-items: baseline; gap: 10rpx; }
.product-price { font-size: 34rpx; font-weight: bold; color: #ef4444; }
.product-origin { font-size: 22rpx; color: #ccc; text-decoration: line-through; }

.qty-controls { display: flex; align-items: center; gap: 16rpx; }
.qty-btn { width: 48rpx; height: 48rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.qty-minus { border: 2rpx solid #ddd; }
.qty-add { background: rgb(255,209,0); box-shadow: 0 4rpx 12rpx rgba(255,209,0,0.4); }
.qty-num { font-size: 28rpx; font-weight: bold; min-width: 36rpx; text-align: center; }

.empty-tip { padding: 60rpx; text-align: center; }

.cart-bar {
  position: fixed; bottom: 40rpx; left: 24rpx; right: 24rpx;
  height: 110rpx; background: #1a1a1a; border-radius: 999rpx;
  display: flex; align-items: center; padding: 0 16rpx; z-index: 100;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.3);
}
.cart-icon-wrap {
  width: 110rpx; height: 110rpx; background: rgb(255,209,0); border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin-top: -40rpx; border: 8rpx solid #1a1a1a; position: relative;
  box-shadow: 0 4rpx 16rpx rgba(255,209,0,0.5);
}
.cart-badge {
  position: absolute; top: -8rpx; right: -8rpx;
  min-width: 36rpx; height: 36rpx; background: #ef4444;
  color: #fff; font-size: 20rpx; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  border: 4rpx solid #1a1a1a;
}
.cart-price-area { flex: 1; margin-left: 20rpx; }
.cart-total { color: #fff; font-size: 36rpx; font-weight: bold; display: block; }
.cart-fee { font-size: 22rpx; color: #999; }
.cart-submit-btn {
  background: rgb(255,209,0); color: #1a1a1a; font-weight: bold; font-size: 28rpx;
  padding: 20rpx 50rpx; border-radius: 999rpx;
}
</style>
