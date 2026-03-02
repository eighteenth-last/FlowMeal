/**
 * 高德地图工具模块 - 商家端
 * API Key: 313edac04a9967175a01eb525c79c53b
 */

const AMAP_KEY = '313edac04a9967175a01eb525c79c53b'
const REST_BASE = 'https://restapi.amap.com/v3'

/**
 * 初始化地图
 */
export function initMap(containerId, options = {}) {
  if (!window.AMap) {
    console.error('AMap JS API 未加载')
    return null
  }
  return new window.AMap.Map(containerId, {
    zoom: options.zoom || 15,
    center: options.center || undefined,
    resizeEnable: true,
    ...options,
  })
}

/**
 * IP定位
 */
export function getIPLocation() {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${REST_BASE}/ip?key=${AMAP_KEY}`,
      success: (res) => {
        if (res.data?.status === '1') {
          resolve({
            city: res.data.city || '未知城市',
            province: res.data.province || '',
            rectangle: res.data.rectangle || '',
            adcode: res.data.adcode || '',
          })
        } else {
          reject(new Error(res.data?.info || 'IP定位失败'))
        }
      },
      fail: reject,
    })
  })
}

/**
 * 浏览器精确定位
 */
export function getBrowserLocation() {
  return new Promise((resolve, reject) => {
    if (!window.AMap) return reject(new Error('AMap not loaded'))
    const geolocation = new window.AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      zoomToAccuracy: true,
    })
    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        resolve({
          lng: result.position.getLng(),
          lat: result.position.getLat(),
          city: result.addressComponent?.city || '',
          district: result.addressComponent?.district || '',
          address: result.formattedAddress || '',
        })
      } else {
        reject(new Error(result.message || '定位失败'))
      }
    })
  })
}

/**
 * 逆地理编码
 */
export function reverseGeocode(lng, lat) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${REST_BASE}/geocode/regeo?key=${AMAP_KEY}&location=${lng},${lat}`,
      success: (res) => {
        if (res.data?.status === '1') {
          const comp = res.data.regeocode?.addressComponent || {}
          resolve({
            address: res.data.regeocode?.formatted_address || '',
            city: comp.city || comp.province || '',
            district: comp.district || '',
          })
        } else {
          reject(new Error('逆地理编码失败'))
        }
      },
      fail: reject,
    })
  })
}

/**
 * 添加标记点
 */
export function addMarker(map, lng, lat, opts = {}) {
  if (!map || !window.AMap) return null
  const marker = new window.AMap.Marker({
    position: new window.AMap.LngLat(lng, lat),
    title: opts.title || '',
    ...opts,
  })
  map.add(marker)
  return marker
}

/**
 * 自适应视图
 */
export function fitView(map) {
  if (map) map.setFitView()
}

export default {
  AMAP_KEY,
  initMap,
  getIPLocation,
  getBrowserLocation,
  reverseGeocode,
  addMarker,
  fitView,
}
