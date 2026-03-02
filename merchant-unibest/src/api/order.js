import request from '@/utils/request'

// 获取商家订单列表
export const getMerchantOrders = (params) => request({ url: '/order/merchant/list', data: params })

// 获取订单详情
export const getOrderDetail = (id) => request({ url: `/order/${id}/detail` })

// 接受订单
export const acceptOrder = (id) => request({ url: `/order/${id}/merchant/accept`, method: 'PUT' })

// 拒绝订单
export const rejectOrder = (id, reason) => request({ url: `/order/${id}/merchant/reject`, method: 'PUT', data: { reason } })
