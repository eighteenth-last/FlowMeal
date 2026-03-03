const BASE_URL = '/api'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('fm_merchant_token')
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token || '',
        ...options.header
      },
      success: (res) => {
        const data = res.data
        if (data.code === 200 || data.code === 0) {
          resolve(data.data !== undefined ? data.data : data)
        } else if (data.code === 401) {
          uni.removeStorageSync('fm_merchant_token')
          uni.reLaunch({ url: '/pages/login/index' })
          reject(data)
        } else {
          uni.showToast({ title: data.msg || '请求失败', icon: 'none' })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default request
