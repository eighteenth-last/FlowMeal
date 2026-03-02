import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(uni.getStorageSync('fm_merchant_token') || '')
  const merchantInfo = ref(JSON.parse(uni.getStorageSync('fm_merchant_info') || '{}'))

  const setAuth = (data) => {
    token.value = data.token || data.tokenValue || ''
    merchantInfo.value = data.merchantInfo || data.userInfo || data
    uni.setStorageSync('fm_merchant_token', token.value)
    uni.setStorageSync('fm_merchant_info', JSON.stringify(merchantInfo.value))
  }

  const clearAuth = () => {
    token.value = ''
    merchantInfo.value = {}
    uni.removeStorageSync('fm_merchant_token')
    uni.removeStorageSync('fm_merchant_info')
  }

  return { token, merchantInfo, setAuth, clearAuth }
})
