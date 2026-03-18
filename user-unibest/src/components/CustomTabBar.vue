<template>
  <view class="custom-tabbar">
    <view
      v-for="(tab, index) in tabs"
      :key="index"
      class="tab-item"
      :class="{ active: currentIndex === index }"
      @click="switchTab(tab, index)"
    >
      <image :src="currentIndex === index ? tab.iconActive : tab.icon" class="tab-icon" mode="aspectFit" />
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
  { pagePath: '/pages/home/index', text: '首页', icon: '/static/shouye.png', iconActive: '/static/shouye.png' },
  { pagePath: '/pages/order-list/index', text: '订单', icon: '/static/dingdan.png', iconActive: '/static/dingdan.png' },
  { pagePath: '/pages/profile/index', text: '我的', icon: '/static/biaoqiankuozhan_wode-180.png', iconActive: '/static/biaoqiankuozhan_wode-180.png' }
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
}
.tab-icon {
  width: 44rpx;
  height: 44rpx;
  opacity: 0.4;
}
.tab-text {
  font-size: 20rpx;
  color: #999;
}
.tab-item.active .tab-icon {
  opacity: 1;
}
.tab-item.active .tab-text {
  color: #1a1a1a;
  font-weight: bold;
}
</style>
