<template>
  <view class="custom-tabbar">
    <view
      v-for="(tab, index) in tabs"
      :key="index"
      class="tab-item"
      :class="{ active: currentIndex === index }"
      @click="switchTab(tab, index)"
    >
      <text :class="'fa ' + tab.icon" class="tab-icon"></text>
      <text class="tab-text">{{ tab.text }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  current: { type: Number, default: 0 }
})

const currentIndex = ref(props.current)

watch(() => props.current, (val) => { currentIndex.value = val })

const tabs = [
  { pagePath: '/pages/orders/index', text: '处理', icon: 'fa-bell' },
  { pagePath: '/pages/products/index', text: '餐品', icon: 'fa-boxes-stacked' },
  { pagePath: '/pages/stats/index', text: '数据', icon: 'fa-chart-pie' },
  { pagePath: '/pages/shop/index', text: '店铺', icon: 'fa-store' }
]

const switchTab = (tab, index) => {
  if (currentIndex.value === index) return
  currentIndex.value = index
  uni.switchTab({ url: tab.pagePath })
}
</script>

<style scoped>
.custom-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 110rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-around;
  border-top: 1rpx solid #f0f0f0;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 9999;
}
.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 4rpx;
  transition: all 0.2s;
}
.tab-icon {
  font-size: 38rpx;
  color: #999;
  transition: color 0.2s;
}
.tab-text {
  font-size: 20rpx;
  color: #999;
  transition: color 0.2s;
}
.tab-item.active .tab-icon {
  color: #1a1a1a;
}
.tab-item.active .tab-text {
  color: #1a1a1a;
  font-weight: bold;
}
</style>
