<template>
  <view class="address-page">
    <view class="addr-card" v-for="addr in addresses" :key="addr.id" @click="selectAddr(addr)">
      <view class="addr-main">
        <view class="addr-top">
          <text class="addr-name font-bold">{{ addr.receiver }}</text>
          <text class="addr-phone text-gray">{{ addr.phone }}</text>
          <view class="default-tag" v-if="addr.isDefault === 1">默认</view>
        </view>
        <text class="addr-detail text-gray">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</text>
      </view>
      <view class="addr-actions">
        <text class="fa fa-pen action-icon" @click.stop="editAddr(addr)"></text>
        <text class="fa fa-trash action-icon" style="color:#ef4444;" @click.stop="delAddr(addr)"></text>
      </view>
    </view>

    <view v-if="addresses.length === 0" class="empty-tip">
      <text class="fa fa-map-marker-alt" style="font-size:64rpx;color:#ddd;"></text>
      <text class="text-gray">暂无收货地址</text>
    </view>

    <view class="add-btn" @click="addNew">
      <text class="fa fa-plus" style="margin-right:10rpx;"></text>
      新增收货地址
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAddresses, deleteAddress, setDefaultAddress } from '@/api/user'

const addresses = ref([])
const isSelectMode = ref(false)

const loadAddresses = async () => {
  try {
    addresses.value = await getAddresses() || []
  } catch (e) {}
}

const selectAddr = (addr) => {
  if (isSelectMode.value) {
    uni.$emit('selectAddress', addr)
    uni.navigateBack()
  }
}

const editAddr = (addr) => {
  uni.navigateTo({ url: `/pages/address-edit/index?id=${addr.id}` })
}

const delAddr = async (addr) => {
  uni.showModal({
    title: '确认删除',
    content: '确定删除该地址？',
    success: async (res) => {
      if (res.confirm) {
        await deleteAddress(addr.id)
        loadAddresses()
      }
    }
  })
}

const addNew = () => {
  uni.navigateTo({ url: '/pages/address-edit/index' })
}

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  isSelectMode.value = currentPage.options?.select === '1'
  loadAddresses()
})
</script>

<style scoped>
.address-page { min-height: 100vh; background: #f8f9fa; padding: 20rpx 30rpx 200rpx; }

.addr-card {
  display: flex; align-items: center; background: #fff; border-radius: 20rpx;
  padding: 30rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.addr-main { flex: 1; }
.addr-top { display: flex; align-items: center; gap: 12rpx; margin-bottom: 10rpx; }
.addr-name { font-size: 30rpx; }
.addr-phone { font-size: 24rpx; }
.default-tag {
  background: #FFD100; color: #1a1a1a; font-size: 20rpx; font-weight: bold;
  padding: 4rpx 14rpx; border-radius: 8rpx;
}
.addr-detail { font-size: 24rpx; }

.addr-actions { display: flex; flex-direction: column; gap: 20rpx; margin-left: 20rpx; }
.action-icon { font-size: 28rpx; color: #999; }

.empty-tip { text-align: center; padding: 120rpx 0; display: flex; flex-direction: column; align-items: center; gap: 20rpx; }

.add-btn {
  position: fixed; bottom: 60rpx; left: 30rpx; right: 30rpx;
  background: #FFD100; color: #1a1a1a; text-align: center;
  padding: 28rpx; border-radius: 16rpx; font-weight: bold; font-size: 30rpx;
}
</style>
