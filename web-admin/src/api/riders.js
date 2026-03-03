import request from './request'

/** 骑手列表（分页） */
export const getRiders = (params) => request.get('/admin/riders', { params })

/** 审核骑手 auditStatus: 1-通过 2-拒绝 */
export const auditRider = (id, auditStatus) =>
  request.put(`/admin/riders/${id}/audit`, null, { params: { auditStatus } })

/** 切换骑手账号状态 */
export const toggleRiderStatus = (id, status) =>
  request.put(`/admin/riders/${id}/status`, null, { params: { status } })

/** 强制设置骑手在线/离线 onlineStatus: 1=在线 0=离线 */
export const setRiderOnline = (id, onlineStatus) =>
  request.put(`/admin/riders/${id}/online`, null, { params: { onlineStatus } })

/** 获取骑手当前进行中的订单 */
export const getRiderActiveOrders = (id) => request.get(`/admin/riders/${id}/active-orders`)

/** 将订单转派给其他骑手 */
export const reassignOrder = (orderId, toRiderId) =>
  request.put(`/admin/orders/${orderId}/reassign`, null, { params: { toRiderId } })
