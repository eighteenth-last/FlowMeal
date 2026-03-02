import { defineStore } from 'pinia'
import { adminLogin } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('fm_admin_token') || '',
    userInfo: JSON.parse(localStorage.getItem('fm_admin_info') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '管理员',
    avatar: (state) => state.userInfo?.avatar || '',
    role: (state) => state.userInfo?.role || 'ADMIN'
  },

  actions: {
    async adminLogin(data) {
      const res = await adminLogin(data)
      this.token = res.token
      this.userInfo = res
      localStorage.setItem('fm_admin_token', res.token)
      localStorage.setItem('fm_admin_info', JSON.stringify(res))
    },

    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('fm_admin_token')
      localStorage.removeItem('fm_admin_info')
    }
  }
})
