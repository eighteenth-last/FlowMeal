import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(uni.getStorageSync('fm_rider_token') || '')
  const riderInfo = ref(JSON.parse(uni.getStorageSync('fm_rider_info') || '{}'))

  const isLoggedIn = () => !!token.value

  const setAuth = (loginResp) => {
    token.value = loginResp.token
    riderInfo.value = loginResp
    uni.setStorageSync('fm_rider_token', loginResp.token)
    uni.setStorageSync('fm_rider_info', JSON.stringify(loginResp))
  }

  const clearAuth = () => {
    token.value = ''
    riderInfo.value = {}
    uni.removeStorageSync('fm_rider_token')
    uni.removeStorageSync('fm_rider_info')
  }

  return { token, riderInfo, isLoggedIn, setAuth, clearAuth }
})
