import axios from 'axios'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：注入 Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('fm_merchant_token')
  if (token) config.headers['Authorization'] = token
  return config
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    if (code === 200) return data
    if (code === 401) {
      localStorage.removeItem('fm_merchant_token')
      router.push('/login')
      return Promise.reject(new Error('请重新登录'))
    }
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    const msg = error.response?.data?.message || error.message || '网络异常'
    return Promise.reject(new Error(msg))
  }
)

export default request
