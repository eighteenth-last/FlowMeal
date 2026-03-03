<template>
  <view class="webview-wrap">
    <web-view :src="url" @message="onMessage" />
  </view>
</template>

<script setup>
import { computed } from 'vue'

// 从页面路由参数获取 url（需 decodeURIComponent）
const pages = getCurrentPages()
const current = pages[pages.length - 1]
const query = current.$page?.options || current.options || {}
const url = computed(() => decodeURIComponent(query.url || ''))

const onMessage = (e) => {
  // 支付宝回调可能通过 postMessage 通知支付结果
  console.log('webview message:', e.detail)
}
</script>

<style scoped>
.webview-wrap { width: 100vw; height: 100vh; }
</style>
