import request from './request'

/** 全平台订单列表（分页+筛选） */
export const getOrders = (params) => request.get('/admin/orders', { params })

/** 订单详情 */
export const getOrderDetail = (id) => request.get(`/admin/orders/${id}`)
