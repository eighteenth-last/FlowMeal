<template>
  <view class="confirm-page">
    <!-- 地址选择 -->
    <view class="address-card" @click="goAddressList">
      <view v-if="address" class="addr-info">
        <view class="addr-top">
          <text class="fa fa-location-dot" style="color:#FFD100;font-size:32rpx;margin-right:12rpx;"></text>
          <text class="addr-name">{{ address.receiver }}</text>
          <text class="addr-phone text-gray">{{ address.phone }}</text>
        </view>
        <text class="addr-detail text-gray">{{ address.province }}{{ address.city }}{{ address.district }} {{ address.detail }}</text>
      </view>
      <view v-else class="addr-empty">
        <text class="fa fa-plus-circle" style="color:#FFD100;font-size:36rpx;margin-right:12rpx;"></text>
        <text>请选择收货地址</text>
      </view>
      <text class="fa fa-chevron-right text-gray"></text>
    </view>

    <!-- 商品清单 -->
    <view class="card">
      <view class="card-title">
        <text class="fa fa-store" style="color:#FFD100;margin-right:10rpx;"></text>
        {{ cartStore.merchantName }}
      </view>
      <view class="order-item" v-for="item in cartStore.items" :key="item.productId">
        <image :src="item.productImage || '/static/default-food.png'" class="item-img" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{ item.productName }}</text>
          <view class="item-bottom">
            <text class="item-price">¥{{ (item.discountPrice || item.price).toFixed(2) }}</text>
            <text class="item-qty text-gray">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 备注 -->
    <view class="card">
      <view class="remark-row">
        <text class="fa fa-pen" style="color:#FFD100;margin-right:12rpx;"></text>
        <input v-model="remark" placeholder="备注（选填，如少辣、多放醋等）" class="remark-input" />
      </view>
    </view>

    <!-- 价格明细 -->
    <view class="card">
      <view class="price-row">
        <text>商品小计</text>
        <text class="font-bold">¥{{ cartStore.totalPrice.toFixed(2) }}</text>
      </view>
      <view class="price-row">
        <text>配送费</text>
        <text>¥{{ deliveryFee.toFixed(2) }}</text>
      </view>
      <view class="price-divider"></view>
      <view class="price-row">
        <text class="font-bold">合计</text>
        <text class="total-price">¥{{ (cartStore.totalPrice + deliveryFee).toFixed(2) }}</text>
      </view>
    </view>

    <!-- 底部下单 -->
    <view class="submit-bar safe-bottom">
      <view class="submit-price">
        <text class="text-gray" style="font-size:24rpx;">应付</text>
        <text class="submit-amount">¥{{ (cartStore.totalPrice + deliveryFee).toFixed(2) }}</text>
      </view>
      <view class="btn-primary submit-btn" @click="handleSubmit">提交订单</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useCartStore } from '@/stores/cart'
import { placeOrder } from '@/api/order'
import { getAddresses } from '@/api/user'

const cartStore = useCartStore()
const address = ref(null)
const remark = ref('')
const deliveryFee = ref(3)
const submitting = ref(false)

const goAddressList = () => {
  uni.navigateTo({ url: '/pages/address-list/index?select=1' })
}

const handleSubmit = async () => {
  if (!address.value) {
    return uni.showToast({ title: '请选择收货地址', icon: 'none' })
  }
  if (cartStore.items.length === 0) {
    return uni.showToast({ title: '购物车为空', icon: 'none' })
  }
  if (submitting.value) return
  submitting.value = true

  try {
    const orderNo = await placeOrder({
      addressId: address.value.id,
      merchantId: cartStore.merchantId,
      remark: remark.value || undefined,
      items: cartStore.items.map(i => ({
        productId: i.productId,
        quantity: i.quantity
      }))
    })
    cartStore.clear()
    uni.showToast({ title: '下单成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: `/pages/order-track/index?orderNo=${orderNo}` })
    }, 1000)
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const list = await getAddresses()
    if (list && list.length > 0) {
      address.value = list.find(a => a.isDefault === 1) || list[0]
    }
  } catch (e) {}

  // 监听地址选择返回
  uni.$on('selectAddress', (addr) => {
    address.value = addr
  })
})
</script>

<style scoped>
.confirm-page { min-height: 100vh; background: #f8f9fa; padding: 20rpx 30rpx 200rpx; }

.address-card {
  display: flex; align-items: center; background: #fff; border-radius: 24rpx;
  padding: 30rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.addr-info { flex: 1; }
.addr-top { display: flex; align-items: center; gap: 10rpx; margin-bottom: 8rpx; }
.addr-name { font-size: 30rpx; font-weight: bold; }
.addr-phone { font-size: 24rpx; }
.addr-detail { font-size: 24rpx; padding-left: 44rpx; }
.addr-empty { flex: 1; display: flex; align-items: center; font-size: 28rpx; color: #999; }

.card-title { font-size: 30rpx; font-weight: bold; margin-bottom: 20rpx; display: flex; align-items: center; }
.order-item { display: flex; gap: 18rpx; padding: 16rpx 0; border-bottom: 2rpx solid #f8f8f8; }
.item-img { width: 100rpx; height: 100rpx; border-radius: 12rpx; flex-shrink: 0; background: #f0f0f0; }
.item-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
.item-name { font-size: 28rpx; font-weight: 500; }
.item-bottom { display: flex; justify-content: space-between; align-items: center; }
.item-price { font-size: 28rpx; font-weight: bold; color: #ff4400; }
.item-qty { font-size: 24rpx; }

.remark-row { display: flex; align-items: center; }
.remark-input { flex: 1; font-size: 26rpx; }

.price-row { display: flex; justify-content: space-between; padding: 12rpx 0; font-size: 28rpx; }
.price-divider { height: 2rpx; background: #f5f5f5; margin: 10rpx 0; }
.total-price { font-size: 36rpx; font-weight: bold; color: #ff4400; }

.submit-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: #fff; padding: 20rpx 30rpx; display: flex; align-items: center;
  justify-content: space-between; box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
}
.submit-price { display: flex; flex-direction: column; }
.submit-amount { font-size: 40rpx; font-weight: bold; color: #ff4400; }
.submit-btn { padding: 20rpx 60rpx; font-size: 30rpx; }
</style>
