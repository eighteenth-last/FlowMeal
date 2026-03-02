import { get, put } from '@/utils/request'

/** 获取骑手信息 */
export const getRiderInfo = () => get('/rider/info')

/** 修改骑手信息 */
export const updateRiderInfo = (data) => put('/rider/info', data)

/** 切换在线/离线状态 */
export const toggleOnline = (onlineStatus) => put('/rider/online', { onlineStatus })

/** 配送历史记录 */
export const getDeliveryRecords = (params) => get('/rider/records', params)
