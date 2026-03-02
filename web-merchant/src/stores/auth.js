import { defineStore } from 'pinia'
import { merchantLogin } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('fm_merchant_token') || '',
    userInfo: JSON.parse(localStorage.getItem('fm_merchant_info') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    shopName: (state) => state.userInfo?.nickname || '我的店铺',
    merchantId: (state) => state.userInfo?.userId || null,
    avatar: (state) => state.userInfo?.avatar || ''
  },

  actions: {
    async merchantLogin(data) {
      const res = await merchantLogin(data)
      this.token = res.token
      this.userInfo = res
      localStorage.setItem('fm_merchant_token', res.token)
      localStorage.setItem('fm_merchant_info', JSON.stringify(res))
    },

    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('fm_merchant_token')
      localStorage.removeItem('fm_merchant_info')
    }
  }
})
