/**
 * uni-app 统一请求封装
 */
// H5 走 vite proxy（相对路径），小程序需要完整地址
let BASE_URL = '/api'
// #ifdef MP-WEIXIN
BASE_URL = 'http://localhost:8012/api'
// #endif

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('fm_user_token')
    // 过滤掉值为 undefined/null 的参数，防止序列化成 "undefined" 字符串
    const data = options.data
      ? Object.fromEntries(Object.entries(options.data).filter(([, v]) => v !== undefined && v !== null))
      : undefined
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': token } : {}),
        ...options.header
      },
      success: (res) => {
        const data = res.data
        if (data.code === 200) {
          resolve(data.data)
        } else if (data.code === 401) {
          uni.removeStorageSync('fm_user_token')
          uni.reLaunch({ url: '/pages/login/index' })
          reject(new Error('请先登录'))
        } else {
          uni.showToast({ title: data.message || '请求失败', icon: 'none' })
          reject(new Error(data.message))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })

export default request
