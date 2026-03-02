/**
 * 高德地图工具模块
 * API Key: 313edac04a9967175a01eb525c79c53b
 */

const AMAP_KEY = '313edac04a9967175a01eb525c79c53b'
const REST_BASE = 'https://restapi.amap.com/v3'

/**
 * 初始化地图
 * @param {string} containerId DOM容器ID
 * @param {Object} options 地图选项
 * @returns {AMap.Map}
 */
export function initMap(containerId, options = {}) {
  if (!window.AMap) {
    console.error('AMap JS API 未加载')
    return null
  }
  return new window.AMap.Map(containerId, {
    zoom: options.zoom || 14,
    center: options.center || undefined,
    resizeEnable: true,
    ...options,
  })
}

/**
 * 通过IP定位获取当前城市
 * REST API: https://restapi.amap.com/v3/ip?key={key}
 * @returns {Promise<{city: string, province: string, rectangle: string}>}
 */
export function getIPLocation() {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${REST_BASE}/ip?key=${AMAP_KEY}`,
      success: (res) => {
        if (res.data && res.data.status === '1') {
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
      fail: (err) => reject(err),
    })
  })
}

/**
 * 浏览器定位（H5）
 * @returns {Promise<{lng: number, lat: number, city: string, address: string}>}
 */
export function getBrowserLocation() {
  return new Promise((resolve, reject) => {
    if (!window.AMap) return reject(new Error('AMap not loaded'))
    const geolocation = new window.AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      buttonOffset: new window.AMap.Pixel(10, 20),
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
 * 逆地理编码 - 坐标转地址
 * @param {number} lng 经度
 * @param {number} lat 纬度
 * @returns {Promise<Object>}
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
            province: comp.province || '',
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
 * @param {AMap.Map} map 地图实例
 * @param {number} lng 经度
 * @param {number} lat 纬度
 * @param {Object} opts 选项
 * @returns {AMap.Marker}
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
 * 绘制两点连线
 * @param {AMap.Map} map
 * @param {Array} path [[lng1,lat1], [lng2,lat2], ...]
 * @param {Object} opts
 * @returns {AMap.Polyline}
 */
export function addPolyline(map, path, opts = {}) {
  if (!map || !window.AMap) return null
  const polyline = new window.AMap.Polyline({
    path: path.map((p) => new window.AMap.LngLat(p[0], p[1])),
    strokeColor: opts.color || '#FFD100',
    strokeWeight: opts.weight || 6,
    strokeOpacity: opts.opacity || 0.9,
    lineJoin: 'round',
    ...opts,
  })
  map.add(polyline)
  return polyline
}

/**
 * 自适应显示所有覆盖物
 * @param {AMap.Map} map
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
  addPolyline,
  fitView,
}
