/**
 * uni-app 统一请求封装
 */
const BASE_URL = '/api'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('fm_user_token')
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'satoken': token } : {}),
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
