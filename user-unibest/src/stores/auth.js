import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(uni.getStorageSync('fm_user_token') || '')
  const userInfo = ref(JSON.parse(uni.getStorageSync('fm_user_info') || '{}'))

  const isLoggedIn = () => !!token.value

  const setAuth = (loginResp) => {
    token.value = loginResp.token
    userInfo.value = loginResp
    uni.setStorageSync('fm_user_token', loginResp.token)
    uni.setStorageSync('fm_user_info', JSON.stringify(loginResp))
  }

  const clearAuth = () => {
    token.value = ''
    userInfo.value = {}
    uni.removeStorageSync('fm_user_token')
    uni.removeStorageSync('fm_user_info')
  }

  return { token, userInfo, isLoggedIn, setAuth, clearAuth }
})
