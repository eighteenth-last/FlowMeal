import request from './request'

/** 商家订单列表（按状态筛选） */
export const getOrders = (params) => request.get('/order/merchant/list', { params })

/** 接单 */
export const acceptOrder = (id) => request.put(`/order/${id}/merchant/accept`)

/** 拒单 */
export const rejectOrder = (id, reason) =>
  request.put(`/order/${id}/merchant/reject`, null, { params: { reason } })

/** 订单详情 */
export const getOrderDetail = (id) => request.get(`/order/${id}`)

/** 标记已出餐/配送中 */
export const updateOrderStatus = (id, status) =>
  request.put(`/order/${id}/status`, { status })
